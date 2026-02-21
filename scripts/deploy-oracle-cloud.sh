#!/bin/bash

###############################################################################
# Healthcare Platform - Oracle Cloud Deployment Script
#
# This script automates the deployment of the entire healthcare platform
# to Oracle Cloud's Always Free tier.
#
# Prerequisites:
# - Oracle Cloud account with Always Free tier
# - OCI CLI installed and configured
# - SSH key pair generated (~/.ssh/id_rsa.pub)
#
# Usage:
#   ./deploy-oracle-cloud.sh
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check if OCI CLI is installed
    if ! command -v oci &> /dev/null; then
        log_error "OCI CLI is not installed. Please install it first."
        log_info "Visit: https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm"
        exit 1
    fi

    # Check if OCI CLI is configured
    if [ ! -f ~/.oci/config ]; then
        log_error "OCI CLI is not configured. Please run 'oci setup config' first."
        exit 1
    fi

    # Check if SSH key exists
    if [ ! -f ~/.ssh/id_rsa.pub ]; then
        log_warning "SSH key not found. Generating new key pair..."
        ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
        log_success "SSH key pair generated"
    fi

    log_success "All prerequisites met"
}

# Get user inputs
get_configuration() {
    log_info "Getting configuration..."

    # Get compartment ID
    echo ""
    echo "Available compartments:"
    oci iam compartment list --all --query 'data[*].{Name:name, ID:id}' --output table
    echo ""
    read -p "Enter your compartment ID: " COMPARTMENT_ID

    # Get availability domain
    echo ""
    echo "Available availability domains:"
    oci iam availability-domain list --compartment-id "$COMPARTMENT_ID" --query 'data[*].name' --output table
    echo ""
    read -p "Enter your availability domain name: " AVAILABILITY_DOMAIN

    # Get ARM image ID
    log_info "Finding Ubuntu ARM image..."
    IMAGE_ID=$(oci compute image list \
        --compartment-id "$COMPARTMENT_ID" \
        --operating-system "Canonical Ubuntu" \
        --operating-system-version "22.04" \
        --shape "VM.Standard.A1.Flex" \
        --limit 1 \
        --query 'data[0].id' \
        --raw-output)

    if [ -z "$IMAGE_ID" ]; then
        log_error "Could not find Ubuntu ARM image"
        exit 1
    fi

    log_success "Using image: $IMAGE_ID"

    # Application configuration
    echo ""
    read -p "Enter PostgreSQL password (strong password): " POSTGRES_PASSWORD
    read -p "Enter Redis password (strong password): " REDIS_PASSWORD
    read -p "Enter MongoDB password (strong password): " MONGO_PASSWORD
    read -p "Enter JWT secret (min 32 chars): " JWT_SECRET
    read -p "Enter JWT refresh secret (min 32 chars): " JWT_REFRESH_SECRET

    echo ""
    log_success "Configuration complete"
}

# Create VCN
create_vcn() {
    log_info "Creating Virtual Cloud Network..."

    VCN_ID=$(oci network vcn create \
        --compartment-id "$COMPARTMENT_ID" \
        --display-name "healthcare-vcn" \
        --cidr-block "10.0.0.0/16" \
        --dns-label "healthcare" \
        --wait-for-state AVAILABLE \
        --query 'data.id' \
        --raw-output)

    log_success "VCN created: $VCN_ID"

    # Create Internet Gateway
    log_info "Creating Internet Gateway..."
    IGW_ID=$(oci network internet-gateway create \
        --compartment-id "$COMPARTMENT_ID" \
        --vcn-id "$VCN_ID" \
        --display-name "healthcare-igw" \
        --is-enabled true \
        --wait-for-state AVAILABLE \
        --query 'data.id' \
        --raw-output)

    log_success "Internet Gateway created: $IGW_ID"

    # Get default route table ID
    ROUTE_TABLE_ID=$(oci network route-table list \
        --compartment-id "$COMPARTMENT_ID" \
        --vcn-id "$VCN_ID" \
        --query 'data[0].id' \
        --raw-output)

    # Add route to Internet Gateway
    log_info "Configuring route table..."
    oci network route-table update \
        --rt-id "$ROUTE_TABLE_ID" \
        --route-rules "[{\"destination\":\"0.0.0.0/0\",\"networkEntityId\":\"$IGW_ID\"}]" \
        --force \
        --wait-for-state AVAILABLE > /dev/null

    log_success "Route table configured"

    # Create Security List
    log_info "Creating security list..."
    SECURITY_LIST_ID=$(oci network security-list create \
        --compartment-id "$COMPARTMENT_ID" \
        --vcn-id "$VCN_ID" \
        --display-name "healthcare-seclist" \
        --ingress-security-rules '[
            {"source":"0.0.0.0/0","protocol":"6","tcpOptions":{"destinationPortRange":{"min":22,"max":22}},"description":"SSH"},
            {"source":"0.0.0.0/0","protocol":"6","tcpOptions":{"destinationPortRange":{"min":80,"max":80}},"description":"HTTP"},
            {"source":"0.0.0.0/0","protocol":"6","tcpOptions":{"destinationPortRange":{"min":443,"max":443}},"description":"HTTPS"},
            {"source":"0.0.0.0/0","protocol":"6","tcpOptions":{"destinationPortRange":{"min":8080,"max":8080}},"description":"API Gateway"}
        ]' \
        --egress-security-rules '[
            {"destination":"0.0.0.0/0","protocol":"all","description":"Allow all outbound"}
        ]' \
        --wait-for-state AVAILABLE \
        --query 'data.id' \
        --raw-output)

    log_success "Security list created: $SECURITY_LIST_ID"

    # Create Subnet
    log_info "Creating subnet..."
    SUBNET_ID=$(oci network subnet create \
        --compartment-id "$COMPARTMENT_ID" \
        --vcn-id "$VCN_ID" \
        --display-name "healthcare-subnet" \
        --cidr-block "10.0.1.0/24" \
        --dns-label "healthcaresub" \
        --route-table-id "$ROUTE_TABLE_ID" \
        --security-list-ids "[\"$SECURITY_LIST_ID\"]" \
        --wait-for-state AVAILABLE \
        --query 'data.id' \
        --raw-output)

    log_success "Subnet created: $SUBNET_ID"
}

# Create compute instances
create_compute_instances() {
    log_info "Creating compute instances (this may take 5-10 minutes)..."

    # Create VM-1 (Infrastructure + Core Services)
    log_info "Creating VM-1 (Infrastructure + Core Services)..."
    VM1_ID=$(oci compute instance launch \
        --compartment-id "$COMPARTMENT_ID" \
        --availability-domain "$AVAILABILITY_DOMAIN" \
        --display-name "healthcare-vm-1" \
        --shape "VM.Standard.A1.Flex" \
        --shape-config '{"ocpus":2,"memoryInGBs":12}' \
        --image-id "$IMAGE_ID" \
        --subnet-id "$SUBNET_ID" \
        --assign-public-ip true \
        --ssh-authorized-keys-file ~/.ssh/id_rsa.pub \
        --wait-for-state RUNNING \
        --query 'data.id' \
        --raw-output)

    log_success "VM-1 created: $VM1_ID"

    # Get VM-1 public IP
    VM1_PUBLIC_IP=$(oci compute instance list-vnics \
        --instance-id "$VM1_ID" \
        --query 'data[0]."public-ip"' \
        --raw-output)

    log_success "VM-1 Public IP: $VM1_PUBLIC_IP"

    # Create VM-2 (Remaining Services)
    log_info "Creating VM-2 (Remaining Services)..."
    VM2_ID=$(oci compute instance launch \
        --compartment-id "$COMPARTMENT_ID" \
        --availability-domain "$AVAILABILITY_DOMAIN" \
        --display-name "healthcare-vm-2" \
        --shape "VM.Standard.A1.Flex" \
        --shape-config '{"ocpus":2,"memoryInGBs":12}' \
        --image-id "$IMAGE_ID" \
        --subnet-id "$SUBNET_ID" \
        --assign-public-ip true \
        --ssh-authorized-keys-file ~/.ssh/id_rsa.pub \
        --wait-for-state RUNNING \
        --query 'data.id' \
        --raw-output)

    log_success "VM-2 created: $VM2_ID"

    # Get VM-2 public IP
    VM2_PUBLIC_IP=$(oci compute instance list-vnics \
        --instance-id "$VM2_ID" \
        --query 'data[0]."public-ip"' \
        --raw-output)

    log_success "VM-2 Public IP: $VM2_PUBLIC_IP"

    # Get VM-1 private IP
    VM1_PRIVATE_IP=$(oci compute instance list-vnics \
        --instance-id "$VM1_ID" \
        --query 'data[0]."private-ip"' \
        --raw-output)

    log_info "Waiting for VMs to be fully initialized (60 seconds)..."
    sleep 60
}

# Install Docker on VMs
install_docker() {
    local VM_IP=$1
    local VM_NAME=$2

    log_info "Installing Docker on $VM_NAME ($VM_IP)..."

    # Wait for SSH to be available
    log_info "Waiting for SSH to be available..."
    for i in {1..30}; do
        if ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 ubuntu@"$VM_IP" "echo 'SSH ready'" &> /dev/null; then
            break
        fi
        sleep 10
    done

    # Install Docker
    ssh -o StrictHostKeyChecking=no ubuntu@"$VM_IP" bash << 'EOF'
        # Update system
        sudo apt-get update

        # Install Docker
        curl -fsSL https://get.docker.com -o get-docker.sh
        sudo sh get-docker.sh
        sudo usermod -aG docker ubuntu

        # Install Docker Compose
        sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose

        # Create app directory
        mkdir -p ~/healthcare-app
EOF

    log_success "Docker installed on $VM_NAME"
}

# Deploy to VM-1
deploy_vm1() {
    log_info "Deploying to VM-1..."

    # Copy repository to VM-1
    log_info "Cloning repository on VM-1..."
    ssh ubuntu@"$VM1_PUBLIC_IP" bash << EOF
        cd ~/healthcare-app
        git clone https://github.com/kally123/doctorApp.git .

        # Create .env file
        cat > .env << ENVEOF
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
REDIS_PASSWORD=$REDIS_PASSWORD
MONGO_PASSWORD=$MONGO_PASSWORD
JWT_SECRET=$JWT_SECRET
JWT_REFRESH_SECRET=$JWT_REFRESH_SECRET
SPRING_PROFILES_ACTIVE=prod
ENVEOF

        # Start infrastructure services
        docker-compose up -d postgres mongodb redis kafka zookeeper elasticsearch localstack mailhog

        # Wait for infrastructure to be ready
        echo "Waiting for infrastructure services to be ready (180 seconds)..."
        sleep 180

        # Start core services
        docker-compose up -d api-gateway user-service doctor-service search-service appointment-service
EOF

    log_success "VM-1 deployment complete"
}

# Deploy to VM-2
deploy_vm2() {
    log_info "Deploying to VM-2..."

    # Copy repository to VM-2
    log_info "Cloning repository on VM-2..."
    ssh ubuntu@"$VM2_PUBLIC_IP" bash << EOF
        cd ~/healthcare-app
        git clone https://github.com/kally123/doctorApp.git .

        # Create .env file pointing to VM-1 for infrastructure
        cat > .env << ENVEOF
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
REDIS_PASSWORD=$REDIS_PASSWORD
MONGO_PASSWORD=$MONGO_PASSWORD
JWT_SECRET=$JWT_SECRET
JWT_REFRESH_SECRET=$JWT_REFRESH_SECRET
SPRING_PROFILES_ACTIVE=prod

# Infrastructure on VM-1
POSTGRES_HOST=$VM1_PRIVATE_IP
MONGODB_HOST=$VM1_PRIVATE_IP
REDIS_HOST=$VM1_PRIVATE_IP
KAFKA_HOST=$VM1_PRIVATE_IP
ELASTICSEARCH_HOST=$VM1_PRIVATE_IP
ENVEOF

        # Start remaining services
        docker-compose up -d payment-service notification-service consultation-service \
            prescription-service ehr-service order-service review-service content-service
EOF

    log_success "VM-2 deployment complete"
}

# Verify deployment
verify_deployment() {
    log_info "Verifying deployment..."

    # Check VM-1 services
    log_info "Checking VM-1 services..."
    ssh ubuntu@"$VM1_PUBLIC_IP" "cd ~/healthcare-app && docker-compose ps"

    # Check VM-2 services
    log_info "Checking VM-2 services..."
    ssh ubuntu@"$VM2_PUBLIC_IP" "cd ~/healthcare-app && docker-compose ps"

    # Check API Gateway health
    log_info "Checking API Gateway health..."
    sleep 30  # Wait for services to fully start

    if curl -f "http://$VM1_PUBLIC_IP:8080/actuator/health" &> /dev/null; then
        log_success "API Gateway is healthy"
    else
        log_warning "API Gateway health check failed. Services may still be starting..."
    fi
}

# Print deployment summary
print_summary() {
    echo ""
    echo "========================================"
    log_success "Deployment Complete!"
    echo "========================================"
    echo ""
    echo "VM-1 (Infrastructure + Core Services):"
    echo "  Public IP:  $VM1_PUBLIC_IP"
    echo "  Private IP: $VM1_PRIVATE_IP"
    echo ""
    echo "VM-2 (Remaining Services):"
    echo "  Public IP:  $VM2_PUBLIC_IP"
    echo ""
    echo "Access URLs:"
    echo "  API Gateway:  http://$VM1_PUBLIC_IP:8080"
    echo "  Health Check: http://$VM1_PUBLIC_IP:8080/actuator/health"
    echo ""
    echo "SSH Access:"
    echo "  VM-1: ssh ubuntu@$VM1_PUBLIC_IP"
    echo "  VM-2: ssh ubuntu@$VM2_PUBLIC_IP"
    echo ""
    echo "View Logs:"
    echo "  ssh ubuntu@$VM1_PUBLIC_IP"
    echo "  cd ~/healthcare-app"
    echo "  docker-compose logs -f"
    echo ""
    echo "Next Steps:"
    echo "  1. Set up a domain name and point it to $VM1_PUBLIC_IP"
    echo "  2. Configure SSL certificate (use Let's Encrypt)"
    echo "  3. Set up load balancer (optional)"
    echo "  4. Configure monitoring and alerts"
    echo "  5. Deploy frontend applications"
    echo ""
    echo "Configuration saved to: deployment-info.txt"

    # Save deployment info
    cat > deployment-info.txt << EOF
Healthcare Platform Deployment Information
Date: $(date)

VM-1:
  ID: $VM1_ID
  Public IP: $VM1_PUBLIC_IP
  Private IP: $VM1_PRIVATE_IP

VM-2:
  ID: $VM2_ID
  Public IP: $VM2_PUBLIC_IP

VCN: $VCN_ID
Subnet: $SUBNET_ID
Security List: $SECURITY_LIST_ID

API Gateway: http://$VM1_PUBLIC_IP:8080
EOF
}

# Main execution
main() {
    echo "========================================"
    echo "Healthcare Platform Deployment"
    echo "Oracle Cloud Always Free Tier"
    echo "========================================"
    echo ""

    check_prerequisites
    get_configuration
    create_vcn
    create_compute_instances

    # Install Docker on both VMs in parallel
    install_docker "$VM1_PUBLIC_IP" "VM-1" &
    install_docker "$VM2_PUBLIC_IP" "VM-2" &
    wait

    deploy_vm1
    deploy_vm2
    verify_deployment
    print_summary
}

# Run main function
main


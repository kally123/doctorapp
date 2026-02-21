# 🚀 Deploying to Free Cloud Environments

This guide covers deploying the Healthcare Platform to various free cloud providers. Each option has different limitations and benefits.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Option 1: Oracle Cloud (Free Tier)](#option-1-oracle-cloud-free-tier) ⭐ **Recommended**
- [Option 2: Google Cloud Platform (GCP Free Tier)](#option-2-google-cloud-platform-gcp-free-tier)
- [Option 3: AWS Free Tier](#option-3-aws-free-tier)
- [Option 4: Azure for Students](#option-4-azure-for-students)
- [Option 5: Railway.app (Hobby Plan)](#option-5-railwayapp-hobby-plan)
- [Option 6: Render.com (Free Tier)](#option-6-rendercom-free-tier)
- [Option 7: Fly.io (Free Tier)](#option-7-flyio-free-tier)
- [Hybrid Approach](#hybrid-approach-recommended)
- [Cost Optimization](#cost-optimization)

---

## 🎯 Overview

### Free Tier Comparison

| Provider | Compute | Database | Storage | Network | Duration |
|----------|---------|----------|---------|---------|----------|
| **Oracle Cloud** | 4 ARM VMs (24GB RAM) | PostgreSQL, MongoDB | 200 GB | 10 TB/month | **Forever** |
| **GCP** | 1 e2-micro VM | Cloud SQL (limited) | 30 GB | 1 GB/day | **Forever** |
| **AWS** | 1 t2.micro (1GB) | RDS (limited) | 5 GB | 15 GB/month | **12 months** |
| **Azure Students** | $100 credit | All services | Various | Various | **12 months** |
| **Railway** | $5/month credit | PostgreSQL, Redis | 1 GB | 100 GB | **Monthly** |
| **Render** | 750 hours/month | PostgreSQL | Limited | 100 GB | **Forever** |
| **Fly.io** | 3 VMs (256MB each) | PostgreSQL | 3 GB | 160 GB | **Forever** |

### Recommended Strategy

For a **complete deployment**, use **Oracle Cloud Free Tier** as it offers:
- ✅ Most generous free tier (forever)
- ✅ Sufficient resources for all 13 services
- ✅ Kubernetes cluster support
- ✅ No credit card required initially
- ✅ ARM-based VMs with excellent performance

---

## ⭐ Option 1: Oracle Cloud (Free Tier) - **RECOMMENDED**

### Why Oracle Cloud?

- **Always Free resources** (no time limit)
- 4 ARM-based Ampere A1 compute instances (4 OCPUs, 24 GB RAM total)
- 2 AMD-based Micro instances (1GB RAM each)
- 200 GB Block Volume storage
- 10 TB outbound data transfer per month
- Load Balancer (10 Mbps)
- Free Kubernetes cluster (OKE)

### Prerequisites

1. Oracle Cloud account (sign up at https://www.oracle.com/cloud/free/)
2. `oci` CLI installed
3. `kubectl` installed

### Step-by-Step Deployment

#### 1. Create Oracle Cloud Account

```bash
# Visit: https://www.oracle.com/cloud/free/
# Sign up for Always Free tier
# Verify email and set up account
```

#### 2. Set Up OCI CLI

```powershell
# Download and install OCI CLI
# Windows PowerShell
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"

# Configure OCI CLI
oci setup config
```

#### 3. Create Virtual Cloud Network (VCN)

```bash
# Create VCN for the healthcare platform
oci network vcn create \
  --display-name healthcare-vcn \
  --cidr-block 10.0.0.0/16 \
  --compartment-id <your-compartment-id> \
  --dns-label healthcare

# Create Internet Gateway
oci network internet-gateway create \
  --display-name healthcare-igw \
  --is-enabled true \
  --vcn-id <vcn-id> \
  --compartment-id <compartment-id>

# Create Subnet
oci network subnet create \
  --display-name healthcare-subnet \
  --cidr-block 10.0.1.0/24 \
  --vcn-id <vcn-id> \
  --compartment-id <compartment-id>
```

#### 4. Create ARM-based Compute Instances

```bash
# Create 1st VM (API Gateway + User Service + Doctor Service)
oci compute instance launch \
  --display-name healthcare-vm-1 \
  --availability-domain <AD-name> \
  --compartment-id <compartment-id> \
  --shape VM.Standard.A1.Flex \
  --shape-config '{"ocpus":2,"memoryInGBs":12}' \
  --image-id <ubuntu-arm-image-id> \
  --subnet-id <subnet-id> \
  --assign-public-ip true \
  --ssh-authorized-keys-file ~/.ssh/id_rsa.pub

# Create 2nd VM (Remaining microservices)
oci compute instance launch \
  --display-name healthcare-vm-2 \
  --availability-domain <AD-name> \
  --compartment-id <compartment-id> \
  --shape VM.Standard.A1.Flex \
  --shape-config '{"ocpus":2,"memoryInGBs":12}' \
  --image-id <ubuntu-arm-image-id> \
  --subnet-id <subnet-id> \
  --assign-public-ip true \
  --ssh-authorized-keys-file ~/.ssh/id_rsa.pub
```

#### 5. Install Docker on VMs

```bash
# SSH into each VM
ssh ubuntu@<vm-public-ip>

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login again
exit
ssh ubuntu@<vm-public-ip>
```

#### 6. Deploy on VM-1 (Infrastructure + Core Services)

```bash
# Clone repository
git clone https://github.com/kally123/doctorApp.git
cd doctorApp

# Create .env file with production settings
cat > .env << EOF
# Database passwords (change these!)
POSTGRES_PASSWORD=your_secure_postgres_password
REDIS_PASSWORD=your_secure_redis_password
MONGO_PASSWORD=your_secure_mongo_password

# JWT Secrets (change these!)
JWT_SECRET=your_production_jwt_secret_min_32_chars
JWT_REFRESH_SECRET=your_production_refresh_secret_min_32_chars

# Payment Gateway (get from Razorpay)
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret

# Video (get from Twilio)
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_API_KEY_SID=your_twilio_api_key_sid
TWILIO_API_KEY_SECRET=your_twilio_api_key_secret
EOF

# Start infrastructure services
docker-compose up -d postgres mongodb redis kafka zookeeper elasticsearch localstack mailhog

# Wait for infrastructure to be ready (2-3 minutes)
sleep 180

# Start core services (on VM-1)
docker-compose up -d api-gateway user-service doctor-service search-service appointment-service
```

#### 7. Deploy on VM-2 (Remaining Services)

```bash
# SSH to VM-2
ssh ubuntu@<vm-2-public-ip>

# Clone repository
git clone https://github.com/kally123/doctorApp.git
cd doctorApp

# Copy .env file from VM-1 or create new one
# Update docker-compose to point to VM-1 for infrastructure

# Create docker-compose.override.yml
cat > docker-compose.override.yml << EOF
version: '3.8'

services:
  payment-service:
    environment:
      SPRING_R2DBC_URL: r2dbc:postgresql://<VM1-IP>:5432/payment_db
      SPRING_DATA_MONGODB_URI: mongodb://admin:mongo_password@<VM1-IP>:27017
      SPRING_REDIS_HOST: <VM1-IP>
      SPRING_KAFKA_BOOTSTRAP_SERVERS: <VM1-IP>:29092
      SPRING_ELASTICSEARCH_URIS: http://<VM1-IP>:9200
  
  # Repeat for other services...
EOF

# Start remaining services
docker-compose up -d payment-service notification-service consultation-service \
  prescription-service ehr-service order-service review-service content-service
```

#### 8. Configure Load Balancer

```bash
# Create Load Balancer (via OCI Console or CLI)
oci lb load-balancer create \
  --display-name healthcare-lb \
  --compartment-id <compartment-id> \
  --subnet-ids '["<subnet-id>"]' \
  --shape-name flexible \
  --shape-details '{"minimumBandwidthInMbps":10,"maximumBandwidthInMbps":10}' \
  --is-private false

# Create Backend Set
oci lb backend-set create \
  --load-balancer-id <lb-id> \
  --name healthcare-backend \
  --policy ROUND_ROBIN \
  --health-checker-protocol HTTP \
  --health-checker-port 8080 \
  --health-checker-url-path /actuator/health

# Add backends (VM-1 and VM-2)
oci lb backend create \
  --backend-set-name healthcare-backend \
  --ip-address <vm-1-private-ip> \
  --port 8080 \
  --load-balancer-id <lb-id>

oci lb backend create \
  --backend-set-name healthcare-backend \
  --ip-address <vm-2-private-ip> \
  --port 8080 \
  --load-balancer-id <lb-id>

# Create Listener
oci lb listener create \
  --load-balancer-id <lb-id> \
  --name healthcare-listener \
  --default-backend-set-name healthcare-backend \
  --port 80 \
  --protocol HTTP
```

#### 9. Set Up Domain (Optional - Free with Freenom)

```bash
# Get a free domain from Freenom (freenom.com)
# Configure DNS A record to point to Load Balancer IP

# Install Certbot for SSL
sudo apt-get install certbot
sudo certbot certonly --standalone -d yourdomain.tk
```

#### 10. Monitor Deployment

```bash
# Check all services
docker ps

# View logs
docker-compose logs -f

# Check health
curl http://<load-balancer-ip>:8080/actuator/health
```

### Resource Allocation on Oracle Cloud

**VM-1** (12 GB RAM, 2 OCPUs):
- PostgreSQL (2 GB)
- MongoDB (2 GB)
- Redis (512 MB)
- Kafka + Zookeeper (2 GB)
- Elasticsearch (2 GB)
- API Gateway (512 MB)
- User Service (1 GB)
- Doctor Service (1 GB)
- Search Service (512 MB)
- Appointment Service (512 MB)

**VM-2** (12 GB RAM, 2 OCPUs):
- Payment Service (1 GB)
- Notification Service (1 GB)
- Consultation Service (1.5 GB)
- Prescription Service (1 GB)
- EHR Service (1.5 GB)
- Order Service (1 GB)
- Review Service (1 GB)
- Content Service (1 GB)
- Frontend (1 GB)

---

## 🌐 Option 2: Google Cloud Platform (GCP Free Tier)

### Free Tier Includes

- 1 e2-micro instance (0.25-1 vCPU, 1 GB RAM) per month
- 30 GB HDD storage
- 1 GB network egress per day (Americas)
- Cloud SQL (limited)

### Limitations

- **Only 1 small VM** - Cannot run all 13 services
- Need to use managed services (extra cost beyond free tier)

### Deployment Strategy (Minimal Setup)

#### Deploy Essential Services Only

```bash
# Install Google Cloud SDK
# Visit: https://cloud.google.com/sdk/docs/install

# Login
gcloud auth login

# Create project
gcloud projects create healthcare-platform-001 --name="Healthcare Platform"
gcloud config set project healthcare-platform-001

# Enable APIs
gcloud services enable compute.googleapis.com
gcloud services enable sqladmin.googleapis.com

# Create e2-micro instance
gcloud compute instances create healthcare-vm \
  --zone=us-central1-a \
  --machine-type=e2-micro \
  --image-family=ubuntu-2204-lts \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --tags=http-server,https-server

# Create firewall rules
gcloud compute firewall-rules create allow-http \
  --allow=tcp:80,tcp:443,tcp:8080 \
  --target-tags=http-server

# SSH into instance
gcloud compute ssh healthcare-vm --zone=us-central1-a

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Deploy ONLY critical services (due to 1GB RAM limitation)
# Create minimal docker-compose.yml with:
# - PostgreSQL (shared by all services)
# - Redis (for caching)
# - API Gateway
# - User Service
# - Doctor Service
# - Appointment Service
# - Patient Frontend

# Use Cloud SQL for PostgreSQL (paid, but more reliable)
# Use Cloud Memorystore for Redis (paid)
```

### Cost Warning

GCP free tier is very limited. You'll quickly exceed it with this architecture. Better for learning/testing only.

---

## ☁️ Option 3: AWS Free Tier

### Free Tier Includes (12 months)

- 1 t2.micro instance (1 GB RAM, 1 vCPU) - 750 hours/month
- 30 GB EBS storage
- 5 GB S3 storage
- RDS db.t2.micro (20 GB) - 750 hours/month
- 15 GB data transfer out

### Limitations

- **Only 12 months free**
- **1 GB RAM** - Insufficient for all services
- Quickly becomes expensive after free tier

### Deployment Strategy

```bash
# Install AWS CLI
# Visit: https://aws.amazon.com/cli/

# Configure AWS CLI
aws configure

# Create EC2 instance
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t2.micro \
  --key-name MyKeyPair \
  --security-groups healthcare-sg \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=healthcare-vm}]'

# Create RDS PostgreSQL instance
aws rds create-db-instance \
  --db-instance-identifier healthcare-postgres \
  --db-instance-class db.t2.micro \
  --engine postgres \
  --master-username postgres \
  --master-user-password YourPassword123 \
  --allocated-storage 20

# Due to limitations, deploy minimal services
# OR use AWS ECS/Fargate (but costs add up quickly)
```

### Recommendation

AWS free tier is too limited for this architecture. Consider it only for learning or use paid tier.

---

## 🎓 Option 4: Azure for Students

### Free Credits

- $100 Azure credit (12 months)
- Free services (limited)

### Deployment

```bash
# Install Azure CLI
# Visit: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli

# Login
az login

# Create resource group
az group create --name healthcare-rg --location eastus

# Create VM
az vm create \
  --resource-group healthcare-rg \
  --name healthcare-vm \
  --image UbuntuLTS \
  --size Standard_B2s \
  --admin-username azureuser \
  --generate-ssh-keys

# Create AKS cluster (uses credits quickly)
az aks create \
  --resource-group healthcare-rg \
  --name healthcare-aks \
  --node-count 2 \
  --node-vm-size Standard_B2s \
  --generate-ssh-keys

# Deploy to AKS
az aks get-credentials --resource-group healthcare-rg --name healthcare-aks
kubectl apply -f k8s/
```

---

## 🚂 Option 5: Railway.app (Hobby Plan)

### Features

- $5/month free credit
- Easy deployment
- PostgreSQL, Redis, MongoDB included
- Automatic SSL
- Git-based deployment

### Deployment

```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Create project
railway init

# Deploy each service
cd backend/user-service
railway up

# Add PostgreSQL
railway add postgres

# Add Redis
railway add redis

# Repeat for each service
```

### Limitations

- $5 credit runs out quickly with 13 services
- Better for 2-3 services maximum

---

## 🎨 Option 6: Render.com (Free Tier)

### Features

- 750 hours/month per service
- Free PostgreSQL database
- Automatic SSL
- Git-based deployment

### Deployment

```yaml
# render.yaml in project root
services:
  - type: web
    name: api-gateway
    env: docker
    dockerfilePath: ./backend/api-gateway/Dockerfile
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
  
  - type: web
    name: user-service
    env: docker
    dockerfilePath: ./backend/user-service/Dockerfile
  
  # Repeat for each service...

databases:
  - name: healthcare-postgres
    databaseName: healthcare
    user: healthcare_user
```

```bash
# Deploy via Render Dashboard
# 1. Connect GitHub repo
# 2. Create services from render.yaml
# 3. Configure environment variables
```

### Limitations

- Services sleep after 15 minutes of inactivity
- Slow cold starts
- Limited to a few services on free tier

---

## 🪰 Option 7: Fly.io (Free Tier)

### Features

- 3 shared-cpu-1x VMs (256MB RAM each)
- 160 GB outbound data transfer
- Automatic SSL
- Global deployment

### Deployment

```bash
# Install flyctl
# Windows PowerShell
iwr https://fly.io/install.ps1 -useb | iex

# Login
flyctl auth login

# Launch app
cd backend/api-gateway
flyctl launch --name healthcare-gateway

# Deploy
flyctl deploy

# Scale (uses free allowance)
flyctl scale count 2

# Add PostgreSQL
flyctl postgres create --name healthcare-db

# Connect app to database
flyctl postgres attach healthcare-db
```

### Limitations

- 256MB RAM per VM - very limited
- Maximum 3 VMs free
- Not suitable for all 13 services

---

## 🎯 Hybrid Approach (RECOMMENDED)

### Best Free Deployment Strategy

Combine multiple providers to maximize free resources:

1. **Oracle Cloud** - Main application (all backend services)
2. **Vercel** - Patient frontend (Next.js) - FREE
3. **Netlify** - Doctor dashboard (React) - FREE
4. **Cloudflare** - CDN, DNS, SSL - FREE
5. **MongoDB Atlas** - MongoDB (512MB free) - FREE
6. **Redis Cloud** - Redis (30MB free) - FREE
7. **Elasticsearch Cloud** - 14-day trial then use self-hosted

### Implementation

```bash
# 1. Deploy backend to Oracle Cloud (see Option 1)

# 2. Deploy Patient Frontend to Vercel
cd frontend/patient-webapp
npm install -g vercel
vercel login
vercel --prod

# 3. Deploy Doctor Dashboard to Netlify
cd frontend/doctor-dashboard
npm install -g netlify-cli
netlify login
netlify deploy --prod --dir=dist

# 4. Set up Cloudflare
# - Add your domain to Cloudflare
# - Point DNS to Oracle Cloud Load Balancer
# - Enable SSL/TLS
# - Enable CDN caching

# 5. Use MongoDB Atlas
# - Create free cluster at mongodb.com/cloud/atlas
# - Update connection strings in services

# 6. Use Redis Cloud
# - Create free database at redis.com/try-free
# - Update connection strings in services
```

---

## 💰 Cost Optimization

### Tips to Stay Within Free Tiers

1. **Use ARM architecture** (Oracle Cloud) - Better performance per dollar
2. **Combine services** - Run multiple Spring Boot apps in single VM
3. **Use serverless for frontends** - Vercel, Netlify (free)
4. **Optimize Docker images** - Use multi-stage builds, Alpine Linux
5. **Enable caching** - Reduce database queries
6. **Use CDN** - Cloudflare (free)
7. **Compress responses** - Enable GZIP
8. **Lazy loading** - Don't start all services if not needed
9. **Monitor usage** - Set up billing alerts
10. **Use spot/preemptible instances** - (not available in free tier, but good for production)

### Resource Optimization

```yaml
# docker-compose.prod.yml - Optimized for low resources
version: '3.8'

services:
  user-service:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M
    environment:
      JAVA_OPTS: -Xmx384m -Xms256m
  
  # Repeat for other services...
```

### JVM Optimization for Low Memory

```bash
# Add to all Spring Boot services
JAVA_OPTS=-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -Xss256k
```

---

## 📊 Deployment Checklist

### Pre-Deployment

- [ ] Change all default passwords
- [ ] Generate strong JWT secrets
- [ ] Set up domain name
- [ ] Configure SSL certificates
- [ ] Set up environment variables
- [ ] Configure CORS origins
- [ ] Set up monitoring/logging
- [ ] Create backups strategy
- [ ] Test all services locally

### Post-Deployment

- [ ] Verify all services are running
- [ ] Check health endpoints
- [ ] Test user registration/login
- [ ] Test critical user flows
- [ ] Set up monitoring alerts
- [ ] Configure automated backups
- [ ] Document deployment details
- [ ] Set up CI/CD pipeline

---

## 🚀 Quick Start - Oracle Cloud Deployment

For the fastest deployment to Oracle Cloud:

```bash
# 1. Create Oracle Cloud account
# https://www.oracle.com/cloud/free/

# 2. Clone deployment scripts
git clone https://github.com/kally123/doctorApp.git
cd doctorApp/scripts

# 3. Run automated deployment
./deploy-oracle-cloud.sh

# Follow the prompts to:
# - Configure OCI CLI
# - Create VMs
# - Deploy services
# - Set up load balancer
```

---

## 📞 Support

For deployment issues:
- Check [Troubleshooting Guide](../README.md#troubleshooting)
- Review [FAQ](../README.md#faq)
- Open [GitHub Issue](https://github.com/kally123/doctorApp/issues)

---

**Last Updated**: February 21, 2026


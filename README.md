# 🏥 Healthcare Platform (DocApp)

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21+-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-61DAFB)](https://react.dev/)

A comprehensive healthcare platform similar to Practo, enabling patients to discover doctors, book appointments, conduct video consultations, manage health records, order medicines, and book lab tests.

### 🚀 Deploy to Free Cloud in 30 Minutes

> **100% FREE deployment** using Oracle Cloud Always Free Tier + Vercel + Netlify

```bash
git clone https://github.com/kally123/doctorApp.git
cd doctorApp
./scripts/deploy-oracle-cloud.sh  # Follow the prompts
```

📖 **[Complete Deployment Guide](docs/deployment/DEPLOYMENT_GUIDE.md)** | **[Free Cloud Options](docs/deployment/deployment-free-cloud.md)**

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Database Management](#database-management)
- [Development](#development)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Security](#security)
- [Monitoring & Observability](#monitoring--observability)
- [Deployment](#deployment)
  - [Free Cloud Deployment](#free-cloud-deployment)
- [Troubleshooting](#troubleshooting)
- [Performance & Resource Requirements](#performance--resource-requirements)
- [FAQ](#faq)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

Healthcare Platform is a full-featured telemedicine and healthcare management system designed to connect patients with healthcare providers. The platform supports multiple user types including patients, doctors, clinics, pharmacies, labs, and administrators.

### Key User Personas

| Persona | Description |
|---------|-------------|
| **Patient** | Find doctors, book appointments, video consultations, health records |
| **Doctor** | Manage practice, conduct consultations, write prescriptions |
| **Clinic/Hospital** | Multi-doctor management, scheduling, billing |
| **Pharmacy** | Process prescriptions, manage inventory |
| **Lab Partner** | Manage test orders, upload reports |
| **Admin** | Platform management, moderation, analytics |

---

## ✨ Features

### For Patients
- 🔍 **Doctor Discovery** - Search doctors by specialty, location, ratings, and availability
- 📅 **Appointment Booking** - Book in-person or video consultations
- 📹 **Video Consultation** - HD video calls with screen sharing and chat
- 💊 **Digital Prescriptions** - View and download prescriptions
- 📁 **Health Records** - Centralized EHR with document uploads
- 🛒 **Medicine Orders** - Order medicines from prescriptions
- 🧪 **Lab Test Booking** - Book tests with home collection option
- ⭐ **Reviews & Ratings** - Rate and review doctors

### For Doctors
- 👨‍⚕️ **Profile Management** - Comprehensive doctor profiles
- 📆 **Availability Management** - Flexible schedule management
- 💻 **Teleconsultation** - Video/audio consultations with patients
- 📝 **Prescription Writing** - Digital prescription with medicine database
- 📊 **Patient History** - Access to patient health records
- 💰 **Earnings Dashboard** - Track earnings and payouts

### Platform Features
- 🔐 **Secure Authentication** - JWT, OAuth 2.0, biometric login
- 🔔 **Real-time Notifications** - Push, SMS, and email notifications
- 💳 **Payment Integration** - Multiple payment gateways
- 📱 **Mobile Apps** - iOS and Android apps for patients and doctors
- 📈 **Analytics** - Comprehensive dashboards and reporting

---

## 🏗️ Architecture

The platform follows a **microservices architecture** with **reactive programming** principles and **event-driven** communication.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT APPLICATIONS                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │  Patient    │ │   Doctor    │ │  Patient    │ │   Doctor    │           │
│  │  Web App    │ │  Dashboard  │ │ Mobile App  │ │ Mobile App  │           │
│  │  (Next.js)  │ │   (React)   │ │(RN/Flutter) │ │(RN/Flutter) │           │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘           │
│         └───────────────┴───────────────┴───────────────┘                   │
│                                   │                                          │
│                          ┌────────▼────────┐                                │
│                          │   API Gateway   │                                │
│                          │ (Spring Cloud)  │                                │
│                          └────────┬────────┘                                │
├───────────────────────────────────┼─────────────────────────────────────────┤
│                          MICROSERVICES LAYER                                 │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐              │
│  │  User   │ │ Doctor  │ │Appoint- │ │ Consult │ │Prescrip-│              │
│  │ Service │ │ Service │ │  ment   │ │  ation  │ │  tion   │              │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘              │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐              │
│  │  EHR    │ │  Order  │ │ Payment │ │ Notif.  │ │ Search  │              │
│  │ Service │ │ Service │ │ Service │ │ Service │ │ Service │              │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘              │
│  ┌─────────┐ ┌─────────┐                                                   │
│  │ Review  │ │ Content │                                                   │
│  │ Service │ │ Service │                                                   │
│  └─────────┘ └─────────┘                                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                            DATA LAYER                                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         │
│  │PostgreSQL│ │ MongoDB  │ │  Redis   │ │Elastic-  │ │  Kafka   │         │
│  │ (R2DBC)  │ │ Reactive │ │ (Cache)  │ │ search   │ │ (Events) │         │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Core Services

| Service | Description | Database |
|---------|-------------|----------|
| **User Service** | Authentication, user management | PostgreSQL |
| **Doctor Service** | Doctor profiles, availability | PostgreSQL + Elasticsearch |
| **Appointment Service** | Booking, scheduling | PostgreSQL |
| **Consultation Service** | Video/audio calls, chat | PostgreSQL + Redis |
| **Prescription Service** | Digital prescriptions | PostgreSQL + MongoDB |
| **EHR Service** | Health records | MongoDB |
| **Order Service** | Medicine & lab orders | PostgreSQL |
| **Payment Service** | Payment processing | PostgreSQL |
| **Notification Service** | Push, SMS, email | MongoDB |
| **Search Service** | Doctor & content search | Elasticsearch |
| **Review Service** | Ratings & reviews | PostgreSQL |
| **Content Service** | Health articles | MongoDB |

### Infrastructure Services

| Service | Version | Purpose |
|---------|---------|---------|
| **PostgreSQL** | 15-alpine | Primary relational database (9 databases) |
| **MongoDB** | 7.0 | Document storage for EHR, chat, content |
| **Redis** | 7-alpine | Caching, session management |
| **Apache Kafka** | 7.5.0 | Event streaming between services |
| **Zookeeper** | 7.5.0 | Kafka coordination |
| **Elasticsearch** | 8.11.0 | Search engine for doctors and content |
| **LocalStack** | 3.0 | AWS S3 simulation for file storage |
| **MailHog** | latest | Email testing (SMTP server + Web UI) |

---

## 🛠️ Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21+ | Primary language |
| Spring Boot | 3.x | Application framework |
| Spring WebFlux | 3.x | Reactive programming |
| R2DBC | Latest | Reactive database driver |
| PostgreSQL | 15+ | Primary relational database |
| MongoDB | 7+ | Document storage (EHR, content) |
| Redis | 7+ | Caching & sessions |
| Apache Kafka | 3.x | Event streaming |
| Elasticsearch | 8+ | Search engine |
| Spring Cloud Gateway | 4.x | API Gateway |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 14+ | Patient web application |
| React | 18+ | Doctor dashboard & Admin portal |
| TypeScript | 5+ | Type safety |
| Tailwind CSS | 3+ | Styling |
| React Query | 5+ | Server state management |
| Shadcn/ui | Latest | UI components |

### Mobile

| Technology | Purpose |
|------------|---------|
| React Native / Flutter | Cross-platform mobile apps |
| Twilio/Agora SDK | Video consultations |

### DevOps & Infrastructure

| Technology | Purpose |
|------------|---------|
| Kubernetes | Container orchestration |
| Docker | Containerization |
| GitHub Actions | CI/CD pipelines |
| HashiCorp Vault | Secrets management |
| Prometheus + Grafana | Monitoring |
| ELK Stack | Logging |
| Jaeger | Distributed tracing |

---

## 📁 Project Structure

```
doctorApp/
├── README.md                         # This file
├── PROJECT_PLAN.md                   # Detailed project plan & tasks
├── ARCHITECTURE_INSTRUCTIONS.md      # Architecture guidelines
├── docker-compose.yaml               # Docker compose configuration
├── phases/                           # Implementation phase documents
│   ├── phase-1-foundation.md         # Weeks 1-6: Setup & core services
│   ├── phase-2-appointment-booking.md# Weeks 7-10: Doctor search & booking
│   ├── phase-3-teleconsultation.md   # Weeks 11-14: Video consultation
│   ├── phase-4-clinical-features.md  # Weeks 15-18: Prescriptions & EHR
│   ├── phase-5-commerce.md           # Weeks 19-22: Orders & lab tests
│   └── phase-6-enhancement-launch.md # Weeks 23-26: Mobile, scale & launch
├── backend/                          # Backend services (Maven multi-module)
│   ├── pom.xml                       # Parent POM
│   ├── healthcare-common/            # Shared libraries & utilities
│   ├── api-gateway/                  # API Gateway
│   ├── user-service/
│   ├── doctor-service/
│   ├── appointment-service/
│   ├── consultation-service/
│   ├── prescription-service/
│   ├── ehr-service/
│   ├── order-service/
│   ├── payment-service/
│   ├── notification-service/
│   ├── search-service/
│   ├── review-service/
│   └── content-service/
├── frontend/                         # Frontend applications
│   ├── patient-webapp/               # Next.js patient app
│   └── doctor-dashboard/             # React/Vite doctor dashboard
├── mobile/                           # Mobile applications
│   ├── patient-app/                  # React Native patient app
│   └── doctor-app/                   # React Native doctor app
├── k8s/                              # Kubernetes manifests
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   ├── ingress.yaml
│   ├── services/                     # Service deployments
│   ├── infrastructure/               # Infrastructure components
│   └── monitoring/                   # Monitoring stack
├── scripts/                          # Utility scripts
│   ├── init-localstack.sh
│   ├── init-mongodb.sh
│   └── init-multiple-databases.sh
└── docs/                             # Additional documentation
    └── production-checklist.md
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - Included via Maven Wrapper (mvnw)
- **Node.js 18+** - [Download](https://nodejs.org/)
- **Docker & Docker Compose** - [Download](https://docker.com/)
- **Kubernetes** (for production) - [Minikube](https://minikube.sigs.k8s.io/) for local
- **Git** - [Download](https://git-scm.com/)

**Note**: Docker Compose is required to run the full stack locally. The project includes 13 application services and 8 infrastructure services.

### Quick Start (Docker Compose - Recommended)

The easiest way to run the entire platform:

```bash
# Clone the repository
git clone https://github.com/kally123/doctorApp.git
cd doctorApp

# Start all services (may take 5-10 minutes on first run)
docker-compose up -d

# Check service health
docker-compose ps

# View logs for a specific service
docker-compose logs -f user-service

# Stop all services
docker-compose down
```

This will start:
- 8 Infrastructure services (PostgreSQL, MongoDB, Redis, Kafka, Zookeeper, Elasticsearch, LocalStack, MailHog)
- 13 Application services (API Gateway + 12 microservices)
- 1 Frontend application (Patient Web App)

### Manual Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/kally123/doctorApp.git
   cd doctorApp
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis zookeeper kafka mongodb elasticsearch mailhog localstack
   ```
   This starts all infrastructure services (PostgreSQL, Redis, Kafka, MongoDB, Elasticsearch, etc).

3. **Build backend services**
   ```bash
   cd backend
   ./mvnw clean install -DskipTests
   ```
   Or on Windows:
   ```bash
   cd backend
   mvnw.cmd clean install -DskipTests
   ```

4. **Start all services**
   ```bash
   docker-compose up -d
   ```

5. **Access the applications**
   - API Gateway: http://localhost:8080
   - Patient Web App: http://localhost:3000
   - Doctor Dashboard: http://localhost:5173 (if built separately)
   - MailHog UI (Email testing): http://localhost:8025
   - PostgreSQL: localhost:5432 (user: postgres, password: postgres)
   - MongoDB: localhost:27017 (user: admin, password: mongo_password)
   - Redis: localhost:6379 (password: redis_password)
   - Kafka: localhost:29092 (external), localhost:9092 (internal)
   - Elasticsearch: http://localhost:9200
   - LocalStack (S3): http://localhost:4566

### Individual Service Ports

| Service | Port |
|---------|------|
| User Service | 8081 |
| Doctor Service | 8082 |
| Search Service | 8083 |
| Appointment Service | 8084 |
| Payment Service | 8085 |
| Notification Service | 8086 |
| Consultation Service | 8087 |
| Prescription Service | 8088 |
| EHR Service | 8089 |
| Order Service | 8090 |
| Review Service | 8091 |
| Content Service | 8092 |

### Platform Implementation Status

| Phase | Status |
|-------|--------|
| Phase 1: Foundation | ✅ Complete |
| Phase 2: Appointment Booking | ✅ Complete |
| Phase 3: Teleconsultation | ✅ Complete |
| Phase 4: Clinical Features | ✅ Complete |
| Phase 5: Commerce | ✅ Complete |
| Phase 6: Enhancement & Launch | ✅ Complete |

**13 Services (12 Microservices + API Gateway)** | **2 Web Apps** | **2 Mobile Apps** | **Production Ready**

See implementation status documents for each phase:
- [PHASE1_IMPLEMENTATION_STATUS.md](PHASE1_IMPLEMENTATION_STATUS.md)
- [PHASE2_IMPLEMENTATION_STATUS.md](PHASE2_IMPLEMENTATION_STATUS.md)
- [PHASE3_IMPLEMENTATION_STATUS.md](PHASE3_IMPLEMENTATION_STATUS.md)
- [PHASE4_IMPLEMENTATION_STATUS.md](PHASE4_IMPLEMENTATION_STATUS.md)
- [PHASE5_IMPLEMENTATION_STATUS.md](PHASE5_IMPLEMENTATION_STATUS.md)
- [PHASE6_IMPLEMENTATION_STATUS.md](PHASE6_IMPLEMENTATION_STATUS.md)

---

## 🔐 Environment Variables

### Required Environment Variables for Production

For local development, default values are provided in `docker-compose.yaml`. For production, set these:

```bash
# Payment Gateway (Razorpay)
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret

# Video Consultation (Twilio)
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_API_KEY_SID=your_twilio_api_key_sid
TWILIO_API_KEY_SECRET=your_twilio_api_key_secret

# JWT Secrets (change these!)
JWT_SECRET=your-production-jwt-secret-key-here
JWT_REFRESH_SECRET=your-production-refresh-secret-key-here

# Database Passwords (change these!)
POSTGRES_PASSWORD=secure_postgres_password
REDIS_PASSWORD=secure_redis_password
MONGO_PASSWORD=secure_mongo_password

# AWS (if using real S3 instead of LocalStack)
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=your_aws_region
```

### Using .env File

Create a `.env` file in the project root (not committed to git):

```bash
RAZORPAY_KEY_ID=rzp_test_xxxxx
RAZORPAY_KEY_SECRET=xxxxx
# ... other variables
```

Docker Compose will automatically load these.

---

## 🗄️ Database Management

### Database Initialization

The project includes initialization scripts in the `scripts/` directory:

```bash
# Initialize multiple PostgreSQL databases
scripts/init-multiple-databases.sh

# Initialize MongoDB with collections and indexes
scripts/init-mongodb.sh

# Initialize LocalStack (S3 buckets)
scripts/init-localstack.sh
```

These scripts run automatically when using Docker Compose.

### Database Schema

| Service | Database Type | Database Name |
|---------|---------------|---------------|
| User Service | PostgreSQL | user_db |
| Doctor Service | PostgreSQL | doctor_db |
| Appointment Service | PostgreSQL | appointment_db |
| Payment Service | PostgreSQL | payment_db |
| Consultation Service | PostgreSQL | consultation_db |
| Prescription Service | PostgreSQL | prescription_db |
| Order Service | PostgreSQL | order_db |
| Review Service | PostgreSQL | review_db |
| Search Service | Elasticsearch | doctors, content |
| EHR Service | MongoDB | ehr_db |
| Notification Service | MongoDB | notification_db |
| Content Service | MongoDB | content_db |

### Database Access

**PostgreSQL:**
```bash
# Via Docker
docker exec -it healthcare-postgres psql -U postgres -d user_db

# Direct connection
psql -h localhost -p 5432 -U postgres -d user_db
```

**MongoDB:**
```bash
# Via Docker
docker exec -it healthcare-mongodb mongosh -u admin -p mongo_password

# Connect to specific database
docker exec -it healthcare-mongodb mongosh -u admin -p mongo_password ehr_db
```

**Redis:**
```bash
# Via Docker
docker exec -it healthcare-redis redis-cli -a redis_password

# Check cached data
docker exec -it healthcare-redis redis-cli -a redis_password KEYS "*"
```

---

## 💻 Development

### Spring Profiles

The application supports multiple Spring profiles:

| Profile | Description | Usage |
|---------|-------------|-------|
| **default** | Local development without Docker | `./mvnw spring-boot:run` |
| **docker** | Running in Docker containers | Used in docker-compose.yaml |
| **test** | Testing environment | Used during tests |
| **prod** | Production environment | Kubernetes deployment |

To run with a specific profile:
```bash
# Local development
./mvnw spring-boot:run -Dspring-boot.run.profiles=default

# Production
java -jar app.jar --spring.profiles.active=prod
```

### Code Style & Standards

- **Backend**: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **Frontend**: ESLint + Prettier configuration provided
- **Commits**: Follow [Conventional Commits](https://conventionalcommits.org/)

### Reactive Programming Guidelines

> ⚠️ **Important**: This project uses reactive programming end-to-end. Never use blocking operations!

```java
// ✅ Correct - Reactive
public Mono<User> findUser(String id) {
    return userRepository.findById(id);
}

// ❌ Wrong - Blocking
public User findUser(String id) {
    return userRepository.findById(id).block();
}
```

### Running Tests

```bash
# Backend tests
cd backend
./mvnw test

# Frontend tests (patient-webapp)
cd frontend/patient-webapp
npm test

# Frontend tests (doctor-dashboard)
cd frontend/doctor-dashboard
npm test

# Integration tests
cd backend
./mvnw verify

# E2E tests
npm run test:e2e
```

### Building for Production

```bash
# Build all backend services
cd backend
./mvnw clean package

# Build Docker images for backend
docker-compose build

# Build patient webapp
cd frontend/patient-webapp
npm run build

# Build doctor dashboard
cd frontend/doctor-dashboard
npm run build
```

---

## 📚 API Documentation

### API Gateway Routes

All client requests go through the API Gateway (port 8080) which routes to backend services:

| Route Pattern | Target Service | Port |
|--------------|----------------|------|
| `/api/users/**` | User Service | 8081 |
| `/api/doctors/**` | Doctor Service | 8082 |
| `/api/search/**` | Search Service | 8083 |
| `/api/appointments/**` | Appointment Service | 8084 |
| `/api/payments/**` | Payment Service | 8085 |
| `/api/notifications/**` | Notification Service | 8086 |
| `/api/consultations/**` | Consultation Service | 8087 |
| `/api/prescriptions/**` | Prescription Service | 8088 |
| `/api/health-records/**` | EHR Service | 8089 |
| `/api/orders/**` | Order Service | 8090 |
| `/api/reviews/**` | Review Service | 8091 |
| `/api/content/**` | Content Service | 8092 |

**Example API Calls:**
```bash
# User registration
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Search doctors
curl http://localhost:8080/api/search/doctors?specialty=cardiology&city=Mumbai

# Book appointment
curl -X POST http://localhost:8080/api/appointments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"doctorId":"123","dateTime":"2026-02-25T10:00:00"}'
```

API documentation is available via OpenAPI/Swagger:

- **Development**: http://localhost:8080/swagger-ui.html
- **Production**: https://api.healthapp.com/docs

### Key API Endpoints

| Service | Base Path | Description |
|---------|-----------|-------------|
| User | `/api/v1/users` | User registration, authentication |
| Doctor | `/api/v1/doctors` | Doctor profiles, search |
| Appointment | `/api/v1/appointments` | Booking, scheduling |
| Consultation | `/api/v1/consultations` | Video calls, chat |
| Prescription | `/api/v1/prescriptions` | Digital prescriptions |
| EHR | `/api/v1/health-records` | Health records |
| Order | `/api/v1/orders` | Medicine & lab orders |
| Payment | `/api/v1/payments` | Payment processing |

---

## 🧪 Testing

### Test Coverage Requirements

| Type | Coverage Target |
|------|-----------------|
| Unit Tests | ≥ 80% |
| Integration Tests | Critical paths |
| E2E Tests | All user flows |

### Testing Tools

- **Backend**: JUnit 5, Mockito, TestContainers, WebTestClient
- **Frontend**: Jest, React Testing Library, Playwright
- **Load Testing**: k6, Artillery

---

## 🔒 Security

### Authentication & Authorization

- **JWT Tokens**: Access tokens (15 min) and refresh tokens (7 days)
- **Password Encryption**: BCrypt with configurable strength
- **Role-Based Access Control (RBAC)**: Patient, Doctor, Admin, Pharmacy, Lab
- **API Rate Limiting**: Configured in API Gateway
- **CORS**: Configured for allowed origins

### Security Headers

All responses include security headers:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000`

### Sensitive Data Protection

- **Encryption at Rest**: Database encryption enabled
- **Encryption in Transit**: TLS 1.3 for all communications
- **Secrets Management**: HashiCorp Vault in production, environment variables in development
- **PII Handling**: HIPAA-compliant data handling for health records
- **Video Privacy**: Encrypted video streams with Twilio

### Security Best Practices

```java
// All endpoints require authentication by default
@PreAuthorize("hasRole('PATIENT')")
public Mono<Appointment> bookAppointment() { }

// Sensitive operations require specific roles
@PreAuthorize("hasRole('DOCTOR')")
public Mono<Prescription> createPrescription() { }

// Admin-only operations
@PreAuthorize("hasRole('ADMIN')")
public Mono<User> deleteUser() { }
```

---

## 📊 Monitoring & Observability

### Health Checks

All services expose Spring Boot Actuator endpoints:

```bash
# Check API Gateway health
curl http://localhost:8080/actuator/health

# Check individual service health
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Doctor Service
# ... etc
```

### Metrics

Metrics are available at `/actuator/metrics` endpoint for each service.

### Kubernetes Monitoring

When deployed to Kubernetes, monitoring stack includes:
- **Prometheus** - Metrics collection
- **Grafana** - Visualization dashboards
- **Jaeger** - Distributed tracing
- **ELK Stack** - Centralized logging

Deploy monitoring stack:
```bash
kubectl apply -f k8s/monitoring/
```

---

## 🚢 Deployment

### Environments

| Environment | Purpose | URL |
|-------------|---------|-----|
| Development | Local development | localhost |
| Staging | Pre-production testing | staging.healthapp.com |
| Production | Live environment | healthapp.com |

### CI/CD Pipeline

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Commit  │───▶│  Build   │───▶│  Test    │───▶│  Deploy  │
│          │    │          │    │          │    │          │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                     │               │               │
                     ▼               ▼               ▼
                 Compile        Unit Tests      Staging
                 Lint           Integration     Production
                 Docker Build   Security Scan   (manual gate)
```

### Kubernetes Deployment

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Apply configurations
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml

# Deploy infrastructure
kubectl apply -f k8s/infrastructure/

# Deploy services
kubectl apply -f k8s/services/

# Deploy monitoring (optional)
kubectl apply -f k8s/monitoring/

# Setup ingress
kubectl apply -f k8s/ingress.yaml
```

---

## ☁️ Free Cloud Deployment

The platform can be deployed to various free cloud providers. **[See all deployment guides →](docs/deployment/)**

### 📚 Deployment Guides

1. **[Free Cloud Deployment Guide](docs/deployment/deployment-free-cloud.md)** - Complete guide covering:
   - ⭐ **Oracle Cloud (Always Free)** - RECOMMENDED for full deployment
   - Google Cloud Platform (GCP Free Tier)
   - AWS Free Tier
   - Azure for Students
   - Hybrid approach (combining multiple providers)

2. **[Railway & Render Deployment](docs/deployment/deployment-railway-render.md)** - Quick deployment to:
   - Railway.app (Hobby plan - $5/month credit)
   - Render.com (Free tier with limitations)
   - Fly.io (Free tier - 3 VMs)

3. **[Docker Optimization Guide](docs/deployment/docker-optimization.md)** - Optimize for resource-constrained environments:
   - Reduce image sizes by 70%
   - Cut RAM usage by 50%
   - Faster startup times
   - Multi-stage builds
   - JVM optimization

### 🎯 Quick Recommendations

| Use Case | Recommended Platform | Services | Cost |
|----------|---------------------|----------|------|
| **Full Production** | Oracle Cloud Always Free | All 13 services | **FREE Forever** |
| **MVP/Testing** | Railway.app | 3-4 core services | $5/month credit |
| **Demo/POC** | Render.com | Essential services | Free (with sleep) |
| **Individual Service Test** | Fly.io | 1-3 services | Free (3 VMs) |
| **Hybrid (Best)** | Oracle + Vercel + Netlify | Backend + Frontends | **FREE** |

### 🚀 Fastest Deployment (Oracle Cloud)

```bash
# Clone repository
git clone https://github.com/kally123/doctorApp.git
cd doctorApp

# Run automated deployment script
chmod +x scripts/deploy-oracle-cloud.sh
./scripts/deploy-oracle-cloud.sh

# Follow the prompts - deployment takes ~20-30 minutes
```

### 📖 Step-by-Step Guides

- **Oracle Cloud**: See [deployment-free-cloud.md](docs/deployment/deployment-free-cloud.md#option-1-oracle-cloud-free-tier)
- **Railway**: See [deployment-railway-render.md](docs/deployment/deployment-railway-render.md#quick-deploy-to-railwayapp)
- **Render**: See [deployment-railway-render.md](docs/deployment/deployment-railway-render.md#quick-deploy-to-rendercom)
- **Docker Optimization**: See [docker-optimization.md](docs/deployment/docker-optimization.md)

### 💰 Cost Comparison

| Provider | Monthly Cost | Limitations | Best For |
|----------|--------------|-------------|----------|
| Oracle Cloud | **$0** | None (Forever free) | Production, Full stack |
| Railway | $0-5 | $5 credit/month | Quick MVP |
| Render | $0 | Services sleep | Demos |
| Vercel | $0 | Bandwidth limits | Frontend only |
| Netlify | $0 | Bandwidth limits | Frontend only |

### ⚠️ Important Notes

- **Oracle Cloud** offers the most generous free tier (4 ARM VMs, 24GB RAM total)
- **Railway** credit runs out quickly with many services
- **Render** services sleep after 15 minutes (slow cold starts)
- **Hybrid approach** (Oracle + Vercel + Netlify) is FREE and production-ready

---

## 🔧 Troubleshooting

### Common Issues

**Services not starting:**
```bash
# Check logs
docker-compose logs [service-name]

# Restart a specific service
docker-compose restart [service-name]

# Rebuild and restart
docker-compose up -d --build [service-name]
```

**Database connection issues:**
```bash
# Ensure infrastructure services are healthy
docker-compose ps

# Check PostgreSQL
docker exec -it healthcare-postgres psql -U postgres -l

# Check MongoDB
docker exec -it healthcare-mongodb mongosh -u admin -p mongo_password
```

**Port conflicts:**
```bash
# Check what's using a port (Windows PowerShell)
netstat -ano | findstr :[PORT]

# Kill process by PID
taskkill /PID [PID] /F
```

**Out of memory errors:**
```bash
# Increase Docker memory limit in Docker Desktop settings
# Recommended: At least 8GB RAM for running all services
```

**Maven build issues:**
```bash
# Clean Maven cache
cd backend
./mvnw clean

# Force update dependencies
./mvnw clean install -U
```

---

## 📈 Project Timeline

| Phase | Duration | Focus |
|-------|----------|-------|
| **Phase 1** | Weeks 1-6 | Foundation, User & Doctor Services |
| **Phase 2** | Weeks 7-10 | Doctor Search & Appointment Booking |
| **Phase 3** | Weeks 11-14 | Teleconsultation (Video/Audio) |
| **Phase 4** | Weeks 15-18 | Prescriptions & EHR |
| **Phase 5** | Weeks 19-22 | Medicine Orders & Lab Tests |
| **Phase 6** | Weeks 23-26 | Mobile Apps, Scale & Launch |

See detailed phase documents in the `/phases` directory.

---

## ⚡ Performance & Resource Requirements

### Minimum System Requirements (Development)

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| RAM | 8 GB | 16 GB |
| CPU | 4 cores | 8 cores |
| Storage | 20 GB | 50 GB |
| Docker Memory | 6 GB | 8 GB |

### Production Recommendations

- **Horizontal Scaling**: All services are stateless and can be scaled horizontally
- **Database Connection Pooling**: Configured with HikariCP for optimal performance
- **Reactive Non-Blocking**: Spring WebFlux ensures high concurrency with low resource usage
- **Caching Strategy**: Redis for session management, query result caching
- **Event-Driven**: Kafka for asynchronous communication reduces coupling and improves throughput

### Performance Targets

| Metric | Target |
|--------|--------|
| API Response Time | < 200ms (p95) |
| Search Query Response | < 100ms |
| Video Call Setup | < 3 seconds |
| Concurrent Users | 10,000+ per service |
| Database Queries | < 50ms (p95) |

---

## ❓ FAQ

### General Questions

**Q: What is the tech stack?**
A: Java 21 + Spring Boot 3.x (WebFlux) for backend, Next.js + React for frontend, React Native for mobile. Uses PostgreSQL, MongoDB, Redis, Kafka, and Elasticsearch.

**Q: Why reactive programming?**
A: Spring WebFlux with reactive programming provides better resource utilization, handles high concurrency with fewer threads, and scales better than traditional blocking I/O.

**Q: Can I run individual services?**
A: Yes! Each service can run independently. Just ensure the required infrastructure (PostgreSQL, Redis, etc.) is available.

**Q: How do I add a new microservice?**
A: See `ARCHITECTURE_INSTRUCTIONS.md` for detailed guidelines on creating new services following the project patterns.

### Development Questions

**Q: Services won't start - port already in use?**
A: Check which process is using the port and stop it:
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID [PID] /F

# Alternatively, change the port in docker-compose.yaml
```

**Q: Database connection errors?**
A: Ensure infrastructure services are running:
```bash
docker-compose up -d postgres mongodb redis
docker-compose ps  # Check status
```

**Q: How do I reset all data?**
A: Remove Docker volumes and restart:
```bash
docker-compose down -v
docker-compose up -d
```

**Q: Maven build fails?**
A: Clean and rebuild:
```bash
cd backend
./mvnw clean install -U -DskipTests
```

### Production Questions

**Q: Is this production-ready?**
A: Yes! Includes monitoring, logging, security, health checks, and Kubernetes deployment configs. See `docs/production-checklist.md`.

**Q: How to deploy to Kubernetes?**
A: Use the manifests in `k8s/` directory. See the Deployment section above.

**Q: What about CI/CD?**
A: GitHub Actions workflows are in `.github/workflows/` for automated build, test, and deployment.

**Q: How to scale services?**
A: In Kubernetes, adjust replicas:
```bash
kubectl scale deployment user-service --replicas=3
```

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Pull Request Guidelines

- Follow the code style guidelines
- Include tests for new features
- Update documentation as needed
- Ensure all tests pass
- Get at least one code review approval

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📞 Contact & Support

- **GitHub Repository**: [kally123/doctorApp](https://github.com/kally123/doctorApp)
- **Issues & Bug Reports**: [GitHub Issues](https://github.com/kally123/doctorApp/issues)
- **Discussions**: [GitHub Discussions](https://github.com/kally123/doctorApp/discussions)
- **Project Documentation**: See `docs/` directory and phase documents in `phases/`

### Getting Help

1. Check the [FAQ](#faq) section above
2. Review the [Troubleshooting](#troubleshooting) section
3. Search existing [GitHub Issues](https://github.com/kally123/doctorApp/issues)
4. Open a new issue with detailed information about your problem

---

## 📖 Quick Reference

### Important Files

| File | Purpose |
|------|---------|
| `README.md` | This file - project overview |
| `PROJECT_PLAN.md` | Detailed project planning & roadmap |
| `ARCHITECTURE_INSTRUCTIONS.md` | Architecture guidelines & best practices |
| `docker-compose.yaml` | Local development environment setup |
| `backend/pom.xml` | Maven parent POM for all backend services |
| `PHASE*_IMPLEMENTATION_STATUS.md` | Implementation status for each phase (1-6) |
| `docs/deployment/DEPLOYMENT_GUIDE.md` | **🚀 Complete deployment guide** |
| `docs/deployment/deployment-free-cloud.md` | Detailed free cloud deployment options |
| `docs/deployment/deployment-railway-render.md` | Quick deploy to Railway/Render/Fly.io |
| `docs/deployment/frontend-deployment.md` | Frontend deployment to Vercel/Netlify |
| `docs/deployment/docker-optimization.md` | Docker image optimization guide |
| `docs/deployment/QUICK_DEPLOY.md` | Quick reference deployment card |
| `docs/production-checklist.md` | Production readiness checklist |

### Quick Commands

```bash
# Start everything
docker-compose up -d

# Stop everything
docker-compose down

# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f user-service

# Rebuild a service
docker-compose up -d --build user-service

# Check service health
curl http://localhost:8080/actuator/health

# Access PostgreSQL database
docker exec -it healthcare-postgres psql -U postgres

# Access MongoDB
docker exec -it healthcare-mongodb mongosh -u admin -p mongo_password

# Access Redis
docker exec -it healthcare-redis redis-cli -a redis_password

# Clean everything (including data volumes)
docker-compose down -v

# Restart all services
docker-compose restart
```

### Service Architecture Pattern

Each microservice follows this consistent structure:
```
service-name/
├── src/main/java/com/healthcare/[service]/
│   ├── config/          # Spring configuration classes
│   ├── controller/      # REST API controllers
│   ├── service/         # Business logic layer
│   ├── repository/      # Data access layer (R2DBC/Reactive)
│   ├── model/           # Domain entities
│   ├── dto/             # Data transfer objects
│   ├── mapper/          # DTO ↔ Entity mappers
│   ├── exception/       # Custom exceptions & handlers
│   └── event/           # Kafka event producers/consumers
├── src/main/resources/
│   ├── application.yaml # Service configuration
│   └── db/migration/    # Flyway migrations (PostgreSQL services)
├── src/test/           # Unit & integration tests
├── Dockerfile          # Docker image definition
└── pom.xml            # Maven dependencies & build
```

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html) - Reactive programming
- [Next.js](https://nextjs.org/) - React framework for patient webapp
- [Vite](https://vitejs.dev/) - Frontend tooling for doctor dashboard
- [Tailwind CSS](https://tailwindcss.com/) - Utility-first CSS framework
- [Shadcn/ui](https://ui.shadcn.com/) - Beautifully designed components
- [React Native](https://reactnative.dev/) - Mobile app development
- [Twilio](https://www.twilio.com/) - Video consultation SDK
- [Razorpay](https://razorpay.com/) - Payment gateway integration
- [Elasticsearch](https://www.elastic.co/) - Search and analytics
- [Apache Kafka](https://kafka.apache.org/) - Event streaming platform

---

<p align="center">
  Made with ❤️ by the Healthcare Platform Team<br/>
  <b>⭐ Star this repository if you find it helpful!</b>
</p>

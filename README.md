# 🏥 Healthcare Platform (DocApp)

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21+-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-61DAFB)](https://react.dev/)

A comprehensive healthcare platform similar to Practo, enabling patients to discover doctors, book appointments, conduct video consultations, manage health records, order medicines, and book lab tests.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Development](#development)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)
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
| MongoDB | 6+ | Document storage (EHR, content) |
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
├── phases/                           # Implementation phase documents
│   ├── phase-1-foundation.md         # Weeks 1-6: Setup & core services
│   ├── phase-2-appointment-booking.md# Weeks 7-10: Doctor search & booking
│   ├── phase-3-teleconsultation.md   # Weeks 11-14: Video consultation
│   ├── phase-4-clinical-features.md  # Weeks 15-18: Prescriptions & EHR
│   ├── phase-5-commerce.md           # Weeks 19-22: Orders & lab tests
│   └── phase-6-enhancement-launch.md # Weeks 23-26: Mobile, scale & launch
├── services/                         # Microservices (to be created)
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
├── shared/                           # Shared libraries
│   ├── common-dto/
│   ├── common-utils/
│   └── event-contracts/
├── gateway/                          # API Gateway
├── frontend/                         # Frontend applications
│   ├── patient-web/                  # Next.js patient app
│   ├── doctor-dashboard/             # React doctor dashboard
│   └── admin-portal/                 # React admin portal
├── mobile/                           # Mobile applications
│   ├── patient-app/
│   └── doctor-app/
├── infrastructure/                   # IaC & deployment configs
│   ├── kubernetes/
│   ├── terraform/
│   └── docker/
└── docs/                             # Additional documentation
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+** - [Download](https://adoptium.net/)
- **Node.js 18+** - [Download](https://nodejs.org/)
- **Docker & Docker Compose** - [Download](https://docker.com/)
- **Kubernetes** (for production) - [Minikube](https://minikube.sigs.k8s.io/) for local
- **Git** - [Download](https://git-scm.com/)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/kally123/doctorApp.git
   cd doctorApp
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose -f infrastructure/docker/docker-compose.dev.yml up -d
   ```
   This starts PostgreSQL, MongoDB, Redis, Kafka, and Elasticsearch.

3. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Start backend services**
   ```bash
   # Using Gradle
   ./gradlew bootRun
   
   # Or start individual services
   cd services/user-service
   ./gradlew bootRun
   ```

5. **Start frontend applications**
   ```bash
   # Patient Web App
   cd frontend/patient-web
   npm install
   npm run dev
   
   # Doctor Dashboard
   cd frontend/doctor-dashboard
   npm install
   npm run dev
   ```

6. **Access the applications**
   - Patient Web: http://localhost:3000
   - Doctor Dashboard: http://localhost:3001
   - API Gateway: http://localhost:8080
   - API Docs: http://localhost:8080/swagger-ui.html

---

## 💻 Development

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
./gradlew test

# Frontend tests
npm test

# Integration tests
./gradlew integrationTest

# E2E tests
npm run test:e2e
```

### Building for Production

```bash
# Build all services
./gradlew build

# Build Docker images
./gradlew bootBuildImage

# Build frontend
cd frontend/patient-web
npm run build
```

---

## 📚 API Documentation

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
# Deploy to staging
kubectl apply -f infrastructure/kubernetes/staging/

# Deploy to production
kubectl apply -f infrastructure/kubernetes/production/
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

- **Documentation**: [docs.healthapp.com](https://docs.healthapp.com)
- **Issues**: [GitHub Issues](https://github.com/kally123/doctorApp/issues)
- **Email**: support@healthapp.com

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [Next.js](https://nextjs.org/) - React framework
- [Tailwind CSS](https://tailwindcss.com/) - Styling
- [Twilio](https://www.twilio.com/) - Video SDK
- [Shadcn/ui](https://ui.shadcn.com/) - UI components

---

<p align="center">
  Made with ❤️ by the Healthcare Platform Team
</p>

# Healthcare Platform - Detailed Project Plan & Task Breakdown

## Project Overview

**Project Name**: Healthcare Platform (Practo-like)  
**Duration**: 26 Weeks  
**Team Structure**: See [Team Roles](#team-roles)  
**Start Date**: January 30, 2026  
**Target Launch Date**: July 30, 2026

---

## Current Status

| Phase | Status | Details |
|-------|--------|---------|
| **Phase 1: Foundation** | ✅ **CODE COMPLETE** | All services implemented, pending build verification |
| Phase 2: Appointment Booking | ⬜ Not Started | - |
| Phase 3: Teleconsultation | ⬜ Not Started | - |
| Phase 4: Clinical Features | ⬜ Not Started | - |
| Phase 5: Commerce | ⬜ Not Started | - |
| Phase 6: Launch | ⬜ Not Started | - |

See [PHASE1_IMPLEMENTATION_STATUS.md](PHASE1_IMPLEMENTATION_STATUS.md) for detailed implementation status.

---

## Team Roles

| Role | Count | Responsibilities |
|------|-------|------------------|
| **Tech Lead / Architect** | 1 | Architecture decisions, code reviews, technical guidance |
| **Backend Engineers** | 4-5 | Microservices development, APIs, integrations |
| **Frontend Engineers** | 3-4 | Web apps (Patient, Doctor, Admin) |
| **Mobile Engineers** | 2 | React Native / Flutter apps |
| **DevOps Engineer** | 1-2 | Infrastructure, CI/CD, monitoring |
| **QA Engineers** | 2 | Testing, automation, quality assurance |
| **UI/UX Designer** | 1 | Design systems, user flows, prototypes |
| **Product Manager** | 1 | Requirements, prioritization, stakeholder management |

---

## Phase 1: Foundation (Weeks 1-6)

### Week 1-2: Project Setup & Infrastructure

#### DevOps Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| D1.1 | Set up Git repositories (monorepo or multi-repo structure) | DevOps | 4 | - | P0 |
| D1.2 | Configure GitHub Actions / GitLab CI pipeline templates | DevOps | 8 | D1.1 | P0 |
| D1.3 | Set up development Kubernetes cluster (EKS/AKS/GKE) | DevOps | 16 | - | P0 |
| D1.4 | Configure container registry (ECR/ACR/GCR) | DevOps | 4 | D1.3 | P0 |
| D1.5 | Set up HashiCorp Vault for secrets management | DevOps | 8 | D1.3 | P0 |
| D1.6 | Deploy PostgreSQL (RDS/CloudSQL) for dev environment | DevOps | 4 | D1.3 | P0 |
| D1.7 | Deploy MongoDB Atlas or self-hosted cluster | DevOps | 4 | D1.3 | P0 |
| D1.8 | Deploy Redis cluster (ElastiCache/Memorystore) | DevOps | 4 | D1.3 | P0 |
| D1.9 | Set up Kafka cluster (MSK/Confluent) | DevOps | 8 | D1.3 | P0 |
| D1.10 | Configure Elasticsearch cluster | DevOps | 8 | D1.3 | P1 |
| D1.11 | Set up monitoring stack (Prometheus + Grafana) | DevOps | 8 | D1.3 | P1 |
| D1.12 | Set up logging stack (ELK / Loki) | DevOps | 8 | D1.3 | P1 |
| D1.13 | Configure distributed tracing (Jaeger/Zipkin) | DevOps | 4 | D1.3 | P2 |

#### Backend - Project Bootstrap Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B1.1 | Create Spring Boot parent POM / Gradle build template | Backend Lead | 8 | D1.1 | P0 |
| B1.2 | Set up shared libraries module (common DTOs, utils, events) | Backend Lead | 16 | B1.1 | P0 |
| B1.3 | Create service project template with WebFlux | Backend Lead | 8 | B1.1 | P0 |
| B1.4 | Configure R2DBC connection pooling template | Backend Lead | 4 | B1.3 | P0 |
| B1.5 | Set up Kafka producer/consumer template | Backend Lead | 8 | B1.3, D1.9 | P0 |
| B1.6 | Create Docker multi-stage build template | Backend Lead | 4 | B1.3 | P0 |
| B1.7 | Set up API documentation (OpenAPI/Springdoc) | Backend Lead | 4 | B1.3 | P1 |
| B1.8 | Configure code quality tools (Checkstyle, SpotBugs) | Backend Lead | 4 | B1.1 | P1 |

#### Frontend - Project Bootstrap Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F1.1 | Set up Patient Web App (Next.js 14+) | Frontend Lead | 8 | D1.1 | P0 |
| F1.2 | Set up Doctor Dashboard (React + Vite + TypeScript) | Frontend Lead | 8 | D1.1 | P0 |
| F1.3 | Set up Admin Portal (React + Vite + TypeScript) | Frontend Lead | 4 | D1.1 | P1 |
| F1.4 | Create shared UI component library (Shadcn/Radix) | Frontend Lead | 16 | F1.1 | P0 |
| F1.5 | Set up API client with React Query | Frontend Lead | 8 | F1.1, F1.2 | P0 |
| F1.6 | Configure ESLint, Prettier, Husky | Frontend | 4 | F1.1 | P1 |
| F1.7 | Set up Storybook for component documentation | Frontend | 8 | F1.4 | P2 |

#### Design Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| U1.1 | Create design system (colors, typography, spacing) | Designer | 16 | - | P0 |
| U1.2 | Design login/registration flows (Patient & Doctor) | Designer | 16 | U1.1 | P0 |
| U1.3 | Design doctor search and listing pages | Designer | 16 | U1.1 | P0 |
| U1.4 | Design doctor profile page | Designer | 8 | U1.1 | P0 |
| U1.5 | Create icon set and illustration library | Designer | 8 | U1.1 | P1 |

---

### Week 3-4: User Service & Authentication

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B2.1 | Create User Service project structure | Backend 1 | 4 | B1.3 | P0 |
| B2.2 | Design and create users database schema (Flyway) | Backend 1 | 4 | B2.1, D1.6 | P0 |
| B2.3 | Implement User entity and R2DBC repository | Backend 1 | 8 | B2.2 | P0 |
| B2.4 | Implement user registration endpoint | Backend 1 | 8 | B2.3 | P0 |
| B2.5 | Implement OTP generation and verification | Backend 1 | 8 | B2.4 | P0 |
| B2.6 | Implement JWT token generation and validation | Backend 1 | 8 | B2.4 | P0 |
| B2.7 | Implement login endpoint (email/phone + password) | Backend 1 | 8 | B2.6 | P0 |
| B2.8 | Implement social login (Google, Apple) | Backend 1 | 12 | B2.6 | P1 |
| B2.9 | Implement password reset flow | Backend 1 | 8 | B2.5 | P0 |
| B2.10 | Implement user profile CRUD operations | Backend 1 | 8 | B2.3 | P0 |
| B2.11 | Publish user.registered event to Kafka | Backend 1 | 4 | B2.4, B1.5 | P0 |
| B2.12 | Write unit tests for User Service | Backend 1 | 12 | B2.10 | P0 |
| B2.13 | Write integration tests for User Service | Backend 1 | 8 | B2.10 | P1 |

#### API Gateway Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B3.1 | Create API Gateway project (Spring Cloud Gateway) | Backend 2 | 8 | B1.3 | P0 |
| B3.2 | Configure route definitions for services | Backend 2 | 4 | B3.1 | P0 |
| B3.3 | Implement JWT validation filter | Backend 2 | 8 | B3.1, B2.6 | P0 |
| B3.4 | Configure rate limiting with Redis | Backend 2 | 8 | B3.1, D1.8 | P0 |
| B3.5 | Implement request logging and correlation IDs | Backend 2 | 4 | B3.1 | P0 |
| B3.6 | Configure CORS for frontend origins | Backend 2 | 2 | B3.1 | P0 |
| B3.7 | Set up health checks and actuator endpoints | Backend 2 | 4 | B3.1 | P0 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F2.1 | Implement Patient registration page | Frontend 1 | 12 | F1.1, U1.2 | P0 |
| F2.2 | Implement OTP verification flow | Frontend 1 | 8 | F2.1 | P0 |
| F2.3 | Implement Patient login page | Frontend 1 | 8 | F1.1, U1.2 | P0 |
| F2.4 | Implement Doctor registration page | Frontend 2 | 12 | F1.2, U1.2 | P0 |
| F2.5 | Implement Doctor login page | Frontend 2 | 8 | F1.2, U1.2 | P0 |
| F2.6 | Create auth context and token management | Frontend 1 | 8 | F2.3 | P0 |
| F2.7 | Implement protected route wrapper | Frontend 1 | 4 | F2.6 | P0 |
| F2.8 | Implement forgot password flow | Frontend 1 | 8 | F2.3 | P1 |
| F2.9 | Create user profile page (Patient) | Frontend 1 | 8 | F2.6 | P1 |
| F2.10 | Create user profile page (Doctor) | Frontend 2 | 8 | F2.6 | P1 |

---

### Week 5-6: Doctor Service & Search

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B4.1 | Create Doctor Service project structure | Backend 2 | 4 | B1.3 | P0 |
| B4.2 | Design doctors database schema (Flyway) | Backend 2 | 8 | B4.1 | P0 |
| B4.3 | Create specializations reference data | Backend 2 | 4 | B4.2 | P0 |
| B4.4 | Implement Doctor entity and R2DBC repository | Backend 2 | 8 | B4.2 | P0 |
| B4.5 | Implement doctor registration endpoint | Backend 2 | 8 | B4.4 | P0 |
| B4.6 | Implement doctor profile management | Backend 2 | 12 | B4.4 | P0 |
| B4.7 | Implement qualification management | Backend 2 | 8 | B4.4 | P0 |
| B4.8 | Implement clinic association management | Backend 2 | 8 | B4.4 | P1 |
| B4.9 | Publish doctor events to Kafka | Backend 2 | 4 | B4.5, B1.5 | P0 |
| B4.10 | Write tests for Doctor Service | Backend 2 | 12 | B4.8 | P0 |

#### Search Service Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B5.1 | Create Search Service project structure | Backend 3 | 4 | B1.3 | P0 |
| B5.2 | Design Elasticsearch doctor index mapping | Backend 3 | 8 | D1.10 | P0 |
| B5.3 | Implement doctor indexing from Kafka events | Backend 3 | 12 | B5.2, B4.9 | P0 |
| B5.4 | Implement basic doctor search endpoint | Backend 3 | 12 | B5.3 | P0 |
| B5.5 | Implement specialty filter | Backend 3 | 4 | B5.4 | P0 |
| B5.6 | Implement location/geo-distance filter | Backend 3 | 8 | B5.4 | P0 |
| B5.7 | Implement price range filter | Backend 3 | 4 | B5.4 | P0 |
| B5.8 | Implement availability filter | Backend 3 | 8 | B5.4 | P1 |
| B5.9 | Implement search autocomplete | Backend 3 | 8 | B5.4 | P1 |
| B5.10 | Implement search result ranking/scoring | Backend 3 | 8 | B5.4 | P1 |
| B5.11 | Write tests for Search Service | Backend 3 | 8 | B5.10 | P0 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F3.1 | Implement landing page with search bar | Frontend 1 | 12 | F1.1 | P0 |
| F3.2 | Implement doctor search results page | Frontend 1 | 16 | F3.1, U1.3 | P0 |
| F3.3 | Implement filter sidebar component | Frontend 1 | 12 | F3.2 | P0 |
| F3.4 | Implement doctor card component | Frontend 1 | 8 | F3.2 | P0 |
| F3.5 | Implement doctor profile page | Frontend 1 | 16 | F3.4, U1.4 | P0 |
| F3.6 | Implement specializations selector | Frontend 1 | 4 | F3.1 | P0 |
| F3.7 | Doctor profile editor (Doctor Dashboard) | Frontend 2 | 16 | F1.2 | P0 |
| F3.8 | Qualification editor (Doctor Dashboard) | Frontend 2 | 8 | F3.7 | P0 |
| F3.9 | Profile photo upload component | Frontend 2 | 8 | F3.7 | P0 |

---

## Phase 1 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Infrastructure ready (K8s, databases, Kafka) | End of Week 2 | ⬜ |
| User Service deployed to dev | End of Week 4 | ⬜ |
| API Gateway deployed to dev | End of Week 4 | ⬜ |
| Patient can register and login | End of Week 4 | ⬜ |
| Doctor can register and login | End of Week 4 | ⬜ |
| Doctor Service deployed to dev | End of Week 6 | ⬜ |
| Doctor search working | End of Week 6 | ⬜ |
| Patient can view doctor profiles | End of Week 6 | ⬜ |

---

## Phase 2: Appointment Booking (Weeks 7-10)

### Week 7-8: Appointment Service Core

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B6.1 | Create Appointment Service project structure | Backend 3 | 4 | B1.3 | P0 |
| B6.2 | Design appointments database schema | Backend 3 | 8 | B6.1 | P0 |
| B6.3 | Design availability slots schema | Backend 3 | 4 | B6.2 | P0 |
| B6.4 | Implement Appointment entity and repository | Backend 3 | 8 | B6.2 | P0 |
| B6.5 | Implement AvailabilitySlot entity and repository | Backend 3 | 8 | B6.3 | P0 |
| B6.6 | Implement availability management endpoints | Backend 3 | 12 | B6.5 | P0 |
| B6.7 | Implement get available slots endpoint | Backend 3 | 8 | B6.6 | P0 |
| B6.8 | Implement slot booking/reservation | Backend 3 | 12 | B6.7 | P0 |
| B6.9 | Implement booking confirmation flow | Backend 3 | 8 | B6.8 | P0 |
| B6.10 | Implement appointment cancellation | Backend 3 | 8 | B6.9 | P0 |
| B6.11 | Implement appointment rescheduling | Backend 3 | 8 | B6.10 | P1 |
| B6.12 | Publish appointment events to Kafka | Backend 3 | 4 | B6.9 | P0 |
| B6.13 | Implement patient appointments list | Backend 3 | 4 | B6.4 | P0 |
| B6.14 | Implement doctor appointments list | Backend 3 | 4 | B6.4 | P0 |
| B6.15 | Write tests for Appointment Service | Backend 3 | 12 | B6.14 | P0 |

#### Doctor Availability (Doctor Service Enhancement)

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B7.1 | Add availability management to Doctor Service | Backend 2 | 8 | B4.6 | P0 |
| B7.2 | Implement weekly schedule configuration | Backend 2 | 8 | B7.1 | P0 |
| B7.3 | Implement holiday/leave management | Backend 2 | 8 | B7.1 | P1 |
| B7.4 | Sync availability to Search Service | Backend 2 | 4 | B7.1, B5.3 | P1 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F4.1 | Design and implement availability calendar | Designer + Frontend 2 | 24 | F3.7 | P0 |
| F4.2 | Implement slot configuration UI | Frontend 2 | 12 | F4.1 | P0 |
| F4.3 | Implement booking calendar on doctor profile | Frontend 1 | 16 | F3.5 | P0 |
| F4.4 | Implement time slot picker component | Frontend 1 | 8 | F4.3 | P0 |
| F4.5 | Implement booking confirmation modal | Frontend 1 | 8 | F4.4 | P0 |
| F4.6 | Implement my appointments page (Patient) | Frontend 1 | 12 | F2.6 | P0 |
| F4.7 | Implement appointment card component | Frontend 1 | 6 | F4.6 | P0 |
| F4.8 | Implement appointments dashboard (Doctor) | Frontend 2 | 16 | F1.2 | P0 |
| F4.9 | Implement appointment details modal | Frontend | 8 | F4.6, F4.8 | P0 |

---

### Week 9-10: Payment Integration & Notifications

#### Payment Service Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B8.1 | Create Payment Service project structure | Backend 4 | 4 | B1.3 | P0 |
| B8.2 | Design payments database schema | Backend 4 | 8 | B8.1 | P0 |
| B8.3 | Implement payment transaction entity | Backend 4 | 8 | B8.2 | P0 |
| B8.4 | Integrate Razorpay/Stripe payment gateway | Backend 4 | 16 | B8.3 | P0 |
| B8.5 | Implement payment initiation endpoint | Backend 4 | 8 | B8.4 | P0 |
| B8.6 | Implement payment webhook handler | Backend 4 | 12 | B8.4 | P0 |
| B8.7 | Publish payment events to Kafka | Backend 4 | 4 | B8.6 | P0 |
| B8.8 | Implement refund processing | Backend 4 | 12 | B8.6 | P0 |
| B8.9 | Implement payment history endpoint | Backend 4 | 4 | B8.3 | P0 |
| B8.10 | Write tests for Payment Service | Backend 4 | 12 | B8.9 | P0 |

#### Booking Saga Implementation

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B9.1 | Implement Saga orchestrator library | Backend Lead | 16 | B1.5 | P0 |
| B9.2 | Implement booking saga (reserve → pay → confirm) | Backend 3 | 16 | B9.1, B6.8, B8.5 | P0 |
| B9.3 | Implement compensation handlers | Backend 3 | 12 | B9.2 | P0 |
| B9.4 | Implement saga state persistence | Backend 3 | 8 | B9.2 | P0 |
| B9.5 | Add idempotency to saga steps | Backend 3 | 8 | B9.2 | P0 |

#### Notification Service Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B10.1 | Create Notification Service project structure | Backend 4 | 4 | B1.3 | P0 |
| B10.2 | Design notifications database schema | Backend 4 | 4 | B10.1 | P0 |
| B10.3 | Implement email sending (SendGrid/SES) | Backend 4 | 8 | B10.1 | P0 |
| B10.4 | Implement SMS sending (Twilio/MSG91) | Backend 4 | 8 | B10.1 | P0 |
| B10.5 | Implement push notifications (FCM) | Backend 4 | 8 | B10.1 | P1 |
| B10.6 | Implement notification templates | Backend 4 | 8 | B10.3, B10.4 | P0 |
| B10.7 | Handle appointment.booked event | Backend 4 | 4 | B10.6, B6.12 | P0 |
| B10.8 | Handle appointment.cancelled event | Backend 4 | 4 | B10.6 | P0 |
| B10.9 | Implement appointment reminders scheduler | Backend 4 | 12 | B10.6 | P0 |
| B10.10 | Write tests for Notification Service | Backend 4 | 8 | B10.9 | P0 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F5.1 | Integrate payment gateway SDK | Frontend 1 | 12 | B8.5 | P0 |
| F5.2 | Implement payment flow UI | Frontend 1 | 16 | F5.1 | P0 |
| F5.3 | Implement payment success/failure pages | Frontend 1 | 8 | F5.2 | P0 |
| F5.4 | Implement appointment cancellation UI | Frontend 1 | 8 | F4.6 | P0 |
| F5.5 | Implement reschedule flow UI | Frontend 1 | 12 | F4.6 | P1 |
| F5.6 | Add real-time notification badge | Frontend 1 | 8 | F2.6 | P1 |
| F5.7 | Doctor's today's appointments view | Frontend 2 | 12 | F4.8 | P0 |

---

## Phase 2 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Appointment Service deployed | End of Week 8 | ⬜ |
| Doctor can set availability | End of Week 8 | ⬜ |
| Patient can see available slots | End of Week 8 | ⬜ |
| Payment Service deployed | End of Week 10 | ⬜ |
| Complete booking flow working | End of Week 10 | ⬜ |
| Booking confirmation emails sent | End of Week 10 | ⬜ |
| Appointment reminders scheduled | End of Week 10 | ⬜ |

---

## Phase 3: Teleconsultation (Weeks 11-14)

### Week 11-12: Consultation Service Core

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B11.1 | Create Consultation Service project structure | Backend 1 | 4 | B1.3 | P0 |
| B11.2 | Design consultation sessions schema | Backend 1 | 8 | B11.1 | P0 |
| B11.3 | Design chat messages schema (MongoDB) | Backend 1 | 4 | B11.1 | P0 |
| B11.4 | Implement session entity and repository | Backend 1 | 8 | B11.2 | P0 |
| B11.5 | Integrate Twilio Video SDK | Backend 1 | 24 | B11.4 | P0 |
| B11.6 | Implement create video room endpoint | Backend 1 | 8 | B11.5 | P0 |
| B11.7 | Implement generate access token endpoint | Backend 1 | 8 | B11.5 | P0 |
| B11.8 | Implement session start/end tracking | Backend 1 | 8 | B11.4 | P0 |
| B11.9 | Implement WebSocket handler for chat | Backend 1 | 16 | B11.3 | P0 |
| B11.10 | Implement chat message persistence | Backend 1 | 8 | B11.9 | P0 |
| B11.11 | Implement chat history retrieval | Backend 1 | 4 | B11.10 | P0 |
| B11.12 | Publish consultation events to Kafka | Backend 1 | 4 | B11.8 | P0 |
| B11.13 | Write tests for Consultation Service | Backend 1 | 12 | B11.12 | P0 |

#### Alternative: Agora Integration

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B11.5a | Integrate Agora Video SDK (alternative to Twilio) | Backend 1 | 24 | B11.4 | P0 |
| B11.6a | Implement Agora token generation | Backend 1 | 8 | B11.5a | P0 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F6.1 | Integrate Twilio Video SDK (Patient App) | Frontend 1 | 16 | B11.6 | P0 |
| F6.2 | Implement video consultation room | Frontend 1 | 24 | F6.1 | P0 |
| F6.3 | Implement video controls (mute, camera, leave) | Frontend 1 | 12 | F6.2 | P0 |
| F6.4 | Implement waiting room UI | Frontend 1 | 8 | F6.2 | P0 |
| F6.5 | Implement in-call chat panel | Frontend 1 | 12 | F6.2, B11.9 | P0 |
| F6.6 | Integrate Twilio Video SDK (Doctor Dashboard) | Frontend 2 | 16 | B11.6 | P0 |
| F6.7 | Implement doctor's consultation room | Frontend 2 | 24 | F6.6 | P0 |
| F6.8 | Implement patient queue/waiting list | Frontend 2 | 12 | F6.7 | P0 |
| F6.9 | Implement call doctor button on appointment | Frontend 1 | 8 | F6.2 | P0 |

---

### Week 13-14: Audio Consultation & Session Management

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B12.1 | Implement audio-only consultation mode | Backend 1 | 8 | B11.5 | P0 |
| B12.2 | Implement consultation duration tracking | Backend 1 | 8 | B11.8 | P0 |
| B12.3 | Implement billing per consultation type | Backend 1 | 8 | B12.2 | P0 |
| B12.4 | Implement session recording (optional) | Backend 1 | 16 | B11.5 | P2 |
| B12.5 | Store recording in S3 with encryption | Backend 1 | 8 | B12.4 | P2 |
| B12.6 | Implement consultation summary endpoint | Backend 1 | 4 | B11.8 | P0 |
| B12.7 | Handle consultation.completed event | Backend 4 | 4 | B11.12 | P0 |
| B12.8 | Send post-consultation feedback request | Backend 4 | 4 | B12.7 | P1 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F7.1 | Implement audio-only consultation UI | Frontend 1, 2 | 12 | B12.1 | P0 |
| F7.2 | Implement call timer display | Frontend 1, 2 | 4 | F6.2 | P0 |
| F7.3 | Implement connection quality indicator | Frontend 1, 2 | 8 | F6.2 | P1 |
| F7.4 | Implement consultation summary view | Frontend 1 | 8 | B12.6 | P0 |
| F7.5 | Implement feedback/rating modal | Frontend 1 | 8 | F7.4 | P1 |
| F7.6 | Implement consultation history page | Frontend 1, 2 | 12 | B12.6 | P0 |

---

## Phase 3 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Consultation Service deployed | End of Week 12 | ⬜ |
| Video consultation working | End of Week 12 | ⬜ |
| In-call chat working | End of Week 12 | ⬜ |
| Audio consultation working | End of Week 14 | ⬜ |
| Session tracking working | End of Week 14 | ⬜ |
| Post-consultation feedback | End of Week 14 | ⬜ |

---

## Phase 4: Clinical Features (Weeks 15-18)

### Week 15-16: Prescription Service

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B13.1 | Create Prescription Service project structure | Backend 2 | 4 | B1.3 | P0 |
| B13.2 | Design prescriptions database schema | Backend 2 | 8 | B13.1 | P0 |
| B13.3 | Import medicine master database | Backend 2 | 12 | B13.2 | P0 |
| B13.4 | Implement medicine search endpoint | Backend 2 | 8 | B13.3 | P0 |
| B13.5 | Implement Prescription entity and repository | Backend 2 | 8 | B13.2 | P0 |
| B13.6 | Implement create prescription endpoint | Backend 2 | 12 | B13.5 | P0 |
| B13.7 | Implement prescription templates | Backend 2 | 8 | B13.5 | P1 |
| B13.8 | Implement digital signature for prescriptions | Backend 2 | 12 | B13.6 | P0 |
| B13.9 | Implement prescription PDF generation | Backend 2 | 12 | B13.6 | P0 |
| B13.10 | Implement prescription sharing endpoint | Backend 2 | 8 | B13.6 | P0 |
| B13.11 | Publish prescription events to Kafka | Backend 2 | 4 | B13.6 | P0 |
| B13.12 | Write tests for Prescription Service | Backend 2 | 12 | B13.11 | P0 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F8.1 | Implement prescription builder UI (Doctor) | Frontend 2 | 24 | B13.6 | P0 |
| F8.2 | Implement medicine search autocomplete | Frontend 2 | 12 | B13.4, F8.1 | P0 |
| F8.3 | Implement dosage/frequency selector | Frontend 2 | 8 | F8.1 | P0 |
| F8.4 | Implement prescription preview | Frontend 2 | 8 | F8.1 | P0 |
| F8.5 | Implement prescription templates selector | Frontend 2 | 8 | B13.7 | P1 |
| F8.6 | Implement view prescription (Patient) | Frontend 1 | 12 | B13.6 | P0 |
| F8.7 | Implement prescription download PDF | Frontend 1 | 8 | B13.9 | P0 |
| F8.8 | Implement prescriptions list (Patient) | Frontend 1 | 8 | F8.6 | P0 |

---

### Week 17-18: EHR Service (Health Records)

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B14.1 | Create EHR Service project structure | Backend 3 | 4 | B1.3 | P0 |
| B14.2 | Design health records schema (MongoDB) | Backend 3 | 8 | B14.1 | P0 |
| B14.3 | Implement HealthRecord entity and repository | Backend 3 | 8 | B14.2 | P0 |
| B14.4 | Implement create health record endpoint | Backend 3 | 8 | B14.3 | P0 |
| B14.5 | Implement document upload to S3 | Backend 3 | 12 | B14.4 | P0 |
| B14.6 | Implement get patient records endpoint | Backend 3 | 8 | B14.3 | P0 |
| B14.7 | Implement record sharing with doctors | Backend 3 | 8 | B14.6 | P0 |
| B14.8 | Implement vitals tracking | Backend 3 | 8 | B14.3 | P1 |
| B14.9 | Handle prescription.created event (auto-add to EHR) | Backend 3 | 8 | B13.11 | P0 |
| B14.10 | Implement medical history timeline | Backend 3 | 12 | B14.6 | P1 |
| B14.11 | Write tests for EHR Service | Backend 3 | 12 | B14.10 | P0 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F9.1 | Implement health records page (Patient) | Frontend 1 | 16 | B14.6 | P0 |
| F9.2 | Implement document upload component | Frontend 1 | 12 | B14.5 | P0 |
| F9.3 | Implement record viewer (PDF, images) | Frontend 1 | 8 | F9.1 | P0 |
| F9.4 | Implement medical timeline view | Frontend 1 | 16 | B14.10 | P1 |
| F9.5 | Implement vitals entry form | Frontend 1 | 8 | B14.8 | P1 |
| F9.6 | Implement vitals charts/graphs | Frontend 1 | 12 | F9.5 | P1 |
| F9.7 | Implement patient records view (Doctor) | Frontend 2 | 12 | B14.6 | P0 |
| F9.8 | Implement record sharing controls | Frontend 1 | 8 | B14.7 | P0 |

---

## Phase 4 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Prescription Service deployed | End of Week 16 | ⬜ |
| Doctor can write prescriptions | End of Week 16 | ⬜ |
| Patient can view/download prescriptions | End of Week 16 | ⬜ |
| EHR Service deployed | End of Week 18 | ⬜ |
| Patient can upload health documents | End of Week 18 | ⬜ |
| Doctor can view patient history | End of Week 18 | ⬜ |

---

## Phase 5: Commerce (Weeks 19-22)

### Week 19-20: Order Service (Medicine Orders)

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B15.1 | Create Order Service project structure | Backend 4 | 4 | B1.3 | P0 |
| B15.2 | Design orders database schema | Backend 4 | 8 | B15.1 | P0 |
| B15.3 | Implement Order entity and repository | Backend 4 | 8 | B15.2 | P0 |
| B15.4 | Implement cart management endpoints | Backend 4 | 12 | B15.3 | P0 |
| B15.5 | Implement add items from prescription | Backend 4 | 8 | B15.4, B13.11 | P0 |
| B15.6 | Implement address management | Backend 4 | 8 | B15.4 | P0 |
| B15.7 | Implement order placement endpoint | Backend 4 | 12 | B15.4 | P0 |
| B15.8 | Integrate with Payment Service | Backend 4 | 8 | B15.7, B8.5 | P0 |
| B15.9 | Implement pharmacy partner assignment logic | Backend 4 | 12 | B15.7 | P0 |
| B15.10 | Implement order status updates | Backend 4 | 8 | B15.7 | P0 |
| B15.11 | Implement order tracking endpoint | Backend 4 | 8 | B15.10 | P0 |
| B15.12 | Publish order events to Kafka | Backend 4 | 4 | B15.7 | P0 |
| B15.13 | Write tests for Order Service | Backend 4 | 12 | B15.12 | P0 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F10.1 | Implement pharmacy/medicine store page | Frontend 3 | 16 | B15.4 | P0 |
| F10.2 | Implement medicine search and listing | Frontend 3 | 12 | B13.4 | P0 |
| F10.3 | Implement shopping cart | Frontend 3 | 12 | B15.4 | P0 |
| F10.4 | Implement "order from prescription" flow | Frontend 3 | 12 | B15.5 | P0 |
| F10.5 | Implement address management | Frontend 3 | 8 | B15.6 | P0 |
| F10.6 | Implement checkout flow | Frontend 3 | 16 | B15.7 | P0 |
| F10.7 | Implement order confirmation page | Frontend 3 | 8 | F10.6 | P0 |
| F10.8 | Implement order tracking page | Frontend 3 | 12 | B15.11 | P0 |
| F10.9 | Implement order history page | Frontend 3 | 8 | B15.3 | P0 |

---

### Week 21-22: Lab Test Booking

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B16.1 | Create lab tests catalog schema | Backend 5 | 8 | B15.2 | P0 |
| B16.2 | Import lab tests master data | Backend 5 | 8 | B16.1 | P0 |
| B16.3 | Implement lab test search endpoint | Backend 5 | 8 | B16.2 | P0 |
| B16.4 | Implement lab partner management | Backend 5 | 12 | B16.1 | P0 |
| B16.5 | Implement lab test booking endpoint | Backend 5 | 12 | B16.4 | P0 |
| B16.6 | Implement home collection slot management | Backend 5 | 12 | B16.5 | P0 |
| B16.7 | Implement report upload by lab partner | Backend 5 | 8 | B16.5 | P0 |
| B16.8 | Integrate report with EHR Service | Backend 5 | 8 | B16.7, B14.4 | P0 |
| B16.9 | Implement lab test order tracking | Backend 5 | 8 | B16.5 | P0 |
| B16.10 | Write tests for Lab Test module | Backend 5 | 12 | B16.9 | P0 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F11.1 | Implement lab tests page | Frontend 3 | 12 | B16.3 | P0 |
| F11.2 | Implement test search and filters | Frontend 3 | 8 | B16.3 | P0 |
| F11.3 | Implement test details and packages | Frontend 3 | 8 | F11.1 | P0 |
| F11.4 | Implement home collection slot picker | Frontend 3 | 12 | B16.6 | P0 |
| F11.5 | Implement lab test booking flow | Frontend 3 | 12 | B16.5 | P0 |
| F11.6 | Implement lab test order tracking | Frontend 3 | 8 | B16.9 | P0 |
| F11.7 | Implement report download from EHR | Frontend 1 | 8 | B16.8 | P0 |

---

## Phase 5 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Order Service deployed | End of Week 20 | ⬜ |
| Patient can order medicines | End of Week 20 | ⬜ |
| Order tracking working | End of Week 20 | ⬜ |
| Lab test booking working | End of Week 22 | ⬜ |
| Lab reports integrated with EHR | End of Week 22 | ⬜ |

---

## Phase 6: Enhancement & Scale (Weeks 23-26)

### Week 23-24: Reviews, Ratings & Content

#### Backend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| B17.1 | Implement doctor reviews schema | Backend 5 | 4 | B4.2 | P0 |
| B17.2 | Implement submit review endpoint | Backend 5 | 8 | B17.1 | P0 |
| B17.3 | Implement rating aggregation | Backend 5 | 8 | B17.2 | P0 |
| B17.4 | Sync ratings to Search Service | Backend 5 | 4 | B17.3 | P0 |
| B17.5 | Create Content Service project | Backend 5 | 4 | B1.3 | P1 |
| B17.6 | Implement health articles CRUD | Backend 5 | 12 | B17.5 | P1 |
| B17.7 | Implement article search with Elasticsearch | Backend 5 | 8 | B17.6 | P1 |
| B17.8 | Implement article recommendations | Backend 5 | 12 | B17.7 | P2 |

#### Frontend Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| F12.1 | Implement review submission flow | Frontend 1 | 12 | B17.2 | P0 |
| F12.2 | Implement reviews on doctor profile | Frontend 1 | 8 | B17.2 | P0 |
| F12.3 | Implement health articles listing | Frontend 1 | 12 | B17.6 | P1 |
| F12.4 | Implement article detail page | Frontend 1 | 8 | F12.3 | P1 |
| F12.5 | Implement doctor response to reviews | Frontend 2 | 8 | B17.2 | P1 |

---

### Week 25: Mobile App Development

#### Mobile Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| M1.1 | Set up React Native / Flutter project (Patient) | Mobile 1 | 8 | - | P0 |
| M1.2 | Implement authentication screens | Mobile 1 | 16 | M1.1, B2.6 | P0 |
| M1.3 | Implement doctor search | Mobile 1 | 16 | M1.2, B5.4 | P0 |
| M1.4 | Implement booking flow | Mobile 1 | 24 | M1.3, B6.8 | P0 |
| M1.5 | Implement video consultation | Mobile 1 | 32 | M1.4, B11.5 | P0 |
| M1.6 | Implement prescriptions view | Mobile 1 | 12 | M1.4, B13.6 | P0 |
| M1.7 | Implement push notifications | Mobile 1 | 12 | M1.4, B10.5 | P0 |
| M2.1 | Set up React Native / Flutter project (Doctor) | Mobile 2 | 8 | - | P0 |
| M2.2 | Implement doctor authentication | Mobile 2 | 12 | M2.1 | P0 |
| M2.3 | Implement appointments dashboard | Mobile 2 | 16 | M2.2 | P0 |
| M2.4 | Implement video consultation | Mobile 2 | 32 | M2.3 | P0 |
| M2.5 | Implement prescription writing | Mobile 2 | 24 | M2.4 | P0 |

---

### Week 26: Performance, Security & Launch Prep

#### DevOps Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| D2.1 | Set up production Kubernetes cluster | DevOps | 16 | - | P0 |
| D2.2 | Configure production databases (HA) | DevOps | 16 | D2.1 | P0 |
| D2.3 | Set up CDN for static assets | DevOps | 8 | D2.1 | P0 |
| D2.4 | Configure auto-scaling policies | DevOps | 8 | D2.1 | P0 |
| D2.5 | Set up disaster recovery | DevOps | 16 | D2.2 | P0 |
| D2.6 | Configure production monitoring alerts | DevOps | 8 | D1.11 | P0 |
| D2.7 | Perform load testing | DevOps | 16 | D2.1 | P0 |
| D2.8 | Document runbooks for operations | DevOps | 16 | D2.6 | P0 |

#### Security Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| S1.1 | Conduct security audit | Security/Backend Lead | 24 | All services | P0 |
| S1.2 | Fix identified vulnerabilities | Backend Team | 24 | S1.1 | P0 |
| S1.3 | Penetration testing | External/Security | 40 | S1.2 | P0 |
| S1.4 | HIPAA compliance review | Compliance | 16 | S1.3 | P0 |
| S1.5 | Data encryption audit | Security | 8 | S1.1 | P0 |

#### QA Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies | Priority |
|---------|------|----------|------------|--------------|----------|
| Q1.1 | End-to-end testing of all flows | QA Team | 40 | All features | P0 |
| Q1.2 | Mobile app testing (iOS + Android) | QA Team | 24 | M1.7, M2.5 | P0 |
| Q1.3 | Performance testing | QA Team | 16 | All services | P0 |
| Q1.4 | UAT with beta users | QA Team + PM | 24 | Q1.1 | P0 |
| Q1.5 | Bug fixes from UAT | All Teams | 40 | Q1.4 | P0 |

---

## Phase 6 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Doctor reviews working | End of Week 24 | ⬜ |
| Health articles published | End of Week 24 | ⬜ |
| Patient mobile app ready | End of Week 25 | ⬜ |
| Doctor mobile app ready | End of Week 25 | ⬜ |
| Production infrastructure ready | End of Week 26 | ⬜ |
| Security audit passed | End of Week 26 | ⬜ |
| UAT completed | End of Week 26 | ⬜ |
| **PLATFORM LAUNCH READY** | End of Week 26 | ⬜ |

---cd backend
mvn clean package -DskipTestscd backend
mvn clean package -DskipTests

## Sprint Planning Template

### Sprint Duration: 2 Weeks

```markdown
## Sprint [Number]: [Start Date] - [End Date]

### Sprint Goal
[Clear, measurable goal for the sprint]

### Team Capacity
| Team Member | Role | Capacity (hours) | Notes |
|-------------|------|------------------|-------|
| | | | |

### Sprint Backlog

| Task ID | Task | Assignee | Estimate | Status |
|---------|------|----------|----------|--------|
| | | | | Not Started |
| | | | | In Progress |
| | | | | In Review |
| | | | | Done |

### Definition of Done
- [ ] Code complete and committed
- [ ] Unit tests written (>80% coverage)
- [ ] Code review approved
- [ ] Integration tests passing
- [ ] Deployed to dev environment
- [ ] API documentation updated
- [ ] QA verified

### Risks & Blockers
| Risk/Blocker | Impact | Mitigation |
|--------------|--------|------------|
| | | |

### Sprint Review Notes
[To be filled at end of sprint]

### Sprint Retrospective
- What went well:
- What could improve:
- Action items:
```

---

## Task Board Columns

```
┌─────────────┬─────────────┬─────────────┬─────────────┬─────────────┐
│   BACKLOG   │   TO DO     │ IN PROGRESS │  IN REVIEW  │    DONE     │
│             │ (Sprint)    │             │             │             │
├─────────────┼─────────────┼─────────────┼─────────────┼─────────────┤
│ Prioritized │ Ready to    │ Currently   │ PR created, │ Merged &    │
│ but not in  │ pick up,    │ being       │ awaiting    │ deployed    │
│ current     │ assigned    │ worked on   │ code review │ to dev      │
│ sprint      │             │             │             │             │
└─────────────┴─────────────┴─────────────┴─────────────┴─────────────┘
```

---

## Daily Standup Template

```
Team: [Team Name]
Date: [Date]

Each member answers:
1. What did I complete yesterday?
2. What will I work on today?
3. Are there any blockers?

Blockers requiring escalation:
- 

Action items:
-
```

---

## Risk Register

| Risk ID | Description | Probability | Impact | Mitigation Strategy | Owner |
|---------|-------------|-------------|--------|---------------------|-------|
| R1 | Video SDK integration complexity | Medium | High | PoC in Week 1, alternate vendor backup | Backend Lead |
| R2 | Payment gateway delays | Low | High | Start integration early, mock services | Backend 4 |
| R3 | HIPAA/Healthcare compliance issues | Medium | High | Early legal/compliance review | PM |
| R4 | Performance issues at scale | Medium | Medium | Load testing in each phase | DevOps |
| R5 | Third-party API rate limits | Low | Medium | Implement caching, retry logic | Backend Team |
| R6 | Mobile app store rejection | Low | High | Follow guidelines, early submission | Mobile Lead |
| R7 | Team member unavailability | Medium | Medium | Cross-training, documentation | Tech Lead |

---

## Communication Plan

| Meeting | Frequency | Participants | Purpose |
|---------|-----------|--------------|---------|
| Daily Standup | Daily (15 min) | Dev Team | Sync, blockers |
| Sprint Planning | Bi-weekly (2 hrs) | Full Team | Plan sprint |
| Sprint Review | Bi-weekly (1 hr) | Full Team + Stakeholders | Demo |
| Sprint Retro | Bi-weekly (1 hr) | Dev Team | Improve process |
| Architecture Review | Weekly (1 hr) | Tech Lead + Seniors | Design decisions |
| Product Sync | Weekly (30 min) | PM + Tech Lead | Requirements |

---

## Documentation Requirements

| Document | Owner | When to Create | Location |
|----------|-------|----------------|----------|
| API Documentation | Backend Dev | With each endpoint | `/docs/api/` |
| Architecture Decision Records | Tech Lead | Major decisions | `/docs/adr/` |
| Runbooks | DevOps | Before production | `/docs/operations/` |
| User Guides | PM + QA | Before launch | `/docs/guides/` |
| Database Schema Docs | Backend Dev | With migrations | `/docs/database/` |
| Deployment Guide | DevOps | Before production | `/docs/deployment/` |

---

## Success Metrics

### Phase-wise KPIs

| Phase | Metric | Target |
|-------|--------|--------|
| Phase 1 | User registration success rate | >95% |
| Phase 1 | Doctor search response time | <200ms |
| Phase 2 | Booking completion rate | >80% |
| Phase 2 | Payment success rate | >98% |
| Phase 3 | Video call connection success | >95% |
| Phase 3 | Average call quality score | >4/5 |
| Phase 4 | Prescription generation time | <30 sec |
| Phase 5 | Order fulfillment rate | >95% |
| Phase 6 | Overall platform uptime | >99.9% |

---

## Appendix: Task Priority Legend

| Priority | Meaning | SLA |
|----------|---------|-----|
| **P0** | Critical - Blocker for milestone | Must complete in sprint |
| **P1** | High - Important feature | Should complete in sprint |
| **P2** | Medium - Nice to have | Can slip to next sprint |
| **P3** | Low - Future enhancement | Backlog |

---

*Document Version: 1.0*  
*Last Updated: January 30, 2026*

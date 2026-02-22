# Phase 1: Foundation - Detailed Implementation Plan

## Phase Overview

| Attribute | Details |
|-----------|---------|
| **Duration** | 6 Weeks |
| **Start Date** | January 30, 2026 |
| **End Date** | March 13, 2026 |
| **Team Size** | 8-10 members |
| **Goal** | Core infrastructure, authentication, and doctor discovery |
| **Status** | ✅ **CODE COMPLETE** - See [PHASE1_IMPLEMENTATION_STATUS.md](../docs/PHASE1_IMPLEMENTATION_STATUS.md) |

---

## Phase 1 Objectives

1. ✅ Set up complete development infrastructure (Kubernetes, databases, messaging)
2. ✅ Implement user registration and authentication system
3. ✅ Build API Gateway with security and rate limiting
4. ✅ Develop doctor profile management
5. ✅ Implement doctor search with Elasticsearch
6. ✅ Create patient web app (landing, search, doctor profiles)
7. ✅ Create doctor dashboard (login, profile management)

---

## Implementation Summary

All Phase 1 code has been implemented:

| Component | Status | Files |
|-----------|--------|-------|
| Backend Parent POM | ✅ | `backend/pom.xml` |
| healthcare-common | ✅ | 15+ classes (DTOs, events, exceptions, security, config) |
| user-service | ✅ | Full auth flow with OTP, JWT, refresh tokens |
| doctor-service | ✅ | Profile management, specializations, clinics |
| search-service | ✅ | Elasticsearch with Kafka consumer |
| api-gateway | ✅ | JWT filter, rate limiting, routing |
| patient-webapp | ✅ | Next.js 14 with home page, components |
| doctor-dashboard | ✅ | React + Vite with all pages |
| Kubernetes | ✅ | Complete manifests for all services |
| CI/CD | ✅ | 6 GitHub Actions workflows |
| Docker Compose | ✅ | Local development environment |

---

## Team Allocation for Phase 1

| Role | Name | Focus Area |
|------|------|------------|
| Tech Lead | _TBD_ | Architecture, code reviews, unblocking |
| Backend 1 | _TBD_ | User Service, Authentication |
| Backend 2 | _TBD_ | API Gateway, Doctor Service |
| Backend 3 | _TBD_ | Search Service, Elasticsearch |
| Frontend 1 | _TBD_ | Patient Web App (Next.js) |
| Frontend 2 | _TBD_ | Doctor Dashboard (React) |
| DevOps | _TBD_ | Infrastructure, CI/CD |
| Designer | _TBD_ | UI/UX Design |
| QA | _TBD_ | Testing, Quality |

---

## Sprint Breakdown

### Sprint 1 (Week 1-2): Infrastructure & Project Setup

**Sprint Goal**: Development environment fully operational with all infrastructure components running.

---

#### DevOps Tasks - Sprint 1

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| D1.1 | Git Repository Setup | Create monorepo or multi-repo structure with branch protection rules, PR templates | DevOps | 4 | P0 | Repos created, team has access, branch rules configured |
| D1.2 | CI/CD Pipeline Templates | Configure GitHub Actions with build, test, docker build, push stages | DevOps | 8 | P0 | Template pipeline running successfully |
| D1.3 | Development K8s Cluster | Provision EKS/AKS/GKE cluster with 3 nodes, configure kubectl access | DevOps | 16 | P0 | Cluster running, team can deploy pods |
| D1.4 | Container Registry | Set up ECR/ACR/GCR, configure credentials for CI/CD | DevOps | 4 | P0 | Can push/pull images from registry |
| D1.5 | Secrets Management | Deploy HashiCorp Vault, configure Kubernetes auth method | DevOps | 8 | P0 | Vault running, can store/retrieve secrets |
| D1.6 | PostgreSQL Setup | Deploy RDS/CloudSQL with dev instance (db.t3.medium or equivalent) | DevOps | 4 | P0 | Database accessible, credentials in Vault |
| D1.7 | MongoDB Setup | Deploy MongoDB Atlas M10 or self-hosted 3-node replica set | DevOps | 4 | P0 | MongoDB running, connection string available |
| D1.8 | Redis Setup | Deploy ElastiCache/Memorystore with cluster mode | DevOps | 4 | P0 | Redis accessible, connection working |
| D1.9 | Kafka Setup | Deploy MSK/Confluent with 3 brokers, create initial topics | DevOps | 8 | P0 | Kafka running, can produce/consume messages |
| D1.10 | Elasticsearch Setup | Deploy ES cluster (3 nodes), configure index templates | DevOps | 8 | P1 | ES cluster healthy, can index documents |
| D1.11 | Monitoring Stack | Deploy Prometheus + Grafana, configure service discovery | DevOps | 8 | P1 | Metrics visible in Grafana dashboards |
| D1.12 | Logging Stack | Deploy ELK/Loki stack, configure log collection | DevOps | 8 | P1 | Logs searchable in Kibana/Grafana |
| D1.13 | Distributed Tracing | Deploy Jaeger, configure sampling | DevOps | 4 | P2 | Traces visible for test requests |

**Sprint 1 DevOps Subtasks Breakdown:**

<details>
<summary><strong>D1.3 - Development K8s Cluster (Detailed Steps)</strong></summary>

```markdown
## Subtasks for D1.3

1. [ ] Choose cloud provider and region
2. [ ] Create VPC/VNet with proper CIDR ranges
3. [ ] Create private and public subnets
4. [ ] Provision Kubernetes cluster
   - Node pools: 3 x t3.large (or equivalent)
   - Kubernetes version: 1.28+
   - Enable autoscaling (min: 3, max: 10)
5. [ ] Configure kubectl access for team
6. [ ] Install essential add-ons:
   - [ ] Ingress controller (nginx-ingress)
   - [ ] Cert-manager for TLS
   - [ ] External-dns (optional)
7. [ ] Create namespaces:
   - [ ] healthcare-dev
   - [ ] monitoring
   - [ ] logging
8. [ ] Set up RBAC for team members
9. [ ] Document access instructions
```
</details>

<details>
<summary><strong>D1.9 - Kafka Setup (Detailed Steps)</strong></summary>

```markdown
## Subtasks for D1.9

1. [ ] Choose Kafka deployment (MSK/Confluent/Self-hosted)
2. [ ] Provision 3-broker cluster
3. [ ] Configure security (SASL/SSL)
4. [ ] Create initial topics:
   - [ ] user-events (partitions: 6, replication: 3)
   - [ ] doctor-events (partitions: 6, replication: 3)
   - [ ] appointment-events (partitions: 12, replication: 3)
   - [ ] notification-events (partitions: 6, replication: 3)
   - [ ] audit-events (partitions: 6, replication: 3)
5. [ ] Set retention policies (7 days default)
6. [ ] Configure Schema Registry
7. [ ] Store connection details in Vault
8. [ ] Test with sample producer/consumer
9. [ ] Document topic naming conventions
```
</details>

---

#### Backend Bootstrap Tasks - Sprint 1

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B1.1 | Parent POM/Gradle Setup | Create multi-module project with shared dependencies, versioning | Backend Lead | 8 | P0 | Project builds successfully, dependencies managed |
| B1.2 | Shared Libraries Module | Create common DTOs, utilities, event schemas, exceptions | Backend Lead | 16 | P0 | Library published to internal repo |
| B1.3 | Service Template | Spring Boot 3.x + WebFlux project template with standard structure | Backend Lead | 8 | P0 | Template generates working service |
| B1.4 | R2DBC Configuration | Connection pooling, transaction management, Flyway integration | Backend Lead | 4 | P0 | Can connect to PostgreSQL reactively |
| B1.5 | Kafka Template | Producer/consumer configuration, serialization, error handling | Backend Lead | 8 | P0 | Can publish/consume events |
| B1.6 | Dockerfile Template | Multi-stage build, JRE 21, proper layering | Backend Lead | 4 | P0 | Image builds, runs correctly |
| B1.7 | API Documentation | OpenAPI 3.0 with Springdoc, auto-generation | Backend Lead | 4 | P1 | Swagger UI accessible |
| B1.8 | Code Quality Tools | Checkstyle, SpotBugs, JaCoCo coverage | Backend Lead | 4 | P1 | Quality gates in CI pipeline |

**Backend Bootstrap Subtasks:**

<details>
<summary><strong>B1.2 - Shared Libraries Module (Detailed)</strong></summary>

```markdown
## healthcare-common library structure

healthcare-common/
├── src/main/java/com/healthapp/common/
│   ├── dto/
│   │   ├── PageRequest.java
│   │   ├── PageResponse.java
│   │   ├── ApiResponse.java
│   │   └── ErrorResponse.java
│   ├── event/
│   │   ├── BaseEvent.java
│   │   ├── EventMetadata.java
│   │   └── EventPublisher.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── NotFoundException.java
│   │   ├── ValidationException.java
│   │   └── GlobalExceptionHandler.java
│   ├── security/
│   │   ├── JwtUtils.java
│   │   ├── SecurityContext.java
│   │   └── UserPrincipal.java
│   ├── util/
│   │   ├── DateTimeUtils.java
│   │   ├── StringUtils.java
│   │   └── ValidationUtils.java
│   └── config/
│       ├── KafkaConfig.java
│       ├── RedisConfig.java
│       └── WebFluxConfig.java
└── pom.xml / build.gradle
```

**Files to create:**
1. [ ] BaseEvent.java - Standard event structure
2. [ ] ApiResponse.java - Standard API response wrapper
3. [ ] GlobalExceptionHandler.java - Centralized error handling
4. [ ] JwtUtils.java - Token generation/validation
5. [ ] KafkaConfig.java - Reactive Kafka configuration
</details>

<details>
<summary><strong>B1.3 - Service Template Structure</strong></summary>

```markdown
## Standard service structure

service-name/
├── src/
│   ├── main/
│   │   ├── java/com/healthapp/servicename/
│   │   │   ├── ServiceNameApplication.java
│   │   │   ├── controller/
│   │   │   │   └── HealthController.java
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   ├── event/
│   │   │   │   ├── publisher/
│   │   │   │   └── consumer/
│   │   │   ├── mapper/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── R2dbcConfig.java
│   │   │   └── exception/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   └── test/
│       └── java/com/healthapp/servicename/
├── Dockerfile
├── docker-compose.yml (local dev)
├── k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── configmap.yaml
└── pom.xml / build.gradle
```
</details>

---

#### Frontend Bootstrap Tasks - Sprint 1

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F1.1 | Patient Web App Setup | Next.js 14+ with App Router, TypeScript, Tailwind CSS | Frontend Lead | 8 | P0 | App runs locally, deploys to dev |
| F1.2 | Doctor Dashboard Setup | React 18 + Vite + TypeScript, Tailwind CSS | Frontend Lead | 8 | P0 | App runs locally, deploys to dev |
| F1.3 | Admin Portal Setup | React 18 + Vite + TypeScript | Frontend Lead | 4 | P1 | Basic setup complete |
| F1.4 | UI Component Library | Set up Shadcn/UI with custom theme | Frontend Lead | 16 | P0 | Core components available |
| F1.5 | API Client Setup | Axios/Fetch wrapper with React Query | Frontend Lead | 8 | P0 | API calls working with mock data |
| F1.6 | Linting & Formatting | ESLint, Prettier, Husky pre-commit hooks | Frontend | 4 | P1 | Linting passes on commit |
| F1.7 | Storybook Setup | Component documentation and testing | Frontend | 8 | P2 | Storybook running with sample |

**Frontend Setup Subtasks:**

<details>
<summary><strong>F1.1 - Patient Web App Setup (Detailed)</strong></summary>

```markdown
## Next.js Project Setup

1. [ ] Create Next.js project
   ```bash
   npx create-next-app@latest patient-web --typescript --tailwind --app --src-dir
   ```

2. [ ] Configure project structure:
   ```
   patient-web/
   ├── src/
   │   ├── app/
   │   │   ├── layout.tsx
   │   │   ├── page.tsx
   │   │   ├── (auth)/
   │   │   │   ├── login/
   │   │   │   └── register/
   │   │   ├── (main)/
   │   │   │   ├── doctors/
   │   │   │   ├── appointments/
   │   │   │   └── profile/
   │   │   └── api/
   │   ├── components/
   │   │   ├── ui/           # Shadcn components
   │   │   ├── layout/       # Header, Footer, Nav
   │   │   └── features/     # Feature-specific
   │   ├── lib/
   │   │   ├── api.ts        # API client
   │   │   ├── auth.ts       # Auth utilities
   │   │   └── utils.ts
   │   ├── hooks/
   │   ├── types/
   │   └── styles/
   ├── public/
   ├── next.config.js
   ├── tailwind.config.ts
   └── package.json
   ```

3. [ ] Install dependencies:
   ```bash
   npm install @tanstack/react-query axios zustand
   npm install -D @types/node
   ```

4. [ ] Configure environment variables:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
   NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
   ```

5. [ ] Set up Shadcn/UI:
   ```bash
   npx shadcn-ui@latest init
   npx shadcn-ui@latest add button card input form
   ```

6. [ ] Create Dockerfile for deployment
7. [ ] Create Kubernetes manifests
8. [ ] Configure CI/CD pipeline
```
</details>

<details>
<summary><strong>F1.4 - UI Component Library (Detailed)</strong></summary>

```markdown
## Core Components to Create

### Layout Components
1. [ ] Header - Logo, navigation, user menu
2. [ ] Footer - Links, copyright
3. [ ] Sidebar - Navigation for dashboard
4. [ ] PageLayout - Consistent page wrapper

### Form Components
5. [ ] Input - Text input with validation
6. [ ] Select - Dropdown selection
7. [ ] Checkbox - With label
8. [ ] Radio - Radio button group
9. [ ] DatePicker - Date selection
10. [ ] TimePicker - Time selection
11. [ ] PhoneInput - With country code
12. [ ] OTPInput - 6-digit OTP entry

### Display Components
13. [ ] Card - Content container
14. [ ] Avatar - User/Doctor image
15. [ ] Badge - Status indicators
16. [ ] Rating - Star rating display
17. [ ] Skeleton - Loading placeholders
18. [ ] EmptyState - No data display

### Feedback Components
19. [ ] Button - Primary, secondary, variants
20. [ ] Modal - Dialog overlay
21. [ ] Toast - Notifications
22. [ ] Alert - Warning/error/success messages
23. [ ] Spinner - Loading indicator

### Navigation Components
24. [ ] Tabs - Tab navigation
25. [ ] Breadcrumb - Page hierarchy
26. [ ] Pagination - Page navigation
```
</details>

---

#### Design Tasks - Sprint 1

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| U1.1 | Design System | Colors, typography, spacing, shadows, border-radius | Designer | 16 | P0 | Design tokens documented |
| U1.2 | Auth Flows Design | Login, register, OTP, forgot password (Patient & Doctor) | Designer | 16 | P0 | Figma mockups approved |
| U1.3 | Doctor Search Design | Search bar, filters, listing, cards | Designer | 16 | P0 | Figma mockups approved |
| U1.4 | Doctor Profile Design | Profile page with booking section | Designer | 8 | P0 | Figma mockups approved |
| U1.5 | Icon & Illustration | Custom icons, empty states, illustrations | Designer | 8 | P1 | Assets exported |

**Design System Specifications:**

<details>
<summary><strong>U1.1 - Design System (Detailed)</strong></summary>

```markdown
## Design Tokens

### Colors
```css
/* Primary */
--primary-50: #E8F5F9;
--primary-100: #B9E3EE;
--primary-500: #0891B2;  /* Main brand color */
--primary-600: #0E7490;
--primary-700: #155E75;

/* Secondary */
--secondary-500: #6366F1;

/* Success */
--success-500: #22C55E;

/* Warning */
--warning-500: #F59E0B;

/* Error */
--error-500: #EF4444;

/* Neutral */
--gray-50: #F9FAFB;
--gray-100: #F3F4F6;
--gray-500: #6B7280;
--gray-900: #111827;
```

### Typography
```css
/* Font Family */
--font-sans: 'Inter', sans-serif;

/* Font Sizes */
--text-xs: 0.75rem;    /* 12px */
--text-sm: 0.875rem;   /* 14px */
--text-base: 1rem;     /* 16px */
--text-lg: 1.125rem;   /* 18px */
--text-xl: 1.25rem;    /* 20px */
--text-2xl: 1.5rem;    /* 24px */
--text-3xl: 1.875rem;  /* 30px */

/* Font Weights */
--font-normal: 400;
--font-medium: 500;
--font-semibold: 600;
--font-bold: 700;
```

### Spacing
```css
--space-1: 0.25rem;   /* 4px */
--space-2: 0.5rem;    /* 8px */
--space-3: 0.75rem;   /* 12px */
--space-4: 1rem;      /* 16px */
--space-6: 1.5rem;    /* 24px */
--space-8: 2rem;      /* 32px */
--space-12: 3rem;     /* 48px */
--space-16: 4rem;     /* 64px */
```

### Border Radius
```css
--radius-sm: 0.25rem;   /* 4px */
--radius-md: 0.375rem;  /* 6px */
--radius-lg: 0.5rem;    /* 8px */
--radius-xl: 0.75rem;   /* 12px */
--radius-full: 9999px;
```

### Shadows
```css
--shadow-sm: 0 1px 2px rgba(0,0,0,0.05);
--shadow-md: 0 4px 6px rgba(0,0,0,0.1);
--shadow-lg: 0 10px 15px rgba(0,0,0,0.1);
--shadow-xl: 0 20px 25px rgba(0,0,0,0.1);
```
```
</details>

---

### Sprint 1 - Daily Task Schedule

#### Week 1

| Day | DevOps | Backend Lead | Frontend Lead | Designer |
|-----|--------|--------------|---------------|----------|
| Mon | D1.1 Git Setup | B1.1 Start | F1.1 Start | U1.1 Start |
| Tue | D1.2 CI/CD | B1.1 Complete | F1.1 Continue | U1.1 Continue |
| Wed | D1.3 K8s Start | B1.2 Start | F1.2 Start | U1.1 Complete |
| Thu | D1.3 K8s Continue | B1.2 Continue | F1.2 Continue | U1.2 Start |
| Fri | D1.3 K8s Complete | B1.2 Continue | F1.4 Start | U1.2 Continue |

#### Week 2

| Day | DevOps | Backend Lead | Frontend Lead | Designer |
|-----|--------|--------------|---------------|----------|
| Mon | D1.4, D1.5 | B1.2 Complete | F1.4 Continue | U1.2 Complete |
| Tue | D1.6, D1.7 | B1.3 Start | F1.4 Continue | U1.3 Start |
| Wed | D1.8 | B1.3, B1.4 | F1.5 Start | U1.3 Continue |
| Thu | D1.9 Start | B1.5 Start | F1.5 Complete | U1.3 Complete |
| Fri | D1.9, D1.10 | B1.5, B1.6 | F1.6, F1.7 | U1.4 |

---

### Sprint 1 Deliverables Checklist

- [ ] **Infrastructure**
  - [ ] Kubernetes cluster running with 3+ nodes
  - [ ] PostgreSQL accessible and configured
  - [ ] MongoDB accessible and configured
  - [ ] Redis cluster running
  - [ ] Kafka cluster with initial topics
  - [ ] Elasticsearch cluster healthy
  - [ ] CI/CD pipeline executing builds
  - [ ] Vault storing secrets

- [ ] **Backend**
  - [ ] Shared library published
  - [ ] Service template ready to use
  - [ ] Sample service deploying to K8s

- [ ] **Frontend**
  - [ ] Patient Web App building and running
  - [ ] Doctor Dashboard building and running
  - [ ] 10+ UI components ready

- [ ] **Design**
  - [ ] Design system documented
  - [ ] Auth flow mockups approved
  - [ ] Search/listing mockups approved

---

### Sprint 2 (Week 3-4): User Service & API Gateway

**Sprint Goal**: Users can register, login, and access protected endpoints through API Gateway.

---

#### User Service Tasks - Sprint 2

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B2.1 | Create User Service | Generate from template, configure for deployment | Backend 1 | 4 | P0 | Service runs locally |
| B2.2 | Database Schema | Design users table, create Flyway migrations | Backend 1 | 4 | P0 | Migrations run successfully |
| B2.3 | User Entity & Repository | User model, R2DBC repository with CRUD | Backend 1 | 8 | P0 | Repository tests pass |
| B2.4 | Registration Endpoint | POST /users/register with validation | Backend 1 | 8 | P0 | Can register new user |
| B2.5 | OTP Service | Generate, store (Redis), verify OTP | Backend 1 | 8 | P0 | OTP flow works |
| B2.6 | JWT Token Service | Generate access + refresh tokens | Backend 1 | 8 | P0 | Tokens generated correctly |
| B2.7 | Login Endpoint | POST /users/login with email/phone + password | Backend 1 | 8 | P0 | Login returns JWT |
| B2.8 | Social Login | Google OAuth2, Apple Sign-In | Backend 1 | 12 | P1 | Social login works |
| B2.9 | Password Reset | Forgot password, reset with OTP | Backend 1 | 8 | P0 | Can reset password |
| B2.10 | Profile Endpoints | GET/PUT /users/me | Backend 1 | 8 | P0 | Profile CRUD works |
| B2.11 | User Events | Publish user.registered to Kafka | Backend 1 | 4 | P0 | Event published |
| B2.12 | Unit Tests | Service layer tests, 80%+ coverage | Backend 1 | 12 | P0 | Tests pass, coverage met |
| B2.13 | Integration Tests | API tests with TestContainers | Backend 1 | 8 | P1 | Integration tests pass |

**User Service Implementation Details:**

<details>
<summary><strong>B2.2 - Database Schema</strong></summary>

```sql
-- V1__create_users_table.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM ('PATIENT', 'DOCTOR', 'ADMIN', 'PHARMACY', 'LAB');
CREATE TYPE user_status AS ENUM ('PENDING_VERIFICATION', 'ACTIVE', 'INACTIVE', 'SUSPENDED');

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'PATIENT',
    status user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    
    -- Profile info
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(200),
    avatar_url VARCHAR(500),
    date_of_birth DATE,
    gender VARCHAR(20),
    
    -- Verification
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    
    -- OAuth
    google_id VARCHAR(100),
    apple_id VARCHAR(100),
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- Constraints
    CONSTRAINT users_email_or_phone CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

-- Indexes
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_phone ON users(phone) WHERE phone IS NOT NULL;
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_google_id ON users(google_id) WHERE google_id IS NOT NULL;

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    revoked_at TIMESTAMP WITH TIME ZONE,
    device_info JSONB
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
```
</details>

<details>
<summary><strong>B2.4 - Registration Endpoint Implementation</strong></summary>

```java
// RegisterRequest.java
public record RegisterRequest(
    @Email @NotBlank String email,
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$") String phone,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    UserRole role  // Default: PATIENT
) {}

// RegisterResponse.java
@Value
@Builder
public class RegisterResponse {
    String userId;
    String email;
    String phone;
    boolean otpSent;
    String message;
}

// UserController.java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }
}

// UserService.java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final UserEventPublisher eventPublisher;
    
    public Mono<RegisterResponse> register(RegisterRequest request) {
        return validateUniqueEmail(request.email())
            .then(validateUniquePhone(request.phone()))
            .then(Mono.defer(() -> {
                User user = User.builder()
                    .email(request.email())
                    .phone(request.phone())
                    .passwordHash(passwordEncoder.encode(request.password()))
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .role(request.role() != null ? request.role() : UserRole.PATIENT)
                    .status(UserStatus.PENDING_VERIFICATION)
                    .build();
                    
                return userRepository.save(user);
            }))
            .flatMap(user -> otpService.sendOtp(user)
                .thenReturn(user))
            .doOnSuccess(user -> eventPublisher.publishUserRegistered(user))
            .map(user -> RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .otpSent(true)
                .message("Registration successful. Please verify with OTP.")
                .build());
    }
}
```
</details>

<details>
<summary><strong>B2.6 - JWT Token Service Implementation</strong></summary>

```java
// JwtTokenService.java
@Service
@RequiredArgsConstructor
public class JwtTokenService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-expiry:900}") // 15 minutes
    private long accessTokenExpiry;
    
    @Value("${jwt.refresh-token-expiry:604800}") // 7 days
    private long refreshTokenExpiry;
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    public Mono<TokenPair> generateTokens(User user) {
        String accessToken = generateAccessToken(user);
        
        return generateRefreshToken(user)
            .map(refreshToken -> TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiry)
                .refreshTokenExpiresIn(refreshTokenExpiry)
                .tokenType("Bearer")
                .build());
    }
    
    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpiry);
        
        return Jwts.builder()
            .setSubject(user.getId())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }
    
    private Mono<String> generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        String tokenHash = hashToken(token);
        
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .tokenHash(tokenHash)
            .expiresAt(Instant.now().plusSeconds(refreshTokenExpiry))
            .build();
            
        return refreshTokenRepository.save(refreshToken)
            .thenReturn(token);
    }
    
    public Mono<User> validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
                
            String userId = claims.getSubject();
            return userRepository.findById(userId);
        } catch (JwtException e) {
            return Mono.error(new UnauthorizedException("Invalid token"));
        }
    }
}

// TokenPair.java
@Value
@Builder
public class TokenPair {
    String accessToken;
    String refreshToken;
    long accessTokenExpiresIn;
    long refreshTokenExpiresIn;
    String tokenType;
}
```
</details>

---

#### API Gateway Tasks - Sprint 2

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B3.1 | Create API Gateway | Spring Cloud Gateway project setup | Backend 2 | 8 | P0 | Gateway runs, routes work |
| B3.2 | Route Configuration | Define routes for User Service | Backend 2 | 4 | P0 | Routes forward correctly |
| B3.3 | JWT Filter | Validate JWT on protected routes | Backend 2 | 8 | P0 | Protected routes secured |
| B3.4 | Rate Limiting | Redis-based rate limiting | Backend 2 | 8 | P0 | Rate limits enforced |
| B3.5 | Request Logging | Log requests with correlation IDs | Backend 2 | 4 | P0 | Logs contain correlation ID |
| B3.6 | CORS Configuration | Allow frontend origins | Backend 2 | 2 | P0 | Frontend can call APIs |
| B3.7 | Health Checks | Actuator endpoints | Backend 2 | 4 | P0 | Health endpoint responds |

**API Gateway Implementation:**

<details>
<summary><strong>B3.1-B3.7 - API Gateway Configuration</strong></summary>

```yaml
# application.yml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      default-filters:
        - name: RequestLogging
        - name: CorrelationId
      routes:
        # Public routes (no auth)
        - id: user-public
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/register, /api/v1/users/login, /api/v1/users/verify-otp, /api/v1/users/forgot-password
          filters:
            - RewritePath=/api/v1/users/(?<path>.*), /${path}
            
        # Protected routes
        - id: user-protected
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - JwtAuthFilter
            - RewritePath=/api/v1/users/(?<path>.*), /${path}
            
        - id: doctor-public
          uri: lb://doctor-service
          predicates:
            - Path=/api/v1/doctors/search/**, /api/v1/doctors/{id}, /api/v1/specializations/**
          filters:
            - RewritePath=/api/v1/doctors/(?<path>.*), /${path}
            
        - id: doctor-protected
          uri: lb://doctor-service
          predicates:
            - Path=/api/v1/doctors/**
          filters:
            - JwtAuthFilter
            - RewritePath=/api/v1/doctors/(?<path>.*), /${path}
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200

# JWT Filter
server:
  port: 8080

jwt:
  secret: ${JWT_SECRET}
  
# Rate limiting
spring.data.redis:
  host: ${REDIS_HOST}
  port: 6379
```

```java
// JwtAuthFilter.java
@Component
public class JwtAuthFilter implements GatewayFilter, Ordered {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }
        
        String token = authHeader.substring(7);
        
        try {
            Claims claims = validateToken(token);
            
            // Add user info to headers for downstream services
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Email", claims.get("email", String.class))
                .header("X-User-Role", claims.get("role", String.class))
                .build();
                
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException e) {
            return unauthorized(exchange, "Invalid or expired token");
        }
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // Return error body
        return exchange.getResponse().setComplete();
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```
</details>

---

#### Frontend Tasks - Sprint 2

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F2.1 | Patient Registration Page | Form with validation, API integration | Frontend 1 | 12 | P0 | Can register patient |
| F2.2 | OTP Verification | 6-digit input, resend, countdown | Frontend 1 | 8 | P0 | OTP flow works |
| F2.3 | Patient Login Page | Email/phone + password login | Frontend 1 | 8 | P0 | Can login |
| F2.4 | Doctor Registration | Multi-step form with qualification | Frontend 2 | 12 | P0 | Can register doctor |
| F2.5 | Doctor Login | Login page for doctor dashboard | Frontend 2 | 8 | P0 | Can login |
| F2.6 | Auth Context | JWT storage, refresh, context provider | Frontend 1 | 8 | P0 | Auth state managed |
| F2.7 | Protected Routes | Redirect to login if not authenticated | Frontend 1 | 4 | P0 | Routes protected |
| F2.8 | Forgot Password | Reset flow UI | Frontend 1 | 8 | P1 | Password reset works |
| F2.9 | Patient Profile Page | View/edit profile | Frontend 1 | 8 | P1 | Profile updates work |
| F2.10 | Doctor Profile Page | View/edit basic profile | Frontend 2 | 8 | P1 | Profile updates work |

**Frontend Implementation Details:**

<details>
<summary><strong>F2.1 - Patient Registration Page</strong></summary>

```typescript
// app/(auth)/register/page.tsx
'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardContent, CardFooter } from '@/components/ui/card';
import { useToast } from '@/hooks/use-toast';
import { authApi } from '@/lib/api/auth';

const registerSchema = z.object({
  firstName: z.string().min(2, 'First name is required'),
  lastName: z.string().min(2, 'Last name is required'),
  email: z.string().email('Invalid email address'),
  phone: z.string().regex(/^\+?[1-9]\d{9,14}$/, 'Invalid phone number'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain uppercase letter')
    .regex(/[a-z]/, 'Password must contain lowercase letter')
    .regex(/[0-9]/, 'Password must contain a number'),
  confirmPassword: z.string(),
}).refine(data => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

type RegisterFormData = z.infer<typeof registerSchema>;

export default function RegisterPage() {
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const { toast } = useToast();
  
  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });
  
  const onSubmit = async (data: RegisterFormData) => {
    setLoading(true);
    try {
      const response = await authApi.register({
        email: data.email,
        phone: data.phone,
        password: data.password,
        firstName: data.firstName,
        lastName: data.lastName,
      });
      
      // Store userId for OTP verification
      sessionStorage.setItem('pendingUserId', response.userId);
      
      toast({
        title: 'Registration successful!',
        description: 'Please verify your account with the OTP sent to your phone.',
      });
      
      router.push('/verify-otp');
    } catch (error) {
      toast({
        title: 'Registration failed',
        description: error.message,
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <h1 className="text-2xl font-bold text-center">Create Account</h1>
          <p className="text-gray-500 text-center">Join our healthcare platform</p>
        </CardHeader>
        
        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="First Name"
                {...register('firstName')}
                error={errors.firstName?.message}
              />
              <Input
                label="Last Name"
                {...register('lastName')}
                error={errors.lastName?.message}
              />
            </div>
            
            <Input
              label="Email"
              type="email"
              {...register('email')}
              error={errors.email?.message}
            />
            
            <Input
              label="Phone Number"
              type="tel"
              placeholder="+91 XXXXXXXXXX"
              {...register('phone')}
              error={errors.phone?.message}
            />
            
            <Input
              label="Password"
              type="password"
              {...register('password')}
              error={errors.password?.message}
            />
            
            <Input
              label="Confirm Password"
              type="password"
              {...register('confirmPassword')}
              error={errors.confirmPassword?.message}
            />
          </CardContent>
          
          <CardFooter className="flex flex-col space-y-4">
            <Button type="submit" className="w-full" loading={loading}>
              Create Account
            </Button>
            
            <p className="text-sm text-gray-500 text-center">
              Already have an account?{' '}
              <a href="/login" className="text-primary-600 hover:underline">
                Sign in
              </a>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
```
</details>

<details>
<summary><strong>F2.6 - Auth Context</strong></summary>

```typescript
// contexts/AuthContext.tsx
'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { authApi } from '@/lib/api/auth';
import { User, TokenPair } from '@/types/auth';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();
  
  useEffect(() => {
    // Check for existing token on mount
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) {
      fetchCurrentUser();
    } else {
      setIsLoading(false);
    }
  }, []);
  
  const fetchCurrentUser = async () => {
    try {
      const user = await authApi.getCurrentUser();
      setUser(user);
    } catch (error) {
      // Token invalid, try refresh
      try {
        await refreshToken();
        const user = await authApi.getCurrentUser();
        setUser(user);
      } catch {
        logout();
      }
    } finally {
      setIsLoading(false);
    }
  };
  
  const login = async (email: string, password: string) => {
    const response = await authApi.login({ email, password });
    storeTokens(response);
    const user = await authApi.getCurrentUser();
    setUser(user);
  };
  
  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    setUser(null);
    router.push('/login');
  };
  
  const refreshToken = async () => {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) throw new Error('No refresh token');
    
    const response = await authApi.refreshToken(refreshToken);
    storeTokens(response);
  };
  
  const storeTokens = (tokens: TokenPair) => {
    localStorage.setItem(TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
  };
  
  return (
    <AuthContext.Provider value={{
      user,
      isLoading,
      isAuthenticated: !!user,
      login,
      logout,
      refreshToken,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
```
</details>

---

### Sprint 2 Deliverables Checklist

- [ ] **User Service**
  - [ ] Registration with OTP verification
  - [ ] Login with JWT tokens
  - [ ] Password reset flow
  - [ ] Profile management
  - [ ] Unit tests (80%+ coverage)
  - [ ] Deployed to dev K8s

- [ ] **API Gateway**
  - [ ] Routes configured
  - [ ] JWT validation working
  - [ ] Rate limiting active
  - [ ] CORS configured
  - [ ] Deployed to dev K8s

- [ ] **Frontend**
  - [ ] Patient registration flow complete
  - [ ] Doctor registration flow complete
  - [ ] Login working for both
  - [ ] Auth context managing state
  - [ ] Protected routes working

---

### Sprint 3 (Week 5-6): Doctor Service & Search

**Sprint Goal**: Doctors can manage profiles; Patients can search and view doctor profiles.

---

#### Doctor Service Tasks - Sprint 3

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B4.1 | Create Doctor Service | Generate from template | Backend 2 | 4 | P0 | Service runs |
| B4.2 | Database Schema | Doctors, specializations, qualifications tables | Backend 2 | 8 | P0 | Migrations run |
| B4.3 | Reference Data | Seed specializations, languages | Backend 2 | 4 | P0 | Data seeded |
| B4.4 | Doctor Entity & Repository | Doctor model with relations | Backend 2 | 8 | P0 | Repository works |
| B4.5 | Doctor Registration | POST /doctors endpoint | Backend 2 | 8 | P0 | Can register doctor |
| B4.6 | Profile Management | GET/PUT /doctors/me | Backend 2 | 12 | P0 | Profile CRUD works |
| B4.7 | Qualification CRUD | Add/update/remove qualifications | Backend 2 | 8 | P0 | Qualifications managed |
| B4.8 | Clinic Association | Link doctors to clinics | Backend 2 | 8 | P1 | Clinic links work |
| B4.9 | Doctor Events | Publish to Kafka | Backend 2 | 4 | P0 | Events published |
| B4.10 | Unit & Integration Tests | 80%+ coverage | Backend 2 | 12 | P0 | Tests pass |

**Doctor Service Schema:**

<details>
<summary><strong>B4.2 - Doctor Database Schema</strong></summary>

```sql
-- V1__create_doctor_tables.sql

-- Specializations reference table
CREATE TABLE specializations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    parent_specialty_id UUID REFERENCES specializations(id),
    description TEXT,
    icon_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0
);

-- Languages reference table
CREATE TABLE languages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE
);

-- Doctors table
CREATE TABLE doctors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,  -- References user-service
    
    -- Basic info
    full_name VARCHAR(200) NOT NULL,
    profile_picture_url VARCHAR(500),
    registration_number VARCHAR(100) UNIQUE,
    registration_council VARCHAR(200),
    experience_years INT,
    bio TEXT,
    
    -- Consultation fees
    consultation_fee DECIMAL(10, 2),
    video_consultation_fee DECIMAL(10, 2),
    followup_fee DECIMAL(10, 2),
    
    -- Ratings
    rating DECIMAL(3, 2) DEFAULT 0,
    review_count INT DEFAULT 0,
    
    -- Status
    is_verified BOOLEAN DEFAULT FALSE,
    is_accepting_patients BOOLEAN DEFAULT TRUE,
    verification_date TIMESTAMP WITH TIME ZONE,
    
    -- Profile completeness
    profile_completeness INT DEFAULT 0,  -- Percentage
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Doctor specializations (many-to-many)
CREATE TABLE doctor_specializations (
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    specialization_id UUID REFERENCES specializations(id),
    is_primary BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (doctor_id, specialization_id)
);

-- Doctor qualifications
CREATE TABLE doctor_qualifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    degree VARCHAR(100) NOT NULL,
    institution VARCHAR(200) NOT NULL,
    year_of_completion INT,
    certificate_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE
);

-- Doctor languages
CREATE TABLE doctor_languages (
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    language_id UUID REFERENCES languages(id),
    PRIMARY KEY (doctor_id, language_id)
);

-- Clinics
CREATE TABLE clinics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    address_line1 VARCHAR(500),
    address_line2 VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    postal_code VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Doctor-Clinic associations
CREATE TABLE doctor_clinics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    clinic_id UUID REFERENCES clinics(id) ON DELETE CASCADE,
    consultation_fee DECIMAL(10, 2),  -- Clinic-specific fee
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE (doctor_id, clinic_id)
);

-- Indexes
CREATE INDEX idx_doctors_user_id ON doctors(user_id);
CREATE INDEX idx_doctors_verified ON doctors(is_verified) WHERE is_verified = TRUE;
CREATE INDEX idx_doctors_accepting ON doctors(is_accepting_patients) WHERE is_accepting_patients = TRUE;
CREATE INDEX idx_doctor_specs_doctor ON doctor_specializations(doctor_id);
CREATE INDEX idx_doctor_specs_spec ON doctor_specializations(specialization_id);
CREATE INDEX idx_clinics_location ON clinics USING GIST (
    ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
);
```
</details>

---

#### Search Service Tasks - Sprint 3

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B5.1 | Create Search Service | WebFlux + Elasticsearch client | Backend 3 | 4 | P0 | Service runs |
| B5.2 | ES Index Design | Doctor index with proper mappings | Backend 3 | 8 | P0 | Index created |
| B5.3 | Kafka Consumer | Index doctors from events | Backend 3 | 12 | P0 | Indexing works |
| B5.4 | Basic Search | GET /search/doctors | Backend 3 | 12 | P0 | Search returns results |
| B5.5 | Specialty Filter | Filter by specialization | Backend 3 | 4 | P0 | Filter works |
| B5.6 | Location Filter | Geo-distance search | Backend 3 | 8 | P0 | Location search works |
| B5.7 | Price Range Filter | Filter by consultation fee | Backend 3 | 4 | P0 | Price filter works |
| B5.8 | Availability Filter | Filter available today/tomorrow | Backend 3 | 8 | P1 | Availability filter works |
| B5.9 | Autocomplete | Search suggestions | Backend 3 | 8 | P1 | Suggestions appear |
| B5.10 | Ranking/Scoring | Relevance-based ranking | Backend 3 | 8 | P1 | Results ranked properly |
| B5.11 | Tests | Unit + integration tests | Backend 3 | 8 | P0 | Tests pass |

**Search Implementation:**

<details>
<summary><strong>B5.2 - Elasticsearch Index Mapping</strong></summary>

```json
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 2,
    "analysis": {
      "analyzer": {
        "autocomplete_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "edge_ngram_filter"]
        },
        "search_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase"]
        }
      },
      "filter": {
        "edge_ngram_filter": {
          "type": "edge_ngram",
          "min_gram": 2,
          "max_gram": 20
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "userId": { "type": "keyword" },
      
      "fullName": {
        "type": "text",
        "analyzer": "autocomplete_analyzer",
        "search_analyzer": "search_analyzer",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      
      "profilePictureUrl": { "type": "keyword", "index": false },
      
      "specializations": {
        "type": "nested",
        "properties": {
          "id": { "type": "keyword" },
          "name": {
            "type": "text",
            "analyzer": "autocomplete_analyzer",
            "search_analyzer": "search_analyzer",
            "fields": {
              "keyword": { "type": "keyword" }
            }
          },
          "isPrimary": { "type": "boolean" }
        }
      },
      
      "qualifications": {
        "type": "nested",
        "properties": {
          "degree": { "type": "text" },
          "institution": { "type": "text" }
        }
      },
      
      "experienceYears": { "type": "integer" },
      "registrationNumber": { "type": "keyword" },
      
      "consultationFee": { "type": "float" },
      "videoConsultationFee": { "type": "float" },
      
      "rating": { "type": "float" },
      "reviewCount": { "type": "integer" },
      
      "isVerified": { "type": "boolean" },
      "isAcceptingPatients": { "type": "boolean" },
      
      "languages": { "type": "keyword" },
      
      "clinics": {
        "type": "nested",
        "properties": {
          "id": { "type": "keyword" },
          "name": { "type": "text" },
          "location": { "type": "geo_point" },
          "city": { "type": "keyword" },
          "address": { "type": "text" }
        }
      },
      
      "primaryLocation": { "type": "geo_point" },
      "city": { "type": "keyword" },
      
      "availableForVideoConsultation": { "type": "boolean" },
      "nextAvailableSlot": { "type": "date" },
      
      "updatedAt": { "type": "date" }
    }
  }
}
```
</details>

<details>
<summary><strong>B5.4 - Search Implementation</strong></summary>

```java
// DoctorSearchService.java
@Service
@RequiredArgsConstructor
public class DoctorSearchService {
    
    private final ReactiveElasticsearchClient esClient;
    
    public Mono<SearchResponse<DoctorSearchResult>> searchDoctors(DoctorSearchRequest request) {
        return buildSearchQuery(request)
            .flatMap(query -> esClient.search(s -> s
                .index("doctors")
                .query(query)
                .from(request.getPage() * request.getSize())
                .size(request.getSize())
                .sort(buildSort(request))
                .highlight(buildHighlight()),
                DoctorDocument.class
            ))
            .map(this::mapToSearchResponse);
    }
    
    private Mono<Query> buildSearchQuery(DoctorSearchRequest request) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        
        // Text search (name, specialization)
        if (StringUtils.hasText(request.getQuery())) {
            boolQuery.should(
                MultiMatchQuery.of(m -> m
                    .query(request.getQuery())
                    .fields("fullName^3", "specializations.name^2", "qualifications.degree")
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                )._toQuery()
            );
            boolQuery.minimumShouldMatch("1");
        }
        
        // Specialty filter
        if (request.getSpecializationId() != null) {
            boolQuery.filter(
                NestedQuery.of(n -> n
                    .path("specializations")
                    .query(q -> q
                        .term(t -> t
                            .field("specializations.id")
                            .value(request.getSpecializationId())
                        )
                    )
                )._toQuery()
            );
        }
        
        // Location filter (geo distance)
        if (request.getLatitude() != null && request.getLongitude() != null) {
            boolQuery.filter(
                GeoDistanceQuery.of(g -> g
                    .field("primaryLocation")
                    .distance(request.getRadiusKm() + "km")
                    .location(l -> l.latlon(ll -> 
                        ll.lat(request.getLatitude())
                          .lon(request.getLongitude())
                    ))
                )._toQuery()
            );
        }
        
        // City filter
        if (StringUtils.hasText(request.getCity())) {
            boolQuery.filter(
                TermQuery.of(t -> t
                    .field("city")
                    .value(request.getCity())
                )._toQuery()
            );
        }
        
        // Price range filter
        if (request.getMinFee() != null || request.getMaxFee() != null) {
            RangeQuery.Builder rangeQuery = new RangeQuery.Builder()
                .field("consultationFee");
            if (request.getMinFee() != null) {
                rangeQuery.gte(JsonData.of(request.getMinFee()));
            }
            if (request.getMaxFee() != null) {
                rangeQuery.lte(JsonData.of(request.getMaxFee()));
            }
            boolQuery.filter(rangeQuery.build()._toQuery());
        }
        
        // Video consultation filter
        if (Boolean.TRUE.equals(request.getVideoConsultation())) {
            boolQuery.filter(
                TermQuery.of(t -> t
                    .field("availableForVideoConsultation")
                    .value(true)
                )._toQuery()
            );
        }
        
        // Only verified and accepting patients
        boolQuery.filter(
            TermQuery.of(t -> t.field("isVerified").value(true))._toQuery()
        );
        boolQuery.filter(
            TermQuery.of(t -> t.field("isAcceptingPatients").value(true))._toQuery()
        );
        
        return Mono.just(Query.of(q -> q.bool(boolQuery.build())));
    }
    
    private List<SortOptions> buildSort(DoctorSearchRequest request) {
        List<SortOptions> sorts = new ArrayList<>();
        
        switch (request.getSortBy()) {
            case "rating":
                sorts.add(SortOptions.of(s -> s.field(f -> 
                    f.field("rating").order(SortOrder.Desc))));
                break;
            case "experience":
                sorts.add(SortOptions.of(s -> s.field(f -> 
                    f.field("experienceYears").order(SortOrder.Desc))));
                break;
            case "fee_low":
                sorts.add(SortOptions.of(s -> s.field(f -> 
                    f.field("consultationFee").order(SortOrder.Asc))));
                break;
            case "fee_high":
                sorts.add(SortOptions.of(s -> s.field(f -> 
                    f.field("consultationFee").order(SortOrder.Desc))));
                break;
            case "distance":
                if (request.getLatitude() != null) {
                    sorts.add(SortOptions.of(s -> s.geoDistance(g -> g
                        .field("primaryLocation")
                        .location(l -> l.latlon(ll -> 
                            ll.lat(request.getLatitude())
                              .lon(request.getLongitude())))
                        .order(SortOrder.Asc)
                        .unit(DistanceUnit.Kilometers)
                    )));
                }
                break;
            default:
                // Relevance (default ES scoring)
                break;
        }
        
        return sorts;
    }
}

// DoctorSearchRequest.java
@Value
@Builder
public class DoctorSearchRequest {
    String query;                    // Free text search
    String specializationId;         // Filter by specialty
    Double latitude;                 // User's location
    Double longitude;
    Integer radiusKm;                // Search radius (default: 10km)
    String city;                     // City filter
    BigDecimal minFee;               // Min consultation fee
    BigDecimal maxFee;               // Max consultation fee
    Boolean videoConsultation;       // Video consultation available
    String sortBy;                   // rating, experience, fee_low, fee_high, distance
    @Builder.Default int page = 0;
    @Builder.Default int size = 20;
}
```
</details>

---

#### Frontend Tasks - Sprint 3

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F3.1 | Landing Page | Hero, search bar, popular specialties | Frontend 1 | 12 | P0 | Landing page live |
| F3.2 | Search Results Page | Doctor listing with pagination | Frontend 1 | 16 | P0 | Results display |
| F3.3 | Filter Sidebar | Specialty, location, price, availability | Frontend 1 | 12 | P0 | Filters work |
| F3.4 | Doctor Card | Name, photo, specialty, rating, fee | Frontend 1 | 8 | P0 | Cards display properly |
| F3.5 | Doctor Profile Page | Full profile with booking section | Frontend 1 | 16 | P0 | Profile page complete |
| F3.6 | Specialty Selector | Autocomplete specialty picker | Frontend 1 | 4 | P0 | Selector works |
| F3.7 | Doctor Profile Editor | Edit profile in dashboard | Frontend 2 | 16 | P0 | Can edit profile |
| F3.8 | Qualification Editor | Add/edit qualifications | Frontend 2 | 8 | P0 | Qualifications editable |
| F3.9 | Photo Upload | Profile picture upload | Frontend 2 | 8 | P0 | Photo uploads |

**Frontend Implementation:**

<details>
<summary><strong>F3.2 - Doctor Search Results Page</strong></summary>

```typescript
// app/doctors/page.tsx
'use client';

import { useSearchParams } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { DoctorCard } from '@/components/doctors/DoctorCard';
import { FilterSidebar } from '@/components/doctors/FilterSidebar';
import { SearchBar } from '@/components/doctors/SearchBar';
import { Pagination } from '@/components/ui/pagination';
import { Skeleton } from '@/components/ui/skeleton';
import { doctorsApi } from '@/lib/api/doctors';
import { DoctorSearchFilters } from '@/types/doctor';

export default function DoctorSearchPage() {
  const searchParams = useSearchParams();
  const [page, setPage] = useState(0);
  
  const filters: DoctorSearchFilters = {
    query: searchParams.get('q') || '',
    specializationId: searchParams.get('specialty') || undefined,
    city: searchParams.get('city') || undefined,
    minFee: searchParams.get('minFee') ? Number(searchParams.get('minFee')) : undefined,
    maxFee: searchParams.get('maxFee') ? Number(searchParams.get('maxFee')) : undefined,
    videoConsultation: searchParams.get('video') === 'true',
    sortBy: searchParams.get('sort') || 'relevance',
  };
  
  const { data, isLoading, error } = useQuery({
    queryKey: ['doctors', filters, page],
    queryFn: () => doctorsApi.search({ ...filters, page, size: 20 }),
  });
  
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Search Header */}
      <div className="bg-white border-b">
        <div className="container mx-auto py-4">
          <SearchBar initialValue={filters.query} />
        </div>
      </div>
      
      <div className="container mx-auto py-6">
        <div className="flex gap-6">
          {/* Filters Sidebar */}
          <aside className="w-64 flex-shrink-0">
            <FilterSidebar filters={filters} />
          </aside>
          
          {/* Results */}
          <main className="flex-1">
            {/* Results Header */}
            <div className="flex justify-between items-center mb-4">
              <h1 className="text-xl font-semibold">
                {data?.totalElements || 0} doctors found
                {filters.query && ` for "${filters.query}"`}
                {filters.city && ` in ${filters.city}`}
              </h1>
              
              <select
                value={filters.sortBy}
                onChange={(e) => updateSort(e.target.value)}
                className="border rounded-lg px-3 py-2"
              >
                <option value="relevance">Relevance</option>
                <option value="rating">Rating</option>
                <option value="experience">Experience</option>
                <option value="fee_low">Fee: Low to High</option>
                <option value="fee_high">Fee: High to Low</option>
              </select>
            </div>
            
            {/* Loading State */}
            {isLoading && (
              <div className="space-y-4">
                {[...Array(5)].map((_, i) => (
                  <Skeleton key={i} className="h-48 w-full" />
                ))}
              </div>
            )}
            
            {/* Error State */}
            {error && (
              <div className="text-center py-12">
                <p className="text-red-500">Failed to load doctors. Please try again.</p>
              </div>
            )}
            
            {/* Results List */}
            {data && (
              <>
                <div className="space-y-4">
                  {data.content.map((doctor) => (
                    <DoctorCard key={doctor.id} doctor={doctor} />
                  ))}
                </div>
                
                {data.content.length === 0 && (
                  <div className="text-center py-12">
                    <p className="text-gray-500">No doctors found matching your criteria.</p>
                    <button 
                      onClick={clearFilters}
                      className="mt-2 text-primary-600 hover:underline"
                    >
                      Clear all filters
                    </button>
                  </div>
                )}
                
                {/* Pagination */}
                {data.totalPages > 1 && (
                  <div className="mt-8 flex justify-center">
                    <Pagination
                      currentPage={page}
                      totalPages={data.totalPages}
                      onPageChange={setPage}
                    />
                  </div>
                )}
              </>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}
```
</details>

<details>
<summary><strong>F3.4 - Doctor Card Component</strong></summary>

```typescript
// components/doctors/DoctorCard.tsx
import Image from 'next/image';
import Link from 'next/link';
import { Star, MapPin, Video, Clock, ThumbsUp } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { DoctorSearchResult } from '@/types/doctor';
import { formatCurrency } from '@/lib/utils';

interface DoctorCardProps {
  doctor: DoctorSearchResult;
}

export function DoctorCard({ doctor }: DoctorCardProps) {
  return (
    <Card className="p-4 hover:shadow-md transition-shadow">
      <div className="flex gap-4">
        {/* Profile Image */}
        <div className="flex-shrink-0">
          <Image
            src={doctor.profilePictureUrl || '/images/doctor-placeholder.png'}
            alt={doctor.fullName}
            width={120}
            height={120}
            className="rounded-lg object-cover"
          />
          {doctor.isVerified && (
            <Badge className="mt-2 w-full justify-center" variant="success">
              ✓ Verified
            </Badge>
          )}
        </div>
        
        {/* Doctor Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between">
            <div>
              <Link 
                href={`/doctors/${doctor.id}`}
                className="text-lg font-semibold text-gray-900 hover:text-primary-600"
              >
                Dr. {doctor.fullName}
              </Link>
              
              <p className="text-gray-600">
                {doctor.specializations.map(s => s.name).join(', ')}
              </p>
              
              <p className="text-sm text-gray-500 mt-1">
                {doctor.experienceYears} years experience
              </p>
              
              {doctor.qualifications.length > 0 && (
                <p className="text-sm text-gray-500">
                  {doctor.qualifications.map(q => q.degree).join(', ')}
                </p>
              )}
            </div>
            
            {/* Rating */}
            <div className="text-right">
              <div className="flex items-center gap-1 bg-green-50 text-green-700 px-2 py-1 rounded">
                <Star className="w-4 h-4 fill-current" />
                <span className="font-medium">{doctor.rating.toFixed(1)}</span>
              </div>
              <p className="text-xs text-gray-500 mt-1">
                {doctor.reviewCount} reviews
              </p>
            </div>
          </div>
          
          {/* Location */}
          {doctor.clinics?.[0] && (
            <div className="flex items-center gap-1 text-sm text-gray-600 mt-2">
              <MapPin className="w-4 h-4" />
              <span>{doctor.clinics[0].name}, {doctor.clinics[0].city}</span>
              {doctor.distance && (
                <span className="text-gray-400">
                  • {doctor.distance.toFixed(1)} km away
                </span>
              )}
            </div>
          )}
          
          {/* Tags */}
          <div className="flex flex-wrap gap-2 mt-3">
            {doctor.availableForVideoConsultation && (
              <Badge variant="outline" className="text-xs">
                <Video className="w-3 h-3 mr-1" />
                Video Consult
              </Badge>
            )}
            {doctor.nextAvailableSlot && (
              <Badge variant="outline" className="text-xs text-green-600">
                <Clock className="w-3 h-3 mr-1" />
                Available Today
              </Badge>
            )}
            <Badge variant="outline" className="text-xs">
              <ThumbsUp className="w-3 h-3 mr-1" />
              {Math.round(doctor.rating * 20)}% Recommended
            </Badge>
          </div>
        </div>
        
        {/* Booking Section */}
        <div className="flex-shrink-0 w-40 border-l pl-4">
          <div className="text-center">
            <p className="text-sm text-gray-500">Consultation Fee</p>
            <p className="text-xl font-bold text-gray-900">
              {formatCurrency(doctor.consultationFee)}
            </p>
          </div>
          
          {doctor.videoConsultationFee && (
            <div className="text-center mt-2">
              <p className="text-xs text-gray-500">Video Consult</p>
              <p className="text-sm font-medium text-gray-700">
                {formatCurrency(doctor.videoConsultationFee)}
              </p>
            </div>
          )}
          
          <Link href={`/doctors/${doctor.id}`}>
            <Button className="w-full mt-4">
              Book Appointment
            </Button>
          </Link>
          
          <Link 
            href={`/doctors/${doctor.id}`}
            className="block text-center text-sm text-primary-600 mt-2 hover:underline"
          >
            View Profile
          </Link>
        </div>
      </div>
    </Card>
  );
}
```
</details>

---

### Sprint 3 Deliverables Checklist

- [ ] **Doctor Service**
  - [ ] Doctor registration and profile creation
  - [ ] Profile management (CRUD)
  - [ ] Qualification management
  - [ ] Events publishing to Kafka
  - [ ] Deployed to dev K8s

- [ ] **Search Service**
  - [ ] Elasticsearch indexing from events
  - [ ] Text search working
  - [ ] All filters working (specialty, location, price)
  - [ ] Sorting options working
  - [ ] Deployed to dev K8s

- [ ] **Frontend - Patient App**
  - [ ] Landing page with search
  - [ ] Doctor search results page
  - [ ] Filter sidebar functional
  - [ ] Doctor profile page
  - [ ] Doctor cards with all info

- [ ] **Frontend - Doctor Dashboard**
  - [ ] Profile editor
  - [ ] Qualification management
  - [ ] Photo upload

---

## Phase 1 Completion Criteria

### Functional Requirements ✓

| Requirement | Acceptance Criteria | Status |
|-------------|---------------------|--------|
| User Registration | Patient/Doctor can register with email/phone + OTP verification | ⬜ |
| User Login | Users can login and receive JWT tokens | ⬜ |
| Password Reset | Users can reset password via OTP | ⬜ |
| Doctor Profile | Doctors can create and edit their profile | ⬜ |
| Doctor Search | Patients can search doctors by name/specialty | ⬜ |
| Search Filters | Location, price, specialty filters work | ⬜ |
| Doctor View | Patients can view doctor profiles | ⬜ |

### Non-Functional Requirements ✓

| Requirement | Criteria | Status |
|-------------|----------|--------|
| API Response Time | < 200ms for 95th percentile | ⬜ |
| Search Response Time | < 300ms for 95th percentile | ⬜ |
| Unit Test Coverage | > 80% for all services | ⬜ |
| API Documentation | 100% endpoints documented | ⬜ |
| Zero Critical Bugs | No P0/P1 bugs in production | ⬜ |

### Infrastructure ✓

| Component | Criteria | Status |
|-----------|----------|--------|
| K8s Cluster | 3+ nodes, autoscaling configured | ⬜ |
| Databases | PostgreSQL, MongoDB, Redis running | ⬜ |
| Kafka | 3 brokers, topics created | ⬜ |
| Elasticsearch | 3 nodes, index healthy | ⬜ |
| Monitoring | Prometheus + Grafana with dashboards | ⬜ |
| CI/CD | Automated build + deploy pipeline | ⬜ |

---

## Phase 1 Sign-off

| Role | Name | Sign-off Date | Signature |
|------|------|---------------|-----------|
| Tech Lead | | | |
| Product Manager | | | |
| QA Lead | | | |
| DevOps Lead | | | |

---

## Appendix

### A. API Contracts (OpenAPI)

See `/docs/api/` for full OpenAPI specifications:
- `user-service-api.yaml`
- `doctor-service-api.yaml`
- `search-service-api.yaml`
- `api-gateway.yaml`

### B. Database ERD

See `/docs/database/phase-1-erd.png`

### C. Architecture Diagrams

See `/docs/architecture/phase-1-architecture.png`

### D. Test Plan

See `/docs/testing/phase-1-test-plan.md`

---

*Document Version: 1.0*  
*Last Updated: January 30, 2026*

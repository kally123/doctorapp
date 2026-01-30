# Healthcare Platform Architecture Instructions (Practo-like)

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Platform Overview](#platform-overview)
3. [Technology Stack](#technology-stack)
4. [Microservices Architecture](#microservices-architecture)
5. [Domain Services](#domain-services)
6. [Event-Driven Architecture](#event-driven-architecture)
7. [API Gateway & Security](#api-gateway--security)
8. [Database Design](#database-design)
9. [Frontend Applications](#frontend-applications)
10. [Infrastructure & DevOps](#infrastructure--devops)
11. [Performance & Scalability](#performance--scalability)
12. [Implementation Phases](#implementation-phases)

---

## Executive Summary

This document provides comprehensive instructions for building a **healthcare platform** similar to Practo, featuring:
- Doctor discovery and appointment booking
- Teleconsultation (video/audio/chat)
- Electronic Health Records (EHR)
- Medicine ordering and pharmacy integration
- Lab test booking
- Health articles and content
- Practice management for doctors/clinics

The architecture follows **reactive programming principles with Spring WebFlux**, **event-driven microservices**, and **Domain-Driven Design (DDD)** as outlined in our organizational guidelines.

---

## Platform Overview

### Core Features

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         HEALTHCARE PLATFORM                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│  PATIENT SERVICES          │  DOCTOR SERVICES         │  ADMIN SERVICES      │
│  ─────────────────         │  ────────────────        │  ──────────────      │
│  • Find Doctors            │  • Profile Management    │  • User Management   │
│  • Book Appointments       │  • Appointment Mgmt      │  • Content Moderation│
│  • Video Consultation      │  • Teleconsultation      │  • Analytics          │
│  • Health Records          │  • Prescription Writing  │  • Audit Logs        │
│  • Medicine Orders         │  • Patient Records       │  • System Config     │
│  • Lab Test Booking        │  • Earnings & Payouts    │  • Partner Mgmt      │
│  • Health Feed/Articles    │  • Practice Management   │                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

### User Personas

| Persona | Key Needs |
|---------|-----------|
| **Patient** | Find doctors, book appointments, consult online, access health records |
| **Doctor** | Manage practice, conduct consultations, write prescriptions, track earnings |
| **Clinic/Hospital** | Multi-doctor management, appointment scheduling, billing |
| **Pharmacy** | Receive prescriptions, process orders, inventory management |
| **Lab Partner** | Receive test orders, upload reports, slot management |
| **Admin** | Platform management, moderation, analytics |

---

## Technology Stack

### Backend (Reactive Microservices)

> ⚠️ **CRITICAL**: As per WebFlux Guidelines - Use reactive programming end-to-end. Never mix blocking code!

| Component | Technology | Rationale |
|-----------|------------|-----------|
| **Framework** | Spring Boot 3.x + WebFlux | Non-blocking, reactive processing |
| **Language** | Java 21+ (Virtual Threads) | Performance, modern features |
| **Database** | PostgreSQL + R2DBC | Reactive database access |
| **Document Store** | MongoDB Reactive | Health records, flexible schemas |
| **Cache** | Redis Reactive (Lettuce) | Distributed caching |
| **Message Broker** | Apache Kafka | Event streaming, reliability |
| **Search** | Elasticsearch | Doctor search, health articles |
| **Real-time** | WebSocket/STOMP | Chat, notifications |
| **Video** | WebRTC + Twilio/Agora | Teleconsultation |
| **API Gateway** | Spring Cloud Gateway | Routing, rate limiting |
| **Service Discovery** | Kubernetes + Consul | Service mesh |

### Frontend

| Platform | Technology |
|----------|------------|
| **Web (Patient)** | Next.js 14+ with React |
| **Web (Doctor Dashboard)** | React + TypeScript |
| **Mobile (Patient)** | React Native / Flutter |
| **Mobile (Doctor)** | React Native / Flutter |
| **Admin Portal** | React + TypeScript |

### Infrastructure

| Component | Technology |
|-----------|------------|
| **Container Orchestration** | Kubernetes (EKS/AKS/GKE) |
| **CI/CD** | GitHub Actions / GitLab CI |
| **Monitoring** | Prometheus + Grafana |
| **Logging** | ELK Stack (Elasticsearch, Logstash, Kibana) |
| **Tracing** | Jaeger / Zipkin |
| **Secrets** | HashiCorp Vault |
| **CDN** | CloudFront / Cloudflare |
| **Object Storage** | AWS S3 / Azure Blob |

---

## Microservices Architecture

### High-Level Architecture Diagram

```
                                    ┌─────────────────────┐
                                    │   Load Balancer     │
                                    │   (AWS ALB/NLB)     │
                                    └──────────┬──────────┘
                                               │
                                    ┌──────────▼──────────┐
                                    │    API Gateway      │
                                    │ (Spring Cloud GW)   │
                                    │  • Rate Limiting    │
                                    │  • Authentication   │
                                    │  • Request Routing  │
                                    └──────────┬──────────┘
                                               │
         ┌─────────────────────────────────────┼─────────────────────────────────────┐
         │                                     │                                     │
         ▼                                     ▼                                     ▼
┌─────────────────┐               ┌─────────────────┐               ┌─────────────────┐
│  User Service   │               │ Doctor Service  │               │Appointment Svc  │
│  ─────────────  │               │ ─────────────── │               │ ─────────────── │
│  • Registration │               │  • Profiles     │               │  • Booking      │
│  • Auth (JWT)   │               │  • Search       │               │  • Scheduling   │
│  • Profiles     │               │  • Availability │               │  • Reminders    │
└────────┬────────┘               └────────┬────────┘               └────────┬────────┘
         │                                  │                                 │
         ▼                                  ▼                                 ▼
┌─────────────────┐               ┌─────────────────┐               ┌─────────────────┐
│Consultation Svc │               │ Prescription Svc│               │   EHR Service   │
│ ─────────────── │               │ ─────────────── │               │ ─────────────── │
│  • Video/Audio  │               │  • Create Rx    │               │  • Records      │
│  • Chat         │               │  • Templates    │               │  • Documents    │
│  • Recording    │               │  • Medicine DB  │               │  • Sharing      │
└────────┬────────┘               └────────┬────────┘               └────────┬────────┘
         │                                  │                                 │
         ▼                                  ▼                                 ▼
┌─────────────────┐               ┌─────────────────┐               ┌─────────────────┐
│  Order Service  │               │ Payment Service │               │Notification Svc │
│ ─────────────── │               │ ─────────────── │               │ ─────────────── │
│  • Medicines    │               │  • Processing   │               │  • Push/SMS     │
│  • Lab Tests    │               │  • Refunds      │               │  • Email        │
│  • Tracking     │               │  • Payouts      │               │  • In-App       │
└─────────────────┘               └─────────────────┘               └─────────────────┘
         │                                  │                                 │
         └──────────────────────────────────┼─────────────────────────────────┘
                                            │
                                 ┌──────────▼──────────┐
                                 │    Apache Kafka     │
                                 │   Event Backbone    │
                                 │  • Domain Events    │
                                 │  • Integration      │
                                 │  • Audit Trail      │
                                 └─────────────────────┘
```

### Service Boundaries (DDD Bounded Contexts)

> As per Microservice Guidelines: Align services with bounded contexts from Domain-Driven Design

| Bounded Context | Service | Core Domain |
|-----------------|---------|-------------|
| Identity & Access | `user-service` | User accounts, authentication, authorization |
| Doctor Management | `doctor-service` | Doctor profiles, qualifications, availability |
| Appointment | `appointment-service` | Booking, scheduling, calendar management |
| Consultation | `consultation-service` | Video/audio calls, chat, session management |
| Clinical | `ehr-service` | Health records, medical history, documents |
| Prescription | `prescription-service` | Rx creation, medicine database, templates |
| Commerce | `order-service` | Medicine/lab orders, cart, checkout |
| Payments | `payment-service` | Transactions, refunds, payouts |
| Notifications | `notification-service` | Multi-channel notifications |
| Content | `content-service` | Health articles, FAQs, blogs |
| Search | `search-service` | Doctor search, autocomplete, filters |
| Analytics | `analytics-service` | Business metrics, reporting |

---

## Domain Services

### 1. User Service

**Responsibility**: User registration, authentication, profile management

```
user-service/
├── src/main/java/com/healthapp/user/
│   ├── controller/
│   │   └── UserController.java          # REST endpoints
│   ├── service/
│   │   ├── UserService.java             # Business logic
│   │   └── AuthService.java             # JWT token management
│   ├── repository/
│   │   └── UserRepository.java          # R2DBC repository
│   ├── model/
│   │   ├── User.java                    # Entity
│   │   ├── UserRole.java                # Enum
│   │   └── UserStatus.java              # Enum
│   ├── dto/
│   │   ├── UserDto.java                 # Immutable DTO
│   │   ├── RegisterRequest.java         # Request record
│   │   └── LoginRequest.java            # Request record
│   ├── event/
│   │   └── UserEventPublisher.java      # Domain events
│   └── config/
│       └── SecurityConfig.java          # WebFlux security
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                    # Flyway migrations
└── src/test/
```

**Key Entities**:

```java
// ✅ Immutable Record (as per WebFlux Guidelines)
public record User(
    String id,
    String email,
    String phone,
    String passwordHash,
    UserRole role,           // PATIENT, DOCTOR, ADMIN, PHARMACY, LAB
    UserStatus status,       // ACTIVE, INACTIVE, PENDING_VERIFICATION
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

// ✅ Immutable DTO using Lombok
@Value
@Builder
public class UserDto {
    String id;
    String email;
    String phone;
    UserRole role;
    String displayName;
    String avatarUrl;
}
```

**API Endpoints**:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/users/register` | User registration |
| POST | `/api/v1/users/login` | Authentication |
| POST | `/api/v1/users/verify-otp` | OTP verification |
| GET | `/api/v1/users/me` | Get current user profile |
| PUT | `/api/v1/users/me` | Update profile |
| POST | `/api/v1/users/forgot-password` | Password reset initiation |
| POST | `/api/v1/users/reset-password` | Password reset |

**Events Published**:

```json
{
  "eventType": "user.registered.v1",
  "eventTime": "2026-01-30T10:00:00Z",
  "source": "user-service",
  "data": {
    "userId": "user-123",
    "email": "patient@example.com",
    "role": "PATIENT"
  }
}
```

---

### 2. Doctor Service

**Responsibility**: Doctor profiles, qualifications, search, availability

```
doctor-service/
├── src/main/java/com/healthapp/doctor/
│   ├── controller/
│   │   ├── DoctorController.java
│   │   └── DoctorSearchController.java
│   ├── service/
│   │   ├── DoctorService.java
│   │   ├── DoctorSearchService.java
│   │   └── AvailabilityService.java
│   ├── repository/
│   │   ├── DoctorRepository.java        # R2DBC
│   │   └── DoctorSearchRepository.java  # Elasticsearch
│   ├── model/
│   │   ├── Doctor.java
│   │   ├── Specialization.java
│   │   ├── Qualification.java
│   │   ├── Clinic.java
│   │   └── AvailabilitySlot.java
│   ├── dto/
│   │   ├── DoctorProfileDto.java
│   │   ├── DoctorSearchRequest.java
│   │   └── DoctorSearchResult.java
│   └── event/
│       └── DoctorEventPublisher.java
```

**Key Entities**:

```java
@Value
@Builder
public class DoctorProfile {
    String id;
    String userId;
    String fullName;
    String profilePictureUrl;
    List<Specialization> specializations;
    List<Qualification> qualifications;
    Integer experienceYears;
    String registrationNumber;      // Medical council registration
    String bio;
    List<Language> languagesSpoken;
    BigDecimal consultationFee;
    BigDecimal videoConsultationFee;
    Double rating;
    Integer reviewCount;
    Boolean isVerified;
    Boolean acceptingNewPatients;
    List<ClinicAssociation> clinics;
}

public record Specialization(
    String id,
    String name,               // e.g., "Cardiologist"
    String parentSpecialty     // e.g., "Internal Medicine"
) {}

public record AvailabilitySlot(
    String id,
    String doctorId,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Integer slotDurationMinutes,
    String clinicId,           // Nullable for video consultations
    ConsultationType type      // IN_PERSON, VIDEO, BOTH
) {}
```

**Search with Elasticsearch**:

```java
// ✅ Reactive Elasticsearch integration
@Component
public class DoctorSearchService {
    
    private final ReactiveElasticsearchClient esClient;
    
    public Flux<DoctorSearchResult> searchDoctors(DoctorSearchRequest request) {
        Query query = buildSearchQuery(request);
        
        return esClient.search(query, DoctorDocument.class)
            .map(this::toSearchResult)
            .take(request.getLimit());
    }
    
    private Query buildSearchQuery(DoctorSearchRequest request) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        
        // Specialty filter
        if (request.getSpecialty() != null) {
            boolQuery.must(MatchQuery.of(m -> 
                m.field("specializations.name")
                 .query(request.getSpecialty()))._toQuery());
        }
        
        // Location filter (geo distance)
        if (request.getLatitude() != null && request.getLongitude() != null) {
            boolQuery.filter(GeoDistanceQuery.of(g ->
                g.field("location")
                 .distance(request.getRadiusKm() + "km")
                 .location(GeoLocation.of(l -> 
                    l.latlon(ll -> ll.lat(request.getLatitude())
                                     .lon(request.getLongitude())))))._toQuery());
        }
        
        return Query.of(q -> q.bool(boolQuery.build()));
    }
}
```

**API Endpoints**:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/doctors/search` | Search doctors with filters |
| GET | `/api/v1/doctors/{id}` | Get doctor profile |
| GET | `/api/v1/doctors/{id}/availability` | Get available slots |
| GET | `/api/v1/doctors/{id}/reviews` | Get doctor reviews |
| PUT | `/api/v1/doctors/me/profile` | Update own profile (doctor) |
| PUT | `/api/v1/doctors/me/availability` | Set availability slots |
| GET | `/api/v1/specializations` | List all specializations |

---

### 3. Appointment Service

**Responsibility**: Booking appointments, scheduling, reminders

```java
@Value
@Builder
public class Appointment {
    String id;
    String patientId;
    String doctorId;
    String clinicId;              // Null for video consultations
    LocalDateTime scheduledAt;
    Integer durationMinutes;
    AppointmentType type;         // IN_PERSON, VIDEO, AUDIO, CHAT
    AppointmentStatus status;     // PENDING, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW
    BigDecimal fee;
    String cancellationReason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

public enum AppointmentStatus {
    PENDING_PAYMENT,
    PAYMENT_FAILED,
    CONFIRMED,
    DOCTOR_CANCELLED,
    PATIENT_CANCELLED,
    IN_PROGRESS,
    COMPLETED,
    NO_SHOW
}
```

**Booking Flow**:

```
┌──────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Patient │───▶│ Select Slot │───▶│ Confirm &   │───▶│   Payment   │
│  Search  │    │  Available  │    │ Book Slot   │    │  Processing │
└──────────┘    └─────────────┘    └─────────────┘    └──────┬──────┘
                                                              │
         ┌────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Confirmed  │───▶│  Reminder   │───▶│ Consultation│
│ Appointment │    │ Notifications│    │   Session   │
└─────────────┘    └─────────────┘    └─────────────┘
```

**API Endpoints**:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/appointments` | Book new appointment |
| GET | `/api/v1/appointments/{id}` | Get appointment details |
| GET | `/api/v1/appointments/patient/me` | Get patient's appointments |
| GET | `/api/v1/appointments/doctor/me` | Get doctor's appointments |
| PUT | `/api/v1/appointments/{id}/cancel` | Cancel appointment |
| PUT | `/api/v1/appointments/{id}/reschedule` | Reschedule appointment |
| PUT | `/api/v1/appointments/{id}/confirm` | Doctor confirms |

**Events**:

```
appointment.booked.v1      → Notify doctor, send confirmation to patient
appointment.confirmed.v1   → Send reminder schedules
appointment.cancelled.v1   → Process refund, notify parties
appointment.completed.v1   → Request review, trigger prescription flow
```

---

### 4. Consultation Service

**Responsibility**: Video/audio calls, chat, session management

> This is a critical real-time service requiring WebSocket and WebRTC integration

**Architecture**:

```
┌──────────────────────────────────────────────────────────────────┐
│                    CONSULTATION SERVICE                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │   Session   │    │   WebRTC    │    │    Chat     │          │
│  │  Manager    │    │   Gateway   │    │   Handler   │          │
│  │             │    │  (Signaling)│    │ (WebSocket) │          │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘          │
│         │                  │                   │                 │
│         ▼                  ▼                   ▼                 │
│  ┌─────────────────────────────────────────────────────┐        │
│  │              Redis (Session State, Pub/Sub)          │        │
│  └─────────────────────────────────────────────────────┘        │
│                                                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │  Recording  │    │   Media     │    │   Billing   │          │
│  │  Service    │    │   Server    │    │   Tracker   │          │
│  │  (Optional) │    │ (Twilio/Agora)   │             │          │
│  └─────────────┘    └─────────────┘    └─────────────┘          │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

**Key Components**:

```java
@Value
@Builder
public class ConsultationSession {
    String id;
    String appointmentId;
    String patientId;
    String doctorId;
    ConsultationType type;        // VIDEO, AUDIO, CHAT
    SessionStatus status;         // WAITING, ACTIVE, PAUSED, ENDED
    String roomId;                // Video room identifier
    LocalDateTime startedAt;
    LocalDateTime endedAt;
    Integer durationSeconds;
    String recordingUrl;          // If recording enabled
}

// WebSocket Handler for Chat
@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    
    private final ChatMessageRepository chatRepository;
    private final ReactiveRedisTemplate<String, ChatMessage> redisTemplate;
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = extractSessionId(session);
        
        Flux<WebSocketMessage> incoming = session.receive()
            .map(this::parseMessage)
            .flatMap(msg -> saveAndBroadcast(sessionId, msg))
            .map(msg -> session.textMessage(toJson(msg)));
            
        return session.send(incoming);
    }
}
```

**Video Integration (Twilio Example)**:

```java
@Service
public class VideoRoomService {
    
    private final WebClient twilioClient;
    
    public Mono<VideoRoom> createRoom(String sessionId) {
        return twilioClient.post()
            .uri("/v1/Rooms")
            .bodyValue(Map.of(
                "UniqueName", sessionId,
                "Type", "group",
                "MaxParticipants", 2,
                "RecordParticipantsOnConnect", false
            ))
            .retrieve()
            .bodyToMono(VideoRoom.class);
    }
    
    public Mono<String> generateAccessToken(String roomName, String identity) {
        // Generate Twilio access token for participant
        AccessToken token = new AccessToken.Builder(
            accountSid, apiKey, apiSecret)
            .identity(identity)
            .grant(new VideoGrant().setRoom(roomName))
            .build();
            
        return Mono.just(token.toJwt());
    }
}
```

---

### 5. Prescription Service

**Responsibility**: Create/manage prescriptions, medicine database

```java
@Value
@Builder
public class Prescription {
    String id;
    String consultationId;
    String patientId;
    String doctorId;
    LocalDate prescriptionDate;
    String diagnosis;
    List<PrescriptionItem> medicines;
    List<String> labTestsRecommended;
    String advice;
    String followUpDate;
    String digitalSignature;      // Doctor's digital signature
    PrescriptionStatus status;
}

@Value
@Builder  
public class PrescriptionItem {
    String medicineId;
    String medicineName;
    String genericName;
    String dosage;                // e.g., "500mg"
    String frequency;             // e.g., "Twice daily"
    String duration;              // e.g., "7 days"
    String timing;                // e.g., "After meals"
    Integer quantity;
    String instructions;          // Special instructions
}
```

**Medicine Database Integration**:

```java
// Reactive MongoDB for medicine database
@Repository
public interface MedicineRepository extends ReactiveMongoRepository<Medicine, String> {
    
    Flux<Medicine> findByNameContainingIgnoreCase(String name);
    
    Flux<Medicine> findByGenericName(String genericName);
    
    @Query("{ 'category': ?0, 'isAvailable': true }")
    Flux<Medicine> findAvailableByCategory(String category);
}
```

---

### 6. Order Service

**Responsibility**: Medicine orders, lab test bookings, cart management

```java
@Value
@Builder
public class Order {
    String id;
    String userId;
    OrderType type;               // MEDICINE, LAB_TEST
    List<OrderItem> items;
    String prescriptionId;        // For medicine orders
    String deliveryAddressId;
    BigDecimal subtotal;
    BigDecimal discount;
    BigDecimal deliveryFee;
    BigDecimal totalAmount;
    OrderStatus status;
    String partnerId;             // Pharmacy or Lab partner
    LocalDateTime createdAt;
    LocalDateTime estimatedDelivery;
}

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PROCESSING,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
```

---

### 7. Payment Service

**Responsibility**: Payment processing, refunds, doctor payouts

> **CRITICAL**: This is a high-security service with PCI-DSS compliance requirements

```java
@Value
@Builder
public class PaymentTransaction {
    String id;
    String orderId;               // Or appointmentId
    String userId;
    PaymentType type;             // APPOINTMENT, ORDER, SUBSCRIPTION
    BigDecimal amount;
    String currency;
    PaymentMethod method;         // CARD, UPI, WALLET, NET_BANKING
    PaymentStatus status;
    String gatewayTransactionId;
    String gatewayResponse;
    LocalDateTime createdAt;
    LocalDateTime completedAt;
}

@Value
@Builder
public class DoctorPayout {
    String id;
    String doctorId;
    BigDecimal amount;
    String currency;
    PayoutStatus status;
    String bankAccountId;
    LocalDate payoutDate;
    List<String> consultationIds;   // Consultations included
}
```

**Payment Flow with Saga Pattern**:

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Initiate  │───▶│   Reserve   │───▶│   Charge    │───▶│   Complete  │
│   Payment   │    │    Slot     │    │   Payment   │    │   Booking   │
└─────────────┘    └──────┬──────┘    └──────┬──────┘    └─────────────┘
                          │                  │
                          ▼                  ▼
                   ┌─────────────┐    ┌─────────────┐
                   │  Compensate │    │  Refund &   │
                   │ Release Slot│◀───│  Rollback   │
                   └─────────────┘    └─────────────┘
                   (On Failure)        (On Failure)
```

---

### 8. Notification Service

**Responsibility**: Multi-channel notifications (Push, SMS, Email, In-App)

```java
@Value
@Builder
public class Notification {
    String id;
    String userId;
    NotificationType type;
    NotificationChannel channel;    // PUSH, SMS, EMAIL, IN_APP
    String title;
    String body;
    Map<String, Object> data;       // Additional payload
    NotificationStatus status;
    LocalDateTime scheduledAt;
    LocalDateTime sentAt;
}

// Event-driven notification handling
@Component
public class NotificationEventHandler {
    
    @KafkaListener(topics = "appointment-events")
    public Mono<Void> handleAppointmentEvent(AppointmentEvent event) {
        return switch (event.getEventType()) {
            case "appointment.booked.v1" -> sendBookingConfirmation(event);
            case "appointment.confirmed.v1" -> scheduleReminders(event);
            case "appointment.cancelled.v1" -> sendCancellationNotice(event);
            default -> Mono.empty();
        };
    }
    
    private Mono<Void> scheduleReminders(AppointmentEvent event) {
        return Flux.just(
            createReminder(event, Duration.ofHours(24)),  // 24h before
            createReminder(event, Duration.ofHours(1)),   // 1h before
            createReminder(event, Duration.ofMinutes(15)) // 15min before
        )
        .flatMap(notificationRepository::save)
        .then();
    }
}
```

---

### 9. EHR Service (Electronic Health Records)

**Responsibility**: Patient health records, medical history, document storage

```java
@Value
@Builder
public class HealthRecord {
    String id;
    String patientId;
    RecordType type;              // CONSULTATION, LAB_REPORT, PRESCRIPTION, VITALS
    String title;
    String description;
    Map<String, Object> data;     // Flexible structure for different record types
    List<String> documentUrls;    // S3 URLs for PDFs, images
    String createdByDoctorId;
    LocalDateTime recordDate;
    LocalDateTime createdAt;
    List<String> tags;
    Boolean isShared;             // Shared with other doctors
}

public record VitalSigns(
    LocalDateTime recordedAt,
    Integer bloodPressureSystolic,
    Integer bloodPressureDiastolic,
    Integer heartRate,
    Double temperature,
    Integer respiratoryRate,
    Integer oxygenSaturation,
    Double weight,
    Double height,
    Double bmi
) {}
```

> **MongoDB** is recommended for EHR due to flexible schema requirements

---

## Event-Driven Architecture

### Event Backbone with Kafka

> As per Event-Driven Architecture Guidelines: Events as First-Class Citizens

**Topic Structure**:

```
healthcare-platform/
├── user-events              # user.registered, user.verified, user.updated
├── doctor-events            # doctor.registered, doctor.verified, doctor.updated
├── appointment-events       # appointment.booked, confirmed, cancelled, completed
├── consultation-events      # consultation.started, ended, chat.message
├── prescription-events      # prescription.created, prescription.shared
├── order-events             # order.created, shipped, delivered
├── payment-events           # payment.initiated, completed, refunded
├── notification-events      # notification.scheduled, sent, failed
└── audit-events             # All audit trail events
```

**Event Schema (Standard Format)**:

```json
{
  "eventId": "evt-uuid-v4",
  "eventType": "appointment.booked.v1",
  "eventTime": "2026-01-30T10:00:00Z",
  "source": "appointment-service",
  "subject": "appointment-12345",
  "dataVersion": "1.0",
  "data": {
    "appointmentId": "apt-12345",
    "patientId": "user-456",
    "doctorId": "doc-789",
    "scheduledAt": "2026-02-01T14:00:00Z",
    "type": "VIDEO",
    "fee": 500.00
  },
  "metadata": {
    "correlationId": "req-abc123",
    "causationId": "cmd-xyz789",
    "userId": "user-456",
    "tenantId": "tenant-001"
  }
}
```

### Saga Orchestration for Complex Workflows

**Appointment Booking Saga**:

```java
@Component
public class AppointmentBookingSaga {
    
    private final SagaOrchestrator orchestrator;
    
    public Mono<Appointment> execute(BookAppointmentCommand command) {
        return orchestrator.start("appointment-booking", command.getCorrelationId())
            .step("validate-slot")
                .execute(() -> appointmentService.validateSlot(command))
                .compensate(() -> Mono.empty())
            .step("reserve-slot")
                .execute(() -> appointmentService.reserveSlot(command))
                .compensate(data -> appointmentService.releaseSlot(data.getSlotId()))
            .step("process-payment")
                .execute(() -> paymentService.processPayment(command))
                .compensate(data -> paymentService.refund(data.getPaymentId()))
            .step("confirm-appointment")
                .execute(() -> appointmentService.confirm(command))
                .compensate(data -> appointmentService.cancel(data.getAppointmentId()))
            .step("send-notifications")
                .execute(() -> notificationService.sendBookingConfirmation(command))
                .compensate(() -> Mono.empty())  // Non-critical, no compensation
            .build()
            .execute();
    }
}
```

### Outbox Pattern for Reliable Event Publishing

```sql
-- Outbox table for transactional event publishing
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    published_at TIMESTAMP NULL,
    retry_count INT DEFAULT 0
);

CREATE INDEX idx_outbox_unpublished ON outbox_events(created_at) 
    WHERE published_at IS NULL;
```

```java
// Outbox publisher using Debezium CDC or polling
@Scheduled(fixedDelay = 1000)
public Flux<Void> publishPendingEvents() {
    return outboxRepository.findUnpublished(100)
        .flatMap(event -> kafkaTemplate.send(event.getEventType(), event.getPayload())
            .then(outboxRepository.markPublished(event.getId())))
        .then();
}
```

---

## API Gateway & Security

### Spring Cloud Gateway Configuration

```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - RewritePath=/api/v1/users/(?<path>.*), /$\{path}
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                
        - id: doctor-service
          uri: lb://doctor-service
          predicates:
            - Path=/api/v1/doctors/**
          filters:
            - RewritePath=/api/v1/doctors/(?<path>.*), /$\{path}
            
        - id: appointment-service
          uri: lb://appointment-service
          predicates:
            - Path=/api/v1/appointments/**
          filters:
            - name: CircuitBreaker
              args:
                name: appointmentCircuitBreaker
                fallbackUri: forward:/fallback/appointments
```

### JWT Authentication

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
                .pathMatchers("/api/v1/doctors/search/**").permitAll()
                .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .pathMatchers("/api/v1/doctors/me/**").hasRole("DOCTOR")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

### Rate Limiting

```java
@Bean
public KeyResolver userKeyResolver() {
    return exchange -> Mono.just(
        exchange.getRequest().getHeaders().getFirst("X-User-Id")
    );
}

// Per-endpoint rate limiting
@GetMapping("/doctors/search")
@RateLimiter(name = "doctorSearch", fallbackMethod = "searchFallback")
public Flux<DoctorDto> searchDoctors(DoctorSearchRequest request) {
    return doctorSearchService.search(request);
}
```

---

## Database Design

### PostgreSQL Schema (Core Entities)

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING_VERIFICATION',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Doctors table
CREATE TABLE doctors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    full_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100) UNIQUE,
    experience_years INT,
    consultation_fee DECIMAL(10,2),
    video_consultation_fee DECIMAL(10,2),
    rating DECIMAL(3,2) DEFAULT 0,
    review_count INT DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    is_accepting_patients BOOLEAN DEFAULT TRUE,
    profile_data JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Doctor specializations (many-to-many)
CREATE TABLE doctor_specializations (
    doctor_id UUID REFERENCES doctors(id),
    specialization_id UUID REFERENCES specializations(id),
    PRIMARY KEY (doctor_id, specialization_id)
);

-- Availability slots
CREATE TABLE availability_slots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID REFERENCES doctors(id),
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT DEFAULT 15,
    consultation_type VARCHAR(50),
    clinic_id UUID REFERENCES clinics(id),
    is_active BOOLEAN DEFAULT TRUE
);

-- Appointments
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID REFERENCES users(id),
    doctor_id UUID REFERENCES doctors(id),
    clinic_id UUID REFERENCES clinics(id),
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INT DEFAULT 15,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING_PAYMENT',
    fee DECIMAL(10,2),
    payment_id UUID,
    cancellation_reason TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for common queries
CREATE INDEX idx_appointments_patient ON appointments(patient_id, scheduled_at);
CREATE INDEX idx_appointments_doctor ON appointments(doctor_id, scheduled_at);
CREATE INDEX idx_appointments_status ON appointments(status, scheduled_at);
CREATE INDEX idx_doctors_specialty ON doctor_specializations(specialization_id);
CREATE INDEX idx_availability_doctor ON availability_slots(doctor_id, day_of_week);
```

### MongoDB Schema (Health Records)

```javascript
// Health Records Collection
{
  "_id": ObjectId("..."),
  "patientId": "user-uuid",
  "type": "LAB_REPORT",
  "title": "Complete Blood Count",
  "recordDate": ISODate("2026-01-15"),
  "data": {
    "hemoglobin": { "value": 14.5, "unit": "g/dL", "reference": "13.5-17.5" },
    "wbc": { "value": 7500, "unit": "cells/mcL", "reference": "4500-11000" },
    "platelets": { "value": 250000, "unit": "cells/mcL", "reference": "150000-400000" }
  },
  "documents": [
    { "type": "PDF", "url": "s3://health-records/...", "name": "report.pdf" }
  ],
  "createdByDoctorId": "doctor-uuid",
  "tags": ["blood-test", "routine-checkup"],
  "isShared": false,
  "createdAt": ISODate("2026-01-15"),
  "updatedAt": ISODate("2026-01-15")
}

// Indexes
db.healthRecords.createIndex({ "patientId": 1, "recordDate": -1 });
db.healthRecords.createIndex({ "patientId": 1, "type": 1 });
db.healthRecords.createIndex({ "tags": 1 });
```

### Elasticsearch Index (Doctor Search)

```json
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 2,
    "analysis": {
      "analyzer": {
        "doctor_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "asciifolding", "edge_ngram_filter"]
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
      "doctorId": { "type": "keyword" },
      "fullName": { 
        "type": "text", 
        "analyzer": "doctor_analyzer",
        "search_analyzer": "standard"
      },
      "specializations": {
        "type": "nested",
        "properties": {
          "id": { "type": "keyword" },
          "name": { "type": "text" }
        }
      },
      "location": { "type": "geo_point" },
      "city": { "type": "keyword" },
      "consultationFee": { "type": "float" },
      "rating": { "type": "float" },
      "reviewCount": { "type": "integer" },
      "isVerified": { "type": "boolean" },
      "isAcceptingPatients": { "type": "boolean" },
      "experienceYears": { "type": "integer" },
      "languagesSpoken": { "type": "keyword" },
      "availableForVideo": { "type": "boolean" }
    }
  }
}
```

---

## Frontend Applications

### Patient Web App (Next.js 14+)

```
patient-web/
├── app/
│   ├── layout.tsx
│   ├── page.tsx                      # Landing page
│   ├── (auth)/
│   │   ├── login/page.tsx
│   │   └── register/page.tsx
│   ├── doctors/
│   │   ├── page.tsx                  # Doctor listing
│   │   ├── [id]/page.tsx             # Doctor profile
│   │   └── [id]/book/page.tsx        # Booking flow
│   ├── appointments/
│   │   ├── page.tsx                  # My appointments
│   │   └── [id]/page.tsx             # Appointment details
│   ├── consultation/
│   │   └── [id]/page.tsx             # Video/Chat room
│   ├── records/
│   │   └── page.tsx                  # Health records
│   ├── pharmacy/
│   │   ├── page.tsx                  # Order medicines
│   │   └── orders/page.tsx           # Order history
│   └── profile/
│       └── page.tsx                  # User profile
├── components/
│   ├── ui/                           # Shadcn/UI components
│   ├── doctors/
│   │   ├── DoctorCard.tsx
│   │   ├── DoctorSearch.tsx
│   │   └── BookingCalendar.tsx
│   ├── consultation/
│   │   ├── VideoRoom.tsx
│   │   └── ChatPanel.tsx
│   └── common/
│       ├── Header.tsx
│       └── Footer.tsx
├── lib/
│   ├── api.ts                        # API client
│   ├── auth.ts                       # Auth utilities
│   └── utils.ts
└── hooks/
    ├── useAuth.ts
    ├── useDoctors.ts
    └── useAppointments.ts
```

### Doctor Dashboard (React + TypeScript)

```
doctor-dashboard/
├── src/
│   ├── pages/
│   │   ├── Dashboard.tsx             # Overview, today's appointments
│   │   ├── Appointments.tsx          # Calendar view
│   │   ├── Patients.tsx              # Patient list
│   │   ├── Consultation.tsx          # Active consultation room
│   │   ├── Prescriptions.tsx         # Write/view prescriptions
│   │   ├── Earnings.tsx              # Payout history
│   │   └── Settings.tsx              # Profile, availability
│   ├── components/
│   │   ├── AppointmentList.tsx
│   │   ├── PatientHistory.tsx
│   │   ├── PrescriptionBuilder.tsx
│   │   ├── VideoConsultation.tsx
│   │   └── AvailabilityManager.tsx
│   ├── services/
│   │   ├── api.ts
│   │   └── websocket.ts
│   └── store/
│       └── slices/
└── package.json
```

---

## Infrastructure & DevOps

### Kubernetes Deployment Architecture

```yaml
# Namespace structure
namespaces:
  - healthcare-prod
  - healthcare-staging
  - healthcare-dev
  - monitoring
  - logging

# Per-service deployment template
apiVersion: apps/v1
kind: Deployment
metadata:
  name: appointment-service
  namespace: healthcare-prod
spec:
  replicas: 3
  selector:
    matchLabels:
      app: appointment-service
  template:
    metadata:
      labels:
        app: appointment-service
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
    spec:
      containers:
        - name: appointment-service
          image: healthcare/appointment-service:v1.2.0
          ports:
            - containerPort: 8080
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: url
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: appointment-service
  namespace: healthcare-prod
spec:
  selector:
    app: appointment-service
  ports:
    - port: 80
      targetPort: 8080
```

### CI/CD Pipeline (GitHub Actions)

```yaml
name: Build and Deploy

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Build with Gradle
        run: ./gradlew build
        
      - name: Run Tests
        run: ./gradlew test
        
      - name: Build Docker Image
        run: |
          docker build -t healthcare/${{ github.event.repository.name }}:${{ github.sha }} .
          
      - name: Push to Registry
        run: |
          docker push healthcare/${{ github.event.repository.name }}:${{ github.sha }}

  deploy:
    needs: build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Kubernetes
        run: |
          kubectl set image deployment/${{ github.event.repository.name }} \
            ${{ github.event.repository.name }}=healthcare/${{ github.event.repository.name }}:${{ github.sha }}
```

### Monitoring Stack

```yaml
# Prometheus + Grafana + Alertmanager
monitoring:
  prometheus:
    scrape_configs:
      - job_name: 'spring-actuator'
        metrics_path: '/actuator/prometheus'
        kubernetes_sd_configs:
          - role: pod
        relabel_configs:
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
            action: keep
            regex: true
            
  grafana:
    dashboards:
      - jvm-metrics
      - api-gateway-metrics
      - appointment-service-metrics
      - kafka-metrics
      
  alertmanager:
    receivers:
      - name: 'slack-notifications'
        slack_configs:
          - channel: '#healthcare-alerts'
```

---

## Performance & Scalability

### Caching Strategy

> As per Performance Guidelines: Use appropriate caching layers

```java
// Level 1: In-memory cache for small, frequently accessed data
@Cacheable(cacheNames = "specializations", key = "'all'")
public Flux<Specialization> getAllSpecializations() {
    return specializationRepository.findAll();
}

// Level 2: Redis for distributed caching
@Service
public class DoctorCacheService {
    
    private final ReactiveRedisTemplate<String, DoctorDto> redisTemplate;
    
    public Mono<DoctorDto> getCachedDoctor(String doctorId) {
        String cacheKey = "doctor:" + doctorId;
        
        return redisTemplate.opsForValue().get(cacheKey)
            .switchIfEmpty(
                doctorRepository.findById(doctorId)
                    .map(doctorMapper::toDto)
                    .flatMap(dto -> 
                        redisTemplate.opsForValue()
                            .set(cacheKey, dto, Duration.ofHours(1))
                            .thenReturn(dto))
            );
    }
}
```

### Database Optimization

```java
// ✅ Use projections for list queries
@Query("""
    SELECT new com.healthapp.dto.AppointmentSummaryDto(
        a.id, a.scheduledAt, a.type, a.status, 
        d.fullName, d.profilePictureUrl
    ) 
    FROM Appointment a 
    JOIN Doctor d ON a.doctorId = d.id 
    WHERE a.patientId = :patientId 
    ORDER BY a.scheduledAt DESC
    """)
Flux<AppointmentSummaryDto> findPatientAppointmentSummaries(String patientId, Pageable pageable);

// ✅ Batch updates instead of individual saves
@Modifying
@Query("UPDATE Appointment a SET a.status = 'REMINDED' WHERE a.id IN :ids")
Mono<Integer> markAsReminded(List<String> ids);
```

### Horizontal Scaling Guidelines

| Service | Scaling Strategy | Key Metrics |
|---------|------------------|-------------|
| API Gateway | HPA based on CPU/Memory | Request rate, latency |
| User Service | HPA based on CPU | Auth requests/sec |
| Doctor Search | HPA + Elasticsearch replicas | Search latency, QPS |
| Appointment Service | HPA based on CPU | Booking rate |
| Consultation Service | HPA based on concurrent connections | Active sessions |
| Payment Service | HPA (limited) + Security | Transaction rate |
| Notification Service | HPA + Kafka partitions | Queue depth, send rate |

---

## Implementation Phases

### Phase 1: Foundation (Weeks 1-6)

**Goal**: Core infrastructure and basic user flows

- [ ] Set up Kubernetes cluster and CI/CD pipelines
- [ ] Implement User Service (registration, authentication)
- [ ] Implement Doctor Service (profiles, basic search)
- [ ] Set up API Gateway with authentication
- [ ] Set up Kafka cluster and basic event publishing
- [ ] Create basic Patient Web App (landing, search, login)
- [ ] Create basic Doctor Dashboard (login, profile)

**Deliverables**:
- Users can register and login
- Patients can search and view doctor profiles
- Doctors can create and manage profiles

### Phase 2: Appointment Booking (Weeks 7-10)

**Goal**: Complete appointment booking flow

- [ ] Implement Appointment Service
- [ ] Implement Payment Service integration
- [ ] Doctor availability management
- [ ] Booking flow in Patient Web App
- [ ] Appointment management in Doctor Dashboard
- [ ] Notification Service (email, SMS for bookings)
- [ ] Implement Saga pattern for booking flow

**Deliverables**:
- Patients can book appointments with payment
- Doctors can manage their calendar
- Both receive notifications

### Phase 3: Teleconsultation (Weeks 11-14)

**Goal**: Video/audio/chat consultation

- [ ] Implement Consultation Service
- [ ] Integrate video provider (Twilio/Agora)
- [ ] WebSocket chat implementation
- [ ] Video consultation room in Patient App
- [ ] Consultation room in Doctor Dashboard
- [ ] Session recording (optional, with consent)

**Deliverables**:
- Complete video consultation flow
- In-session chat
- Session history

### Phase 4: Clinical Features (Weeks 15-18)

**Goal**: Prescriptions and health records

- [ ] Implement Prescription Service
- [ ] Medicine database integration
- [ ] Prescription builder in Doctor Dashboard
- [ ] Implement EHR Service
- [ ] Health records viewer in Patient App
- [ ] Document upload (lab reports, etc.)

**Deliverables**:
- Doctors can write digital prescriptions
- Patients can view prescriptions and health records
- Document management

### Phase 5: Commerce (Weeks 19-22)

**Goal**: Medicine ordering and lab test booking

- [ ] Implement Order Service
- [ ] Pharmacy partner integration
- [ ] Medicine ordering in Patient App
- [ ] Lab partner integration
- [ ] Lab test booking flow
- [ ] Order tracking

**Deliverables**:
- Patients can order medicines
- Lab test booking with home collection
- Order tracking

### Phase 6: Enhancement & Scale (Weeks 23-26)

**Goal**: Polish, optimization, and additional features

- [ ] Advanced doctor search (filters, recommendations)
- [ ] Doctor reviews and ratings
- [ ] Health articles/content service
- [ ] Mobile app development
- [ ] Performance optimization
- [ ] Security audit
- [ ] Load testing and scaling validation

**Deliverables**:
- Production-ready platform
- Mobile apps
- Content management

---

## Appendix

### A. Service Communication Matrix

| From ↓ To → | User | Doctor | Appointment | Consultation | Payment | Notification |
|-------------|------|--------|-------------|--------------|---------|--------------|
| **User** | - | Sync | Sync | - | - | Event |
| **Doctor** | Sync | - | Sync | - | - | Event |
| **Appointment** | Sync | Sync | - | Event | Sync | Event |
| **Consultation** | Sync | Sync | Sync | - | Event | Event |
| **Payment** | Sync | - | Event | - | - | Event |

### B. Event Catalog

| Event Type | Publisher | Subscribers |
|------------|-----------|-------------|
| `user.registered.v1` | User Service | Notification, Analytics |
| `doctor.verified.v1` | Doctor Service | Search, Notification |
| `appointment.booked.v1` | Appointment Service | Payment, Notification, Analytics |
| `appointment.completed.v1` | Appointment Service | EHR, Payment (payout), Notification |
| `consultation.started.v1` | Consultation Service | Analytics, Billing |
| `payment.completed.v1` | Payment Service | Appointment, Order, Notification |
| `prescription.created.v1` | Prescription Service | EHR, Notification, Order |

### C. API Versioning Strategy

```
/api/v1/...   # Current stable version
/api/v2/...   # Next version (when breaking changes needed)

Headers:
  Accept: application/vnd.healthcare.v1+json
```

### D. Security Checklist

- [ ] All APIs require authentication (except public endpoints)
- [ ] JWT tokens with short expiry (15 min) + refresh tokens
- [ ] Rate limiting on all endpoints
- [ ] Input validation and sanitization
- [ ] SQL injection prevention (parameterized queries)
- [ ] XSS prevention (output encoding)
- [ ] HTTPS everywhere
- [ ] Secrets in Vault, never in code
- [ ] Audit logging for sensitive operations
- [ ] PCI-DSS compliance for payment handling
- [ ] HIPAA compliance for health data
- [ ] Regular security audits and penetration testing

---

## References

- [WebFlux Guidelines](/.copilot/webflux-guidelines.md)
- [Microservice Guidelines](/.copilot/microservice-guidelines.md)
- [Event-Driven Architecture Guidelines](/.copilot/event-driven-architecture-guidelines.md)
- [Performance Guidelines](/.copilot/performance-guidelines.md)
- [Coding Guidelines](/.copilot/coding-guidelines.md)

---

*Document Version: 1.0*  
*Last Updated: January 30, 2026*  
*Author: Architecture Team*

# Phase 3: Teleconsultation Feature - Implementation Status

## Overview
Phase 3 implements the core teleconsultation service with video/audio calling via Twilio, real-time chat via WebSocket, session management, and post-consultation feedback.

## ✅ Implementation Complete

### Module Structure
- **consultation-service** module added to parent POM
- Port: **8087**
- Package: `com.healthapp.consultation`

### Database Schema
Located in: `consultation-service/src/main/resources/db/migration/V1__create_consultation_tables.sql`

| Table | Purpose |
|-------|---------|
| `consultation_sessions` | Main session tracking (status, timing, room details, pricing) |
| `session_participants` | Tracks patient/doctor join times and connection quality |
| `session_events` | Event log for analytics (joins, leaves, connections) |
| `consultation_pricing` | Per-doctor pricing by mode (FLAT, PER_MINUTE, TIERED) |
| `consultation_feedback` | Post-consultation ratings and reviews |

**Note:** Chat messages are stored in MongoDB (collection: `chat_messages`)

### Domain Enums
| Enum | Values |
|------|--------|
| `ConsultationMode` | VIDEO, AUDIO, CHAT |
| `SessionStatus` | SCHEDULED, WAITING, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED, NO_SHOW, FAILED |
| `ParticipantType` | PATIENT, DOCTOR |
| `PricingType` | FLAT, PER_MINUTE, TIERED |
| `MessageType` | TEXT, IMAGE, FILE, SYSTEM |
| `MessageStatus` | SENT, DELIVERED, READ |

### Domain Entities
| Entity | Storage | Description |
|--------|---------|-------------|
| `ConsultationSession` | PostgreSQL (R2DBC) | Core session entity with full lifecycle tracking |
| `SessionParticipant` | PostgreSQL (R2DBC) | Participant join/leave tracking |
| `SessionEvent` | PostgreSQL (R2DBC) | Analytics events |
| `ConsultationPricing` | PostgreSQL (R2DBC) | Doctor pricing configuration |
| `ConsultationFeedback` | PostgreSQL (R2DBC) | Patient ratings and feedback |
| `ChatMessage` | MongoDB | Real-time chat message storage |

### Repositories
- `ConsultationSessionRepository` - Session CRUD with status queries
- `SessionParticipantRepository` - Participant tracking
- `SessionEventRepository` - Event logging
- `ConsultationPricingRepository` - Pricing lookups by doctor/mode
- `ConsultationFeedbackRepository` - Feedback submission and retrieval
- `ChatMessageRepository` - MongoDB reactive repository for chat

### DTOs
| Request DTOs | Response DTOs |
|--------------|---------------|
| `CreateSessionRequest` | `SessionResponse` |
| `JoinSessionRequest` | `VideoTokenResponse` |
| `SendMessageRequest` | `ChatMessageResponse` |
| `SubmitFeedbackRequest` | |

### Configuration Classes
| Class | Purpose |
|-------|---------|
| `TwilioConfig` | Twilio credentials, room settings, token TTL |
| `WebSocketConfig` | STOMP endpoint configuration at `/ws/consultation` |
| `KafkaConfig` | Producer for consultation events |

### Services
| Service | Responsibility |
|---------|----------------|
| `TwilioVideoService` | Room creation, token generation, room management |
| `ConsultationSessionService` | Full session lifecycle (create, join, leave, end) |
| `ChatService` | Message sending, history, read receipts |
| `FeedbackService` | Submit feedback, calculate doctor ratings |

### Controllers
| Controller | Endpoints |
|------------|-----------|
| `ConsultationController` | `/api/v1/consultations/**` - Session CRUD, join/leave/end |
| `ChatController` | `/api/v1/chat/**` - Message history, send via REST |
| `ChatWebSocketController` | STOMP handlers for real-time messaging |
| `FeedbackController` | `/api/v1/feedback/**` - Submit and retrieve feedback |
| `TwilioWebhookController` | `/api/v1/consultations/webhook/**` - Status callbacks |

### Events
| Class | Purpose |
|-------|---------|
| `ConsultationEvent` | Event model with types: SESSION_CREATED, SESSION_STARTED, SESSION_ENDED, PARTICIPANT_JOINED, PARTICIPANT_LEFT, FEEDBACK_SUBMITTED |
| `ConsultationEventPublisher` | Publishes to Kafka topic `consultation-events` |

### Consumers
| Class | Listens To | Action |
|-------|------------|--------|
| `AppointmentEventConsumer` | `appointment-events` | Auto-creates consultation session when VIDEO/AUDIO appointment is confirmed |

### Docker Configuration
- **Dockerfile** created for consultation-service (port 8087)
- **docker-compose.yaml** updated:
  - Added MongoDB 7.0 with `mongodb_data` volume
  - Added `consultation_db` to PostgreSQL databases
  - Added consultation-service container
  - Updated API gateway dependencies and environment

### API Gateway Routes
Added to `api-gateway/src/main/resources/application.yml`:
- `consultation-websocket` - WebSocket at `/ws/consultation/**`
- `consultation-webhook` - Twilio callbacks (no auth)
- `consultations-protected` - Main consultation endpoints (JWT auth)
- `chat-protected` - Chat REST endpoints (JWT auth)
- `feedback-protected` - Feedback endpoints (JWT auth)

## Build Status

| Service | Status |
|---------|--------|
| Healthcare Common | ✅ SUCCESS |
| User Service | ✅ SUCCESS |
| API Gateway | ✅ SUCCESS |
| Appointment Service | ✅ SUCCESS |
| Payment Service | ✅ SUCCESS |
| Notification Service | ✅ SUCCESS |
| **Consultation Service** | ✅ SUCCESS |

**Note:** Doctor Service and Search Service have pre-existing compilation issues unrelated to Phase 3.

## Key Features Implemented

### 1. Twilio Video Integration
- Room creation with configurable type (peer-to-peer or group)
- Access token generation for participants
- Room status management (in-progress, completed)
- Status callback webhook support
- **Test mode** support (simulated rooms when account SID starts with "test_")

### 2. Real-Time Chat
- WebSocket with STOMP over SockJS
- Message types: TEXT, IMAGE, FILE, SYSTEM
- Message status tracking (SENT, DELIVERED, READ)
- MongoDB storage for scalable chat history
- Typing indicators support
- Mark messages as read functionality

### 3. Session Management
- Full lifecycle: SCHEDULED → WAITING → IN_PROGRESS → COMPLETED
- Participant tracking with join/leave times
- Connection quality monitoring
- Automatic session creation from confirmed appointments
- Duration calculation and billing support

### 4. Pricing & Billing
- Three pricing types: FLAT, PER_MINUTE, TIERED
- Per-doctor, per-mode pricing configuration
- Estimated vs actual cost calculation
- Integration with payment service via events

### 5. Feedback System
- 5-star rating with review text
- Anonymous feedback option
- Doctor rating aggregation
- Recommendation tracking

## API Endpoints Summary

### Consultation Endpoints (`/api/v1/consultations`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create new session |
| GET | `/{id}` | Get session details |
| POST | `/{id}/join` | Join session, get video token |
| POST | `/{id}/leave` | Leave session |
| POST | `/{id}/end` | End session |
| GET | `/appointment/{appointmentId}` | Get session by appointment |

### Chat Endpoints (`/api/v1/chat`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/sessions/{sessionId}/messages` | Get chat history |
| POST | `/sessions/{sessionId}/messages` | Send message (REST) |
| PUT | `/sessions/{sessionId}/messages/{messageId}/read` | Mark as read |

### WebSocket Endpoints (`/ws/consultation`)
| Destination | Purpose |
|-------------|---------|
| `/app/chat.send/{sessionId}` | Send message |
| `/app/chat.typing/{sessionId}` | Typing indicator |
| `/topic/session/{sessionId}` | Subscribe to messages |

### Feedback Endpoints (`/api/v1/feedback`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Submit feedback |
| GET | `/sessions/{sessionId}` | Get session feedback |
| GET | `/doctors/{doctorId}` | Get doctor's feedback |
| GET | `/doctors/{doctorId}/summary` | Get doctor rating summary |

## Configuration Requirements

### Environment Variables
```properties
# Twilio (required for video)
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_API_KEY_SID=your_api_key_sid
TWILIO_API_KEY_SECRET=your_api_key_secret

# Databases
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
MONGODB_HOST=localhost
MONGODB_PORT=27017

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Next Steps (Phase 4)
- Medical Records Service
- Prescription Management
- Health Vitals Tracking
- Lab Reports Integration

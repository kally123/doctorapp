# Phase 3: Teleconsultation - Detailed Implementation Plan

## Phase Overview

| Attribute | Details |
|-----------|---------|
| **Duration** | 4 Weeks |
| **Start Date** | _Phase 2 End Date + 1 day_ |
| **End Date** | _Start Date + 4 weeks_ |
| **Team Size** | 10-12 members |
| **Goal** | Complete video/audio consultation with real-time chat and session management |

---

## Phase 3 Objectives

1. ✅ Build Consultation Service with video room management
2. ✅ Integrate video SDK (Twilio/Agora) for HD video calls
3. ✅ Implement audio-only consultation mode
4. ✅ Build real-time WebSocket chat during consultations
5. ✅ Implement session tracking and duration monitoring
6. ✅ Create waiting room experience for patients
7. ✅ Build consultation UI for both patient and doctor apps
8. ✅ Implement post-consultation feedback and ratings

---

## Prerequisites from Phase 2

Before starting Phase 3, ensure the following are complete:

| Prerequisite | Status |
|--------------|--------|
| Appointment Service deployed and functional | ⬜ |
| Payment Service deployed and functional | ⬜ |
| Complete booking flow working end-to-end | ⬜ |
| Notification Service sending emails/SMS | ⬜ |
| Patient Web App with booking capabilities | ⬜ |
| Doctor Dashboard with appointment management | ⬜ |
| Kafka event streaming operational | ⬜ |

---

## Team Allocation for Phase 3

| Role | Name | Focus Area |
|------|------|------------|
| Tech Lead | _TBD_ | Architecture, WebRTC, code reviews |
| Backend 1 | _TBD_ | Consultation Service, Video SDK integration |
| Backend 2 | _TBD_ | WebSocket Chat, Session Management |
| Backend 3 | _TBD_ | Recording, Analytics, Events |
| Frontend 1 | _TBD_ | Patient Web App - Video Room |
| Frontend 2 | _TBD_ | Doctor Dashboard - Consultation Room |
| Mobile 1 | _TBD_ | Mobile Video Integration (if applicable) |
| DevOps | _TBD_ | TURN/STUN servers, Media infrastructure |
| QA 1 | _TBD_ | Testing - Video/Audio quality |
| QA 2 | _TBD_ | Testing - Chat, Session flows |

---

## Architecture Overview

### Teleconsultation Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         TELECONSULTATION ARCHITECTURE                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐         ┌──────────────┐         ┌──────────────┐        │
│  │   Patient    │◄───────►│  Twilio/     │◄───────►│    Doctor    │        │
│  │   Browser    │  WebRTC │  Agora       │  WebRTC │   Browser    │        │
│  │   (Video)    │         │  Media SFU   │         │   (Video)    │        │
│  └──────┬───────┘         └──────────────┘         └──────┬───────┘        │
│         │                                                  │                │
│         │ WebSocket                              WebSocket │                │
│         │                                                  │                │
│         ▼                                                  ▼                │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                    Consultation Service                          │       │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │       │
│  │  │ Session Mgr │  │ Chat Handler│  │ Token Generation        │  │       │
│  │  │ (WebFlux)   │  │ (WebSocket) │  │ (Twilio/Agora SDK)      │  │       │
│  │  └──────┬──────┘  └──────┬──────┘  └─────────────────────────┘  │       │
│  │         │                │                                       │       │
│  │         ▼                ▼                                       │       │
│  │  ┌─────────────┐  ┌─────────────┐                               │       │
│  │  │ PostgreSQL  │  │  MongoDB    │                               │       │
│  │  │ (Sessions)  │  │  (Messages) │                               │       │
│  │  └─────────────┘  └─────────────┘                               │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                           │                                                 │
│                           ▼                                                 │
│                    ┌─────────────┐                                         │
│                    │   Kafka     │                                         │
│                    │  (Events)   │                                         │
│                    └─────────────┘                                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Technology Choices

| Component | Primary Choice | Alternative | Rationale |
|-----------|---------------|-------------|-----------|
| **Video SDK** | Twilio Video | Agora | Reliable, good documentation, HIPAA compliant |
| **WebRTC** | Twilio SDK | Agora SDK | Handles NAT traversal, ICE negotiation |
| **Real-time Chat** | Spring WebSocket + STOMP | Socket.io | Native Spring integration |
| **Chat Storage** | MongoDB | PostgreSQL JSONB | Flexible schema for messages |
| **Session State** | PostgreSQL + Redis | PostgreSQL only | Fast lookups, persistence |

---

## Sprint Breakdown

### Sprint 6 (Week 11-12): Consultation Service Core & Video Integration

**Sprint Goal**: Basic video consultation working between patient and doctor with session management.

---

#### DevOps Tasks - Sprint 6

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| D6.1 | Twilio Account Setup | Create Twilio account, enable Video API, configure credentials | DevOps | 4 | P0 | API credentials stored in Vault |
| D6.2 | TURN Server Configuration | Configure Twilio Network Traversal or deploy coturn | DevOps | 8 | P0 | TURN server accessible |
| D6.3 | WebSocket Load Balancer | Configure sticky sessions for WebSocket connections | DevOps | 8 | P0 | WebSocket connections stable |
| D6.4 | MongoDB Cluster (Chat) | Deploy MongoDB replica set for chat messages | DevOps | 4 | P0 | MongoDB accessible, credentials in Vault |
| D6.5 | Media Recording Storage | Configure S3 bucket for consultation recordings | DevOps | 4 | P2 | S3 bucket with encryption enabled |
| D6.6 | Monitoring Dashboards | Create Grafana dashboards for video metrics | DevOps | 8 | P1 | Dashboard showing call quality metrics |

**DevOps Subtasks:**

<details>
<summary><strong>D6.1 - Twilio Account Setup (Detailed Steps)</strong></summary>

```markdown
## Subtasks for D6.1

1. [ ] Create Twilio account (or use existing)
2. [ ] Enable Twilio Video API
3. [ ] Generate API Key SID and Secret
4. [ ] Configure allowed origins for CORS
5. [ ] Set up HIPAA compliance settings (if required)
6. [ ] Store credentials in HashiCorp Vault:
   - TWILIO_ACCOUNT_SID
   - TWILIO_API_KEY_SID
   - TWILIO_API_KEY_SECRET
7. [ ] Configure webhook URLs for status callbacks
8. [ ] Test with Twilio Video Quickstart
9. [ ] Document integration details
```
</details>

<details>
<summary><strong>D6.3 - WebSocket Load Balancer (Detailed Steps)</strong></summary>

```markdown
## Subtasks for D6.3

1. [ ] Configure Kubernetes Ingress for WebSocket
2. [ ] Enable sticky sessions (session affinity)
3. [ ] Configure connection timeouts (idle: 300s)
4. [ ] Set up health checks for WebSocket endpoints
5. [ ] Configure SSL/TLS termination
6. [ ] Test WebSocket connections with wscat
7. [ ] Load test with multiple concurrent connections
8. [ ] Document connection limits and scaling
```
</details>

---

#### Backend Tasks - Consultation Service Core

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B11.1 | Create Consultation Service | Spring Boot 3.x + WebFlux project with standard structure | Backend 1 | 4 | P0 | Service builds and runs |
| B11.2 | Sessions Database Schema | Design consultation_sessions table with Flyway migrations | Backend 1 | 8 | P0 | Migrations run successfully |
| B11.3 | Chat Messages Schema | Design MongoDB schema for chat messages | Backend 2 | 4 | P0 | Schema documented, indexes created |
| B11.4 | Session Entity & Repository | R2DBC models for consultation sessions | Backend 1 | 8 | P0 | Repository tests pass |
| B11.5 | Twilio Video Integration | Integrate Twilio Video SDK, configure client | Backend 1 | 24 | P0 | Can create video rooms |
| B11.6 | Create Video Room API | Endpoint to create/get video room for appointment | Backend 1 | 8 | P0 | Room created, returns room details |
| B11.7 | Generate Access Token API | Generate Twilio access token for participant | Backend 1 | 8 | P0 | Token generated, validated |
| B11.8 | Session Lifecycle Management | Start, pause, resume, end session tracking | Backend 1 | 12 | P0 | Session states tracked correctly |
| B11.9 | WebSocket Configuration | Configure Spring WebSocket with STOMP | Backend 2 | 8 | P0 | WebSocket connections working |
| B11.10 | Chat Message Handler | Implement STOMP message handlers | Backend 2 | 12 | P0 | Messages sent/received in real-time |
| B11.11 | Chat Message Persistence | Store messages in MongoDB | Backend 2 | 8 | P0 | Messages persisted, retrievable |
| B11.12 | Chat History API | Retrieve chat history for session | Backend 2 | 4 | P0 | Returns paginated history |
| B11.13 | Consultation Events | Publish events to Kafka | Backend 1 | 4 | P0 | Events published correctly |
| B11.14 | Unit & Integration Tests | 80%+ coverage for core functionality | Backend 1, 2 | 16 | P0 | Tests pass, coverage met |

**Database Schema:**

<details>
<summary><strong>B11.2 - Consultation Sessions Schema</strong></summary>

```sql
-- V1__create_consultation_tables.sql

-- Consultation type enum
CREATE TYPE consultation_mode AS ENUM ('VIDEO', 'AUDIO', 'CHAT');

-- Session status enum
CREATE TYPE session_status AS ENUM (
    'SCHEDULED',      -- Consultation is scheduled
    'WAITING',        -- Patient in waiting room
    'IN_PROGRESS',    -- Active consultation
    'PAUSED',         -- Temporarily paused
    'COMPLETED',      -- Successfully completed
    'CANCELLED',      -- Cancelled before start
    'NO_SHOW',        -- Patient/Doctor didn't join
    'FAILED'          -- Technical failure
);

-- Main consultation sessions table
CREATE TABLE consultation_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- References
    appointment_id UUID NOT NULL UNIQUE,  -- One session per appointment
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    
    -- Session configuration
    consultation_mode consultation_mode NOT NULL DEFAULT 'VIDEO',
    scheduled_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_duration_minutes INT NOT NULL DEFAULT 15,
    
    -- Video room details
    room_name VARCHAR(100),               -- Unique room identifier
    room_sid VARCHAR(100),                -- Twilio room SID
    
    -- Session tracking
    status session_status NOT NULL DEFAULT 'SCHEDULED',
    
    -- Participant join times
    patient_joined_at TIMESTAMP WITH TIME ZONE,
    doctor_joined_at TIMESTAMP WITH TIME ZONE,
    
    -- Actual timing
    actual_start_time TIMESTAMP WITH TIME ZONE,
    actual_end_time TIMESTAMP WITH TIME ZONE,
    total_duration_seconds INT,
    
    -- Quality metrics
    patient_connection_quality VARCHAR(20),  -- 'excellent', 'good', 'poor'
    doctor_connection_quality VARCHAR(20),
    
    -- Recording (optional)
    is_recorded BOOLEAN DEFAULT FALSE,
    recording_url VARCHAR(500),
    recording_duration_seconds INT,
    
    -- Metadata
    end_reason VARCHAR(100),              -- 'completed', 'patient_left', 'doctor_left', 'timeout'
    notes TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT valid_duration CHECK (scheduled_duration_minutes > 0 AND scheduled_duration_minutes <= 120)
);

-- Indexes
CREATE INDEX idx_sessions_appointment ON consultation_sessions(appointment_id);
CREATE INDEX idx_sessions_patient ON consultation_sessions(patient_id);
CREATE INDEX idx_sessions_doctor ON consultation_sessions(doctor_id);
CREATE INDEX idx_sessions_status ON consultation_sessions(status);
CREATE INDEX idx_sessions_scheduled_time ON consultation_sessions(scheduled_start_time);
CREATE INDEX idx_sessions_room ON consultation_sessions(room_name);

-- Participant tracking table
CREATE TABLE session_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES consultation_sessions(id),
    
    user_id UUID NOT NULL,
    participant_type VARCHAR(20) NOT NULL,  -- 'patient', 'doctor'
    
    -- Connection events
    joined_at TIMESTAMP WITH TIME ZONE,
    left_at TIMESTAMP WITH TIME ZONE,
    rejoin_count INT DEFAULT 0,
    
    -- Device info
    device_type VARCHAR(50),              -- 'web', 'mobile', 'tablet'
    browser VARCHAR(100),
    os VARCHAR(100),
    
    -- Connection quality log
    avg_audio_level FLOAT,
    avg_video_bitrate INT,
    packet_loss_percent FLOAT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_participant UNIQUE (session_id, user_id)
);

-- Session events log (for analytics and debugging)
CREATE TABLE session_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES consultation_sessions(id),
    
    event_type VARCHAR(50) NOT NULL,      -- 'patient_joined', 'doctor_joined', 'video_muted', etc.
    user_id UUID,
    event_data JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_session_events_session ON session_events(session_id);
CREATE INDEX idx_session_events_type ON session_events(event_type);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_consultation_session_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER consultation_session_updated
    BEFORE UPDATE ON consultation_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_consultation_session_timestamp();
```
</details>

<details>
<summary><strong>B11.3 - Chat Messages MongoDB Schema</strong></summary>

```javascript
// MongoDB Collection: chat_messages

// Document Schema
{
    "_id": ObjectId,
    "sessionId": "uuid-string",           // Reference to consultation_sessions
    "senderId": "uuid-string",            // User who sent message
    "senderType": "patient" | "doctor",
    "senderName": "Dr. Smith",            // Display name
    
    "messageType": "text" | "image" | "file" | "system",
    
    // For text messages
    "content": "Hello, how are you feeling today?",
    
    // For file/image messages
    "attachment": {
        "fileName": "report.pdf",
        "fileUrl": "https://s3.../...",
        "fileSize": 1024000,
        "mimeType": "application/pdf",
        "thumbnailUrl": "https://..."     // For images
    },
    
    // Message status
    "status": "sent" | "delivered" | "read",
    "deliveredAt": ISODate,
    "readAt": ISODate,
    
    // Metadata
    "createdAt": ISODate,
    "updatedAt": ISODate,
    "isDeleted": false
}

// Indexes
db.chat_messages.createIndex({ "sessionId": 1, "createdAt": 1 });
db.chat_messages.createIndex({ "sessionId": 1, "senderId": 1 });
db.chat_messages.createIndex({ "createdAt": 1 }, { expireAfterSeconds: 31536000 }); // 1 year TTL

// Compound index for efficient queries
db.chat_messages.createIndex({ 
    "sessionId": 1, 
    "createdAt": -1 
}, { 
    name: "session_messages_desc" 
});
```
</details>

<details>
<summary><strong>B11.5 - Twilio Video Integration (Detailed)</strong></summary>

```markdown
## Subtasks for B11.5

1. [ ] Add Twilio SDK dependency to pom.xml/build.gradle
2. [ ] Create TwilioConfig class with credentials
3. [ ] Implement TwilioVideoService:
   - [ ] createRoom(roomName, options) - Create video room
   - [ ] getRoom(roomSid) - Get room details
   - [ ] endRoom(roomSid) - End active room
   - [ ] generateAccessToken(identity, roomName) - Create participant token
4. [ ] Configure room types:
   - [ ] Group rooms (for potential multi-party)
   - [ ] Peer-to-peer rooms (for 1:1 consultations)
5. [ ] Handle room status callbacks:
   - [ ] room-created
   - [ ] room-ended
   - [ ] participant-connected
   - [ ] participant-disconnected
6. [ ] Implement token refresh mechanism
7. [ ] Add error handling and retries
8. [ ] Write unit tests with mocked Twilio client
```

```java
// TwilioVideoService.java example structure
@Service
@Slf4j
public class TwilioVideoService {
    
    private final String accountSid;
    private final String apiKeySid;
    private final String apiKeySecret;
    
    public Mono<Room> createRoom(String roomName, RoomType type) {
        return Mono.fromCallable(() -> {
            Room room = Room.creator()
                .setUniqueName(roomName)
                .setType(type)
                .setStatusCallback(URI.create(callbackUrl))
                .setMaxParticipants(2)
                .create();
            return room;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public String generateAccessToken(String identity, String roomName) {
        VideoGrant grant = new VideoGrant();
        grant.setRoom(roomName);
        
        AccessToken token = new AccessToken.Builder(
            accountSid, apiKeySid, apiKeySecret
        )
        .identity(identity)
        .grant(grant)
        .ttl(3600) // 1 hour
        .build();
        
        return token.toJwt();
    }
}
```
</details>

<details>
<summary><strong>B11.9 - WebSocket Configuration (Detailed)</strong></summary>

```java
// WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for subscriptions
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/consultation")
            .setAllowedOriginPatterns("*")
            .withSockJS(); // Fallback for older browsers
    }
}

// ChatMessage destinations:
// - Subscribe: /topic/session.{sessionId} - Receive messages for session
// - Send: /app/chat.send - Send message to session
// - Subscribe: /user/queue/notifications - Personal notifications
```
</details>

---

#### Frontend Tasks - Sprint 6

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F6.1 | Twilio SDK Setup (Patient) | Install and configure Twilio Video SDK | Frontend 1 | 8 | P0 | SDK initialized, connects |
| F6.2 | Video Room Component | Build video consultation room layout | Frontend 1 | 24 | P0 | Video displays correctly |
| F6.3 | Video Controls Component | Mute audio, toggle video, end call buttons | Frontend 1 | 12 | P0 | Controls work correctly |
| F6.4 | Waiting Room UI | Pre-consultation waiting experience | Frontend 1 | 12 | P0 | Patient can wait for doctor |
| F6.5 | In-Call Chat Panel | Side panel for text chat during call | Frontend 1 | 16 | P0 | Messages send/receive |
| F6.6 | Twilio SDK Setup (Doctor) | Install and configure SDK in dashboard | Frontend 2 | 8 | P0 | SDK initialized, connects |
| F6.7 | Doctor Consultation Room | Doctor's video consultation view | Frontend 2 | 24 | P0 | Video displays, can consult |
| F6.8 | Patient Queue/Waiting List | Show waiting patients to doctor | Frontend 2 | 12 | P0 | Queue displayed, can admit |
| F6.9 | Join Consultation Button | Add call button to appointment card | Frontend 1 | 8 | P0 | Opens consultation room |
| F6.10 | Device Permissions Handler | Camera/mic permission flow | Frontend 1, 2 | 8 | P0 | Handles all permission states |

**Frontend Component Details:**

<details>
<summary><strong>F6.2 - Video Room Component (Detailed)</strong></summary>

```markdown
## Video Room Component Structure

components/
├── consultation/
│   ├── VideoRoom/
│   │   ├── index.tsx                 # Main room container
│   │   ├── VideoRoom.styles.ts       # Styled components
│   │   ├── LocalVideo.tsx            # Self video preview
│   │   ├── RemoteVideo.tsx           # Remote participant video
│   │   ├── VideoGrid.tsx             # Grid layout for videos
│   │   ├── ScreenShare.tsx           # Screen sharing display
│   │   └── hooks/
│   │       ├── useVideoRoom.ts       # Room connection logic
│   │       ├── useLocalTracks.ts     # Local audio/video tracks
│   │       ├── useParticipant.ts     # Participant track handling
│   │       └── useScreenShare.ts     # Screen sharing logic
│   ├── Controls/
│   │   ├── VideoControls.tsx         # Main control bar
│   │   ├── AudioToggle.tsx           # Mute/unmute audio
│   │   ├── VideoToggle.tsx           # Camera on/off
│   │   ├── ScreenShareButton.tsx     # Share screen
│   │   ├── EndCallButton.tsx         # End consultation
│   │   └── SettingsMenu.tsx          # Device selection
│   ├── Chat/
│   │   ├── ChatPanel.tsx             # Chat sidebar
│   │   ├── ChatMessage.tsx           # Single message
│   │   ├── ChatInput.tsx             # Message input
│   │   └── hooks/
│   │       └── useChat.ts            # WebSocket chat logic
│   └── WaitingRoom/
│       ├── WaitingRoom.tsx           # Pre-call waiting
│       ├── DeviceTest.tsx            # Camera/mic test
│       └── ReadyToJoin.tsx           # Ready state

## Layout Structure

┌─────────────────────────────────────────────────────────────────────┐
│                         Header (Doctor info / Timer)                 │
├───────────────────────────────────────────────┬─────────────────────┤
│                                               │                     │
│                                               │    Chat Panel       │
│          Main Video Area                      │    ─────────────    │
│          (Remote Participant)                 │    [Messages...]    │
│                                               │                     │
│                                               │    ─────────────    │
│     ┌─────────────┐                          │    [Input field]    │
│     │ Self Video  │                          │                     │
│     │ (Picture-   │                          │                     │
│     │  in-Picture)│                          │                     │
│     └─────────────┘                          │                     │
├───────────────────────────────────────────────┴─────────────────────┤
│    [🎤 Mute]  [📹 Video]  [🖥️ Share]  [⚙️ Settings]  [📞 End Call]  │
└─────────────────────────────────────────────────────────────────────┘
```
</details>

<details>
<summary><strong>F6.4 - Waiting Room UI (Detailed)</strong></summary>

```typescript
// WaitingRoom.tsx structure

interface WaitingRoomProps {
    appointmentId: string;
    doctorInfo: DoctorSummary;
    scheduledTime: Date;
}

// States to handle:
// 1. Device check - Camera/mic permissions and testing
// 2. Waiting - Doctor hasn't joined yet
// 3. Ready - Doctor is available, can join
// 4. Joining - Connecting to room
// 5. Error - Connection failed

// UI Elements:
// - Doctor avatar and name
// - Appointment time display
// - Device preview (camera test)
// - Audio level indicator
// - "Test audio" button
// - Network quality indicator
// - Join button (enabled when doctor ready)
// - Cancel button
// - Tips/instructions text
```
</details>

---

### Sprint 7 (Week 13-14): Audio Consultation, Session Management & Feedback

**Sprint Goal**: Audio-only mode, session tracking, recording (optional), and post-consultation feedback.

---

#### Backend Tasks - Sprint 7

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B12.1 | Audio-Only Mode | Implement audio-only consultation option | Backend 1 | 8 | P0 | Audio calls work without video |
| B12.2 | Duration Tracking | Track actual consultation duration | Backend 1 | 8 | P0 | Duration recorded accurately |
| B12.3 | Billing by Mode/Duration | Calculate charges based on type and time | Backend 1 | 12 | P0 | Billing calculated correctly |
| B12.4 | Session Recording | Implement optional call recording | Backend 3 | 16 | P2 | Recording saved to storage |
| B12.5 | Recording Storage | Upload recordings to S3 with encryption | Backend 3 | 8 | P2 | Encrypted, accessible |
| B12.6 | Consultation Summary API | Get session summary with stats | Backend 1 | 4 | P0 | Summary returned correctly |
| B12.7 | Handle Completion Event | Process consultation.completed event | Backend 3 | 4 | P0 | Event handled, notifications sent |
| B12.8 | Feedback Request | Trigger feedback request after consultation | Backend 3 | 4 | P1 | Feedback notification sent |
| B12.9 | Feedback Submission API | Submit rating and review for consultation | Backend 3 | 8 | P1 | Feedback stored |
| B12.10 | Connection Quality Tracking | Track and store connection quality metrics | Backend 2 | 8 | P1 | Metrics recorded |
| B12.11 | Reconnection Handling | Handle dropped connections gracefully | Backend 2 | 12 | P0 | Reconnection works |
| B12.12 | Session Timeout Handler | Auto-end sessions after timeout | Backend 1 | 8 | P0 | Sessions end correctly |
| B12.13 | Unit & Integration Tests | Tests for Sprint 7 features | Backend 1, 2, 3 | 12 | P0 | Tests pass |

**Backend Subtasks:**

<details>
<summary><strong>B12.3 - Billing by Mode/Duration (Detailed)</strong></summary>

```sql
-- Consultation pricing table
CREATE TABLE consultation_pricing (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    
    consultation_mode consultation_mode NOT NULL,
    
    -- Pricing model
    pricing_type VARCHAR(20) NOT NULL DEFAULT 'FLAT',  -- 'FLAT', 'PER_MINUTE', 'TIERED'
    
    -- For FLAT pricing
    flat_fee DECIMAL(10, 2),
    
    -- For PER_MINUTE pricing
    per_minute_rate DECIMAL(10, 2),
    minimum_minutes INT DEFAULT 5,
    
    -- For TIERED pricing
    tier_config JSONB,  -- [{"upToMinutes": 15, "price": 500}, {"upToMinutes": 30, "price": 800}]
    
    currency VARCHAR(3) DEFAULT 'INR',
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_doctor_mode UNIQUE (doctor_id, consultation_mode)
);
```

```java
// ConsultationBillingService.java
@Service
public class ConsultationBillingService {
    
    public Mono<BillingDetails> calculateBilling(ConsultationSession session) {
        return pricingRepository.findByDoctorAndMode(
            session.getDoctorId(), 
            session.getConsultationMode()
        ).map(pricing -> {
            BigDecimal amount = switch (pricing.getPricingType()) {
                case FLAT -> pricing.getFlatFee();
                case PER_MINUTE -> calculatePerMinute(session, pricing);
                case TIERED -> calculateTiered(session, pricing);
            };
            return new BillingDetails(session.getId(), amount, pricing.getCurrency());
        });
    }
}
```
</details>

<details>
<summary><strong>B12.9 - Feedback Submission API (Detailed)</strong></summary>

```sql
-- Consultation feedback table
CREATE TABLE consultation_feedback (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL UNIQUE REFERENCES consultation_sessions(id),
    
    -- Who is giving feedback
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    
    -- Ratings (1-5 scale)
    overall_rating INT CHECK (overall_rating BETWEEN 1 AND 5),
    audio_quality_rating INT CHECK (audio_quality_rating BETWEEN 1 AND 5),
    video_quality_rating INT CHECK (video_quality_rating BETWEEN 1 AND 5),
    doctor_behaviour_rating INT CHECK (doctor_behaviour_rating BETWEEN 1 AND 5),
    
    -- Text feedback
    review_text TEXT,
    
    -- Quick feedback tags
    feedback_tags VARCHAR[] DEFAULT '{}',  -- ['professional', 'on-time', 'helpful']
    
    -- Would recommend
    would_recommend BOOLEAN,
    
    -- Response from doctor
    doctor_response TEXT,
    doctor_responded_at TIMESTAMP WITH TIME ZONE,
    
    -- Visibility
    is_public BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT TRUE,  -- Verified purchase/consultation
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_feedback_doctor ON consultation_feedback(doctor_id);
CREATE INDEX idx_feedback_rating ON consultation_feedback(overall_rating);
```
</details>

<details>
<summary><strong>B12.11 - Reconnection Handling (Detailed)</strong></summary>

```markdown
## Reconnection Flow

1. Detect disconnection (WebSocket close, video track ended)
2. Start reconnection timer (max 60 seconds)
3. Attempt automatic reconnection:
   - Regenerate access token
   - Rejoin room with same identity
   - Restore chat subscription
4. Track reconnection attempts
5. After 3 failed attempts or 60s timeout:
   - Notify other participant
   - Mark as "connection lost"
   - Allow manual rejoin

## Events to Track:
- participant.reconnecting
- participant.reconnected
- participant.disconnected (permanent)
```

```java
// ReconnectionHandler.java
@Component
public class ReconnectionHandler {
    
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final Duration RECONNECT_TIMEOUT = Duration.ofSeconds(60);
    
    @Async
    public void handleDisconnection(String sessionId, String participantId) {
        // Notify other participant
        messagingTemplate.convertAndSend(
            "/topic/session." + sessionId,
            new ParticipantStatusEvent(participantId, "RECONNECTING")
        );
        
        // Start timeout countdown
        scheduler.schedule(() -> {
            checkReconnectionStatus(sessionId, participantId);
        }, RECONNECT_TIMEOUT);
    }
}
```
</details>

---

#### Frontend Tasks - Sprint 7

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F7.1 | Audio-Only UI | Simplified UI for audio consultations | Frontend 1, 2 | 12 | P0 | Audio-only mode works |
| F7.2 | Call Timer Display | Show elapsed time during consultation | Frontend 1, 2 | 4 | P0 | Timer accurate |
| F7.3 | Connection Quality Indicator | Show network quality to users | Frontend 1, 2 | 8 | P1 | Quality displayed |
| F7.4 | Consultation Summary View | Post-call summary screen | Frontend 1 | 8 | P0 | Summary displayed |
| F7.5 | Feedback/Rating Modal | Post-consultation feedback form | Frontend 1 | 12 | P1 | Feedback submitted |
| F7.6 | Consultation History Page | List past consultations | Frontend 1, 2 | 16 | P0 | History displayed |
| F7.7 | Reconnection UI | Handle and display reconnection state | Frontend 1, 2 | 8 | P0 | Reconnection handled |
| F7.8 | Device Settings Modal | Change audio/video devices mid-call | Frontend 1, 2 | 8 | P1 | Device switching works |
| F7.9 | Screen Sharing | Enable screen sharing during call | Frontend 1, 2 | 12 | P1 | Screen sharing works |
| F7.10 | Mobile Responsiveness | Ensure video room works on mobile | Frontend 1 | 12 | P0 | Mobile layout works |

**Frontend Subtasks:**

<details>
<summary><strong>F7.1 - Audio-Only UI (Detailed)</strong></summary>

```markdown
## Audio-Only Consultation UI

┌─────────────────────────────────────────────────────────────────┐
│                        Audio Consultation                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                    ┌─────────────────┐                          │
│                    │                 │                          │
│                    │   Doctor        │                          │
│                    │   Avatar        │                          │
│                    │                 │                          │
│                    │   (Animated     │                          │
│                    │   audio waves   │                          │
│                    │   when speaking)│                          │
│                    │                 │                          │
│                    └─────────────────┘                          │
│                                                                  │
│                    Dr. Sarah Smith                               │
│                    Cardiologist                                  │
│                                                                  │
│                    ⏱️ 05:32                                      │
│                                                                  │
│          ┌─────────────────────────────────────┐                │
│          │ 🔊 |||||||||||||| (Audio level)    │                │
│          └─────────────────────────────────────┘                │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│         [🎤 Mute]        [💬 Chat]        [📞 End Call]         │
└─────────────────────────────────────────────────────────────────┘

Features:
- Animated audio visualization
- Speaking indicator
- Minimal UI, clear focus
- Chat panel toggle
- No video preview
```
</details>

<details>
<summary><strong>F7.5 - Feedback/Rating Modal (Detailed)</strong></summary>

```typescript
// ConsultationFeedback.tsx

interface FeedbackFormData {
    overallRating: number;          // 1-5 stars
    audioQuality: number;           // 1-5
    videoQuality: number;           // 1-5 (optional for audio-only)
    doctorBehaviour: number;        // 1-5
    reviewText?: string;            // Optional text
    feedbackTags: string[];         // ['professional', 'helpful', 'punctual']
    wouldRecommend: boolean;
}

// Modal sections:
// 1. Overall rating (large stars)
// 2. Quick tags (chips to select)
// 3. Detailed ratings (optional accordion)
// 4. Text review (optional)
// 5. Would recommend? (thumbs up/down)
// 6. Submit button

// Tags options:
const FEEDBACK_TAGS = [
    'Professional',
    'Friendly',
    'Knowledgeable',
    'On-time',
    'Patient',
    'Clear explanations',
    'Good listener',
    'Thorough',
];
```
</details>

---

## Kafka Events

### Events Published by Consultation Service

| Event Type | Trigger | Payload | Consumers |
|------------|---------|---------|-----------|
| `consultation.session.created` | Session initialized | sessionId, appointmentId, mode | Notification Service |
| `consultation.waiting.patient` | Patient enters waiting room | sessionId, patientId | Doctor Dashboard |
| `consultation.started` | Both participants connected | sessionId, startTime | Analytics, Billing |
| `consultation.ended` | Session completed | sessionId, duration, endReason | Notification, EHR |
| `consultation.failed` | Technical failure | sessionId, error | Support, Analytics |
| `consultation.feedback.submitted` | Patient submits feedback | sessionId, rating | Doctor Service |

**Event Schema Examples:**

<details>
<summary><strong>Event Schemas</strong></summary>

```json
// consultation.started.v1
{
    "eventType": "consultation.started.v1",
    "eventTime": "2026-01-30T14:30:00Z",
    "source": "consultation-service",
    "correlationId": "corr-123",
    "data": {
        "sessionId": "session-uuid",
        "appointmentId": "appt-uuid",
        "patientId": "patient-uuid",
        "doctorId": "doctor-uuid",
        "consultationMode": "VIDEO",
        "actualStartTime": "2026-01-30T14:30:00Z",
        "roomName": "room-12345"
    }
}

// consultation.ended.v1
{
    "eventType": "consultation.ended.v1",
    "eventTime": "2026-01-30T14:45:00Z",
    "source": "consultation-service",
    "correlationId": "corr-123",
    "data": {
        "sessionId": "session-uuid",
        "appointmentId": "appt-uuid",
        "patientId": "patient-uuid",
        "doctorId": "doctor-uuid",
        "actualStartTime": "2026-01-30T14:30:00Z",
        "actualEndTime": "2026-01-30T14:45:00Z",
        "durationSeconds": 900,
        "consultationMode": "VIDEO",
        "endReason": "completed",
        "isRecorded": false
    }
}

// consultation.feedback.submitted.v1
{
    "eventType": "consultation.feedback.submitted.v1",
    "eventTime": "2026-01-30T14:50:00Z",
    "source": "consultation-service",
    "data": {
        "sessionId": "session-uuid",
        "doctorId": "doctor-uuid",
        "patientId": "patient-uuid",
        "overallRating": 5,
        "wouldRecommend": true,
        "feedbackTags": ["professional", "helpful"]
    }
}
```
</details>

---

## API Endpoints

### Consultation Service APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/consultations/sessions` | Create session for appointment | Yes |
| GET | `/api/v1/consultations/sessions/{id}` | Get session details | Yes |
| POST | `/api/v1/consultations/sessions/{id}/join` | Generate access token and join | Yes |
| POST | `/api/v1/consultations/sessions/{id}/leave` | Leave session | Yes |
| POST | `/api/v1/consultations/sessions/{id}/end` | End session (doctor only) | Yes |
| GET | `/api/v1/consultations/sessions/{id}/chat` | Get chat history | Yes |
| GET | `/api/v1/consultations/sessions/{id}/summary` | Get session summary | Yes |
| POST | `/api/v1/consultations/sessions/{id}/feedback` | Submit feedback | Yes |
| GET | `/api/v1/consultations/history` | Get consultation history | Yes |

**API Details:**

<details>
<summary><strong>API Request/Response Examples</strong></summary>

```yaml
# POST /api/v1/consultations/sessions
# Request
{
    "appointmentId": "appt-uuid-123",
    "consultationMode": "VIDEO"
}

# Response
{
    "sessionId": "session-uuid-456",
    "appointmentId": "appt-uuid-123",
    "status": "SCHEDULED",
    "consultationMode": "VIDEO",
    "scheduledStartTime": "2026-01-30T14:30:00Z",
    "roomName": "consultation-room-12345"
}

---

# POST /api/v1/consultations/sessions/{id}/join
# Response
{
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "roomName": "consultation-room-12345",
    "identity": "patient-uuid-789",
    "participantType": "PATIENT",
    "iceServers": [
        {
            "urls": "turn:global.turn.twilio.com:3478",
            "username": "...",
            "credential": "..."
        }
    ],
    "tokenExpiresAt": "2026-01-30T15:30:00Z"
}

---

# GET /api/v1/consultations/sessions/{id}/summary
# Response
{
    "sessionId": "session-uuid-456",
    "appointmentId": "appt-uuid-123",
    "doctorName": "Dr. Sarah Smith",
    "specialization": "Cardiology",
    "consultationMode": "VIDEO",
    "scheduledDuration": 15,
    "actualDuration": 12,
    "startTime": "2026-01-30T14:30:00Z",
    "endTime": "2026-01-30T14:42:00Z",
    "status": "COMPLETED",
    "chatMessageCount": 5,
    "prescriptionId": "rx-uuid-789",  // If prescription was created
    "feedbackSubmitted": false,
    "amountCharged": 500.00,
    "currency": "INR"
}
```
</details>

---

## Service Project Structure

```
consultation-service/
├── src/main/java/com/healthapp/consultation/
│   ├── ConsultationServiceApplication.java
│   ├── controller/
│   │   ├── SessionController.java          # REST endpoints
│   │   └── ChatController.java             # WebSocket message handlers
│   ├── service/
│   │   ├── ConsultationSessionService.java # Session business logic
│   │   ├── TwilioVideoService.java         # Twilio integration
│   │   ├── ChatService.java                # Chat operations
│   │   ├── FeedbackService.java            # Feedback handling
│   │   └── BillingService.java             # Duration-based billing
│   ├── repository/
│   │   ├── SessionRepository.java          # R2DBC repository
│   │   ├── ParticipantRepository.java      # R2DBC repository
│   │   └── ChatMessageRepository.java      # MongoDB repository
│   ├── model/
│   │   ├── ConsultationSession.java        # Entity
│   │   ├── SessionParticipant.java         # Entity
│   │   ├── ChatMessage.java                # MongoDB document
│   │   ├── ConsultationMode.java           # Enum
│   │   └── SessionStatus.java              # Enum
│   ├── dto/
│   │   ├── CreateSessionRequest.java
│   │   ├── JoinSessionResponse.java
│   │   ├── SessionSummaryDto.java
│   │   ├── ChatMessageDto.java
│   │   └── FeedbackRequest.java
│   ├── event/
│   │   ├── ConsultationEventPublisher.java # Kafka publisher
│   │   └── AppointmentEventConsumer.java   # Consume appointment events
│   ├── websocket/
│   │   ├── WebSocketConfig.java
│   │   ├── ChatMessageHandler.java
│   │   └── SessionPresenceHandler.java
│   └── config/
│       ├── TwilioConfig.java
│       ├── MongoConfig.java
│       └── SecurityConfig.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── db/migration/
│       ├── V1__create_consultation_tables.sql
│       └── V2__create_feedback_tables.sql
└── src/test/
    ├── java/com/healthapp/consultation/
    │   ├── controller/
    │   ├── service/
    │   └── repository/
    └── resources/
```

---

## Testing Strategy

### Unit Tests

| Component | Test Focus | Coverage Target |
|-----------|------------|-----------------|
| SessionService | Session lifecycle, state transitions | 90% |
| TwilioVideoService | Token generation, room creation (mocked) | 85% |
| ChatService | Message handling, persistence | 85% |
| BillingService | Pricing calculations | 95% |
| FeedbackService | Validation, storage | 85% |

### Integration Tests

| Test Scenario | Description |
|---------------|-------------|
| Session Creation Flow | Create session from appointment |
| Join Session | Generate token and join room |
| Chat Flow | Send and receive messages via WebSocket |
| Session End | Complete session and calculate billing |
| Feedback Submission | Submit and validate feedback |

### End-to-End Tests

| Test Scenario | Description |
|---------------|-------------|
| Complete Video Consultation | Full flow from join to end |
| Audio-Only Consultation | Full audio-only flow |
| Reconnection | Simulate disconnect and reconnect |
| Multi-device | Same user on different devices |

### Performance Tests

| Test | Target |
|------|--------|
| WebSocket connections | 1000 concurrent connections |
| Message throughput | 100 messages/second per session |
| Session creation | < 500ms latency |
| Token generation | < 100ms latency |

---

## Quality Metrics

### Code Quality Gates

| Metric | Threshold |
|--------|-----------|
| Code Coverage | ≥ 80% |
| Sonar Quality Gate | Pass |
| Checkstyle Violations | 0 |
| SpotBugs Issues | 0 Critical/High |

### Video Quality Metrics (Twilio)

| Metric | Target |
|--------|--------|
| Video Bitrate | ≥ 1.5 Mbps (720p) |
| Audio Quality (MOS) | ≥ 4.0 |
| Packet Loss | < 1% |
| Connection Time | < 3 seconds |

---

## Risk Mitigation

| Risk | Mitigation Strategy |
|------|---------------------|
| **Video quality issues** | Adaptive bitrate, fallback to audio |
| **Network instability** | Automatic reconnection, session state persistence |
| **Browser compatibility** | Thorough testing, SockJS fallback |
| **SDK rate limits** | Token caching, proper retry strategies |
| **Recording storage costs** | Optional recording, retention policies |
| **HIPAA compliance** | Encrypted storage, audit logs, consent |

---

## Definition of Done - Phase 3

- [ ] All tasks completed and code merged to main branch
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests passing
- [ ] API documentation updated (OpenAPI/Swagger)
- [ ] No P0/P1 bugs open
- [ ] Performance benchmarks met
- [ ] Security review completed
- [ ] Video consultation demo successful
- [ ] Audio consultation demo successful
- [ ] Chat during call tested
- [ ] Post-consultation feedback working
- [ ] DevOps monitoring dashboards configured

---

## Phase 3 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Consultation Service deployed to dev | End of Week 11 | ⬜ |
| Video consultation working (basic) | End of Week 12 | ⬜ |
| Real-time chat during call working | End of Week 12 | ⬜ |
| Waiting room experience complete | End of Week 12 | ⬜ |
| Audio-only consultation working | End of Week 13 | ⬜ |
| Session tracking and duration working | End of Week 13 | ⬜ |
| Reconnection handling working | End of Week 14 | ⬜ |
| Post-consultation feedback working | End of Week 14 | ⬜ |
| Consultation history page complete | End of Week 14 | ⬜ |
| All tests passing | End of Week 14 | ⬜ |

---

## Dependencies on Other Teams

| Dependency | From Team | Description | Required By |
|------------|-----------|-------------|-------------|
| Appointment events | Appointment Service | appointment.confirmed event | Week 11 |
| User authentication | User Service | JWT validation | Week 11 |
| Doctor availability | Doctor Service | Check consultation types | Week 11 |
| Payment integration | Payment Service | Consultation billing | Week 13 |
| Notification triggers | Notification Service | Session reminders | Week 12 |

---

## Post-Phase Considerations

### Future Enhancements (Phase 4+)

1. **Group Consultations** - Multiple patients/family members
2. **Specialist Referrals** - In-call referral to another doctor
3. **Live Transcription** - AI-powered call transcription
4. **Virtual Whiteboard** - Draw and explain to patients
5. **Medical Imaging Sharing** - Share X-rays, MRIs during call
6. **Language Translation** - Real-time translation support
7. **Recording with Consent** - Full session recording option

### Technical Debt to Address

1. Evaluate Agora as Twilio alternative
2. Implement SFU/MCU for better scalability
3. Add WebRTC statistics logging
4. Implement connection quality prediction
5. Add mobile native SDK integration

---

## Appendix

### A. Environment Variables

```yaml
# Twilio Configuration
TWILIO_ACCOUNT_SID: ${vault:twilio/account_sid}
TWILIO_API_KEY_SID: ${vault:twilio/api_key_sid}
TWILIO_API_KEY_SECRET: ${vault:twilio/api_key_secret}

# MongoDB Configuration
MONGODB_URI: ${vault:mongodb/connection_uri}
MONGODB_DATABASE: healthcare_chat

# PostgreSQL Configuration
POSTGRES_HOST: ${vault:postgres/host}
POSTGRES_DATABASE: consultation_db

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS: ${vault:kafka/bootstrap_servers}

# Redis Configuration
REDIS_HOST: ${vault:redis/host}
REDIS_PORT: 6379

# S3 Configuration (for recordings)
AWS_S3_BUCKET: healthcare-recordings
AWS_S3_REGION: ap-south-1
```

### B. Twilio Video Room Settings

```json
{
    "roomType": "group",
    "maxParticipants": 2,
    "recordParticipantsOnConnect": false,
    "videoCodecs": ["VP8", "H264"],
    "audioCodecs": ["opus"],
    "statusCallback": "https://api.healthapp.com/webhooks/twilio/room-status",
    "statusCallbackMethod": "POST"
}
```

### C. WebSocket Message Format

```json
// Client → Server (Send message)
{
    "type": "CHAT_MESSAGE",
    "payload": {
        "sessionId": "session-uuid",
        "content": "Hello, how are you?",
        "messageType": "text"
    }
}

// Server → Client (Receive message)
{
    "type": "CHAT_MESSAGE",
    "payload": {
        "messageId": "msg-uuid",
        "sessionId": "session-uuid",
        "senderId": "user-uuid",
        "senderName": "Dr. Smith",
        "senderType": "doctor",
        "content": "Hello, I'm doing well!",
        "messageType": "text",
        "timestamp": "2026-01-30T14:35:00Z"
    }
}

// Session status updates
{
    "type": "SESSION_STATUS",
    "payload": {
        "sessionId": "session-uuid",
        "status": "IN_PROGRESS",
        "participants": [
            {"userId": "patient-uuid", "status": "connected"},
            {"userId": "doctor-uuid", "status": "connected"}
        ]
    }
}
```

### D. Alternative: Agora Integration

If choosing Agora instead of Twilio:

```java
// AgoraVideoService.java
@Service
public class AgoraVideoService {
    
    private final String appId;
    private final String appCertificate;
    
    public String generateToken(String channelName, String uid, int expirationTime) {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        return tokenBuilder.buildTokenWithUid(
            appId, 
            appCertificate, 
            channelName, 
            uid,
            RtcTokenBuilder2.Role.ROLE_PUBLISHER,
            expirationTime,
            expirationTime
        );
    }
}
```

---

*Document Version: 1.0*  
*Last Updated: January 30, 2026*  
*Author: Healthcare Platform Team*

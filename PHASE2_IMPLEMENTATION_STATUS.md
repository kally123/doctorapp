# Phase 2 Implementation Status

## Overview
Phase 2 focuses on Appointment Booking, Payment Integration, and Notification services, as outlined in the Phase 2 plan document.

## Completed Components

### 1. Appointment Service (Port 8084)
**Status: ✅ Complete**

#### Database Schema
- `V1__create_appointment_tables.sql` - 7 tables for appointment management
  - `weekly_availability` - Doctor weekly schedules
  - `availability_overrides` - Holiday/vacation overrides
  - `blocked_slots` - Blocked time slots
  - `available_slots` - Generated bookable slots
  - `appointments` - Core appointment records
  - `appointment_status_history` - Status change tracking
  - `scheduled_reminders` - Reminder scheduling

#### Domain Layer
- Enums: `ConsultationType`, `AppointmentStatus`, `SlotStatus`
- Entities: `WeeklyAvailability`, `Appointment`, `AppointmentStatusHistory`, `BlockedSlot`, `AvailableSlot`

#### Repository Layer
- `WeeklyAvailabilityRepository`
- `AvailableSlotRepository`
- `AppointmentRepository`
- `AppointmentStatusHistoryRepository`
- `BlockedSlotRepository`

#### Service Layer
- `AvailabilityService` - Weekly schedule CRUD, slot generation
- `AvailableSlotService` - Get available slots
- `AppointmentService` - Reserve, confirm, cancel appointments
- `ReservationExpiryHandler` - Scheduled job for expired reservations

#### Controller Layer
- `AvailabilityController` - `/api/v1/availability/**`
- `AvailableSlotsController` - `/api/v1/availability/doctors/{doctorId}/slots`
- `AppointmentController` - `/api/v1/appointments/**`

#### Events
- `AppointmentEventPublisher` - Publishes to Kafka `appointment-events` topic
- Event types: RESERVED, CONFIRMED, CANCELLED, RESCHEDULED, COMPLETED, NO_SHOW

---

### 2. Payment Service (Port 8085)
**Status: ✅ Complete**

#### Database Schema
- `V1__create_payment_tables.sql` - 3 tables
  - `payment_transactions` - Payment records
  - `refunds` - Refund records
  - `doctor_payouts` - Payout tracking

#### Domain Layer
- Enums: `PaymentStatus`, `PaymentMethod`
- Entities: `PaymentTransaction`, `Refund`

#### Repository Layer
- `PaymentRepository`
- `RefundRepository`

#### Service Layer
- `PaymentService` - Initiate, verify, complete, refund
- `RazorpayGateway` - Razorpay API integration

#### Controller Layer
- `PaymentController` - `/api/v1/payments/**`
  - POST `/initiate` - Create payment order
  - POST `/verify` - Verify payment signature
  - POST `/webhook` - Razorpay webhooks
  - POST `/{id}/refund` - Process refund
  - GET `/user/{userId}` - Payment history

#### Events
- `PaymentEventPublisher` - Publishes to Kafka `payment-events` topic
- Event types: PAYMENT_INITIATED, COMPLETED, FAILED, REFUND_INITIATED, REFUND_COMPLETED

---

### 3. Notification Service (Port 8086)
**Status: ✅ Complete**

#### Database Schema
- `V1__create_notification_tables.sql` - 5 tables
  - `notification_templates` - Email/SMS templates with Thymeleaf
  - `notification_logs` - Sent notification history
  - `scheduled_notifications` - Queued notifications
  - `user_notification_preferences` - User settings
  - `user_devices` - Push notification tokens

#### Domain Layer
- Enums: `NotificationChannel`, `NotificationType`, `NotificationStatus`
- Entities: `NotificationTemplate`, `NotificationLog`, `ScheduledNotification`, `UserNotificationPreferences`, `UserDevice`

#### Repository Layer
- `NotificationTemplateRepository`
- `NotificationLogRepository`
- `ScheduledNotificationRepository`
- `UserNotificationPreferencesRepository`
- `UserDeviceRepository`

#### Provider Layer
- `EmailProvider` interface + `SmtpEmailProvider` implementation
- `SmsProvider` interface + `TwilioSmsProvider` implementation
- `PushProvider` interface + `FcmPushProvider` implementation

#### Service Layer
- `TemplateService` - Thymeleaf template processing
- `NotificationService` - Send notifications via all channels
- `UserPreferencesService` - Manage user notification preferences
- `DeviceService` - Manage push notification device tokens
- `ReminderScheduler` - Scheduled job for sending reminders

#### Consumer Layer
- `AppointmentEventConsumer` - Handles appointment events from Kafka
- `PaymentEventConsumer` - Handles payment events from Kafka

#### Controller Layer
- `NotificationController` - `/api/v1/notifications/**`
  - POST `/send` - Send notification
  - GET `/user/{userId}` - Get user notifications
  - GET/PUT `/preferences/{userId}` - Manage preferences
  - POST `/devices/register` - Register push token
  - DELETE `/devices/{token}` - Unregister device

---

## Infrastructure Updates

### Docker Compose
- ✅ Added `appointment-service` container
- ✅ Added `payment-service` container
- ✅ Added `notification-service` container
- ✅ Updated `api-gateway` dependencies
- ✅ Updated `postgres` to create new databases (payment_db, notification_db)

### Kubernetes Manifests
- ✅ Created `k8s/services/appointment-service.yaml`
- ✅ Created `k8s/services/payment-service.yaml`
- ✅ Created `k8s/services/notification-service.yaml`
- ✅ Added secrets for Razorpay, Twilio, FCM, Mail

### API Gateway
- ✅ Added routes for appointment service
- ✅ Added routes for payment service (including webhook)
- ✅ Added routes for notification service

---

## Integration Points

### Kafka Topics
1. **appointment-events**
   - Producer: Appointment Service
   - Consumer: Notification Service

2. **payment-events**
   - Producer: Payment Service
   - Consumer: Notification Service

### Event Flow
1. Patient books appointment → Appointment Service publishes `APPOINTMENT_CONFIRMED`
2. Notification Service receives event → Sends confirmation email/push
3. Notification Service schedules reminders (24h, 1h, 15min before)
4. Payment completed → Payment Service publishes `PAYMENT_COMPLETED`
5. Notification Service sends payment receipt

---

## API Endpoints Summary

### Appointment Service (8084)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/availability/doctors/{doctorId}` | Get weekly schedule |
| POST | `/api/v1/availability/weekly` | Set weekly availability |
| POST | `/api/v1/availability/block` | Block time slot |
| GET | `/api/v1/availability/doctors/{doctorId}/slots` | Get available slots |
| POST | `/api/v1/appointments/reserve` | Reserve slot (5 min hold) |
| POST | `/api/v1/appointments/{id}/confirm` | Confirm with payment |
| POST | `/api/v1/appointments/{id}/cancel` | Cancel appointment |
| GET | `/api/v1/appointments/doctor/{doctorId}` | Doctor's appointments |
| GET | `/api/v1/appointments/patient/{patientId}` | Patient's appointments |

### Payment Service (8085)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments/initiate` | Create Razorpay order |
| POST | `/api/v1/payments/verify` | Verify payment signature |
| POST | `/api/v1/payments/webhook` | Razorpay webhook |
| POST | `/api/v1/payments/{id}/refund` | Process refund |
| GET | `/api/v1/payments/user/{userId}` | Payment history |

### Notification Service (8086)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/notifications/send` | Send notification |
| GET | `/api/v1/notifications/user/{userId}` | Get notifications |
| GET | `/api/v1/notifications/preferences/{userId}` | Get preferences |
| PUT | `/api/v1/notifications/preferences` | Update preferences |
| POST | `/api/v1/notifications/devices/register` | Register device |
| DELETE | `/api/v1/notifications/devices/{token}` | Unregister device |

---

## Configuration Required

### Environment Variables

#### Payment Service
```
RAZORPAY_KEY_ID=your_key_id
RAZORPAY_KEY_SECRET=your_key_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret
```

#### Notification Service
```
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email
SPRING_MAIL_PASSWORD=your_app_password

# Optional SMS (Twilio)
TWILIO_ACCOUNT_SID=your_sid
TWILIO_AUTH_TOKEN=your_token
TWILIO_FROM_NUMBER=+1234567890

# Optional Push (FCM)
FCM_SERVER_KEY=your_fcm_key
```

---

## Pending Tasks for Phase 2

### Frontend Updates (Next Sprint)
- [ ] Patient Web App - Booking flow UI
- [ ] Patient Web App - Payment integration (Razorpay checkout)
- [ ] Patient Web App - Appointment history
- [ ] Doctor Dashboard - Availability calendar
- [ ] Doctor Dashboard - Appointment management

### Additional Backend Tasks
- [ ] Add video call integration (Twilio/Daily.co) in Phase 3
- [ ] Add comprehensive test coverage
- [ ] Add API documentation (OpenAPI/Swagger)

---

## Running Phase 2 Services

```bash
# Start all services with Docker Compose
docker-compose up -d

# Or start individual services
docker-compose up -d appointment-service payment-service notification-service

# Build services
cd backend
mvn clean package -pl appointment-service,payment-service,notification-service -am
```

---

## Notes

1. **Razorpay Integration**: Currently configured for test mode. Update credentials for production.

2. **Email Provider**: Using Spring Mail with SMTP. Can be swapped for SendGrid/AWS SES.

3. **SMS Provider**: Twilio integration ready but disabled by default (`notification.sms.enabled=false`).

4. **Push Notifications**: FCM integration ready but disabled by default.

5. **Slot Generation**: Availability service generates slots based on weekly schedule. Slots are created for the next 30 days by default.

6. **Reservation Expiry**: Reserved slots expire after 5 minutes if not confirmed. Handled by `ReservationExpiryHandler` scheduled job.

---

Last Updated: Phase 2 Implementation Complete

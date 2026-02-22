# Phase 5 Implementation Status: Commerce

## Overview
Phase 5 implements the commerce functionality for the healthcare platform, enabling medicine ordering and lab test booking. This phase introduces the Order Service microservice with complete e-commerce capabilities.

**Status**: ✅ Completed  
**Duration**: Week 13-16  
**Start Date**: January 2024

---

## Completed Features

### 1. Order Service (Backend)

#### 1.1 Module Setup ✅
- **pom.xml**: Full Maven configuration with dependencies
  - Spring Boot WebFlux (reactive)
  - R2DBC PostgreSQL
  - Spring Data Redis Reactive
  - Spring Kafka
  - Elasticsearch client
  - AWS S3 SDK
  - WebSocket/STOMP
- **Application Configuration**: Complete `application.yml` with all service settings
- **Port**: 8089

#### 1.2 Domain Entities ✅
| Entity | Description |
|--------|-------------|
| `Order` | Main order entity with pricing, status, tracking |
| `OrderItem` | Individual items in an order |
| `DeliveryAddress` | User delivery addresses |
| `OrderStatusHistory` | Audit trail for status changes |
| `Partner` | Pharmacy and lab partners |
| `PartnerInventory` | Partner stock and pricing |
| `LabTest` | Lab test catalog |
| `TestCategory` | Lab test categories |
| `TestPackage` | Bundled test packages |
| `LabBooking` | Lab test bookings |
| `CollectionSlot` | Home collection time slots |
| `Phlebotomist` | Home collection staff |
| `Cart` | Redis-stored shopping cart |
| `CartItem` | Items within cart |

#### 1.3 Enums ✅
- `OrderType`: MEDICINE, LAB_TEST
- `OrderStatus`: 13 states (CART → REFUNDED)
- `PaymentStatus`: PENDING, COMPLETED, FAILED, REFUNDED
- `DeliveryType`: STANDARD, EXPRESS, SCHEDULED
- `PartnerType`: PHARMACY, LAB
- `AddressType`: HOME, WORK, OTHER
- `LabBookingStatus`: 8 states (PENDING → CANCELLED)
- `BookingType`: HOME_COLLECTION, WALK_IN

#### 1.4 DTOs ✅
| Category | DTOs |
|----------|------|
| Cart | AddToCartRequest, UpdateCartItemRequest, CartResponse, CartItemResponse, CartSummary |
| Orders | PlaceOrderRequest, OrderResponse, OrderItemResponse |
| Addresses | AddressRequest, AddressResponse |
| Tracking | TrackingInfo, TrackingStep |
| Lab Tests | LabTestResponse, TestPackageResponse, CreateLabBookingRequest, LabBookingResponse, AvailableSlotResponse |

#### 1.5 Repositories ✅
- OrderRepository, OrderItemRepository
- DeliveryAddressRepository, OrderStatusHistoryRepository
- PartnerRepository, PartnerInventoryRepository
- LabTestRepository, TestCategoryRepository, TestPackageRepository
- LabBookingRepository, CollectionSlotRepository, PhlebotomistRepository

#### 1.6 Services ✅
| Service | Responsibilities |
|---------|------------------|
| `CartService` | Cart CRUD, Redis storage, coupon handling, prescription integration |
| `AddressService` | Delivery address management, default address handling |
| `OrderService` | Order lifecycle, payment confirmation, status updates, tracking |
| `PharmacyAssignmentService` | Intelligent pharmacy selection based on stock and rating |
| `LabBookingService` | Test search, booking creation, slot management, report upload |

#### 1.7 Events ✅
- `OrderEvent` / `OrderEventPublisher`: Kafka events for order lifecycle
- `LabBookingEvent` / `LabBookingEventPublisher`: Kafka events for lab bookings

#### 1.8 Controllers ✅
| Controller | Endpoints |
|------------|-----------|
| `CartController` | GET/POST/PUT/DELETE cart operations |
| `OrderController` | Order placement, tracking, cancellation |
| `AddressController` | Address CRUD with default handling |
| `LabTestController` | Test catalog browsing and search |
| `LabBookingController` | Booking creation and management |
| `PartnerController` | Partner (pharmacy/lab) browsing |

#### 1.9 Configuration ✅
- `RedisConfig`: ReactiveRedisTemplate for Cart objects
- `KafkaConfig`: Topics for order-events and lab-booking-events
- `WebClientConfig`: Inter-service communication
- `WebSocketConfig`: STOMP at /ws/order-tracking

#### 1.10 Exception Handling ✅
- GlobalExceptionHandler with custom exceptions
- OrderNotFoundException, BookingNotFoundException
- InvalidOrderStateException, InsufficientStockException
- InvalidCouponException, SlotNotAvailableException

#### 1.11 Database ✅
- Complete Flyway migration `V1__create_order_tables.sql`
- 14 tables with proper indexes
- Sample test categories seeded

#### 1.12 Infrastructure ✅
- `Dockerfile`: Production-ready container image
- `k8s/services/order-service.yaml`: Full K8s deployment
  - Deployment with 2 replicas
  - Service (ClusterIP)
  - HorizontalPodAutoscaler
  - PodDisruptionBudget
  - Secrets for database credentials

---

### 2. Frontend (Patient Web App)

#### 2.1 Pharmacy Store ✅
| Page | Route | Features |
|------|-------|----------|
| Pharmacy Home | `/pharmacy` | Search, categories, featured products, prescription upload CTA |
| Upload Prescription | `/pharmacy/upload-prescription` | Multi-file upload, camera capture, guidelines |
| Cart | `/pharmacy/cart` | Item management, coupon codes, price breakdown |
| Checkout | `/pharmacy/checkout` | Address selection, delivery slots, payment methods |
| Orders List | `/pharmacy/orders` | Order history with status filters |
| Order Detail | `/pharmacy/orders/[orderNumber]` | Tracking, delivery status, agent contact |

#### 2.2 Lab Tests ✅
| Page | Route | Features |
|------|-------|----------|
| Lab Tests Home | `/lab-tests` | Categories, packages, popular tests, how it works |
| Book Test | `/lab-tests/book` | Multi-step booking flow (schedule, details, confirm) |
| Bookings List | `/lab-tests/bookings` | Booking history with report access |

---

## API Endpoints

### Cart APIs
```
GET    /api/v1/cart                          - Get cart
POST   /api/v1/cart/items                    - Add item
PUT    /api/v1/cart/items/{productId}        - Update item
DELETE /api/v1/cart/items/{productId}        - Remove item
DELETE /api/v1/cart                          - Clear cart
POST   /api/v1/cart/coupon                   - Apply coupon
DELETE /api/v1/cart/coupon                   - Remove coupon
GET    /api/v1/cart/summary                  - Get summary
POST   /api/v1/cart/prescription/{id}        - Add from prescription
```

### Order APIs
```
POST   /api/v1/orders                        - Place order
GET    /api/v1/orders                        - Get user orders
GET    /api/v1/orders/{orderId}              - Get order details
GET    /api/v1/orders/number/{orderNumber}   - Get by order number
GET    /api/v1/orders/{orderId}/tracking     - Get tracking info
POST   /api/v1/orders/{orderId}/confirm-payment - Confirm payment
POST   /api/v1/orders/{orderId}/cancel       - Cancel order
PUT    /api/v1/orders/{orderId}/status       - Update status (partner)
GET    /api/v1/orders/partner                - Get partner orders
```

### Address APIs
```
GET    /api/v1/addresses                     - Get user addresses
GET    /api/v1/addresses/{addressId}         - Get address
POST   /api/v1/addresses                     - Add address
PUT    /api/v1/addresses/{addressId}         - Update address
DELETE /api/v1/addresses/{addressId}         - Delete address
PUT    /api/v1/addresses/{addressId}/default - Set default
GET    /api/v1/addresses/default             - Get default
```

### Lab Test APIs
```
GET    /api/v1/lab-tests/search              - Search tests
GET    /api/v1/lab-tests/{testId}            - Get test details
GET    /api/v1/lab-tests/category/{id}       - Get by category
GET    /api/v1/lab-tests/popular             - Get popular tests
GET    /api/v1/lab-tests/packages            - Get packages
GET    /api/v1/lab-tests/packages/{id}       - Get package details
GET    /api/v1/lab-tests/packages/popular    - Get popular packages
```

### Lab Booking APIs
```
GET    /api/v1/lab-bookings/slots            - Get available slots
POST   /api/v1/lab-bookings                  - Create booking
GET    /api/v1/lab-bookings                  - Get user bookings
GET    /api/v1/lab-bookings/{bookingId}      - Get booking details
POST   /api/v1/lab-bookings/{id}/confirm-payment - Confirm payment
POST   /api/v1/lab-bookings/{id}/cancel      - Cancel booking
POST   /api/v1/lab-bookings/{id}/reschedule  - Reschedule booking
PUT    /api/v1/lab-bookings/{id}/status      - Update status (partner)
POST   /api/v1/lab-bookings/{id}/upload-report - Upload report (partner)
GET    /api/v1/lab-bookings/partner          - Get partner bookings
```

### Partner APIs
```
GET    /api/v1/partners                      - Get partners by type
GET    /api/v1/partners/{partnerId}          - Get partner details
GET    /api/v1/partners/nearby               - Get nearby partners
GET    /api/v1/partners/pharmacies           - Get pharmacies
GET    /api/v1/partners/labs                 - Get lab partners
```

---

## Kafka Topics

| Topic | Publisher | Events |
|-------|-----------|--------|
| `order-events` | OrderEventPublisher | ORDER_CREATED, ORDER_CONFIRMED, ORDER_STATUS_UPDATED, ORDER_CANCELLED |
| `lab-booking-events` | LabBookingEventPublisher | LAB_BOOKING_CREATED, LAB_BOOKING_CONFIRMED, LAB_BOOKING_STATUS_UPDATED, LAB_BOOKING_CANCELLED, LAB_REPORT_UPLOADED |

---

## Database Schema

### Core Tables
- `partners` - Pharmacy and lab partner data
- `partner_inventory` - Pharmacy stock/pricing
- `delivery_addresses` - User addresses
- `orders` - Main order table
- `order_items` - Order line items
- `order_status_history` - Status audit trail

### Lab Test Tables
- `test_categories` - Test categorization
- `lab_tests` - Test catalog
- `test_packages` - Bundled packages
- `collection_slots` - Available time slots
- `phlebotomists` - Collection staff
- `lab_bookings` - Booking records

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2.2 with WebFlux |
| Database | PostgreSQL with R2DBC |
| Cache | Redis (cart storage, 7-day TTL) |
| Events | Apache Kafka |
| Search | Elasticsearch |
| Storage | AWS S3 (lab reports) |
| Real-time | WebSocket/STOMP |
| Frontend | Next.js 14, React, Tailwind CSS |

---

## Integration Points

1. **Prescription Service**: Fetch prescription items for cart population
2. **Payment Service**: Process order payments via Razorpay
3. **EHR Service**: Share lab reports for patient health records
4. **Notification Service**: Send order/booking status notifications
5. **User Service**: Validate user tokens and fetch profile data

---

## Files Created

### Backend (order-service)
```
backend/order-service/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/com/healthapp/order/
    │   ├── OrderServiceApplication.java
    │   ├── config/
    │   │   ├── RedisConfig.java
    │   │   ├── KafkaConfig.java
    │   │   ├── WebClientConfig.java
    │   │   └── WebSocketConfig.java
    │   ├── controller/
    │   │   ├── CartController.java
    │   │   ├── OrderController.java
    │   │   ├── AddressController.java
    │   │   ├── LabTestController.java
    │   │   ├── LabBookingController.java
    │   │   └── PartnerController.java
    │   ├── domain/
    │   │   ├── Order.java, OrderItem.java
    │   │   ├── DeliveryAddress.java, OrderStatusHistory.java
    │   │   ├── Partner.java, PartnerInventory.java
    │   │   ├── LabTest.java, TestCategory.java, TestPackage.java
    │   │   ├── LabBooking.java, CollectionSlot.java, Phlebotomist.java
    │   │   └── Cart.java, CartItem.java
    │   ├── dto/
    │   │   ├── AddToCartRequest.java, UpdateCartItemRequest.java
    │   │   ├── CartResponse.java, CartItemResponse.java, CartSummary.java
    │   │   ├── PlaceOrderRequest.java, OrderResponse.java, OrderItemResponse.java
    │   │   ├── AddressRequest.java, AddressResponse.java
    │   │   ├── TrackingInfo.java, TrackingStep.java
    │   │   ├── LabTestResponse.java, TestPackageResponse.java
    │   │   ├── CreateLabBookingRequest.java, LabBookingResponse.java
    │   │   └── AvailableSlotResponse.java
    │   ├── enums/
    │   │   ├── OrderType.java, OrderStatus.java, PaymentStatus.java
    │   │   ├── DeliveryType.java, PartnerType.java, AddressType.java
    │   │   └── LabBookingStatus.java, BookingType.java
    │   ├── event/
    │   │   ├── OrderEvent.java, OrderEventPublisher.java
    │   │   └── LabBookingEvent.java, LabBookingEventPublisher.java
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── OrderNotFoundException.java, BookingNotFoundException.java
    │   │   ├── InvalidOrderStateException.java, InsufficientStockException.java
    │   │   ├── InvalidCouponException.java, SlotNotAvailableException.java
    │   ├── repository/
    │   │   ├── OrderRepository.java, OrderItemRepository.java
    │   │   ├── DeliveryAddressRepository.java, OrderStatusHistoryRepository.java
    │   │   ├── PartnerRepository.java, PartnerInventoryRepository.java
    │   │   ├── LabTestRepository.java, TestCategoryRepository.java, TestPackageRepository.java
    │   │   └── LabBookingRepository.java, CollectionSlotRepository.java, PhlebotomistRepository.java
    │   └── service/
    │       ├── CartService.java
    │       ├── AddressService.java
    │       ├── OrderService.java
    │       ├── PharmacyAssignmentService.java
    │       └── LabBookingService.java
    └── resources/
        ├── application.yml
        └── db/migration/V1__create_order_tables.sql
```

### Kubernetes
```
k8s/services/order-service.yaml
```

### Frontend (patient-webapp)
```
frontend/patient-webapp/src/app/
├── pharmacy/
│   ├── page.tsx
│   ├── cart/page.tsx
│   ├── checkout/page.tsx
│   ├── upload-prescription/page.tsx
│   └── orders/
│       ├── page.tsx
│       └── [orderNumber]/page.tsx
└── lab-tests/
    ├── page.tsx
    ├── book/page.tsx
    └── bookings/page.tsx
```

---

## Next Steps (Phase 6)

1. **Admin Dashboard**: Partner management, order oversight
2. **Analytics**: Sales reports, inventory analytics
3. **Promotions**: Discount campaigns, loyalty programs
4. **Reviews**: Order and product ratings
5. **Enhanced Search**: Elasticsearch-powered product search
6. **Mobile App**: React Native integration

---

## Notes

- Cart data stored in Redis with 7-day expiration
- WebSocket endpoint for real-time order tracking: `/ws/order-tracking`
- Lab reports uploaded to S3 and optionally shared with EHR
- All endpoints require `X-User-Id` or `X-Partner-Id` headers
- Pagination supported on all list endpoints

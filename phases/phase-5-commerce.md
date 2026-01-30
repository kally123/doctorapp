# Phase 5: Commerce - Detailed Implementation Plan

## Phase Overview

| Attribute | Details |
|-----------|---------|
| **Duration** | 4 Weeks |
| **Start Date** | _Phase 4 End Date + 1 day_ |
| **End Date** | _Start Date + 4 weeks_ |
| **Team Size** | 12-14 members |
| **Goal** | Complete medicine ordering and lab test booking with partner integrations |

---

## Phase 5 Objectives

1. ✅ Build Order Service for medicine ordering
2. ✅ Implement shopping cart with prescription integration
3. ✅ Enable "order from prescription" flow
4. ✅ Integrate pharmacy partner management
5. ✅ Implement real-time order tracking
6. ✅ Build lab test catalog and search
7. ✅ Implement lab test booking with home collection
8. ✅ Enable lab partner report uploads
9. ✅ Integrate lab reports with EHR Service

---

## Prerequisites from Phase 4

Before starting Phase 5, ensure the following are complete:

| Prerequisite | Status |
|--------------|--------|
| Prescription Service deployed and functional | ⬜ |
| EHR Service deployed and functional | ⬜ |
| Payment Service operational (from Phase 2) | ⬜ |
| Medicine database indexed in Elasticsearch | ⬜ |
| User address management available | ⬜ |
| Notification Service operational | ⬜ |
| Kafka event streaming operational | ⬜ |

---

## Team Allocation for Phase 5

| Role | Name | Focus Area |
|------|------|------------|
| Tech Lead | _TBD_ | Architecture, partner integrations, code reviews |
| Backend 1 | _TBD_ | Order Service - Core, Cart |
| Backend 2 | _TBD_ | Order Service - Fulfillment, Tracking |
| Backend 3 | _TBD_ | Lab Test Booking |
| Backend 4 | _TBD_ | Partner Management, Integrations |
| Backend 5 | _TBD_ | Lab Report Integration |
| Frontend 1 | _TBD_ | Patient Web App - Pharmacy Store |
| Frontend 2 | _TBD_ | Patient Web App - Lab Tests |
| Frontend 3 | _TBD_ | Partner Portal (Pharmacy/Lab) |
| DevOps | _TBD_ | Partner API gateway, integrations |
| QA 1 | _TBD_ | Testing - Order flows |
| QA 2 | _TBD_ | Testing - Lab booking flows |

---

## Architecture Overview

### Commerce Services Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         COMMERCE ARCHITECTURE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│                              ┌─────────────────┐                            │
│                              │  Patient App    │                            │
│                              │  ┌───────────┐  │                            │
│                              │  │  Pharmacy │  │                            │
│                              │  │  + Labs   │  │                            │
│                              │  └─────┬─────┘  │                            │
│                              └────────┼────────┘                            │
│                                       │                                      │
│                              ┌────────▼────────┐                            │
│                              │   API Gateway   │                            │
│                              └────────┬────────┘                            │
│                                       │                                      │
│      ┌────────────────────────────────┼────────────────────────────────┐   │
│      │                                │                                 │   │
│      ▼                                ▼                                 ▼   │
│  ┌─────────────────┐    ┌─────────────────────┐    ┌─────────────────┐    │
│  │  ORDER SERVICE  │    │  LAB TEST SERVICE   │    │ PARTNER SERVICE │    │
│  │  ─────────────  │    │  ─────────────────  │    │ ─────────────── │    │
│  │  • Cart         │    │  • Test Catalog     │    │ • Pharmacy Mgmt │    │
│  │  • Checkout     │    │  • Slot Management  │    │ • Lab Mgmt      │    │
│  │  • Order Mgmt   │    │  • Booking          │    │ • Inventory     │    │
│  │  • Tracking     │    │  • Report Upload    │    │ • Payouts       │    │
│  └────────┬────────┘    └──────────┬──────────┘    └────────┬────────┘    │
│           │                        │                         │             │
│           │    ┌───────────────────┼─────────────────────────┘             │
│           │    │                   │                                        │
│           ▼    ▼                   ▼                                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐        │
│  │   PostgreSQL    │    │  Elasticsearch  │    │    MongoDB      │        │
│  │   (Orders)      │    │  (Test Catalog) │    │   (Inventory)   │        │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘        │
│                                                                              │
│           │                        │                                        │
│           ▼                        ▼                                        │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                        Apache Kafka                               │     │
│  │   order.created │ order.shipped │ lab.booked │ report.uploaded   │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│           │                        │                                        │
│           ▼                        ▼                                        │
│  ┌─────────────────┐    ┌─────────────────┐                               │
│  │ Payment Service │    │   EHR Service   │                               │
│  └─────────────────┘    └─────────────────┘                               │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                    PARTNER INTEGRATIONS                           │     │
│  │   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐     │     │
│  │   │ Pharmacy │   │ Pharmacy │   │   Lab    │   │   Lab    │     │     │
│  │   │    A     │   │    B     │   │    A     │   │    B     │     │     │
│  │   └──────────┘   └──────────┘   └──────────┘   └──────────┘     │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Technology Choices

| Component | Technology | Rationale |
|-----------|------------|-----------|
| **Order DB** | PostgreSQL + R2DBC | Transactional consistency |
| **Cart Storage** | Redis | Fast, ephemeral cart data |
| **Test Catalog** | Elasticsearch | Fast search, filters |
| **Inventory** | MongoDB | Flexible partner inventory |
| **Partner API** | REST + Webhooks | Standard integration |
| **Real-time Tracking** | WebSocket + Redis Pub/Sub | Live order updates |

---

## Sprint Breakdown

### Sprint 10 (Week 19-20): Order Service - Medicine Orders

**Sprint Goal**: Patients can order medicines from prescriptions or browse pharmacy. Complete checkout and order tracking.

---

#### DevOps Tasks - Sprint 10

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| D10.1 | Order Service Deployment | K8s manifests, ConfigMaps, Secrets | DevOps | 8 | P0 | Service deployed |
| D10.2 | Redis for Cart | Configure Redis cluster for cart storage | DevOps | 4 | P0 | Redis accessible |
| D10.3 | Partner API Gateway | Set up API gateway for partner integrations | DevOps | 12 | P0 | Gateway operational |
| D10.4 | Webhook Infrastructure | Configure webhook endpoints and retry logic | DevOps | 8 | P0 | Webhooks working |
| D10.5 | Order Tracking WebSocket | Configure WebSocket for real-time tracking | DevOps | 8 | P1 | WebSocket operational |

**DevOps Subtasks:**

<details>
<summary><strong>D10.3 - Partner API Gateway (Detailed Steps)</strong></summary>

```markdown
## Subtasks for D10.3

1. [ ] Set up dedicated API gateway for partner access
2. [ ] Configure API key authentication
3. [ ] Set up rate limiting per partner
4. [ ] Configure IP whitelisting (optional)
5. [ ] Set up request/response logging
6. [ ] Configure SSL/TLS mutual authentication
7. [ ] Create partner onboarding documentation
8. [ ] Set up sandbox environment for testing

## Partner API Structure:
/partner/v1/
├── orders/
│   ├── GET /{orderId}           # Get order details
│   ├── PUT /{orderId}/status    # Update order status
│   └── PUT /{orderId}/tracking  # Update tracking info
├── inventory/
│   ├── PUT /sync                # Bulk inventory sync
│   └── PUT /{medicineId}        # Update single item
└── webhooks/
    └── POST /subscribe          # Subscribe to events

## Authentication:
- API Key in header: X-Partner-API-Key
- Optional: OAuth 2.0 client credentials
```
</details>

---

#### Backend Tasks - Order Service Core

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B15.1 | Create Order Service | Spring Boot 3.x + WebFlux project setup | Backend 1 | 4 | P0 | Service builds and runs |
| B15.2 | Orders Database Schema | Design tables with Flyway migrations | Backend 1 | 12 | P0 | Migrations run successfully |
| B15.3 | Order Entity & Repository | R2DBC models for orders | Backend 1 | 8 | P0 | Repository tests pass |
| B15.4 | Cart Management API | Add/update/remove cart items | Backend 1 | 16 | P0 | Cart operations work |
| B15.5 | Add from Prescription | Parse prescription and add items to cart | Backend 1 | 12 | P0 | Prescription items added |
| B15.6 | Address Management API | CRUD for delivery addresses | Backend 1 | 8 | P0 | Addresses managed |
| B15.7 | Order Placement API | Create order from cart | Backend 2 | 16 | P0 | Orders created |
| B15.8 | Payment Integration | Integrate with Payment Service | Backend 2 | 12 | P0 | Payments processed |
| B15.9 | Pharmacy Assignment | Logic to assign nearest pharmacy | Backend 2 | 16 | P0 | Pharmacy assigned |
| B15.10 | Order Status Updates | Status transition and validation | Backend 2 | 12 | P0 | Status updates work |
| B15.11 | Order Tracking API | Real-time tracking with WebSocket | Backend 2 | 12 | P0 | Tracking works |
| B15.12 | Order Events | Publish to Kafka | Backend 1 | 4 | P0 | Events published |
| B15.13 | Partner Order API | API for pharmacy partners | Backend 4 | 16 | P0 | Partner API works |
| B15.14 | Unit & Integration Tests | 80%+ coverage | Backend 1, 2 | 16 | P0 | Tests pass |

**Database Schema:**

<details>
<summary><strong>B15.2 - Orders Database Schema</strong></summary>

```sql
-- V1__create_order_tables.sql

-- Order type enum
CREATE TYPE order_type AS ENUM ('MEDICINE', 'LAB_TEST');

-- Order status enum
CREATE TYPE order_status AS ENUM (
    'CART',                -- In cart, not yet placed
    'PENDING_PAYMENT',     -- Awaiting payment
    'PAYMENT_FAILED',      -- Payment failed
    'CONFIRMED',           -- Payment successful, order confirmed
    'PROCESSING',          -- Being processed by partner
    'PACKED',              -- Items packed
    'SHIPPED',             -- Handed to delivery
    'OUT_FOR_DELIVERY',    -- With delivery agent
    'DELIVERED',           -- Successfully delivered
    'CANCELLED',           -- Cancelled by user/system
    'RETURN_REQUESTED',    -- Return initiated
    'RETURNED',            -- Return completed
    'REFUNDED'             -- Refund processed
);

-- Payment status
CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED',
    'REFUNDED',
    'PARTIALLY_REFUNDED'
);

-- Main orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number VARCHAR(50) UNIQUE NOT NULL,  -- Human-readable: ORD-2026-001234
    
    -- Customer info
    user_id UUID NOT NULL,
    
    -- Order type
    order_type order_type NOT NULL DEFAULT 'MEDICINE',
    
    -- Prescription reference (for medicine orders)
    prescription_id UUID,
    
    -- Partner info
    partner_id UUID,                    -- Pharmacy or Lab
    partner_type VARCHAR(20),           -- 'PHARMACY', 'LAB'
    partner_name VARCHAR(255),
    partner_accepted_at TIMESTAMP WITH TIME ZONE,
    
    -- Delivery info
    delivery_address_id UUID,
    delivery_address_snapshot JSONB,    -- Snapshot at order time
    delivery_type VARCHAR(20) DEFAULT 'STANDARD',  -- 'EXPRESS', 'STANDARD', 'SCHEDULED'
    scheduled_delivery_date DATE,
    scheduled_delivery_slot VARCHAR(50),  -- '9AM-12PM', '2PM-5PM'
    
    -- Pricing
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    coupon_code VARCHAR(50),
    delivery_fee DECIMAL(10, 2) DEFAULT 0,
    tax_amount DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'INR',
    
    -- Payment
    payment_status payment_status DEFAULT 'PENDING',
    payment_id UUID,                    -- Reference to payment transaction
    payment_method VARCHAR(50),
    paid_at TIMESTAMP WITH TIME ZONE,
    
    -- Status
    status order_status NOT NULL DEFAULT 'CART',
    status_updated_at TIMESTAMP WITH TIME ZONE,
    
    -- Tracking
    tracking_number VARCHAR(100),
    delivery_partner VARCHAR(100),      -- 'DUNZO', 'SWIGGY', 'INTERNAL'
    estimated_delivery TIMESTAMP WITH TIME ZONE,
    actual_delivery TIMESTAMP WITH TIME ZONE,
    
    -- Metadata
    notes TEXT,                         -- Customer notes
    internal_notes TEXT,                -- Internal notes
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancellation_reason TEXT
);

-- Indexes
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_partner ON orders(partner_id);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_orders_created ON orders(created_at);
CREATE INDEX idx_orders_payment ON orders(payment_id);

-- Order items table
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    
    -- Item type
    item_type VARCHAR(20) NOT NULL DEFAULT 'MEDICINE',  -- 'MEDICINE', 'LAB_TEST'
    
    -- Product info (snapshot at order time)
    product_id VARCHAR(50) NOT NULL,    -- Medicine ID or Test ID
    product_name VARCHAR(255) NOT NULL,
    product_description TEXT,
    manufacturer VARCHAR(255),
    
    -- For medicines
    strength VARCHAR(50),
    formulation VARCHAR(50),
    pack_size INT,
    
    -- Pricing
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    discount_percent DECIMAL(5, 2) DEFAULT 0,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    tax_percent DECIMAL(5, 2) DEFAULT 0,
    tax_amount DECIMAL(10, 2) DEFAULT 0,
    total_price DECIMAL(10, 2) NOT NULL,
    
    -- Prescription reference
    prescription_item_id UUID,          -- Link to prescription item
    requires_prescription BOOLEAN DEFAULT FALSE,
    
    -- Fulfillment
    is_available BOOLEAN DEFAULT TRUE,
    substituted_with VARCHAR(255),      -- If item was substituted
    fulfilled_quantity INT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- Delivery addresses
CREATE TABLE delivery_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    
    address_type VARCHAR(20) DEFAULT 'HOME',  -- 'HOME', 'WORK', 'OTHER'
    label VARCHAR(100),                       -- 'My Home', 'Office'
    
    recipient_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    alternate_phone VARCHAR(20),
    
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    landmark VARCHAR(255),
    
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) DEFAULT 'India',
    
    -- Geolocation
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_addresses_user ON delivery_addresses(user_id);

-- Order status history
CREATE TABLE order_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id),
    
    from_status order_status,
    to_status order_status NOT NULL,
    
    changed_by UUID,                    -- User or system
    changed_by_type VARCHAR(20),        -- 'CUSTOMER', 'PARTNER', 'SYSTEM', 'ADMIN'
    
    notes TEXT,
    metadata JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_status_history_order ON order_status_history(order_id);

-- Pharmacy/Lab partners
CREATE TABLE partners (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    partner_type VARCHAR(20) NOT NULL,  -- 'PHARMACY', 'LAB'
    
    -- Business info
    business_name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    registration_number VARCHAR(100),
    license_number VARCHAR(100),
    license_expiry DATE,
    
    -- Contact
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    
    -- Address
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    
    -- Operating hours
    operating_hours JSONB,              -- {"mon": {"open": "09:00", "close": "21:00"}, ...}
    
    -- Service area
    service_radius_km INT DEFAULT 10,
    serviceable_pincodes TEXT[],
    
    -- API integration
    api_key_hash VARCHAR(256),
    webhook_url VARCHAR(500),
    is_api_enabled BOOLEAN DEFAULT FALSE,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMP WITH TIME ZONE,
    
    -- Ratings
    rating DECIMAL(3, 2),
    total_orders INT DEFAULT 0,
    
    -- Commission
    commission_percent DECIMAL(5, 2) DEFAULT 10.00,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_partners_type ON partners(partner_type);
CREATE INDEX idx_partners_location ON partners USING GIST (
    ll_to_earth(latitude, longitude)
);
CREATE INDEX idx_partners_pincodes ON partners USING GIN (serviceable_pincodes);

-- Partner inventory (for real-time availability)
CREATE TABLE partner_inventory (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id UUID NOT NULL REFERENCES partners(id),
    product_id VARCHAR(50) NOT NULL,
    
    quantity_available INT DEFAULT 0,
    unit_price DECIMAL(10, 2),
    mrp DECIMAL(10, 2),
    discount_percent DECIMAL(5, 2) DEFAULT 0,
    
    is_available BOOLEAN DEFAULT TRUE,
    last_synced_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_partner_product UNIQUE (partner_id, product_id)
);

CREATE INDEX idx_inventory_partner ON partner_inventory(partner_id);
CREATE INDEX idx_inventory_product ON partner_inventory(product_id);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_order_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_updated
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_order_timestamp();
```
</details>

<details>
<summary><strong>B15.4 - Cart Management API (Detailed)</strong></summary>

```java
// CartService.java
@Service
@Slf4j
public class CartService {
    
    private final ReactiveRedisTemplate<String, Cart> redisTemplate;
    private final MedicineClient medicineClient;
    private final Duration CART_EXPIRY = Duration.ofDays(7);
    
    /**
     * Get or create cart for user
     */
    public Mono<Cart> getCart(String userId) {
        String key = buildCartKey(userId);
        return redisTemplate.opsForValue().get(key)
            .defaultIfEmpty(Cart.empty(userId));
    }
    
    /**
     * Add item to cart
     */
    public Mono<Cart> addItem(String userId, AddToCartRequest request) {
        return medicineClient.getMedicine(request.getProductId())
            .switchIfEmpty(Mono.error(new ProductNotFoundException(request.getProductId())))
            .flatMap(medicine -> {
                if (medicine.getRequiresPrescription() && request.getPrescriptionId() == null) {
                    return Mono.error(new PrescriptionRequiredException(medicine.getName()));
                }
                return getCart(userId)
                    .map(cart -> cart.addItem(toCartItem(medicine, request)))
                    .flatMap(cart -> saveCart(userId, cart));
            });
    }
    
    /**
     * Update item quantity
     */
    public Mono<Cart> updateQuantity(String userId, String itemId, int quantity) {
        if (quantity <= 0) {
            return removeItem(userId, itemId);
        }
        
        return getCart(userId)
            .map(cart -> cart.updateQuantity(itemId, quantity))
            .flatMap(cart -> saveCart(userId, cart));
    }
    
    /**
     * Remove item from cart
     */
    public Mono<Cart> removeItem(String userId, String itemId) {
        return getCart(userId)
            .map(cart -> cart.removeItem(itemId))
            .flatMap(cart -> saveCart(userId, cart));
    }
    
    /**
     * Add items from prescription
     */
    public Mono<Cart> addFromPrescription(String userId, String prescriptionId) {
        return prescriptionClient.getPrescription(prescriptionId)
            .flatMapMany(prescription -> Flux.fromIterable(prescription.getItems()))
            .flatMap(item -> medicineClient.findByName(item.getMedicineName())
                .map(medicine -> toCartItemFromPrescription(medicine, item, prescriptionId)))
            .collectList()
            .flatMap(items -> getCart(userId)
                .map(cart -> cart.addItems(items))
                .flatMap(cart -> saveCart(userId, cart)));
    }
    
    /**
     * Apply coupon code
     */
    public Mono<Cart> applyCoupon(String userId, String couponCode) {
        return couponService.validateCoupon(couponCode)
            .flatMap(coupon -> getCart(userId)
                .map(cart -> cart.applyCoupon(coupon))
                .flatMap(cart -> saveCart(userId, cart)));
    }
    
    /**
     * Calculate cart totals
     */
    public Mono<CartSummary> getCartSummary(String userId, String addressId) {
        return Mono.zip(
            getCart(userId),
            addressService.getAddress(addressId),
            deliveryService.calculateDeliveryFee(addressId)
        ).map(tuple -> {
            Cart cart = tuple.getT1();
            Address address = tuple.getT2();
            BigDecimal deliveryFee = tuple.getT3();
            
            return CartSummary.builder()
                .items(cart.getItems())
                .itemCount(cart.getTotalItems())
                .subtotal(cart.getSubtotal())
                .discount(cart.getDiscountAmount())
                .deliveryFee(deliveryFee)
                .tax(calculateTax(cart.getSubtotal()))
                .total(cart.getSubtotal()
                    .subtract(cart.getDiscountAmount())
                    .add(deliveryFee)
                    .add(calculateTax(cart.getSubtotal())))
                .deliveryAddress(address)
                .estimatedDelivery(estimateDelivery(address))
                .build();
        });
    }
    
    /**
     * Clear cart after order placement
     */
    public Mono<Void> clearCart(String userId) {
        return redisTemplate.delete(buildCartKey(userId)).then();
    }
    
    private Mono<Cart> saveCart(String userId, Cart cart) {
        String key = buildCartKey(userId);
        return redisTemplate.opsForValue()
            .set(key, cart, CART_EXPIRY)
            .thenReturn(cart);
    }
    
    private String buildCartKey(String userId) {
        return "cart:" + userId;
    }
}

@Value
@Builder
public class Cart {
    String userId;
    List<CartItem> items;
    String couponCode;
    BigDecimal discountAmount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    public Cart addItem(CartItem item) {
        // Check if item already exists, update quantity if so
        Optional<CartItem> existing = items.stream()
            .filter(i -> i.getProductId().equals(item.getProductId()))
            .findFirst();
        
        List<CartItem> newItems = new ArrayList<>(items);
        if (existing.isPresent()) {
            newItems.remove(existing.get());
            newItems.add(existing.get().withQuantity(
                existing.get().getQuantity() + item.getQuantity()));
        } else {
            newItems.add(item);
        }
        
        return this.toBuilder()
            .items(newItems)
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    public BigDecimal getSubtotal() {
        return items.stream()
            .map(CartItem::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getTotalItems() {
        return items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
}

@Value
@Builder
@With
public class CartItem {
    String itemId;              // Unique cart item ID
    String productId;           // Medicine ID
    String productName;
    String manufacturer;
    String strength;
    String formulation;
    int packSize;
    BigDecimal unitPrice;
    BigDecimal mrp;
    int quantity;
    boolean requiresPrescription;
    String prescriptionId;      // If from prescription
    String prescriptionItemId;
    String imageUrl;
    
    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    public BigDecimal getSavings() {
        return mrp.subtract(unitPrice).multiply(BigDecimal.valueOf(quantity));
    }
}
```
</details>

<details>
<summary><strong>B15.9 - Pharmacy Assignment (Detailed)</strong></summary>

```java
// PharmacyAssignmentService.java
@Service
@Slf4j
public class PharmacyAssignmentService {
    
    private final PartnerRepository partnerRepository;
    private final InventoryRepository inventoryRepository;
    
    /**
     * Find best pharmacy for order
     * Criteria:
     * 1. Has all items in stock
     * 2. Within delivery radius
     * 3. Currently open
     * 4. Highest rating
     * 5. Closest distance
     */
    public Mono<Partner> assignPharmacy(Order order, Address deliveryAddress) {
        return findEligiblePharmacies(deliveryAddress)
            .filterWhen(partner -> hasAllItemsInStock(partner, order.getItems()))
            .filter(partner -> isCurrentlyOpen(partner))
            .sort(Comparator
                .comparing(Partner::getRating).reversed()
                .thenComparing(p -> calculateDistance(p, deliveryAddress)))
            .next()
            .switchIfEmpty(Mono.error(new NoPharmacyAvailableException(
                "No pharmacy available for this order")));
    }
    
    /**
     * Find pharmacies within delivery radius
     */
    private Flux<Partner> findEligiblePharmacies(Address address) {
        return partnerRepository.findNearbyPharmacies(
            address.getLatitude(),
            address.getLongitude(),
            50  // Max radius in km
        )
        .filter(partner -> 
            partner.getServiceablePincodes().contains(address.getPostalCode()) ||
            partner.getServiceablePincodes().isEmpty()
        );
    }
    
    /**
     * Check if pharmacy has all items
     */
    private Mono<Boolean> hasAllItemsInStock(Partner partner, List<OrderItem> items) {
        return Flux.fromIterable(items)
            .flatMap(item -> inventoryRepository
                .findByPartnerAndProduct(partner.getId(), item.getProductId())
                .map(inv -> inv.getQuantityAvailable() >= item.getQuantity())
                .defaultIfEmpty(false))
            .all(available -> available);
    }
    
    /**
     * Check if pharmacy is currently open
     */
    private boolean isCurrentlyOpen(Partner partner) {
        LocalTime now = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        
        Map<String, OperatingHours> hours = partner.getOperatingHours();
        String dayKey = today.name().toLowerCase().substring(0, 3);
        
        if (!hours.containsKey(dayKey)) {
            return false;
        }
        
        OperatingHours todayHours = hours.get(dayKey);
        return now.isAfter(todayHours.getOpen()) && now.isBefore(todayHours.getClose());
    }
    
    /**
     * Calculate distance using Haversine formula
     */
    private double calculateDistance(Partner partner, Address address) {
        double lat1 = Math.toRadians(partner.getLatitude());
        double lat2 = Math.toRadians(address.getLatitude());
        double lon1 = Math.toRadians(partner.getLongitude());
        double lon2 = Math.toRadians(address.getLongitude());
        
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.pow(Math.sin(dLon / 2), 2);
        
        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6371; // Earth radius in km
        
        return c * r;
    }
}

// Alternative algorithm for multi-pharmacy orders
@Service
public class MultiPharmacyAssignmentService {
    
    /**
     * Split order across multiple pharmacies if needed
     * to maximize fulfillment
     */
    public Mono<List<PartnerAssignment>> assignMultiplePharmacies(
        Order order, 
        Address address
    ) {
        // 1. Try single pharmacy first
        return pharmacyAssignmentService.assignPharmacy(order, address)
            .map(partner -> List.of(new PartnerAssignment(partner, order.getItems())))
            .onErrorResume(NoPharmacyAvailableException.class, ex -> {
                // 2. Fall back to multi-pharmacy split
                return splitOrderAcrossPharmacies(order, address);
            });
    }
    
    private Mono<List<PartnerAssignment>> splitOrderAcrossPharmacies(
        Order order, 
        Address address
    ) {
        // Implementation for splitting order across multiple pharmacies
        // to maximize fulfillment
        // ...
    }
}
```
</details>

<details>
<summary><strong>B15.11 - Order Tracking API (Detailed)</strong></summary>

```java
// OrderTrackingService.java
@Service
@Slf4j
public class OrderTrackingService {
    
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ReactiveRedisTemplate<String, TrackingUpdate> redisTemplate;
    
    /**
     * Get current tracking info
     */
    public Mono<TrackingInfo> getTrackingInfo(String orderId) {
        return orderRepository.findById(orderId)
            .map(this::buildTrackingInfo);
    }
    
    /**
     * Update tracking (from delivery partner webhook)
     */
    public Mono<Order> updateTracking(String orderId, TrackingUpdateRequest request) {
        return orderRepository.findById(orderId)
            .flatMap(order -> {
                // Update order
                Order updated = order.toBuilder()
                    .status(request.getStatus())
                    .trackingNumber(request.getTrackingNumber())
                    .estimatedDelivery(request.getEstimatedDelivery())
                    .statusUpdatedAt(Instant.now())
                    .build();
                
                // Add to history
                OrderStatusHistory history = OrderStatusHistory.builder()
                    .orderId(orderId)
                    .fromStatus(order.getStatus())
                    .toStatus(request.getStatus())
                    .changedByType("DELIVERY_PARTNER")
                    .notes(request.getNotes())
                    .metadata(Map.of(
                        "location", request.getLocation(),
                        "trackingNumber", request.getTrackingNumber()
                    ))
                    .build();
                
                return Mono.zip(
                    orderRepository.save(updated),
                    statusHistoryRepository.save(history)
                ).map(Tuple2::getT1);
            })
            .doOnSuccess(order -> {
                // Broadcast to WebSocket subscribers
                broadcastTrackingUpdate(order);
                
                // Publish event
                publishTrackingEvent(order);
            });
    }
    
    /**
     * Subscribe to real-time tracking updates
     */
    public Flux<TrackingUpdate> subscribeToTracking(String orderId) {
        return redisTemplate.listenToChannel("order-tracking:" + orderId)
            .map(ReactiveSubscription.Message::getMessage);
    }
    
    private void broadcastTrackingUpdate(Order order) {
        TrackingUpdate update = TrackingUpdate.builder()
            .orderId(order.getId())
            .status(order.getStatus())
            .statusText(getStatusText(order.getStatus()))
            .estimatedDelivery(order.getEstimatedDelivery())
            .lastUpdated(Instant.now())
            .build();
        
        // Send to WebSocket topic
        messagingTemplate.convertAndSend(
            "/topic/order-tracking/" + order.getId(),
            update
        );
        
        // Also publish to Redis for scaling
        redisTemplate.convertAndSend(
            "order-tracking:" + order.getId(),
            update
        ).subscribe();
    }
    
    private TrackingInfo buildTrackingInfo(Order order) {
        List<TrackingStep> steps = buildTrackingSteps(order);
        
        return TrackingInfo.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .currentStatus(order.getStatus())
            .statusText(getStatusText(order.getStatus()))
            .trackingNumber(order.getTrackingNumber())
            .deliveryPartner(order.getDeliveryPartner())
            .estimatedDelivery(order.getEstimatedDelivery())
            .partnerName(order.getPartnerName())
            .partnerPhone(getPartnerPhone(order))
            .steps(steps)
            .canCancel(canCancel(order))
            .canReturn(canReturn(order))
            .build();
    }
    
    private List<TrackingStep> buildTrackingSteps(Order order) {
        List<TrackingStep> steps = new ArrayList<>();
        
        steps.add(TrackingStep.builder()
            .status(OrderStatus.CONFIRMED)
            .title("Order Confirmed")
            .description("Your order has been placed successfully")
            .timestamp(order.getCreatedAt())
            .isComplete(order.getStatus().ordinal() >= OrderStatus.CONFIRMED.ordinal())
            .isCurrent(order.getStatus() == OrderStatus.CONFIRMED)
            .build());
        
        steps.add(TrackingStep.builder()
            .status(OrderStatus.PROCESSING)
            .title("Processing")
            .description("Pharmacy is preparing your order")
            .isComplete(order.getStatus().ordinal() >= OrderStatus.PROCESSING.ordinal())
            .isCurrent(order.getStatus() == OrderStatus.PROCESSING)
            .build());
        
        steps.add(TrackingStep.builder()
            .status(OrderStatus.PACKED)
            .title("Packed")
            .description("Your order has been packed")
            .isComplete(order.getStatus().ordinal() >= OrderStatus.PACKED.ordinal())
            .isCurrent(order.getStatus() == OrderStatus.PACKED)
            .build());
        
        steps.add(TrackingStep.builder()
            .status(OrderStatus.SHIPPED)
            .title("Shipped")
            .description("Order handed to delivery partner")
            .isComplete(order.getStatus().ordinal() >= OrderStatus.SHIPPED.ordinal())
            .isCurrent(order.getStatus() == OrderStatus.SHIPPED)
            .build());
        
        steps.add(TrackingStep.builder()
            .status(OrderStatus.OUT_FOR_DELIVERY)
            .title("Out for Delivery")
            .description("Delivery partner is on the way")
            .isComplete(order.getStatus().ordinal() >= OrderStatus.OUT_FOR_DELIVERY.ordinal())
            .isCurrent(order.getStatus() == OrderStatus.OUT_FOR_DELIVERY)
            .build());
        
        steps.add(TrackingStep.builder()
            .status(OrderStatus.DELIVERED)
            .title("Delivered")
            .description("Order delivered successfully")
            .timestamp(order.getActualDelivery())
            .isComplete(order.getStatus() == OrderStatus.DELIVERED)
            .isCurrent(order.getStatus() == OrderStatus.DELIVERED)
            .build());
        
        return steps;
    }
}

@Value
@Builder
public class TrackingInfo {
    String orderId;
    String orderNumber;
    OrderStatus currentStatus;
    String statusText;
    String trackingNumber;
    String deliveryPartner;
    Instant estimatedDelivery;
    String partnerName;
    String partnerPhone;
    List<TrackingStep> steps;
    boolean canCancel;
    boolean canReturn;
}

@Value
@Builder
public class TrackingStep {
    OrderStatus status;
    String title;
    String description;
    Instant timestamp;
    boolean isComplete;
    boolean isCurrent;
}
```
</details>

---

#### Frontend Tasks - Sprint 10

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F10.1 | Pharmacy Store Page | Medicine listing and categories | Frontend 1 | 20 | P0 | Store page works |
| F10.2 | Medicine Search | Search with autocomplete and filters | Frontend 1 | 12 | P0 | Search works |
| F10.3 | Shopping Cart | Cart sidebar/page with quantities | Frontend 1 | 16 | P0 | Cart works |
| F10.4 | Order from Prescription | Add prescription items to cart | Frontend 1 | 12 | P0 | Prescription flow works |
| F10.5 | Address Management | Add/edit/select delivery address | Frontend 1 | 12 | P0 | Addresses managed |
| F10.6 | Checkout Flow | Review → Payment → Confirmation | Frontend 1 | 20 | P0 | Checkout works |
| F10.7 | Order Confirmation Page | Success page with order details | Frontend 1 | 8 | P0 | Confirmation displays |
| F10.8 | Order Tracking Page | Real-time tracking with steps | Frontend 1 | 16 | P0 | Tracking works |
| F10.9 | Order History Page | List of past orders | Frontend 1 | 12 | P0 | History displays |
| F10.10 | Reorder Flow | Quick reorder from history | Frontend 1 | 8 | P1 | Reorder works |

**Frontend Component Details:**

<details>
<summary><strong>F10.1 - Pharmacy Store Page (Detailed)</strong></summary>

```markdown
## Pharmacy Store Page Layout

┌─────────────────────────────────────────────────────────────────────────┐
│  💊 Pharmacy Store                      [🔍 Search...]    [🛒 Cart (3)] │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  📋 Order from your prescription                      [Upload Rx]  │ │
│  │  Get medicines from your recent prescriptions directly            │ │
│  └────────────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Categories                                                              │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐     │
│  │  💊   │ │  🩹   │ │  🧴   │ │  💉   │ │  👶   │ │  🏥   │     │
│  │Diabetes│ │Cardiac │ │Skin    │ │Vitamins│ │ Baby   │ │ More   │     │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘ └────────┘     │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│  Popular Medicines                                             [See All]│
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐   │
│  │ [Image]      │ │ [Image]      │ │ [Image]      │ │ [Image]      │   │
│  │ Dolo 650mg   │ │ Crocin Advance│ │ Azithral 500│ │ Pan D        │   │
│  │ ₹30  ₹35    │ │ ₹25  ₹28    │ │ ₹120  ₹145  │ │ ₹85  ₹95    │   │
│  │ [Add to Cart]│ │ [Add to Cart]│ │ [Rx Required]│ │ [Add to Cart]│   │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘   │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│  Recently Ordered                                              [See All]│
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                    │
│  │ Amlodipine 5 │ │ Metformin 500│ │ Aspirin 75   │                    │
│  │ ₹45          │ │ ₹25          │ │ ₹12          │                    │
│  │ [Add to Cart]│ │ [Add to Cart]│ │ [Add to Cart]│                    │
│  └──────────────┘ └──────────────┘ └──────────────┘                    │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│  Offers & Discounts                                                      │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  🏷️ FLAT 20% OFF on first order | Use code: FIRST20              │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

<details>
<summary><strong>F10.8 - Order Tracking Page (Detailed)</strong></summary>

```markdown
## Order Tracking Page Layout

┌─────────────────────────────────────────────────────────────────────────┐
│  ← Back to Orders                                    Order #ORD-2026-1234│
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    🚚 OUT FOR DELIVERY                              │ │
│  │               Arriving by 2:00 PM - 4:00 PM                        │ │
│  │                                                                     │ │
│  │  Delivery Partner: Dunzo                                            │ │
│  │  [📞 Call Delivery Partner]                                        │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Tracking                                                           │ │
│  │                                                                     │ │
│  │  ✅ ─────────── ✅ ─────────── ✅ ─────────── ✅ ─────────── ◯     │ │
│  │  Confirmed    Processing    Packed       Shipped     Delivered     │ │
│  │                                                                     │ │
│  ├────────────────────────────────────────────────────────────────────┤ │
│  │                                                                     │ │
│  │  ✅ Order Confirmed                              Jan 30, 10:00 AM  │ │
│  │     Your order has been placed successfully                        │ │
│  │                                                                     │ │
│  │  ✅ Processing                                   Jan 30, 10:15 AM  │ │
│  │     MedPlus Pharmacy is preparing your order                       │ │
│  │                                                                     │ │
│  │  ✅ Packed                                       Jan 30, 11:30 AM  │ │
│  │     Your order has been packed and is ready                        │ │
│  │                                                                     │ │
│  │  ✅ Shipped                                      Jan 30, 12:00 PM  │ │
│  │     Order picked up by delivery partner                            │ │
│  │     Tracking #: DZO-987654321                                      │ │
│  │                                                                     │ │
│  │  🔵 Out for Delivery                             Jan 30, 1:30 PM   │ │
│  │     Delivery partner is on the way                                 │ │
│  │                                                                     │ │
│  │  ◯ Delivered                                                       │ │
│  │     Estimated: 2:00 PM - 4:00 PM                                   │ │
│  │                                                                     │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Delivery Address                                                   │ │
│  │  John Doe                                                           │ │
│  │  123 Main Street, Apartment 4B                                      │ │
│  │  Mumbai, Maharashtra 400001                                         │ │
│  │  📞 +91 98765 43210                                                │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Order Summary                                              ₹285.00│ │
│  │  ─────────────────────────────────────────────────────────────────│ │
│  │  Amlodipine 5mg x 2                                          ₹90.00│ │
│  │  Metformin 500mg x 1                                         ₹45.00│ │
│  │  Aspirin 75mg x 3                                           ₹120.00│ │
│  │  ─────────────────────────────────────────────────────────────────│ │
│  │  Subtotal                                                   ₹255.00│ │
│  │  Delivery Fee                                                ₹30.00│ │
│  │  Discount (FIRST20)                                         -₹51.00│ │
│  │  ─────────────────────────────────────────────────────────────────│ │
│  │  Total Paid                                                 ₹234.00│ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  [Need Help?]                              [Cancel Order] (if eligible) │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

---

### Sprint 11 (Week 21-22): Lab Test Booking

**Sprint Goal**: Patients can search, book lab tests with home collection. Lab partners can upload reports.

---

#### DevOps Tasks - Sprint 11

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| D11.1 | Lab Tests Elasticsearch | Index lab tests catalog | DevOps | 8 | P0 | Tests searchable |
| D11.2 | Lab Partner Portal Deploy | Deploy partner portal | DevOps | 8 | P0 | Portal accessible |
| D11.3 | Report Upload S3 | Configure S3 for lab reports | DevOps | 4 | P0 | Bucket configured |

---

#### Backend Tasks - Lab Test Booking

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B16.1 | Lab Tests Catalog Schema | Design schema for tests and packages | Backend 3 | 12 | P0 | Schema complete |
| B16.2 | Import Lab Tests Data | Load lab tests master data | Backend 3 | 12 | P0 | Tests indexed |
| B16.3 | Lab Test Search API | Search with filters | Backend 3 | 12 | P0 | Search works |
| B16.4 | Lab Partner Management | CRUD for lab partners | Backend 4 | 12 | P0 | Partners managed |
| B16.5 | Lab Test Booking API | Book test with slot selection | Backend 3 | 16 | P0 | Booking works |
| B16.6 | Home Collection Slots | Slot management for home visits | Backend 3 | 16 | P0 | Slots work |
| B16.7 | Report Upload API | Lab uploads reports | Backend 5 | 12 | P0 | Upload works |
| B16.8 | EHR Integration | Add reports to health records | Backend 5 | 12 | P0 | Integration works |
| B16.9 | Lab Order Tracking | Track sample collection and report | Backend 3 | 12 | P0 | Tracking works |
| B16.10 | Lab Partner Portal API | APIs for partner operations | Backend 4 | 16 | P0 | APIs work |
| B16.11 | Unit & Integration Tests | 80%+ coverage | Backend 3, 4, 5 | 16 | P0 | Tests pass |

**Database Schema:**

<details>
<summary><strong>B16.1 - Lab Tests Catalog Schema</strong></summary>

```sql
-- V1__create_lab_test_tables.sql

-- Test category
CREATE TABLE test_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE
);

-- Lab tests catalog
CREATE TABLE lab_tests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    test_code VARCHAR(50) UNIQUE NOT NULL,
    
    -- Basic info
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(100),
    description TEXT,
    
    -- Category
    category_id UUID REFERENCES test_categories(id),
    
    -- Test details
    sample_type VARCHAR(100),           -- 'Blood', 'Urine', 'Stool', 'Swab'
    sample_volume VARCHAR(50),          -- '5ml', '10ml'
    fasting_required BOOLEAN DEFAULT FALSE,
    fasting_hours INT,                  -- Hours of fasting required
    
    -- Preparation
    preparation_instructions TEXT,
    
    -- Reporting
    report_available_in VARCHAR(50),    -- '24 hours', '2-3 days'
    report_format VARCHAR(20) DEFAULT 'PDF',
    
    -- Parameters tested
    parameters JSONB,                   -- List of parameters in this test
    
    -- Pricing (base price, partners may vary)
    base_price DECIMAL(10, 2) NOT NULL,
    mrp DECIMAL(10, 2),
    
    -- Flags
    is_popular BOOLEAN DEFAULT FALSE,
    requires_doctor_referral BOOLEAN DEFAULT FALSE,
    home_collection_available BOOLEAN DEFAULT TRUE,
    
    -- SEO/Search
    keywords TEXT[],
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_lab_tests_category ON lab_tests(category_id);
CREATE INDEX idx_lab_tests_code ON lab_tests(test_code);
CREATE INDEX idx_lab_tests_keywords ON lab_tests USING GIN (keywords);

-- Test packages (bundled tests)
CREATE TABLE test_packages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    package_code VARCHAR(50) UNIQUE NOT NULL,
    
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Included tests
    included_tests UUID[] NOT NULL,     -- Array of lab_test IDs
    
    -- Package pricing
    package_price DECIMAL(10, 2) NOT NULL,
    individual_price DECIMAL(10, 2),    -- Sum of individual test prices
    discount_percent DECIMAL(5, 2),
    
    -- Details
    total_parameters INT,
    sample_types TEXT[],
    fasting_required BOOLEAN DEFAULT FALSE,
    
    -- Targeting
    target_gender VARCHAR(10),          -- 'MALE', 'FEMALE', 'ALL'
    target_age_group VARCHAR(50),       -- 'ADULT', 'SENIOR', 'ALL'
    
    is_popular BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Lab test bookings
CREATE TABLE lab_bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_number VARCHAR(50) UNIQUE NOT NULL,  -- LAB-2026-001234
    
    -- Customer
    user_id UUID NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    patient_age INT,
    patient_gender VARCHAR(10),
    patient_phone VARCHAR(20) NOT NULL,
    
    -- Booking type
    booking_type VARCHAR(20) NOT NULL,  -- 'HOME_COLLECTION', 'WALK_IN'
    
    -- Lab partner
    lab_partner_id UUID NOT NULL REFERENCES partners(id),
    lab_partner_name VARCHAR(255),
    
    -- For home collection
    collection_address_id UUID,
    collection_address_snapshot JSONB,
    scheduled_date DATE NOT NULL,
    scheduled_slot VARCHAR(50),         -- '9AM-11AM', '11AM-1PM'
    
    -- For walk-in
    lab_center_id UUID,
    
    -- Tests booked
    tests JSONB NOT NULL,               -- [{testId, testName, price}, ...]
    package_id UUID,                    -- If booked as package
    
    -- Pricing
    subtotal DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    home_collection_fee DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    
    -- Payment
    payment_status payment_status DEFAULT 'PENDING',
    payment_id UUID,
    paid_at TIMESTAMP WITH TIME ZONE,
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING, CONFIRMED, PHLEBOTOMIST_ASSIGNED, SAMPLE_COLLECTED, 
    -- PROCESSING, REPORT_READY, COMPLETED, CANCELLED
    
    -- Phlebotomist (for home collection)
    phlebotomist_id UUID,
    phlebotomist_name VARCHAR(255),
    phlebotomist_phone VARCHAR(20),
    
    -- Sample tracking
    sample_collected_at TIMESTAMP WITH TIME ZONE,
    sample_received_at_lab TIMESTAMP WITH TIME ZONE,
    
    -- Report
    report_ready_at TIMESTAMP WITH TIME ZONE,
    report_url VARCHAR(500),
    report_document_id UUID,            -- Reference to EHR document
    
    -- Metadata
    notes TEXT,
    internal_notes TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancellation_reason TEXT
);

CREATE INDEX idx_lab_bookings_user ON lab_bookings(user_id);
CREATE INDEX idx_lab_bookings_partner ON lab_bookings(lab_partner_id);
CREATE INDEX idx_lab_bookings_status ON lab_bookings(status);
CREATE INDEX idx_lab_bookings_date ON lab_bookings(scheduled_date);

-- Home collection slots
CREATE TABLE collection_slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_partner_id UUID NOT NULL REFERENCES partners(id),
    
    -- Date and time
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_label VARCHAR(50),             -- '9AM-11AM'
    
    -- Capacity
    max_bookings INT DEFAULT 10,
    current_bookings INT DEFAULT 0,
    
    -- Service area
    serviceable_pincodes TEXT[],
    
    is_available BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_partner_slot UNIQUE (lab_partner_id, slot_date, start_time)
);

CREATE INDEX idx_slots_partner_date ON collection_slots(lab_partner_id, slot_date);

-- Phlebotomists
CREATE TABLE phlebotomists (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_partner_id UUID NOT NULL REFERENCES partners(id),
    
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    
    certification_number VARCHAR(100),
    experience_years INT,
    
    -- Current location (for assignment)
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    last_location_update TIMESTAMP WITH TIME ZONE,
    
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_phlebotomists_partner ON phlebotomists(lab_partner_id);
```
</details>

<details>
<summary><strong>B16.6 - Home Collection Slots (Detailed)</strong></summary>

```java
// HomeCollectionService.java
@Service
@Slf4j
public class HomeCollectionService {
    
    private final CollectionSlotRepository slotRepository;
    private final LabPartnerRepository partnerRepository;
    
    /**
     * Get available slots for date range
     */
    public Flux<AvailableSlot> getAvailableSlots(
        String pincode,
        LocalDate startDate,
        LocalDate endDate
    ) {
        // Find lab partners serving this pincode
        return partnerRepository.findByServiceablePincode(pincode)
            .flatMap(partner -> 
                slotRepository.findAvailableSlots(
                    partner.getId(), 
                    startDate, 
                    endDate
                )
                .map(slot -> toAvailableSlot(slot, partner))
            )
            .filter(slot -> slot.getAvailableCapacity() > 0)
            .sort(Comparator
                .comparing(AvailableSlot::getDate)
                .thenComparing(AvailableSlot::getStartTime));
    }
    
    /**
     * Reserve slot for booking
     */
    public Mono<SlotReservation> reserveSlot(String slotId, String bookingId) {
        return slotRepository.findById(slotId)
            .filter(slot -> slot.getCurrentBookings() < slot.getMaxBookings())
            .switchIfEmpty(Mono.error(new SlotNotAvailableException(slotId)))
            .flatMap(slot -> {
                slot.setCurrentBookings(slot.getCurrentBookings() + 1);
                return slotRepository.save(slot);
            })
            .map(slot -> SlotReservation.builder()
                .slotId(slot.getId())
                .bookingId(bookingId)
                .reservedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
                .build());
    }
    
    /**
     * Assign phlebotomist to booking
     */
    public Mono<Phlebotomist> assignPhlebotomist(LabBooking booking) {
        return phlebotomistRepository
            .findAvailableByPartner(booking.getLabPartnerId())
            .filter(p -> canReachInTime(p, booking))
            .sort(Comparator.comparing(p -> 
                calculateDistance(p, booking.getCollectionAddress())))
            .next()
            .doOnSuccess(phlebotomist -> {
                if (phlebotomist != null) {
                    booking.setPhlebotomistId(phlebotomist.getId());
                    booking.setPhlebotomistName(phlebotomist.getName());
                    booking.setPhlebotomistPhone(phlebotomist.getPhone());
                    bookingRepository.save(booking).subscribe();
                }
            });
    }
    
    /**
     * Generate slots for lab partner
     */
    public Flux<CollectionSlot> generateSlots(
        String partnerId,
        LocalDate startDate,
        LocalDate endDate,
        SlotConfiguration config
    ) {
        return Flux.range(0, (int) ChronoUnit.DAYS.between(startDate, endDate) + 1)
            .map(startDate::plusDays)
            .flatMap(date -> generateSlotsForDate(partnerId, date, config));
    }
    
    private Flux<CollectionSlot> generateSlotsForDate(
        String partnerId,
        LocalDate date,
        SlotConfiguration config
    ) {
        return Flux.fromIterable(config.getSlotDefinitions())
            .map(def -> CollectionSlot.builder()
                .labPartnerId(partnerId)
                .slotDate(date)
                .startTime(def.getStartTime())
                .endTime(def.getEndTime())
                .slotLabel(def.getLabel())
                .maxBookings(def.getMaxBookings())
                .serviceablePincodes(config.getServiceablePincodes())
                .isAvailable(true)
                .build())
            .flatMap(slotRepository::save);
    }
}

@Value
@Builder
public class AvailableSlot {
    String slotId;
    String labPartnerId;
    String labPartnerName;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    String slotLabel;          // "9:00 AM - 11:00 AM"
    int availableCapacity;
    BigDecimal homeCollectionFee;
}

@Value
@Builder
public class SlotConfiguration {
    List<SlotDefinition> slotDefinitions;
    List<String> serviceablePincodes;
}

@Value
@Builder
public class SlotDefinition {
    LocalTime startTime;
    LocalTime endTime;
    String label;
    int maxBookings;
}
```
</details>

<details>
<summary><strong>B16.7 - Report Upload API (Detailed)</strong></summary>

```java
// LabReportService.java
@Service
@Slf4j
public class LabReportService {
    
    private final S3Client s3Client;
    private final LabBookingRepository bookingRepository;
    private final EhrClient ehrClient;
    private final NotificationPublisher notificationPublisher;
    
    /**
     * Upload lab report (called by lab partner)
     */
    public Mono<ReportUploadResult> uploadReport(
        String bookingId,
        String partnerId,
        FilePart reportFile,
        ReportMetadata metadata
    ) {
        return bookingRepository.findById(bookingId)
            .filter(booking -> booking.getLabPartnerId().equals(partnerId))
            .switchIfEmpty(Mono.error(new UnauthorizedException("Partner mismatch")))
            .flatMap(booking -> {
                // Validate file
                return validateReportFile(reportFile)
                    .then(uploadToS3(booking, reportFile))
                    .flatMap(s3Url -> updateBookingWithReport(booking, s3Url, metadata))
                    .flatMap(this::addToPatientEhr)
                    .doOnSuccess(result -> notifyPatient(booking));
            });
    }
    
    /**
     * Get report download URL
     */
    public Mono<String> getReportUrl(String bookingId, String userId) {
        return bookingRepository.findById(bookingId)
            .filter(booking -> booking.getUserId().equals(userId))
            .switchIfEmpty(Mono.error(new UnauthorizedException()))
            .flatMap(booking -> generatePresignedUrl(booking.getReportUrl()));
    }
    
    private Mono<Void> validateReportFile(FilePart file) {
        String contentType = file.headers().getContentType().toString();
        if (!contentType.equals("application/pdf")) {
            return Mono.error(new InvalidFileTypeException("Only PDF reports allowed"));
        }
        return Mono.empty();
    }
    
    private Mono<String> uploadToS3(LabBooking booking, FilePart file) {
        String key = String.format(
            "lab-reports/%s/%s/%s-report.pdf",
            booking.getUserId(),
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")),
            booking.getBookingNumber()
        );
        
        return DataBufferUtils.join(file.content())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                
                return Mono.fromFuture(
                    s3Client.putObject(
                        PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("application/pdf")
                            .serverSideEncryption(ServerSideEncryption.AES256)
                            .build(),
                        AsyncRequestBody.fromBytes(bytes)
                    )
                ).thenReturn(key);
            });
    }
    
    private Mono<LabBooking> updateBookingWithReport(
        LabBooking booking,
        String s3Url,
        ReportMetadata metadata
    ) {
        booking.setReportUrl(s3Url);
        booking.setReportReadyAt(Instant.now());
        booking.setStatus("REPORT_READY");
        
        return bookingRepository.save(booking);
    }
    
    private Mono<ReportUploadResult> addToPatientEhr(LabBooking booking) {
        CreateRecordRequest ehrRequest = CreateRecordRequest.builder()
            .patientId(booking.getUserId())
            .recordType("LAB_REPORT")
            .title(buildReportTitle(booking))
            .recordDate(LocalDate.now())
            .source(RecordSource.builder()
                .type("LAB")
                .sourceId(booking.getLabPartnerId())
                .sourceName(booking.getLabPartnerName())
                .build())
            .documents(List.of(Document.builder()
                .fileName(booking.getBookingNumber() + "-report.pdf")
                .s3Key(booking.getReportUrl())
                .fileType("application/pdf")
                .build()))
            .structuredData(Map.of(
                "bookingNumber", booking.getBookingNumber(),
                "tests", booking.getTests()
            ))
            .build();
        
        return ehrClient.createRecord(ehrRequest)
            .map(ehrRecord -> {
                booking.setReportDocumentId(ehrRecord.getId());
                bookingRepository.save(booking).subscribe();
                
                return ReportUploadResult.builder()
                    .bookingId(booking.getId())
                    .reportUrl(booking.getReportUrl())
                    .ehrRecordId(ehrRecord.getId())
                    .uploadedAt(Instant.now())
                    .build();
            });
    }
    
    private void notifyPatient(LabBooking booking) {
        NotificationRequest notification = NotificationRequest.builder()
            .userId(booking.getUserId())
            .type(NotificationType.LAB_REPORT_READY)
            .title("Lab Report Ready")
            .body(String.format(
                "Your lab report for %s is ready. View it now.",
                getTestNames(booking.getTests())
            ))
            .data(Map.of(
                "bookingId", booking.getId(),
                "action", "VIEW_REPORT"
            ))
            .channels(List.of(Channel.PUSH, Channel.EMAIL, Channel.SMS))
            .build();
        
        notificationPublisher.publish(notification);
    }
}
```
</details>

---

#### Frontend Tasks - Sprint 11

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F11.1 | Lab Tests Page | Test catalog with categories | Frontend 2 | 16 | P0 | Page works |
| F11.2 | Test Search & Filters | Search with filters | Frontend 2 | 12 | P0 | Search works |
| F11.3 | Test Details & Packages | Test info and packages | Frontend 2 | 12 | P0 | Details display |
| F11.4 | Home Collection Slots | Date and slot picker | Frontend 2 | 16 | P0 | Slot picker works |
| F11.5 | Lab Booking Flow | Complete booking checkout | Frontend 2 | 20 | P0 | Booking works |
| F11.6 | Lab Booking Tracking | Track sample and report | Frontend 2 | 12 | P0 | Tracking works |
| F11.7 | Report View/Download | View report in EHR | Frontend 1 | 8 | P0 | Report accessible |
| F11.8 | Partner Portal - Dashboard | Lab partner dashboard | Frontend 3 | 16 | P0 | Dashboard works |
| F11.9 | Partner Portal - Bookings | Manage bookings | Frontend 3 | 16 | P0 | Bookings managed |
| F11.10 | Partner Portal - Upload | Upload reports | Frontend 3 | 12 | P0 | Upload works |

**Frontend Component Details:**

<details>
<summary><strong>F11.4 - Home Collection Slots (Detailed)</strong></summary>

```markdown
## Slot Picker Component

┌─────────────────────────────────────────────────────────────────────────┐
│  Select Date & Time for Sample Collection                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  📍 Collection Address: 123 Main Street, Mumbai 400001        [Change]  │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Select Date                                                        │ │
│  │                                                                     │ │
│  │  ◀  January 2026  ▶                                                │ │
│  │                                                                     │ │
│  │  Sun   Mon   Tue   Wed   Thu   Fri   Sat                          │ │
│  │   -     -    28    29   [30]   31     1                            │ │
│  │   2     3     4     5     6     7     8                            │ │
│  │   9    10    11    12    13    14    15                            │ │
│  │                                                                     │ │
│  │  ● Selected: Thursday, January 30, 2026                            │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Select Time Slot                                                   │ │
│  │                                                                     │ │
│  │  Morning                                                            │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │ │
│  │  │ ○ 6:00-8:00 AM  │  │ ● 8:00-10:00 AM│  │ ○ 10:00-12:00 PM│    │ │
│  │  │   3 slots left   │  │   ✓ Selected   │  │   Full          │    │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘    │ │
│  │                                                                     │ │
│  │  Afternoon                                                          │ │
│  │  ┌─────────────────┐  ┌─────────────────┐                         │ │
│  │  │ ○ 2:00-4:00 PM  │  │ ○ 4:00-6:00 PM  │                         │ │
│  │  │   5 slots left   │  │   2 slots left  │                         │ │
│  │  └─────────────────┘  └─────────────────┘                         │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ⓘ Fasting Required: Please fast for 10-12 hours before sample         │
│    collection. Only water is allowed.                                   │
│                                                                          │
│  Lab Partner: PathLabs Diagnostics                                       │
│  Home Collection Fee: ₹50                                               │
│                                                                          │
│                                      [← Back]  [Continue to Payment →]  │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

<details>
<summary><strong>F11.6 - Lab Booking Tracking (Detailed)</strong></summary>

```markdown
## Lab Booking Tracking Page

┌─────────────────────────────────────────────────────────────────────────┐
│  ← Back to Bookings                               Booking #LAB-2026-5678│
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    🔬 SAMPLE COLLECTED                              │ │
│  │             Report expected by Feb 1, 2026                         │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Tracking                                                           │ │
│  │                                                                     │ │
│  │  ✅ ─────────── ✅ ─────────── ✅ ─────────── ◯ ─────────── ◯     │ │
│  │  Confirmed   Assigned    Collected    Processing   Report Ready    │ │
│  │                                                                     │ │
│  ├────────────────────────────────────────────────────────────────────┤ │
│  │                                                                     │ │
│  │  ✅ Booking Confirmed                            Jan 29, 10:00 AM  │ │
│  │     Your lab test booking is confirmed                             │ │
│  │                                                                     │ │
│  │  ✅ Phlebotomist Assigned                        Jan 30, 7:00 AM   │ │
│  │     Rajesh Kumar | 📞 +91 98765 43210                              │ │
│  │     Will arrive at 8:00 AM - 10:00 AM                              │ │
│  │                                                                     │ │
│  │  ✅ Sample Collected                             Jan 30, 8:45 AM   │ │
│  │     Sample collected successfully                                   │ │
│  │     Tubes: 2 | Sample IDs: SP-001, SP-002                          │ │
│  │                                                                     │ │
│  │  🔵 Processing at Lab                            In Progress       │ │
│  │     PathLabs Diagnostics                                            │ │
│  │     Expected: Feb 1, 2026                                          │ │
│  │                                                                     │ │
│  │  ◯ Report Ready                                                    │ │
│  │     We'll notify you when your report is ready                     │ │
│  │                                                                     │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Tests Booked                                                       │ │
│  │  ─────────────────────────────────────────────────────────────────│ │
│  │  🔬 Complete Blood Count (CBC)                                     │ │
│  │     Parameters: 24 | Report in: 24 hours                           │ │
│  │                                                                     │ │
│  │  🔬 Lipid Profile                                                  │ │
│  │     Parameters: 8 | Report in: 24 hours                            │ │
│  │                                                                     │ │
│  │  🔬 Thyroid Profile (T3, T4, TSH)                                  │ │
│  │     Parameters: 3 | Report in: 24 hours                            │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Collection Details                                                 │ │
│  │  Date: Jan 30, 2026 | Slot: 8:00 AM - 10:00 AM                    │ │
│  │  Address: 123 Main Street, Mumbai 400001                           │ │
│  │  Lab: PathLabs Diagnostics                                          │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  [Need Help?]                                                            │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

---

## Kafka Events

### Events Published by Order Service

| Event Type | Trigger | Payload | Consumers |
|------------|---------|---------|-----------|
| `order.created` | Order placed | orderId, userId, items, total | Notification |
| `order.confirmed` | Payment success | orderId, partnerId | Partner Service |
| `order.shipped` | Order shipped | orderId, trackingNumber | Notification |
| `order.delivered` | Order delivered | orderId, deliveredAt | Notification, Analytics |
| `order.cancelled` | Order cancelled | orderId, reason | Payment (refund) |

### Events Published by Lab Service

| Event Type | Trigger | Payload | Consumers |
|------------|---------|---------|-----------|
| `lab.booked` | Booking confirmed | bookingId, tests, slot | Notification |
| `lab.phlebotomist.assigned` | Assigned | bookingId, phlebotomist | Notification |
| `lab.sample.collected` | Sample collected | bookingId, sampleIds | Notification |
| `lab.report.uploaded` | Report ready | bookingId, reportUrl | EHR, Notification |

---

## API Endpoints

### Order Service APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/cart` | Get cart | Patient |
| POST | `/api/v1/cart/items` | Add to cart | Patient |
| PUT | `/api/v1/cart/items/{id}` | Update quantity | Patient |
| DELETE | `/api/v1/cart/items/{id}` | Remove from cart | Patient |
| POST | `/api/v1/cart/prescription/{id}` | Add from prescription | Patient |
| POST | `/api/v1/cart/coupon` | Apply coupon | Patient |
| GET | `/api/v1/cart/summary` | Get cart summary | Patient |
| POST | `/api/v1/orders` | Place order | Patient |
| GET | `/api/v1/orders/{id}` | Get order | Patient |
| GET | `/api/v1/orders` | List orders | Patient |
| GET | `/api/v1/orders/{id}/tracking` | Get tracking | Patient |
| PUT | `/api/v1/orders/{id}/cancel` | Cancel order | Patient |
| GET | `/api/v1/addresses` | List addresses | Patient |
| POST | `/api/v1/addresses` | Add address | Patient |
| PUT | `/api/v1/addresses/{id}` | Update address | Patient |
| DELETE | `/api/v1/addresses/{id}` | Delete address | Patient |

### Lab Test APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/lab-tests` | List tests | Public |
| GET | `/api/v1/lab-tests/{id}` | Get test details | Public |
| GET | `/api/v1/lab-tests/search` | Search tests | Public |
| GET | `/api/v1/lab-tests/packages` | List packages | Public |
| GET | `/api/v1/lab-tests/categories` | List categories | Public |
| GET | `/api/v1/lab-bookings/slots` | Get available slots | Patient |
| POST | `/api/v1/lab-bookings` | Create booking | Patient |
| GET | `/api/v1/lab-bookings/{id}` | Get booking | Patient |
| GET | `/api/v1/lab-bookings` | List bookings | Patient |
| GET | `/api/v1/lab-bookings/{id}/tracking` | Get tracking | Patient |
| PUT | `/api/v1/lab-bookings/{id}/cancel` | Cancel booking | Patient |
| GET | `/api/v1/lab-bookings/{id}/report` | Download report | Patient |

### Partner Portal APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/partner/v1/orders` | List orders for partner | Partner |
| PUT | `/partner/v1/orders/{id}/status` | Update status | Partner |
| PUT | `/partner/v1/orders/{id}/tracking` | Update tracking | Partner |
| PUT | `/partner/v1/inventory` | Sync inventory | Partner |
| GET | `/partner/v1/lab-bookings` | List lab bookings | Lab Partner |
| PUT | `/partner/v1/lab-bookings/{id}/status` | Update status | Lab Partner |
| POST | `/partner/v1/lab-bookings/{id}/report` | Upload report | Lab Partner |

---

## Phase 5 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Order Service deployed to dev | End of Week 19 | ⬜ |
| Cart functionality working | End of Week 19 | ⬜ |
| Medicine ordering from prescription | End of Week 20 | ⬜ |
| Pharmacy assignment working | End of Week 20 | ⬜ |
| Order tracking working | End of Week 20 | ⬜ |
| Partner portal for pharmacy | End of Week 20 | ⬜ |
| Lab test catalog indexed | End of Week 21 | ⬜ |
| Lab test booking working | End of Week 21 | ⬜ |
| Home collection slots working | End of Week 22 | ⬜ |
| Report upload and EHR integration | End of Week 22 | ⬜ |
| Partner portal for lab | End of Week 22 | ⬜ |
| All tests passing | End of Week 22 | ⬜ |

---

## Definition of Done - Phase 5

- [ ] All tasks completed and code merged to main branch
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests passing
- [ ] API documentation updated (OpenAPI/Swagger)
- [ ] No P0/P1 bugs open
- [ ] Partner portal functional
- [ ] Medicine ordering demo successful
- [ ] Lab booking demo successful
- [ ] Order tracking real-time updates working
- [ ] Report upload and EHR sync working
- [ ] DevOps monitoring dashboards configured

---

## Dependencies on Other Teams

| Dependency | From Team | Description | Required By |
|------------|-----------|-------------|-------------|
| Medicine database | Prescription Service | Medicine catalog | Week 19 |
| Payment processing | Payment Service | Order payments | Week 19 |
| Prescription data | Prescription Service | Order from Rx | Week 19 |
| EHR integration | EHR Service | Lab report storage | Week 22 |
| Notifications | Notification Service | Order/booking updates | Week 19 |
| User addresses | User Service | Delivery addresses | Week 19 |

---

## Appendix

### A. Environment Variables

```yaml
# Order Service
REDIS_HOST: ${vault:redis/host}
REDIS_PORT: 6379
PAYMENT_SERVICE_URL: http://payment-service:8080
PRESCRIPTION_SERVICE_URL: http://prescription-service:8080

# Lab Service
ELASTICSEARCH_HOST: ${vault:elasticsearch/host}
S3_BUCKET_LAB_REPORTS: healthcare-lab-reports

# Partner Integration
PARTNER_API_GATEWAY_URL: https://partner-api.healthapp.com
WEBHOOK_SECRET: ${vault:partner/webhook_secret}
```

### B. Order Status Transitions

```
CART → PENDING_PAYMENT → CONFIRMED → PROCESSING → PACKED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
          ↓                   ↓          ↓          ↓          ↓
     PAYMENT_FAILED      CANCELLED   CANCELLED  CANCELLED  CANCELLED
                                                              ↓
                                               RETURN_REQUESTED → RETURNED → REFUNDED
```

### C. Lab Booking Status Transitions

```
PENDING → CONFIRMED → PHLEBOTOMIST_ASSIGNED → SAMPLE_COLLECTED → PROCESSING → REPORT_READY → COMPLETED
    ↓         ↓              ↓                       ↓
CANCELLED  CANCELLED     CANCELLED              (cannot cancel after collection)
```

### D. Partner Webhook Events

```json
// Order status update webhook
POST /partner/webhooks/order-update
{
    "event": "order.status_updated",
    "timestamp": "2026-01-30T14:30:00Z",
    "data": {
        "orderId": "order-uuid",
        "orderNumber": "ORD-2026-001234",
        "previousStatus": "PROCESSING",
        "newStatus": "PACKED",
        "trackingNumber": "TRK-987654",
        "estimatedDelivery": "2026-01-30T18:00:00Z"
    },
    "signature": "sha256=..."
}

// Lab report uploaded webhook
POST /webhooks/lab-report
{
    "event": "lab.report_uploaded",
    "timestamp": "2026-01-30T10:00:00Z",
    "data": {
        "bookingId": "booking-uuid",
        "bookingNumber": "LAB-2026-005678",
        "reportUrl": "https://s3.../report.pdf",
        "testsCovered": ["CBC", "Lipid Profile"]
    },
    "signature": "sha256=..."
}
```

---

*Document Version: 1.0*  
*Last Updated: January 30, 2026*  
*Author: Healthcare Platform Team*

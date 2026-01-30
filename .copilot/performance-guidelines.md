# Performance Guidelines

## Caching Strategies

### In-Memory Caching
- **Small Datasets**: Use in-memory cache for frequently accessed small data sets
- **Quick Access**: Avoid database calls for repetitive data fetching
- **Memory Efficient**: Best for data that fits comfortably in application memory

```java
// ✅ Good: In-memory caching for small datasets
@Service
@CacheConfig(cacheNames = "currencies")
public class CurrencyService {
    
    @Cacheable(key = "#code")
    public Currency findByCode(String code) {
        return currencyRepository.findByCode(code);
    }
    
    @CacheEvict(allEntries = true)
    @Scheduled(fixedRate = 3600000) // Refresh every hour
    public void refreshCurrencyCache() {
        log.info("Refreshing currency cache");
    }
}
```

### Distributed Caching with Redis
- **Large Datasets**: Use Redis for bigger data that exceeds memory limits
- **Scalability**: Shared cache across multiple application instances
- **Persistence**: Data survives application restarts

```java
// ✅ Good: Redis distributed caching
@Service
public class ProductService {
    
    private final RedisTemplate<String, ProductDto> redisTemplate;
    private final ProductRepository productRepository;
    
    public ProductDto findById(String productId) {
        String cacheKey = "product:" + productId;
        
        // Try cache first
        ProductDto cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Fallback to database
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
            
        ProductDto dto = productMapper.toDto(product);
        
        // Cache for 1 hour
        redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofHours(1));
        
        return dto;
    }
}
```

## Database Query Optimization

### Minimize Database Queries
```java
// ❌ Bad: Multiple database calls
public List<OrderSummaryDto> getOrderSummaries(List<String> orderIds) {
    List<OrderSummaryDto> summaries = new ArrayList<>();
    for (String orderId : orderIds) {
        Order order = orderRepository.findById(orderId).orElse(null); // N+1 problem!
        if (order != null) {
            summaries.add(createSummary(order));
        }
    }
    return summaries;
}

// ✅ Good: Single database call
public List<OrderSummaryDto> getOrderSummaries(List<String> orderIds) {
    List<Order> orders = orderRepository.findAllById(orderIds);
    return orders.stream()
        .map(this::createSummary)
        .collect(Collectors.toList());
}
```

### Batch Operations
```java
// ✅ Good: Batch insert/update operations
@Service
@Transactional
public class OrderBatchService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    // Batch insert
    public void createOrders(List<CreateOrderRequest> requests) {
        List<Order> orders = requests.stream()
            .map(orderMapper::toEntity)
            .collect(Collectors.toList());
            
        orderRepository.saveAll(orders); // Single batch operation
    }
    
    // Batch update using custom query
    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id IN :orderIds")
    public int updateOrderStatus(@Param("orderIds") List<String> orderIds, 
                                @Param("status") OrderStatus status) {
        return orderRepository.updateOrderStatus(orderIds, status);
    }
}
```

### Avoid findById for Complex Objects
```java
// ❌ Bad: Using findById for complex objects with relationships
public OrderDetailsDto getOrderDetails(String orderId) {
    Order order = orderRepository.findById(orderId).orElse(null); // Loads unnecessary data
    return orderMapper.toDetailsDto(order);
}

// ✅ Good: Custom query binding to DTO/record
@Query("""
    SELECT new com.example.dto.OrderDetailsDto(
        o.id, o.customerName, o.totalAmount, o.status, o.createdAt
    ) FROM Order o WHERE o.id = :orderId
    """)
OrderDetailsDto findOrderDetailsById(@Param("orderId") String orderId);

// ✅ Good: Using records for performance
public record OrderDetailsDto(
    String id,
    String customerName,
    BigDecimal totalAmount,
    OrderStatus status,
    LocalDateTime createdAt
) {}
```

### Use Update Queries Instead of Save
```java
// ❌ Bad: Loading entire entity to update few properties
public void updateOrderStatus(String orderId, OrderStatus status) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.setStatus(status);
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order); // Saves entire entity
}

// ✅ Good: Direct update query for specific properties
@Modifying
@Query("UPDATE Order o SET o.status = :status, o.updatedAt = :updatedAt WHERE o.id = :orderId")
int updateOrderStatus(@Param("orderId") String orderId, 
                     @Param("status") OrderStatus status,
                     @Param("updatedAt") LocalDateTime updatedAt);
```

## Asynchronous Processing

### Use @Async and Virtual Threads
```java
// ✅ Good: Asynchronous processing for non-blocking operations
@Service
public class NotificationService {
    
    @Async("taskExecutor")
    public CompletableFuture<Void> sendEmailNotification(String email, String message) {
        // Long-running email operation
        emailClient.sendEmail(email, message);
        return CompletableFuture.completedFuture(null);
    }
    
    @Async("taskExecutor")
    public CompletableFuture<Void> processOrderAsync(Order order) {
        // Heavy processing that doesn't need to block the main thread
        performComplexCalculations(order);
        updateInventory(order);
        return CompletableFuture.completedFuture(null);
    }
}

// Configuration for virtual threads (Java 21+)
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

### Kafka for Reliable Communication
```java
// ✅ Good: Use Kafka instead of synchronous REST calls
@Service
public class OrderEventService {
    
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    // Asynchronous, reliable communication
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .totalAmount(order.getTotalAmount())
            .timestamp(LocalDateTime.now())
            .build();
            
        kafkaTemplate.send("order-events", order.getId(), event);
    }
}

// ❌ Avoid: Synchronous REST calls for non-critical operations
public void notifyPaymentService(Order order) {
    // This blocks the thread and creates tight coupling
    paymentRestClient.notifyOrderCreated(order);
}
```

## Resource Optimization

### Clean Up Unused Components
```java
// ✅ Good: Remove unused services and schedulers
@Component
public class OptimizedScheduler {
    
    // Only keep necessary scheduled tasks
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredSessions() {
        sessionService.removeExpiredSessions();
    }
    
    // Remove unused schedulers that consume resources
    // @Scheduled(fixedRate = 60000) // REMOVED: Unnecessary frequent task
    // public void unnecessaryTask() { ... }
}

// ✅ Good: Conditional bean creation
@Configuration
public class ConditionalConfig {
    
    @Bean
    @ConditionalOnProperty(name = "feature.analytics.enabled", havingValue = "true")
    public AnalyticsService analyticsService() {
        return new AnalyticsService();
    }
}
```

### Optimize API Request/Response
```java
// ❌ Bad: Returning unnecessary data
@GetMapping("/orders/{id}")
public ResponseEntity<Order> getOrder(@PathVariable String id) {
    Order order = orderService.findById(id); // Contains all fields
    return ResponseEntity.ok(order);
}

// ✅ Good: Return only required data
@GetMapping("/orders/{id}")
public ResponseEntity<OrderSummaryDto> getOrderSummary(@PathVariable String id) {
    OrderSummaryDto summary = orderService.findOrderSummary(id);
    return ResponseEntity.ok(summary);
}

public record OrderSummaryDto(
    String id,
    String customerName,
    BigDecimal totalAmount,
    OrderStatus status
    // Only essential fields for this endpoint
) {}
```

## Logging Best Practices

### Log Only Required Data
```java
// ❌ Bad: Logging sensitive or excessive data
@Service
@Slf4j
public class PaymentService {
    
    public void processPayment(PaymentRequest request) {
        log.info("Processing payment: {}", request); // Contains sensitive data!
        
        log.info("Full customer object: {}", request.getCustomer()); // Too much data!
    }
}

// ✅ Good: Log only necessary, non-sensitive data
@Service
@Slf4j
public class PaymentService {
    
    public void processPayment(PaymentRequest request) {
        log.info("Processing payment for customer: {}, amount: {}", 
            request.getCustomerId(), request.getAmount());
        
        log.debug("Payment request details: {}", sanitizeForLogging(request));
    }
    
    private PaymentRequestLog sanitizeForLogging(PaymentRequest request) {
        return PaymentRequestLog.builder()
            .customerId(request.getCustomerId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            // Exclude sensitive fields like card numbers, CVV, etc.
            .build();
    }
}
```

## Conditional Checks Optimization

### Primitive Data Checks First
```java
// ✅ Good: Primitive checks first, expensive operations last
public boolean isValidOrder(Order order) {
    // Fast primitive checks first
    if (!"ACTIVE".equals(order.getStatus()) || order.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        return false;
    }
    
    // Expensive validation last
    return performComplexValidation(order);
}

// ❌ Bad: Expensive operations first
public boolean isValidOrderBad(Order order) {
    // Expensive operation first - wastes resources
    return performComplexValidation(order) && "ACTIVE".equals(order.getStatus());
}
```

## Data Structure Optimization

### Primitive Data Types as Keys
```java
// ❌ Bad: Custom objects as Map keys
Map<ReviewCounterparty, List<FinancialSecurity>> financialSecuritiesGroupedByCounterparty = 
    financialSecurities.stream()
        .collect(Collectors.groupingBy(FinancialSecurity::getReviewCounterparty));

// ✅ Good: Primitive types as keys
Map<String, List<FinancialSecurity>> financialSecuritiesGroupedByCounterpartyId = 
    financialSecurities.stream()
        .collect(Collectors.groupingBy(fs -> fs.getReviewCounterparty().getId()));

// ✅ Good: Using primitive wrapper types
Map<Long, UserSession> activeSessions = new ConcurrentHashMap<>();
Map<String, CachedData> cache = new HashMap<>();
```

### Pass Objects Instead of IDs
```java
// ❌ Bad: Passing IDs and reloading objects
public void processOrder(String orderId, String customerId) {
    Order order = orderRepository.findById(orderId).orElseThrow(); // Unnecessary query
    Customer customer = customerRepository.findById(customerId).orElseThrow(); // Unnecessary query
    
    // Process with loaded objects
}

// ✅ Good: Pass already loaded objects
public void processOrder(Order order, Customer customer) {
    // Objects already loaded - no additional queries needed
    // Avoids memory allocation for duplicate objects
    performOrderProcessing(order, customer);
}
```

## Loop and Iteration Optimization

### Avoid Heavy Operations in Loops
```java
// ❌ Bad: Heavy operations inside loop
public List<EnrichedOrder> enrichOrders(List<Order> orders) {
    List<EnrichedOrder> enriched = new ArrayList<>();
    for (Order order : orders) {
        Customer customer = customerService.findById(order.getCustomerId()); // DB call in loop!
        PaymentInfo payment = paymentService.getPaymentInfo(order.getId()); // Another DB call!
        enriched.add(new EnrichedOrder(order, customer, payment));
    }
    return enriched;
}

// ✅ Good: Batch operations outside loop
public List<EnrichedOrder> enrichOrders(List<Order> orders) {
    // Collect all IDs first
    Set<String> customerIds = orders.stream()
        .map(Order::getCustomerId)
        .collect(Collectors.toSet());
    Set<String> orderIds = orders.stream()
        .map(Order::getId)
        .collect(Collectors.toSet());
    
    // Single batch calls
    Map<String, Customer> customers = customerService.findByIds(customerIds)
        .stream()
        .collect(Collectors.toMap(Customer::getId, Function.identity()));
    Map<String, PaymentInfo> payments = paymentService.getPaymentInfos(orderIds)
        .stream()
        .collect(Collectors.toMap(PaymentInfo::getOrderId, Function.identity()));
    
    // Fast loop with map lookups
    return orders.stream()
        .map(order -> new EnrichedOrder(
            order,
            customers.get(order.getCustomerId()),
            payments.get(order.getId())
        ))
        .collect(Collectors.toList());
}
```

## Entity Relationship Optimization

### Avoid Bidirectional Relationships
```java
// ❌ Bad: Bidirectional relationships
@Entity
public class Order {
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> items; // Creates proxy objects, uses memory
}

@Entity
public class OrderItem {
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order; // Bidirectional reference
}

// ✅ Good: Unidirectional with explicit queries
@Entity
public class Order {
    // No bidirectional relationship
}

@Entity
public class OrderItem {
    @Column(name = "order_id")
    private String orderId; // Just store the ID
}

// Use explicit queries when needed
@Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId")
List<OrderItem> findByOrderId(@Param("orderId") String orderId);
```

## Dependency Injection Optimization

### Minimal Dependencies
```java
// ❌ Bad: Unnecessary dependencies and configurations
@Service
public class OrderService {
    
    @Autowired
    private AnalyticsService analyticsService; // Not always needed
    
    @Autowired
    private AuditService auditService; // Heavy service
    
    @Autowired
    private NotificationService notificationService; // Could be async
}

// ✅ Good: Minimal, targeted dependencies
@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final PaymentService paymentService; // Only essential dependencies
    
    @Autowired(required = false)
    private AnalyticsService analyticsService; // Optional dependency
    
    // Use events for non-essential services
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Handle analytics and notifications asynchronously
    }
}
```

## Performance Monitoring Tools

### Recommended Tools
```yaml
# Stackify Prefix - API performance monitoring
# Add to application.yml
management:
  endpoints:
    web:
      exposure:
        include: "health,metrics,prometheus"
  metrics:
    export:
      prometheus:
        enabled: true

# VisualVM integration
# Add JVM arguments for profiling
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
```

### Performance Monitoring Configuration
```java
@Component
@Slf4j
public class PerformanceMonitor {
    
    @EventListener
    public void handleSlowQuery(SlowQueryEvent event) {
        if (event.getExecutionTime() > Duration.ofSeconds(2)) {
            log.warn("Slow query detected: {} took {}ms", 
                event.getQuery(), event.getExecutionTime().toMillis());
        }
    }
    
    @Scheduled(fixedRate = 60000)
    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        if (usedMemory > totalMemory * 0.8) {
            log.warn("High memory usage: {} / {}", usedMemory, totalMemory);
        }
    }
}
```

---

*Note: Use Stackify Prefix (https://stackify.com/prefix) for API performance monitoring and VisualVM (https://visualvm.github.io/download.html) for Java profiling.*
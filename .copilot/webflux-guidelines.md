# WebFlux Guidelines

## Getting Started with WebFlux

### Learning Path
- **Education First**: Take WebFlux courses at Udemy.com before starting development
- **Reactive Paradigm**: Spend a couple of days learning the reactive paradigm thoroughly
- **Non-blocking Model**: Understand that reactive paradigm is a non-blocking threading model
- **End-to-End Commitment**: Use WebFlux throughout the entire project - mixing blocking code defeats the purpose

## Reactive Programming Principles

### Core Concepts
- **Non-blocking**: Never block threads - use reactive operators instead
- **Backpressure**: Handle slow consumers gracefully with flow control
- **Event-driven**: Respond to events rather than polling
- **Resilience**: Design for failure with circuit breakers and timeouts
- **Immutability**: All POJOs should be immutable

### Reactive Streams
```java
// Example: Proper reactive chain with immutable POJOs
@GetMapping("/users/{id}")
public Mono<ResponseEntity<UserDto>> getUser(@PathVariable String id) {
    return userService.findById(id)
        .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
        .defaultIfEmpty(ResponseEntity.notFound().build())
        .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
}
```

### Immutable POJOs
```java
// ✅ Good: Immutable POJO using builder pattern
@Value
@Builder
public class OrderDto {
    String id;
    String customerId;
    BigDecimal totalAmount;
    OrderStatus status;
    LocalDateTime createdAt;
    List<OrderItemDto> items;
}

// ✅ Good: Immutable entity with records (Java 14+)
public record User(String id, String name, String email, LocalDateTime createdAt) {}
```

### Threading Model
- **Event Loop**: Use non-blocking I/O with event loops
- **Scheduler Types**: Understand `parallel()`, `elastic()`, and `boundedElastic()`
- **Thread Safety**: Ensure reactive operators are thread-safe
- **Avoid Blocking**: Never use blocking operations in reactive chains

### Database Integration - CRITICAL RULES
- **NO Blocking ORMs**: Never use Spring JPA-Data or Hibernate in WebFlux projects
- **Reactive Repositories Only**: Use R2DBC, MongoDB Reactive, or other reactive database clients
- **End-to-End Reactive**: Maintain reactive streams from controller to database

```java
// ❌ FORBIDDEN: Blocking JPA repositories
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // This will block and destroy WebFlux benefits!
}

// ✅ REQUIRED: Reactive R2DBC repositories
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String> {
    
    @Query("SELECT * FROM users WHERE email = :email")
    Mono<User> findByEmail(String email);
    
    @Query("SELECT * FROM users WHERE status = :status")
    Flux<User> findByStatus(UserStatus status);
}
```

## WebFlux Architecture

### Controller Design
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public Mono<ResponseEntity<OrderDto>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request)
            .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order))
            .onErrorResume(ValidationException.class, 
                ex -> Mono.just(ResponseEntity.badRequest().build()));
    }
    
    @GetMapping
    public Flux<OrderDto> getOrders(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return orderService.findOrders(PageRequest.of(page, size))
            .timeout(Duration.ofSeconds(30))
            .onErrorResume(ex -> Flux.empty());
    }
}
```

### Service Layer Patterns
```java
@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    
    public Mono<OrderDto> createOrder(CreateOrderRequest request) {
        return validateRequest(request)
            .flatMap(this::reserveInventory)
            .flatMap(this::processPayment)
            .flatMap(orderRepository::save)
            .map(orderMapper::toDto)
            .doOnSuccess(order -> publishOrderCreatedEvent(order))
            .doOnError(ex -> log.error("Failed to create order", ex));
    }
    
    private Mono<Order> validateRequest(CreateOrderRequest request) {
        return Mono.fromCallable(() -> {
            // Validation logic
            return orderMapper.toEntity(request);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
```

### Repository Integration
```java
// ✅ R2DBC Repository - REQUIRED for WebFlux
@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, String> {
    
    @Query("SELECT * FROM orders WHERE customer_id = :customerId AND status = :status")
    Flux<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
    
    @Query("SELECT * FROM orders WHERE created_at >= :startDate")
    Flux<Order> findOrdersAfter(LocalDateTime startDate);
}

// ✅ Custom Repository Implementation - Reactive
@Component
public class CustomOrderRepository {
    
    private final R2dbcEntityTemplate template;
    
    public Flux<Order> findOrdersWithPagination(Pageable pageable) {
        return template.select(Order.class)
            .matching(Query.empty()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()))
            .all();
    }
}

// ✅ MongoDB Reactive Repository
@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    
    Flux<Product> findByCategoryAndPriceGreaterThan(String category, BigDecimal price);
    
    @Query("{ 'tags': ?0 }")
    Flux<Product> findByTag(String tag);
}
```

## Development Dependencies - MANDATORY REACTIVE CLIENTS

### External API Integration
```java
// ✅ REQUIRED: Use WebClient for all REST API calls
@Component
public class PaymentClient {
    
    private final WebClient webClient;
    
    public PaymentClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://payment-service.com")
            .build();
    }
    
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return webClient.post()
            .uri("/payments")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PaymentResponse.class)
            .timeout(Duration.ofSeconds(10));
    }
}

// ❌ FORBIDDEN: Blocking HTTP clients
public class BadPaymentClient {
    private final RestTemplate restTemplate; // DON'T USE THIS!
    
    public PaymentResponse processPayment(PaymentRequest request) {
        return restTemplate.postForObject("/payments", request, PaymentResponse.class);
        // This blocks the thread and defeats WebFlux purpose!
    }
}
```

### Database Configuration
```java
// ✅ R2DBC Configuration
@Configuration
@EnableR2dbcRepositories
public class DatabaseConfig {
    
    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(DRIVER, "postgresql")
            .option(HOST, "localhost")
            .option(PORT, 5432)
            .option(USER, "dbuser")
            .option(PASSWORD, "dbpass")
            .option(DATABASE, "mydb")
            .build());
    }
}
```
```

## Critical Rules - Block() and Subscribe() Usage

### Forbidden Patterns
```java
// ❌ NEVER USE: block() in production code
@Service
public class BadOrderService {
    
    public OrderDto createOrder(CreateOrderRequest request) {
        // THIS IS FORBIDDEN - defeats WebFlux purpose!
        return orderRepository.save(order).block();
    }
    
    public List<OrderDto> getAllOrders() {
        // THIS IS ALSO FORBIDDEN!
        return orderRepository.findAll().collectList().block();
    }
}
```

### Acceptable Patterns
```java
// ✅ Good: Use subscribe() in specific scenarios
@Service
public class OrderService {
    
    public Mono<OrderDto> createOrder(CreateOrderRequest request) {
        return orderRepository.save(orderMapper.toEntity(request))
            .map(orderMapper::toDto)
            .doOnSuccess(order -> {
                // Use subscribe() for fire-and-forget operations
                publishOrderEvent(order).subscribe(
                    result -> log.info("Event published for order: {}", order.getId()),
                    error -> log.error("Failed to publish event", error)
                );
            });
    }
    
    private Mono<Void> publishOrderEvent(OrderDto order) {
        return eventPublisher.publish(OrderCreatedEvent.from(order));
    }
}
```

### Development Tools - BlockHound Integration
```java
// ✅ RECOMMENDED: Use BlockHound in development
@SpringBootApplication
public class Application {
    
    static {
        // Install BlockHound to detect blocking calls in development
        if (isDevProfile()) {
            BlockHound.install(
                BlockHound.builder()
                    .allowBlockingCallsInside("java.util.UUID", "randomUUID")
                    .build()
            );
        }
    }
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    private static boolean isDevProfile() {
        return Arrays.asList(System.getProperty("spring.profiles.active", "").split(","))
            .contains("dev");
    }
}
```

### Maven/Gradle Configuration for BlockHound
```xml
<!-- Add to pom.xml for development -->
<dependency>
    <groupId>io.projectreactor.tools</groupId>
    <artifactId>blockhound</artifactId>
    <version>1.0.8.RELEASE</version>
    <scope>provided</scope>
</dependency>
```

### Exception Handling Strategies
```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", "application/json");
        
        if (ex instanceof ValidationException) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return writeErrorResponse(response, "Validation failed", ex.getMessage());
        } else if (ex instanceof ResourceNotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return writeErrorResponse(response, "Resource not found", ex.getMessage());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return writeErrorResponse(response, "Internal server error", "An unexpected error occurred");
        }
    }
}
```

### Circuit Breaker Pattern
```java
@Component
public class PaymentClient {
    
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return circuitBreaker.executeSupplier(() ->
            webClient.post()
                .uri("/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .timeout(Duration.ofSeconds(10))
        ).onErrorResume(ex -> Mono.error(new PaymentException("Payment failed", ex)));
    }
}
```

### Retry Strategies
```java
public Mono<ApiResponse> callExternalService(Request request) {
    return webClient.post()
        .uri("/external-api")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(ApiResponse.class)
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
            .filter(ex -> ex instanceof ConnectException)
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                new ServiceUnavailableException("External service unavailable")));
}
```

## Performance Optimization

### WebClient Configuration
```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
            .maxConnections(100)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(5))
            .build();
            
        HttpClient httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(30))
                    .addHandlerLast(new WriteTimeoutHandler(30)));
                    
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
```

### Caching Strategies
```java
@Service
public class UserService {
    
    private final Mono<List<User>> cachedUsers = userRepository.findAll()
        .collectList()
        .cache(Duration.ofMinutes(5))
        .doOnNext(users -> log.info("Refreshed user cache with {} users", users.size()));
    
    public Flux<User> getAllUsers() {
        return cachedUsers.flatMapMany(Flux::fromIterable);
    }
    
    public Mono<User> findById(String id) {
        return getAllUsers()
            .filter(user -> user.getId().equals(id))
            .next()
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    }
}
```

### Parallel Processing
```java
public Mono<OrderSummary> processOrderWithParallelCalls(String orderId) {
    Mono<Order> orderMono = orderRepository.findById(orderId).cache();
    
    Mono<Customer> customerMono = orderMono
        .flatMap(order -> customerService.findById(order.getCustomerId()));
    
    Mono<List<OrderItem>> itemsMono = orderMono
        .flatMapMany(order -> orderItemRepository.findByOrderId(order.getId()))
        .collectList();
    
    Mono<PaymentInfo> paymentMono = orderMono
        .flatMap(order -> paymentService.getPaymentInfo(order.getId()));
    
    return Mono.zip(orderMono, customerMono, itemsMono, paymentMono)
        .map(tuple -> OrderSummary.builder()
            .order(tuple.getT1())
            .customer(tuple.getT2())
            .items(tuple.getT3())
            .payment(tuple.getT4())
            .build());
}
```

## Security Integration

### JWT Authentication
```java
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
    
    private final JwtDecoder jwtDecoder;
    
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication.getCredentials().toString())
            .flatMap(this::validateToken)
            .map(this::createAuthentication);
    }
    
    private Mono<Jwt> validateToken(String token) {
        return Mono.fromCallable(() -> jwtDecoder.decode(token))
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(ex -> Mono.error(new InvalidTokenException("Invalid JWT token")));
    }
}
```

### Security Configuration
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/api/v1/public/**").permitAll()
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

## Testing Guidelines

### WebTestClient Usage
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private OrderService orderService;
    
    @Test
    void shouldCreateOrder() {
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId("customer-123")
            .items(List.of(OrderItem.builder().productId("product-1").quantity(2).build()))
            .build();
            
        OrderDto expectedOrder = OrderDto.builder()
            .id("order-123")
            .customerId("customer-123")
            .status(OrderStatus.PENDING)
            .build();
            
        when(orderService.createOrder(any())).thenReturn(Mono.just(expectedOrder));
        
        webTestClient.post()
            .uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(OrderDto.class)
            .value(order -> {
                assertThat(order.getId()).isEqualTo("order-123");
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            });
    }
}

// ⚠️ ONLY IN TESTS: block() is acceptable for waiting for results
@Test
void blockOnlyInTestsExample() {
    OrderDto result = orderService.createOrder(request).block(Duration.ofSeconds(5));
    assertThat(result).isNotNull();
}
```

### Reactive Testing - PREFERRED APPROACH
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void shouldHandleEmptyResult() {
        when(orderRepository.findById("non-existent")).thenReturn(Mono.empty());
        
        // ✅ PREFERRED: Use StepVerifier instead of block()
        StepVerifier.create(orderService.findById("non-existent"))
            .expectError(OrderNotFoundException.class)
            .verify();
    }
    
    @Test
    void shouldRetryOnFailure() {
        when(orderRepository.save(any()))
            .thenReturn(Mono.error(new DataAccessException("DB error")))
            .thenReturn(Mono.just(createOrder()));
        
        // ✅ PREFERRED: StepVerifier for reactive testing
        StepVerifier.create(orderService.createOrderWithRetry(createOrderRequest()))
            .expectNextMatches(order -> order.getId() != null)
            .verifyComplete();
    }
    
    @Test
    void blockOnlyInTestsWhenNecessary() {
        // ⚠️ ACCEPTABLE: block() only in tests when StepVerifier isn't suitable
        when(orderRepository.findById("order-123")).thenReturn(Mono.just(createOrder()));
        
        OrderDto result = orderService.findById("order-123").block(Duration.ofSeconds(5));
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("order-123");
    }
}
```

## Monitoring and Observability

### Metrics Configuration
```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.deny(id -> {
            String uri = id.getTag("uri");
            return uri != null && uri.startsWith("/actuator");
        });
    }
}

// Usage in service
@Service
public class OrderService {
    
    @Timed(name = "order.creation", description = "Time taken to create order")
    public Mono<OrderDto> createOrder(CreateOrderRequest request) {
        return processOrder(request);
    }
}
```

### Logging Best Practices
```java
@Service
@Slf4j
public class OrderService {
    
    public Mono<OrderDto> createOrder(CreateOrderRequest request) {
        return Mono.just(request)
            .doOnNext(req -> log.info("Creating order for customer: {}", req.getCustomerId()))
            .flatMap(this::validateAndSave)
            .doOnSuccess(order -> log.info("Successfully created order: {}", order.getId()))
            .doOnError(ex -> log.error("Failed to create order for customer: {}", 
                request.getCustomerId(), ex))
            .contextWrite(Context.of("customerId", request.getCustomerId()));
    }
}
```

## Best Practices

### Resource Management
- **Connection Pooling**: Configure appropriate connection pool sizes
- **Memory Management**: Be mindful of buffer sizes and memory usage
- **Cleanup**: Properly dispose of resources in finally blocks or using `using()` operator
- **Backpressure**: Handle backpressure appropriately with `onBackpressureBuffer()` or `onBackpressureDrop()`

### Common Anti-patterns to Avoid
```java
// ❌ CRITICAL ERROR: Blocking in reactive chain
public Mono<String> criticalError() {
    return Mono.fromCallable(() -> {
        return blockingHttpClient.get("/data"); // This blocks and destroys WebFlux benefits!
    });
}

// ❌ FORBIDDEN: Using blocking ORM frameworks
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // JPA/Hibernate is blocking - NEVER use with WebFlux!
}

// ❌ FORBIDDEN: Converting to blocking
public String destroysWebFluxPurpose() {
    return reactiveService.getData().block(); // This defeats the entire purpose!
}

// ❌ FORBIDDEN: Mixed blocking and reactive code
@Service
public class MixedService {
    @Autowired
    private JpaUserRepository jpaRepo; // Blocking!
    
    @Autowired
    private ReactiveOrderRepository reactiveRepo; // Reactive!
    
    // This mixing destroys all WebFlux benefits!
}

// ✅ CORRECT: Pure reactive approach
public Mono<String> correctExample() {
    return webClient.get()
        .uri("/data")
        .retrieve()
        .bodyToMono(String.class);
}

// ✅ CORRECT: Keep reactive chain throughout
public Mono<ProcessedData> correctProcessingExample() {
    return reactiveService.getData()
        .map(this::processData)
        .flatMap(this::enrichData)
        .doOnSuccess(data -> log.info("Processed: {}", data.getId()));
}

// ✅ CORRECT: All reactive dependencies
@Service
public class CorrectService {
    private final ReactiveUserRepository userRepo;
    private final WebClient webClient;
    private final ReactiveEventPublisher eventPublisher;
    
    // All dependencies are reactive - maintains end-to-end reactive benefits
}
```

## WebFlux Project Checklist

### ✅ REQUIRED Components
- [ ] R2DBC or MongoDB Reactive for database access
- [ ] WebClient for all external REST API calls
- [ ] Reactive repositories (ReactiveCrudRepository, ReactiveMongoRepository)
- [ ] Immutable POJOs/DTOs using @Value, @Builder, or records
- [ ] StepVerifier for testing reactive streams
- [ ] BlockHound integration in development profile

### ❌ FORBIDDEN Components
- [ ] Spring Data JPA / Hibernate
- [ ] RestTemplate or blocking HTTP clients
- [ ] JDBC repositories
- [ ] block() calls in production code (except specific fire-and-forget scenarios)
- [ ] Mixing blocking and reactive code

### ⚠️ Development Guidelines
- [ ] Complete Udemy WebFlux course before starting
- [ ] Use BlockHound to detect blocking calls during development
- [ ] Prefer StepVerifier over block() in tests
- [ ] Use subscribe() only for fire-and-forget operations
- [ ] Maintain reactive streams from controller to database

---

*Note: Adapt these guidelines based on your specific Spring Boot version, database choice, and infrastructure requirements.*
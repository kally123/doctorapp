# Event-Driven Architecture Guidelines

## Core Principles

### Event-First Design
- **Events as First-Class Citizens**: Design around business events, not just data changes
- **Event Storming**: Use collaborative modeling to identify domain events
- **Immutable Events**: Events should never be modified once published
- **Event Sourcing**: Consider storing events as the source of truth

### Decoupling and Autonomy
- **Loose Coupling**: Services communicate through events, not direct calls
- **Temporal Decoupling**: Producers and consumers operate independently in time
- **Publisher Independence**: Publishers don't know about subscribers
- **Consumer Autonomy**: Each consumer processes events at its own pace

## Event Design Standards

### Event Structure
```json
{
  "eventId": "uuid-v4",
  "eventType": "order.created.v1",
  "eventTime": "2025-10-20T10:00:00Z",
  "source": "order-service",
  "subject": "order-12345",
  "dataVersion": "1.0",
  "data": {
    "orderId": "12345",
    "customerId": "customer-456",
    "totalAmount": 99.99,
    "currency": "SEK"
  },
  "metadata": {
    "correlationId": "correlation-789",
    "causationId": "command-abc",
    "userId": "user-123"
  }
}
```

### Naming Conventions
- **Event Types**: Use format `{domain}.{action}.{version}` (e.g., `payment.processed.v1`)
- **Actions**: Use past tense verbs (`created`, `updated`, `deleted`, `processed`)
- **Domains**: Align with bounded contexts from DDD
- **Versioning**: Include version in event type for schema evolution

### Schema Management
- **Schema Registry**: Centralize event schema definitions and validation
- **Backward Compatibility**: Ensure new schema versions don't break existing consumers
- **Schema Evolution**: Use additive changes when possible
- **Validation**: Validate events against schemas at production boundaries

## Messaging Patterns

### Event Patterns
- **Domain Events**: Business-meaningful occurrences within a bounded context
- **Integration Events**: Cross-service communication events
- **Command Events**: Intent to perform an action (use sparingly)
- **Notification Events**: Inform about state changes without data

### Message Ordering
- **Partition Keys**: Use consistent keys for related events (e.g., customerId)
- **Ordering Guarantees**: Understand per-partition vs global ordering
- **Sequence Numbers**: Include sequence information when order matters
- **Idempotency**: Design consumers to handle duplicate events gracefully

### Error Handling
```javascript
// Example: Retry with exponential backoff
const retryConfig = {
  maxRetries: 3,
  baseDelay: 1000,
  maxDelay: 30000,
  backoffMultiplier: 2
};

async function processEvent(event) {
  try {
    await businessLogic(event);
  } catch (error) {
    if (isRetryable(error)) {
      await scheduleRetry(event, retryConfig);
    } else {
      await sendToDeadLetterQueue(event, error);
    }
  }
}
```

## Infrastructure Patterns

### Message Brokers
- **Apache Kafka**: High-throughput, persistent message streaming
- **Azure Service Bus**: Enterprise messaging with advanced features
- **RabbitMQ**: Flexible routing and reliable delivery
- **Choose Based On**: Throughput, persistence, ordering, and ecosystem needs

### Topic Strategy
- **Topic per Aggregate**: One topic per domain aggregate root
- **Topic per Event Type**: Separate topics for different event types
- **Mixed Strategy**: Balance between granularity and operational complexity
- **Partitioning**: Partition by aggregate ID or tenant for scalability

### Consumer Groups
```yaml
# Example Kafka consumer configuration
consumer:
  group.id: "payment-processor"
  enable.auto.commit: false
  auto.offset.reset: "earliest"
  max.poll.records: 100
  session.timeout.ms: 30000
```

## Implementation Patterns

### Saga Pattern
- **Orchestration**: Central coordinator manages the saga flow
- **Choreography**: Services coordinate through events without central control
- **Compensation**: Define compensating actions for each saga step
- **State Management**: Track saga state for recovery and monitoring

### CQRS Integration
- **Command Side**: Publishes events after state changes
- **Query Side**: Builds read models from events
- **Event Store**: Persist events for replay and auditing
- **Projections**: Create materialized views optimized for queries

### Outbox Pattern
```sql
-- Example outbox table
CREATE TABLE outbox_events (
  id UUID PRIMARY KEY,
  aggregate_id UUID NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  event_data JSONB NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  processed_at TIMESTAMP NULL
);
```

## Monitoring and Observability

### Event Tracking
- **Event Lineage**: Track event flow across services
- **Processing Metrics**: Monitor lag, throughput, and error rates
- **Business Metrics**: Track domain-specific KPIs through events
- **Correlation IDs**: Trace requests across service boundaries

### Health Monitoring
- **Consumer Lag**: Monitor how far behind consumers are
- **Dead Letter Queues**: Alert on messages that cannot be processed
- **Schema Validation**: Track schema validation failures
- **Throughput Monitoring**: Monitor events per second by topic/service

### Alerting Strategy
```yaml
# Example monitoring alerts
alerts:
  - name: "High Consumer Lag"
    condition: "consumer_lag > 1000"
    severity: "warning"
  - name: "Dead Letter Queue Growth"
    condition: "dlq_messages > 10"
    severity: "critical"
  - name: "Schema Validation Failures"
    condition: "schema_errors > 5% in 5min"
    severity: "warning"
```

## Security Considerations

### Event Security
- **Encryption**: Encrypt sensitive data in events
- **Access Control**: Implement topic-level access controls
- **Audit Trail**: Log all event access and modifications
- **Data Classification**: Mark events with sensitivity levels

### Network Security
- **TLS**: Use TLS for all message broker communications
- **Authentication**: Implement strong authentication for producers/consumers
- **Authorization**: Fine-grained permissions for topic access
- **Network Isolation**: Use VPCs and firewall rules

## Testing Strategies

### Event Testing
```javascript
// Example: Testing event handlers
describe('OrderCreatedHandler', () => {
  it('should create shipping record when order created', async () => {
    const event = createOrderCreatedEvent();
    await orderCreatedHandler.handle(event);
    
    const shipping = await shippingRepo.findByOrderId(event.data.orderId);
    expect(shipping).toBeDefined();
    expect(shipping.status).toBe('pending');
  });
});
```

### Integration Testing
- **Test Containers**: Use Docker containers for integration tests
- **Event Simulation**: Create test events for end-to-end scenarios
- **Consumer Testing**: Test consumer behavior with various event scenarios
- **Schema Compatibility**: Test schema evolution scenarios

## Migration and Evolution

### Legacy Integration
- **Event Gateway**: Translate between legacy systems and events
- **Dual Write**: Gradually migrate from sync to async patterns
- **Change Data Capture**: Extract events from legacy database changes
- **Adapter Pattern**: Wrap legacy services with event-driven interfaces

### Schema Evolution
- **Versioning Strategy**: Plan for schema changes from day one
- **Consumer Updates**: Coordinate consumer updates for breaking changes
- **Rollback Plans**: Maintain ability to rollback schema changes
- **Migration Tools**: Automate schema migration processes

---

*Note: Adapt these guidelines based on your specific messaging infrastructure, compliance requirements, and business domain.*
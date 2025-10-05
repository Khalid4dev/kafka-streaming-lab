# Spring Cloud Stream - Kafka Streams Sales Analytics

## Project Overview

This project demonstrates real-time sales analytics using Spring Cloud Stream and Kafka Streams. It processes sales events in real-time and provides a live dashboard for monitoring product sales.

## Architecture

### SaleEvent Record

First, we created a SaleEvent record representing sales transactions:

```java
public record SaleEvent(String product, String customer, Date date, double amount) {}
```

### REST Controller

The REST controller provides endpoints for publishing sales and retrieving analytics:

- `GET /publish?product={product}&topic={topic}`: Publishes a sale event to a Kafka topic
- `GET /analytics`: Server-Sent Events stream providing real-time sales counts

### Supplier

A supplier generates mock sales data and sends it to topic `T3` with a configurable delay.

```properties
spring.cloud.stream.bindings.pageEventSupplier-out-0.destination=T3
spring.cloud.stream.bindings.pageEventSupplier-out-0.producer.poller.fixed-delay=200
```

### KStream Function

The KStream function processes sales from topic `T3`, filters sales with amount > 100, and counts sales per product over 5-second windows.

```java
public Function<KStream<String, SaleEvent>, KStream<String, Long>> kStreamFunction() {
    return (input) ->
            input.filter((k, v) -> v.amount() > 100)
                    .map((k, v) -> new KeyValue<>(v.product(), 1L))
                    .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                    .windowedBy(TimeWindows.of(Duration.ofSeconds(5)))
                    .count(Materialized.as("count-store"));
}
```

### Analytics Dashboard

The `index.html` page displays real-time charts of sales counts for different products using Smoothie.js.

## Configuration

Key configuration in `application.properties`:

```properties
spring.cloud.stream.bindings.pageEvenConsumer-in-0.destination=T1
spring.cloud.stream.bindings.pageEventSupplier-out-0.destination=T3
spring.cloud.stream.bindings.kStreamFunction-in-0.destination=T3
spring.cloud.stream.bindings.kStreamFunction-out-0.destination=T4
spring.cloud.function.definition=pageEvenConsumer;pageEventSupplier;kStreamFunction
```

## Running the Application

1. Ensure Kafka is running
2. Run the Spring Boot application
3. Access the dashboard at `http://localhost:8080`
4. View real-time sales analytics

## Technologies Used

- Spring Boot
- Spring Cloud Stream
- Kafka Streams
- Reactive Programming (Project Reactor)
- Smoothie.js for real-time charting

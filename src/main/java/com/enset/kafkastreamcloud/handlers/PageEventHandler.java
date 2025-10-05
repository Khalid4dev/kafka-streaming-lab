package com.enset.kafkastreamcloud.handlers;

import com.enset.kafkastreamcloud.event.SaleEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class PageEventHandler {

    @Bean
    public Consumer<SaleEvent> pageEventConsumer(){
        return (input) -> {
            System.out.println("***************");
            System.out.println(input.date());
            System.out.println("***************");
        };
    }

    @Bean
    public Supplier<SaleEvent> pageEventSupplier(){
        return () -> {
            return new SaleEvent(
                Math.random()>0.5?"Laptop":"Phone",
                Math.random()>0.5?"C1":"C2",
                new Date(),
                 50+new Random().nextInt(500));
        };
    }

    @Bean
    public Function<KStream<String, SaleEvent>, KStream<String, Long>> kStreamFunction(){
        return (input) ->
            input.filter((k,v) -> v.amount()>100)
                    .map((k,v)-> new KeyValue<>(v.product(), 1L))  // Count sales per product
                    .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                    .windowedBy(TimeWindows.of(Duration.ofSeconds(5)))
                    .count(Materialized.as("count-store"))
                    .toStream()
                    .map((k,v)-> new KeyValue<>(k.key(), v));
    }
}

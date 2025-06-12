package com.mt564.processing.svc.processor;

import com.mt564.processing.svc.model.dto.Mt564EventDto;
import com.mt564.processing.svc.service.Mt564EventBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Kafka batch processor that consumes MT564 event data from the 'mt564-events' topic.
 * The records are consumed as a batch of Mt564EventDto objects and then passed to
 * the service layer for persistence.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Mt564EventsKafkaBatchProcessor {

    private final Mt564EventBuildService eventService;

    /**
     * Listens to the Kafka topic 'mt564-events' and consumes messages in batches.
     * Each message is deserialized into Mt564EventDto.
     * The batch is then passed to the service layer for processing and persistence.
     *
     * We are using this approach to:
     * - Isolate the consumption of events from the rest-api from the processing service executions.
     * - Horizontal Scaling : Distribute the workload of mt564 events processing amongst all instances of the microservice
     * - Resilliency and Recovery:  Impact and recovery process limited to only the failed batches and not to other records
     */
    @KafkaListener(
            topics = "${app.kafka-topic}",
            containerFactory = "kafkaBatchListenerContainerFactory",
            groupId = "mt564-event-processor-group"
    )
    public void consumeBatch(List<ConsumerRecord<String, Mt564EventDto>> records) {
        log.info("Received batch with {} records", records.size());

        List<Mt564EventDto> events = records.stream()
                .map(ConsumerRecord::value)
                .collect(Collectors.toList());

        eventService.processDailyEvents(events);
    }

}

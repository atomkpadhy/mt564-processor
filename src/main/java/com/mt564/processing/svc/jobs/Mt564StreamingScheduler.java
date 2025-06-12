package com.mt564.processing.svc.jobs;

import com.mt564.processing.svc.model.dto.Mt564EventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class Mt564StreamingScheduler {

    @Value("${app.mt564-endpoint}")
    private String endpoint;

    @Value("${app.kafka-topic}")
    private String kafkaTopic;

    @Autowired
    private final WebClient webClient;
    private final KafkaTemplate<String, Mt564EventDto> kafkaTemplate;

    public Mt564StreamingScheduler(WebClient webClient,
                                   KafkaTemplate<String, Mt564EventDto> kafkaTemplate) {
        this.webClient = webClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Scheduled job to fetch MT564 event data from a remote service and stream it to a Kafka topic.
     *
     * This method performs the following actions:
     * - Logs the start of the scheduled job.
     * - Sends an HTTP GET request to the configured endpoint using WebClient.
     * - Accepts the response as a stream of MT564EventDto objects (NDJSON or JSON).
     * - For each event received:
     *   - Publishes the event to the configured Kafka topic using the event reference as the key.
     *   - Logs the Kafka publishing action for traceability.
     * - Handles any errors encountered during the fetch or streaming process.
     * - Logs completion once all events have been processed and sent.
     *
     * This method is non-blocking and reactive. It uses WebClient to stream the events
     * and publishes them asynchronously to Kafka using KafkaTemplate.
     *
     * I have built this job assuming that the MT564 Events data are fed via a Asynchronous Rest API. This job would
     * would change if the data is fed via batch file/object store or via any messaging interface.
     */

    @Scheduled(cron = "${app.cron}")
    public void fetchAndStreamEvents() {
        log.info("Scheduled job started: Fetching MT564 events");

        webClient.get()
                .uri(endpoint)
                .accept(MediaType.APPLICATION_NDJSON) // or MediaType.APPLICATION_JSON
                .retrieve()
                .bodyToFlux(Mt564EventDto.class)
                .doOnNext(dto -> {
                    kafkaTemplate.send(kafkaTopic, dto.getEventReference(), dto);
                    log.debug("Published to Kafka: {}", dto.getEventReference());
                })
                .doOnError(error -> log.error("Error fetching or processing events", error))
                .doOnComplete(() -> log.info("Completed streaming MT564 events to Kafka"))
                .subscribe();
    }
}

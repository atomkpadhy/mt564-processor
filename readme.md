# 📄 MT564 Event Processing Service

This Spring Boot microservice processes streaming MT564 event data, persists only changed records, and maintains field-level version history for audit and delta tracking.

---

## 🧩 Overview

The system ingests corporate action events (MT564) from an upstream source via a scheduled job. It processes and stores only the changed records in a Oracle database, maintaining historical snapshots for every version. Field-level deltas can be queried via a REST API for downstream reporting or reconciliation.

---
| Component                        | Responsibility                                                           |
| -------------------------------- | ------------------------------------------------------------------------ |
| `Mt564StreamingScheduler`        | Scheduled job to fetch MT564 events and stream to Kafka                  |
| `Mt564EventsKafkaBatchProcessor` | Batch Kafka consumer to process and hand off events to the service layer |
| `Mt564EventBuildService`         | Deduplicates events, performs upserts, and builds audit snapshots        |
| `Mt564EventAuditRepository`      | Stores historical versions of each MT564 event                           |
| `EventDeltaReportingService`     | Computes field-level differences between versions over a timeframe       |
| `Mt564EventController`           | Exposes REST endpoint to query version deltas                            |

---

## 🔁 Architecture Flow

```mermaid
flowchart TD
    A[Scheduler: Mt564StreamingScheduler] --> B[REST API Call: MT564 Source]
    B --> C[Streaming JSON Response of Mt564EventDto]
    C --> D[Kafka Producer: Publishes to 'mt564-events' topic]
    D --> E[Kafka Consumer: Mt564EventsKafkaBatchProcessor]
    E --> F[Service: Mt564EventBuildService]
    F --> G[Persist New/Updated Events in mt564_events table]
    F --> H[Persist Snapshot JSON in mt564_event_audit table]
    H --> I[EventDeltaReportingService: Computes Field-level Deltas]
    I --> J[REST API: /api/mt564/event-deltas]

    style A fill:#f9f,stroke:#333,stroke-width:2px
    style J fill:#bbf,stroke:#333,stroke-width:2px

package com.mt564.processing.svc.model.entity;

import com.mt564.processing.svc.model.dto.Mt564EventDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mt564_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mt564Event {

    @EmbeddedId
    private Mt564EventId id;

    @Version
    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "business_hash", length = 64)
    private String businessHash;

    @Column(name = "related_message_reference", length = 35)
    private String relatedMessageReference;

    @Column(name = "corporate_action_event_id", length = 35)
    private String corporateActionEventId;

    @Column(name = "corporate_action_event_type", length = 4)
    private String corporateActionEventType;

    @Column(name = "mandatory_voluntary_indicator", length = 4)
    private String mandatoryVoluntaryIndicator;

    @Column(name = "event_processing_status", length = 4)
    private String eventProcessingStatus;

    @Column(name = "event_status_code", length = 4)
    private String eventStatusCode;

    @Column(name = "official_corporate_action_event_id", length = 35)
    private String officialCorporateActionEventId;

    @Column(name = "link_event_id", length = 35)
    private String linkEventId;

    @Column(name = "safekeeping_account", length = 35)
    private String safekeepingAccount;

    @Column(name = "place_of_safekeeping", length = 35)
    private String placeOfSafekeeping;

    @Column(name = "event_creation_datetime")
    private LocalDateTime eventCreationDatetime;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "ex_date")
    private LocalDate exDate;

    @Column(name = "announcement_date")
    private LocalDate announcementDate;

    @Column(name = "response_deadline")
    private LocalDate responseDeadline;

    @Column(name = "general_meeting_date")
    private LocalDate generalMeetingDate;

    @Lob
    @Column(name = "narrative")
    private String narrative;

    @Column(name = "market_disclosure_flag")
    private Boolean marketDisclosureFlag;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Mt564Event of(Mt564EventDto dto) {
        Mt564EventId id = new Mt564EventId(
                dto.getEventReference(),
                dto.getFinancialInstrumentId(),
                dto.getSenderBic()
        );

        return Mt564Event.builder()
                .id(id)
                .businessHash(dto.getBusinessHash())
                .relatedMessageReference(dto.getRelatedMessageReference())
                .corporateActionEventId(dto.getCorporateActionEventId())
                .corporateActionEventType(dto.getCorporateActionEventType())
                .mandatoryVoluntaryIndicator(dto.getMandatoryVoluntaryIndicator())
                .eventProcessingStatus(dto.getEventProcessingStatus())
                .eventStatusCode(dto.getEventStatusCode())
                .officialCorporateActionEventId(dto.getOfficialCorporateActionEventId())
                .linkEventId(dto.getLinkEventId())
                .safekeepingAccount(dto.getSafekeepingAccount())
                .placeOfSafekeeping(dto.getPlaceOfSafekeeping())
                .eventCreationDatetime(dto.getEventCreationDatetime())
                .effectiveDate(dto.getEffectiveDate())
                .recordDate(dto.getRecordDate())
                .exDate(dto.getExDate())
                .announcementDate(dto.getAnnouncementDate())
                .responseDeadline(dto.getResponseDeadline())
                .generalMeetingDate(dto.getGeneralMeetingDate())
                .narrative(dto.getNarrative())
                .marketDisclosureFlag(dto.getMarketDisclosureFlag())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

}

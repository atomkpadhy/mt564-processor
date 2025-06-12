package com.mt564.processing.svc.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt564.processing.svc.model.dto.Mt564EventDto;
import com.mt564.processing.svc.model.entity.Mt564Event;
import com.mt564.processing.svc.model.entity.Mt564EventAudit;
import com.mt564.processing.svc.model.entity.Mt564EventAuditId;
import com.mt564.processing.svc.model.entity.Mt564EventId;
import com.mt564.processing.svc.repository.Mt564EventAuditRepository;
import com.mt564.processing.svc.repository.Mt564EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class Mt564EventBuildService {

    @Autowired
    private final Mt564EventRepository eventRepository;
    @Autowired
    private final Mt564EventAuditRepository auditRepository;
    @Autowired
    private final ObjectMapper objectMapper;

    /**
     * Process daily full feed of MT564 events. Persist only new and changed (insert/update) into the mt564_events table
     * as well as the mt564_events_audit table which stores the whole entity object in json format.
     */
    @Transactional
    public void processDailyEvents(List<Mt564EventDto> dailyDtos) {

        // Extract composite keys from input list
        Set<Mt564EventId> inputIds = dailyDtos.stream()
                .map(dto -> new Mt564EventId(
                        dto.getEventReference(),
                        dto.getFinancialInstrumentId(),
                        dto.getSenderBic()
                ))
                .collect(Collectors.toSet());

        // Fetch existing events from DB by composite keys
        Map<Mt564EventId, Mt564Event> existingEvents = eventRepository.findAllById(inputIds).stream()
                .collect(Collectors.toMap(Mt564Event::getId, e -> e));

        List<Mt564Event> entitiesToSave = new ArrayList<>();

        dailyDtos.forEach(dto -> {
            Mt564EventId id = Mt564EventId.of(dto.getEventReference(), dto.getFinancialInstrumentId(), dto.getSenderBic());

            // Standard Hibernate Entity Upsert process.
            //   If Entity exists and any attribute has changed, then update the attributes
            //   if not, then create new entity.
            Optional.ofNullable(existingEvents.get(id)).ifPresentOrElse(
                    existing -> {
                        if (hasChanged(dto, existing)) {
                            // Existing event has changed — update
                            Mt564Event updated = copyMT564Fields(existing, dto);
                            entitiesToSave.add(updated);
                        }
                    },
                    () -> {
                        // New event — insert
                        entitiesToSave.add(Mt564Event.of(dto));
                    }
            );
        });

        if (CollectionUtils.isNotEmpty(entitiesToSave)) {
            // Bulk save new and updated entities
            List<Mt564Event> persistedEvents = eventRepository.saveAll(entitiesToSave);
            // Bulk save Audits JSON Objects for the persisted Events. This will be later used by the API to return Delta changes.
            buildAudit(persistedEvents);
        }
    }


    /**
     * Builds and persists audit entries for a list of MT564 event entities.
     *
     * This method performs the following steps:
     * - Iterates over each MT564 event in the provided list.
     * - Converts each event entity to a JSON snapshot using ObjectMapper.
     * - Constructs an Mt564EventAudit object containing:
     *     - A composite audit ID (eventReference, financialInstrumentId, senderBic, versionNumber)
     *     - The business hash from the event
     *     - The current timestamp as the update time
     *     - The serialized snapshot of the event entity
     * - Filters out any failed conversions (null values due to JSON processing exceptions).
     * - Persists all valid audit records to the audit repository using saveAllAndFlush.
     *
     * This function is essential for tracking historical changes to MT564 events
     * and enabling delta comparison between versions.
     *
     * @param persistedEvents the list of MT564 event entities that were newly persisted or updated.
     */
    private void buildAudit(List<Mt564Event> persistedEvents) {
        List<Mt564EventAudit> audits = persistedEvents.stream().map(mt564Event -> {
            try {
                String snapshotJson = objectMapper.writeValueAsString(mt564Event);
                return Mt564EventAudit.builder()
                        .id(new Mt564EventAuditId(mt564Event.getId().getEventReference(), mt564Event.getId().getFinancialInstrumentId(), mt564Event.getId().getSenderBic(), mt564Event.getVersionNumber()))
                        .businessHash(mt564Event.getBusinessHash())
                        .updatedAt(LocalDateTime.now())
                        .snapshot(snapshotJson)
                        .build();
            } catch (JsonProcessingException e) {
                log.error("Json Processing Exception Encountered for [{}]", mt564Event);
                return null;
            }
        }).filter(Objects::nonNull).toList();
        auditRepository.saveAllAndFlush(audits);
    }

    private boolean hasChanged(Mt564EventDto dto, Mt564Event entity) {
        String incomingHash = dto.getBusinessHash();
        return !incomingHash.equals(entity.getBusinessHash());
    }

    private Mt564Event copyMT564Fields(Mt564Event existingEntity,Mt564EventDto updatedDto) {
        existingEntity.setRelatedMessageReference(updatedDto.getRelatedMessageReference());
        existingEntity.setCorporateActionEventId(updatedDto.getCorporateActionEventId());
        existingEntity.setCorporateActionEventType(updatedDto.getCorporateActionEventType());
        existingEntity.setMandatoryVoluntaryIndicator(updatedDto.getMandatoryVoluntaryIndicator());
        existingEntity.setEventProcessingStatus(updatedDto.getEventProcessingStatus());
        existingEntity.setEventStatusCode(updatedDto.getEventStatusCode());
        existingEntity.setOfficialCorporateActionEventId(updatedDto.getOfficialCorporateActionEventId());
        existingEntity.setLinkEventId(updatedDto.getLinkEventId());
        existingEntity.setSafekeepingAccount(updatedDto.getSafekeepingAccount());
        existingEntity.setPlaceOfSafekeeping(updatedDto.getPlaceOfSafekeeping());
        existingEntity.setEventCreationDatetime(updatedDto.getEventCreationDatetime());
        existingEntity.setEffectiveDate(updatedDto.getEffectiveDate());
        existingEntity.setRecordDate(updatedDto.getRecordDate());
        existingEntity.setExDate(updatedDto.getExDate());
        existingEntity.setAnnouncementDate(updatedDto.getAnnouncementDate());
        existingEntity.setResponseDeadline(updatedDto.getResponseDeadline());
        existingEntity.setGeneralMeetingDate(updatedDto.getGeneralMeetingDate());
        existingEntity.setNarrative(updatedDto.getNarrative());
        existingEntity.setMarketDisclosureFlag(updatedDto.getMarketDisclosureFlag());
        existingEntity.setBusinessHash(updatedDto.getBusinessHash());
        existingEntity.setUpdatedAt(updatedDto.getUpdatedAt());
        return existingEntity;
    }

}

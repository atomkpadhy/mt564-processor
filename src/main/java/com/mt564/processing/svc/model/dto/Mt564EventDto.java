package com.mt564.processing.svc.model.dto;

import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Inbound MT564 Sample Fields that we are gonna process in this application.
public class Mt564EventDto {

    // Composite Key Fields
    private String eventReference;           // :20C::CORP
    private String financialInstrumentId;    // :35B:
    private String senderBic;                // Sender's BIC

    // Event Attributes
    private String relatedMessageReference;           // :20C::RELA
    private String corporateActionEventId;            // :20C::CAEV
    private String corporateActionEventType;          // :22F::CAEV
    private String mandatoryVoluntaryIndicator;       // :22F::CAMV
    private String eventProcessingStatus;             // :22F::STCO
    private String eventStatusCode;                   // :22F::EVST
    private String officialCorporateActionEventId;    // :20C::OFFI
    private String linkEventId;                       // :20C::LINK
    private String safekeepingAccount;                // :97A:/97B:
    private String placeOfSafekeeping;                // :93B::PSTI

    private LocalDateTime eventCreationDatetime;      // Event creation date

    private LocalDate effectiveDate;                  // :98A::EFFE
    private LocalDate recordDate;                     // :98A::RDTE
    private LocalDate exDate;                         // :98A::EXDI
    private LocalDate announcementDate;               // :98A::ANOU
    private LocalDate responseDeadline;               // :98A::RDPD or :98C::RDPD
    private LocalDate generalMeetingDate;             // :98A::MEET

    private String narrative;                         // :70E::ADTX
    private Boolean marketDisclosureFlag;             // Custom flag

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnore
    public String getBusinessHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String input = String.join("|",
                    Optional.ofNullable(getRelatedMessageReference()).orElse(""),
                    Optional.ofNullable(getCorporateActionEventId()).orElse(""),
                    Optional.ofNullable(getCorporateActionEventType()).orElse(""),
                    Optional.ofNullable(getMandatoryVoluntaryIndicator()).orElse(""),
                    Optional.ofNullable(getEventProcessingStatus()).orElse(""),
                    Optional.ofNullable(getEventStatusCode()).orElse(""),
                    Optional.ofNullable(getOfficialCorporateActionEventId()).orElse(""),
                    Optional.ofNullable(getLinkEventId()).orElse(""),
                    Optional.ofNullable(getSafekeepingAccount()).orElse(""),
                    Optional.ofNullable(getPlaceOfSafekeeping()).orElse(""),
                    String.valueOf(getEventCreationDatetime()),
                    String.valueOf(getEffectiveDate()),
                    String.valueOf(getRecordDate()),
                    String.valueOf(getExDate()),
                    String.valueOf(getAnnouncementDate()),
                    String.valueOf(getResponseDeadline()),
                    String.valueOf(getGeneralMeetingDate()),
                    Optional.ofNullable(getNarrative()).orElse(""),
                    String.valueOf(getMarketDisclosureFlag())
            );

            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not found", e);
        }
    }
}

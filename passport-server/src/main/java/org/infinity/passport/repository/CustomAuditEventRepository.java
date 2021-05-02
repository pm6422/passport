package org.infinity.passport.repository;

import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.annotation.ExecutionSwitch;
import org.infinity.passport.component.AuditEventConverter;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.PersistentAuditEvent;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of Spring Boot's AuditEventRepository.
 */
@Repository
@Slf4j
public class CustomAuditEventRepository implements AuditEventRepository {

    private static final String AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE";

    /**
     * Should be the same as in Liquibase migration.
     */
    protected static final int EVENT_DATA_COLUMN_MAX_LENGTH = 255;

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    private final AuditEventConverter auditEventConverter;

    public CustomAuditEventRepository(PersistenceAuditEventRepository persistenceAuditEventRepository,
                                      AuditEventConverter auditEventConverter) {

        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        List<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository
                .findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal, after, type);
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @ExecutionSwitch(on = "application.user-audit-event.enabled")
    public void add(AuditEvent event) {
        if (!AUTHORIZATION_FAILURE.equals(event.getType()) && !Authority.ANONYMOUS.equals(event.getPrincipal())) {
            PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent();
            persistentAuditEvent.setPrincipal(event.getPrincipal());
            persistentAuditEvent.setAuditEventType(event.getType());
            persistentAuditEvent.setAuditEventDate(event.getTimestamp());
            Map<String, String> eventData = auditEventConverter.convertDataToStrings(event.getData());
            persistentAuditEvent.setData(truncate(eventData));
            // Automatically delete after 90 days
            persistentAuditEvent.setExpiryTime(Instant.now().plus(90, ChronoUnit.DAYS));
            persistenceAuditEventRepository.save(persistentAuditEvent);
        }
    }

    /**
     * Truncate event data that might exceed column length.
     */
    private Map<String, String> truncate(Map<String, String> data) {
        Map<String, String> results = new HashMap<>(data.size());

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                int length = value.length();
                if (length > EVENT_DATA_COLUMN_MAX_LENGTH) {
                    value = value.substring(0, EVENT_DATA_COLUMN_MAX_LENGTH);
                    log.warn(
                            "Event data for {} too long ({}) has been truncated to {}. Consider increasing column width.",
                            entry.getKey(), length, EVENT_DATA_COLUMN_MAX_LENGTH);
                }
            }
            results.put(entry.getKey(), value);
        }
        return results;
    }
}

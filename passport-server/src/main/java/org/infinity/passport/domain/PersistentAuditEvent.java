package org.infinity.passport.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Persist AuditEvent managed by the Spring Boot actuator
 * {@link org.springframework.boot.actuate.audit.AuditEvent}
 */
@Document(collection = "PersistentAuditEvent")
@Data
public class PersistentAuditEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String principal;

    @Field("event_date")
    private Instant auditEventDate;

    @Field("event_type")
    private String auditEventType;
    /**
     * Delete records at a specific time automatically by mongoDB
     */
    @Indexed(expireAfterSeconds = 0)
    private Date expiration;

    private Map<String, String> data = new HashMap<>();

}

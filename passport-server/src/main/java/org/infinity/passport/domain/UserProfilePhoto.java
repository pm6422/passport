package org.infinity.passport.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.Binary;
import org.infinity.passport.domain.base.AbstractAuditableDomain;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Spring Data MongoDB collection for the UserProfilePhoto entity.
 */
@Document(collection = "UserProfilePhoto")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class UserProfilePhoto extends AbstractAuditableDomain implements Serializable {

    private static final long serialVersionUID = -8375847941374800940L;

    @NotNull
    @Indexed(unique = true)
    private String userId;

    private Binary profilePhoto;

    public UserProfilePhoto(String userId) {
        this.userId = userId;
    }
}

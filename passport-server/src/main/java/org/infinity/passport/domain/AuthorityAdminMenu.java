package org.infinity.passport.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Spring Data MongoDB collection for the AuthorityAdminMenu entity.
 */
@Document(collection = "AuthorityAdminMenu")
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class AuthorityAdminMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(min = 1, max = 20)
    private String authorityName;

    @NotNull
    private String adminMenuId;

    public AuthorityAdminMenu(String authorityName, String adminMenuId) {
        super();
        this.authorityName = authorityName;
        this.adminMenuId = adminMenuId;
    }

    public static AuthorityAdminMenu of(String authorityName, String adminMenuId) {
        return new AuthorityAdminMenu(authorityName, adminMenuId);
    }
}
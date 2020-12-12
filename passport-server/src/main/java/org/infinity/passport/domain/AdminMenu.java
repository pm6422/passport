package org.infinity.passport.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.infinity.passport.domain.base.AbstractAuditableDomain;
import org.infinity.passport.dto.AdminMenuDTO;
import org.infinity.passport.dto.AdminMenuTreeDTO;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Spring Data MongoDB collection for the AdminMenu entity.
 */
@Document(collection = "AdminMenu")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class AdminMenu extends AbstractAuditableDomain implements Serializable {
    private static final long   serialVersionUID = 1L;
    public static final  String FIELD_LEVEL      = "level";
    public static final  String FIELD_SEQUENCE   = "sequence";

    @NotNull
    @Size(min = 1, max = 20)
    @Indexed
    private String  appName;
    @NotNull
    @Size(min = 1, max = 50)
    private String  name;
    private String  label;
    @Field(FIELD_LEVEL)
    private Integer level;
    private String  url;
    @Field(FIELD_SEQUENCE)
    private Integer sequence;
    private String  parentId;
    /**
     * DO NOT persist to DB
     */
    @Transient
    private Boolean checked;

    public AdminMenu(@NotNull @Size(min = 1, max = 20) String appName) {
        this.appName = appName;
    }

    public AdminMenu(String appName, String name, String label, Integer level, String url,
                     Integer sequence, String parentId) {
        super();
        this.appName = appName;
        this.name = name;
        this.label = label;
        this.level = level;
        this.url = url;
        this.sequence = sequence;
        this.parentId = parentId;
    }

    public AdminMenuDTO toDTO() {
        AdminMenuDTO dto = new AdminMenuDTO();
        BeanCopier beanCopier = BeanCopier.create(AdminMenu.class, AdminMenuDTO.class, false);
        beanCopier.copy(this, dto, null);
        return dto;
    }

    public static AdminMenu of(AdminMenuDTO dto) {
        AdminMenu dest = new AdminMenu();
        BeanCopier beanCopier = BeanCopier.create(AdminMenuDTO.class, AdminMenu.class, false);
        beanCopier.copy(dto, dest, null);
        return dest;
    }

    public AdminMenuTreeDTO toTreeDTO() {
        AdminMenuTreeDTO dto = new AdminMenuTreeDTO();
        BeanCopier beanCopier = BeanCopier.create(AdminMenu.class, AdminMenuTreeDTO.class, false);
        beanCopier.copy(this, dto, null);
        return dto;
    }
}
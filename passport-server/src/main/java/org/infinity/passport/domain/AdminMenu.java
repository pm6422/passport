package org.infinity.passport.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.infinity.passport.domain.base.BaseAdminMenu;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Spring Data MongoDB collection for the AdminMenu entity.
 */
@ApiModel("管理系统菜单")
@Document(collection = "AdminMenu")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AdminMenu extends BaseAdminMenu implements Serializable {

    private static final long serialVersionUID = 5423774898556939254L;

    public AdminMenu(String appName, String name, String label, Integer level, String url,
                     Integer sequence, String parentId) {
        super(appName, name, label, level, url, sequence, parentId);
    }
}
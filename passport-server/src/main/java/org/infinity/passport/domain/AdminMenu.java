package org.infinity.passport.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.infinity.passport.domain.base.AbstractAuditableDomain;
import org.infinity.passport.dto.AdminMenuTreeDTO;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * Spring Data MongoDB collection for the AdminMenu entity.
 */
@ApiModel("管理系统菜单")
@Document(collection = "AdminMenu")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class AdminMenu extends AbstractAuditableDomain implements Serializable {
    private static final long   serialVersionUID = 1L;
    public static final  String FIELD_LEVEL      = "level";
    public static final  String FIELD_SEQUENCE   = "sequence";

    @ApiModelProperty(value = "应用名称", required = true)
    @NotNull
    @Size(min = 1, max = 20)
    @Indexed
    private String appName;

    @ApiModelProperty(value = "菜单名称", required = true)
    @NotNull
    @Size(min = 1, max = 30)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{EP5901}")
    @Indexed
    private String name;

    @ApiModelProperty(value = "菜单显示文本", required = true)
    @NotNull
    @Size(min = 1, max = 30)
    private String label;

    @ApiModelProperty(value = "菜单层级", required = true)
    @Min(1)
    @Max(9)
    private Integer level;

    @ApiModelProperty(value = "菜单链接地址", required = true)
    @NotNull
    @Size(min = 3, max = 200)
    private String url;

    @ApiModelProperty(value = "菜单排序序号", required = true)
    @Min(1)
    @Max(999)
    private Integer sequence;

    @ApiModelProperty("父菜单ID")
    private String parentId;

    @ApiModelProperty("是否选中")
    @Transient
    private Boolean checked;

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

    public AdminMenuTreeDTO toTreeDTO() {
        AdminMenuTreeDTO dto = new AdminMenuTreeDTO();
        BeanCopier beanCopier = BeanCopier.create(AdminMenu.class, AdminMenuTreeDTO.class, false);
        beanCopier.copy(this, dto, null);
        return dto;
    }
}
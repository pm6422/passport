package org.infinity.passport.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.infinity.passport.domain.base.AbstractAuditableDomain;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Spring Data MongoDB collection for the DictItem entity.
 */
@ApiModel("数据字典项")
@Document(collection = "DictItem")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class DictItem extends AbstractAuditableDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字典编号", required = true)
    @NotNull
    @Indexed
    private String dictCode;

    @ApiModelProperty(value = "字典名称", required = true)
    private String dictName;

    @ApiModelProperty(value = "字典项编号", required = true)
    @NotNull
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{EP5901}")
    @Indexed
    private String dictItemCode;

    @ApiModelProperty(value = "字典项名称", required = true)
    @NotNull
    @Size(min = 2, max = 50)
    private String dictItemName;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否可用")
    private Boolean enabled;

}
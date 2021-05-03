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
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Spring Data MongoDB collection for the Dict entity.
 */
@ApiModel("数据字典")
@Document(collection = "Dict")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class Dict extends AbstractAuditableDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字典编号", required = true)
    @NotNull
    @Size(min = 2, max = 50)
    @Indexed(unique = true)
    private String dictCode;

    @ApiModelProperty(value = "字典名称", required = true)
    @NotNull
    @Size(min = 2, max = 50)
    private String dictName;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否可用")
    private Boolean enabled;

    public Dict(String dictName, Boolean enabled) {
        this.dictName = dictName;
        this.enabled = enabled;
    }

    public Dict(String dictCode, String dictName, String remark, Boolean enabled) {
        super();
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.remark = remark;
        this.enabled = enabled;
    }
}
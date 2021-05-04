package org.infinity.passport.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AdminMenuTreeDTO implements Serializable {

    private static final long                   serialVersionUID = -3123723565571697648L;
    @ApiModelProperty(value = "ID", required = true)
    private              String                 id;
    @ApiModelProperty(value = "名称", required = true)
    private              String                 name;
    @ApiModelProperty(value = "链接地址", required = true)
    private              String                 url;
    @ApiModelProperty(value = "排序序号", required = true)
    private transient    Integer                sequence;
    @ApiModelProperty("是否选中")
    private              Boolean                checked;
    @ApiModelProperty(value = "叶子节点")
    private              List<AdminMenuTreeDTO> children;
}

package org.infinity.passport.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@ApiModel("重置密码DTO")
@Data
public class ResetKeyAndPasswordDTO implements Serializable {

    private static final long serialVersionUID = -6442194590613017034L;

    @ApiModelProperty("重置码")
    @NotNull
    private String key;

    @ApiModelProperty("新密码")
    @NotNull
    @Size(min = ManagedUserDTO.RAW_PASSWORD_MIN_LENGTH, max = ManagedUserDTO.RAW_PASSWORD_MAX_LENGTH)
    private String newPassword;

}

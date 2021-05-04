package org.infinity.passport.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.infinity.passport.domain.User;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * A DTO extending the UserDTO, which is meant to be used in the user management UI.
 */
@ApiModel("用户扩展DTO")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ManagedUserDTO extends User {

    private static final long serialVersionUID = -8095593058946091229L;

    public static final int RAW_PASSWORD_MIN_LENGTH = 5;

    public static final int RAW_PASSWORD_MAX_LENGTH = 50;

    @ApiModelProperty(value = "密码", required = true)
    @NotNull
    @Size(min = RAW_PASSWORD_MIN_LENGTH, max = RAW_PASSWORD_MAX_LENGTH)
    private String password;

    public ManagedUserDTO(User user, Set<String> authorities) {
        BeanUtils.copyProperties(user, this);
        setAuthorities(authorities);
    }

    public User toUser() {
        User user = new User();
        BeanUtils.copyProperties(this, user);
        return user;
    }
}

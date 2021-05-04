package org.infinity.passport.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.infinity.passport.domain.base.AbstractAuditableDomain;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

/**
 * Spring Data MongoDB collection for the User entity.
 */
@ApiModel("用户")
@Document(collection = "User")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class User extends AbstractAuditableDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_PASSWORD = "123456";

    public static final String FIELD_USER_NAME = "userName";

    public static final String FIELD_EMAIL = "email";

    public static final String FIELD_MOBILE_NO = "mobileNo";

    @ApiModelProperty(value = "用户名", required = true)
    @NotNull
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{EP5901}")
    @Size(min = 3, max = 20)
    @Indexed(unique = true)
    private String userName;

    @ApiModelProperty(value = "名", required = true)
    @NotNull
    @Size(min = 1, max = 20)
    private String firstName;

    @ApiModelProperty(value = "姓", required = true)
    @NotNull
    @Size(min = 1, max = 20)
    private String lastName;

    @ApiModelProperty(value = "电子邮件", required = true)
    @NotNull
    @Email
    @Size(min = 3, max = 30)
    @Indexed
    private String email;

    @ApiModelProperty(value = "手机号", required = true)
    @NotNull
    @Pattern(regexp = "^(13[0-9]|15[012356789]|17[03678]|18[0-9]|14[57])[0-9]{8}$", message = "{EP5951}")
    @Size(min = 11, max = 13)
    @Indexed
    private String mobileNo;

    @JsonIgnore
    private String passwordHash;

    @ApiModelProperty(value = "是否激活")
    private Boolean activated;

    @ApiModelProperty(value = "激活密钥")
    @JsonIgnore
    private String activationKey;

    @ApiModelProperty(value = "重置密钥")
    @JsonIgnore
    private String resetKey;

    @ApiModelProperty(value = "重置密码时间")
    private Instant resetTime;

    @ApiModelProperty(value = "是否包含个人头像")
    private Boolean hasProfilePhoto;

    @ApiModelProperty(value = "是否可用")
    private Boolean enabled;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "权限列表")
    @Transient
    private Set<String> authorities;
}

package org.infinity.passport.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.infinity.passport.domain.base.AbstractAuditableDomain;
import org.infinity.passport.dto.MongoOAuth2RefreshTokenDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "MongoOAuth2RefreshToken")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MongoOAuth2RefreshToken extends AbstractAuditableDomain implements Serializable {

    private static final long                 serialVersionUID = 1L;
    @Id
    private              String               id;
    private              String               userName;
    private              String               clientId;
    private              OAuth2RefreshToken   oAuth2RefreshToken;
    @Indexed(expireAfterSeconds = 0)//Expire Documents at a Specific Clock Time
    private              Date                 expiration;
    private              OAuth2Authentication authentication;

    public MongoOAuth2RefreshToken(OAuth2RefreshToken oAuth2RefreshToken, OAuth2Authentication authentication) {
        this.id = oAuth2RefreshToken.getValue();
        this.userName = authentication.getName();
        this.clientId = authentication.getOAuth2Request().getClientId();
        this.oAuth2RefreshToken = oAuth2RefreshToken;
        this.expiration = ((DefaultExpiringOAuth2RefreshToken) oAuth2RefreshToken).getExpiration();
        this.authentication = authentication;
    }

    public MongoOAuth2RefreshTokenDTO asDTO() {
        MongoOAuth2RefreshTokenDTO dest = new MongoOAuth2RefreshTokenDTO();
        BeanUtils.copyProperties(this, dest);
        return dest;
    }
}

package org.infinity.passport.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.infinity.passport.domain.base.AbstractAuditableDomain;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.io.Serializable;
import java.time.Instant;

@ApiModel("刷新令牌信息")
@Document(collection = "MongoOAuth2RefreshToken")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class MongoOAuth2RefreshToken extends AbstractAuditableDomain implements Serializable {

    private static final long                 serialVersionUID = 1L;
    private              String               userName;
    private              String               clientId;
    private              OAuth2RefreshToken   oAuth2RefreshToken;
    /**
     * Delete records at a specific time automatically by mongoDB
     */
    @Indexed(expireAfterSeconds = 0)
    private              Instant              expiration;
    private              OAuth2Authentication authentication;

    public MongoOAuth2RefreshToken(OAuth2RefreshToken oAuth2RefreshToken, OAuth2Authentication authentication) {
        this.id = oAuth2RefreshToken.getValue();
        this.userName = authentication.getName();
        this.clientId = authentication.getOAuth2Request().getClientId();
        this.oAuth2RefreshToken = oAuth2RefreshToken;
        this.expiration = ((DefaultExpiringOAuth2RefreshToken) oAuth2RefreshToken).getExpiration().toInstant();
        this.authentication = authentication;
    }
}

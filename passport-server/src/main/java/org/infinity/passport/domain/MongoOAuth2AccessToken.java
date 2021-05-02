package org.infinity.passport.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.infinity.passport.domain.base.AbstractAuditableDomain;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.io.Serializable;
import java.time.Instant;

@ApiModel("访问令牌信息")
@Document(collection = "MongoOAuth2AccessToken")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class MongoOAuth2AccessToken extends AbstractAuditableDomain implements Serializable {

    private static final long                 serialVersionUID = 1L;
    /**
     * AccessTokenId存储在id字段则保证所有客户端共享，一个客户端退出可以退出所有客户端
     */
    private              OAuth2AccessToken    oAuth2AccessToken;
    private              String               authenticationId;
    private              String               userName;
    private              String               clientId;
    /**
     * Delete records at a specific time automatically by mongoDB
     */
    @Indexed(expireAfterSeconds = 0)
    private              Instant              expiration;
    private              OAuth2Authentication authentication;
    private              String               refreshToken;

    @PersistenceConstructor
    public MongoOAuth2AccessToken(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication authentication,
                                  String authenticationId) {
        this.id = oAuth2AccessToken.getValue();
        this.oAuth2AccessToken = oAuth2AccessToken;
        this.authenticationId = authenticationId;
        this.userName = authentication.getName();
        this.clientId = authentication.getOAuth2Request().getClientId();
        this.expiration = oAuth2AccessToken.getExpiration().toInstant();

        this.authentication = authentication;
        if (oAuth2AccessToken.getRefreshToken() != null) {
            this.refreshToken = oAuth2AccessToken.getRefreshToken().getValue();
        }
    }
}

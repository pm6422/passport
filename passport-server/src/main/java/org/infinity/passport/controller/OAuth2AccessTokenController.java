package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.MongoOAuth2AccessToken;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.OAuth2AccessTokenRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

@RestController
@Api(tags = "访问令牌")
@Slf4j
public class OAuth2AccessTokenController {

    private final OAuth2AccessTokenRepository oAuth2AccessTokenRepository;
    private final HttpHeaderCreator           httpHeaderCreator;

    public OAuth2AccessTokenController(OAuth2AccessTokenRepository oAuth2AccessTokenRepository,
                                       HttpHeaderCreator httpHeaderCreator) {
        this.oAuth2AccessTokenRepository = oAuth2AccessTokenRepository;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("分页检索访问令牌列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/oauth2-access-tokens")
    @Secured(Authority.ADMIN)
    public ResponseEntity<List<MongoOAuth2AccessToken>> find(Pageable pageable,
                                                             @ApiParam(value = "访问令牌ID") @RequestParam(value = "tokenId", required = false) String tokenId,
                                                             @ApiParam(value = "客户端ID") @RequestParam(value = "clientId", required = false) String clientId,
                                                             @ApiParam(value = "用户名") @RequestParam(value = "userName", required = false) String userName,
                                                             @ApiParam(value = "刷新令牌") @RequestParam(value = "refreshToken", required = false) String refreshToken) {
        MongoOAuth2AccessToken probe = new MongoOAuth2AccessToken();
        probe.setId(tokenId);
        probe.setClientId(clientId);
        probe.setUserName(userName);
        probe.setRefreshToken(refreshToken);
        Page<MongoOAuth2AccessToken> tokens = oAuth2AccessTokenRepository.findAll(Example.of(probe), pageable);
        HttpHeaders headers = generatePageHeaders(tokens);
        return ResponseEntity.ok().headers(headers).body(tokens.getContent());
    }

    @ApiOperation("根据ID检索访问令牌")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "访问令牌不存在")})
    @GetMapping("/api/oauth2-access-tokens/{id}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<MongoOAuth2AccessToken> findById(
            @ApiParam(value = "访问令牌ID", required = true) @PathVariable String id) {
        MongoOAuth2AccessToken domain = oAuth2AccessTokenRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        return ResponseEntity.ok(domain);
    }

    @ApiOperation(value = "根据ID删除访问令牌", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "访问令牌不存在")})
    @DeleteMapping("/api/oauth2-access-tokens/{id}")
    @Secured(Authority.ADMIN)
    public ResponseEntity<Void> delete(@ApiParam(value = "访问令牌ID", required = true) @PathVariable String id) {
        log.debug("REST request to delete oauth2 access token: {}", id);
        oAuth2AccessTokenRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        oAuth2AccessTokenRepository.deleteById(id);
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("SM1003", id)).build();
    }
}

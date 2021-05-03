package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.MongoOAuth2RefreshToken;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.OAuth2RefreshTokenRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
@Api(tags = "刷新令牌信息")
@Slf4j
public class OAuth2RefreshTokenController {

    private final OAuth2RefreshTokenRepository oAuth2RefreshTokenRepository;

    private final HttpHeaderCreator httpHeaderCreator;

    public OAuth2RefreshTokenController(OAuth2RefreshTokenRepository oAuth2RefreshTokenRepository,
                                        HttpHeaderCreator httpHeaderCreator) {
        this.oAuth2RefreshTokenRepository = oAuth2RefreshTokenRepository;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("分页检索刷新令牌列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/oauth2-refresh-tokens")
    @Secured(Authority.ADMIN)
    public ResponseEntity<List<MongoOAuth2RefreshToken>> find(Pageable pageable,
                                                              @ApiParam(value = "刷新令牌ID") @RequestParam(value = "tokenId", required = false) String tokenId,
                                                              @ApiParam(value = "客户端ID") @RequestParam(value = "clientId", required = false) String clientId,
                                                              @ApiParam(value = "用户名") @RequestParam(value = "userName", required = false) String userName) {
        MongoOAuth2RefreshToken probe = new MongoOAuth2RefreshToken();
        probe.setId(tokenId);
        probe.setClientId(clientId);
        probe.setUserName(userName);
        // Ignore query parameter if it has a null value
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
        Page<MongoOAuth2RefreshToken> tokens = oAuth2RefreshTokenRepository.findAll(Example.of(probe, matcher), pageable);
        HttpHeaders headers = generatePageHeaders(tokens);
        return ResponseEntity.ok().headers(headers).body(tokens.getContent());
    }

    @ApiOperation("根据ID检索刷新令牌")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "刷新令牌不存在")})
    @GetMapping("/api/oauth2-refresh-tokens/{id}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<MongoOAuth2RefreshToken> findById(
            @ApiParam(value = "刷新令牌ID", required = true) @PathVariable String id) {
        MongoOAuth2RefreshToken domain = oAuth2RefreshTokenRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        return ResponseEntity.ok(domain);
    }

    @ApiOperation(value = "根据ID删除刷新令牌", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "刷新令牌不存在")})
    @DeleteMapping("/api/oauth2-refresh-tokens/{id}")
    @Secured(Authority.ADMIN)
    public ResponseEntity<Void> delete(@ApiParam(value = "刷新令牌ID", required = true) @PathVariable String id) {
        log.debug("REST request to delete oauth2 access token: {}", id);
        oAuth2RefreshTokenRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        oAuth2RefreshTokenRepository.deleteById(id);
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("SM1003", id))
                .build();
    }
}

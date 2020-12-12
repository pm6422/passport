package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.MongoOAuth2AuthorizationCode;
import org.infinity.passport.dto.MongoOAuth2AuthorizationCodeDTO;
import org.infinity.passport.exception.NoDataException;
import org.infinity.passport.repository.OAuth2AuthorizationCodeRepository;
import org.infinity.passport.component.HttpHeaderCreator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

@RestController
@Api(tags = "登录授权码")
@Slf4j
public class OAuth2AuthorizationCodeController {

    private final OAuth2AuthorizationCodeRepository oAuth2AuthorizationCodeRepository;
    private final HttpHeaderCreator                 httpHeaderCreator;

    public OAuth2AuthorizationCodeController(OAuth2AuthorizationCodeRepository oAuth2AuthorizationCodeRepository,
                                             HttpHeaderCreator httpHeaderCreator) {
        this.oAuth2AuthorizationCodeRepository = oAuth2AuthorizationCodeRepository;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    /**
     * Authorization code will be deleted immediately after authentication process.
     * So the database will be always empty.
     *
     * @param pageable            pagination info
     * @param authorizationCodeId authorization code id
     * @param code                authorization code
     * @return code list
     * @throws URISyntaxException if exception occurs
     */
    @ApiOperation("分页检索授权码列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/oauth2-authorization-code/codes")
    @Secured(Authority.ADMIN)
    public ResponseEntity<List<MongoOAuth2AuthorizationCodeDTO>> find(Pageable pageable,
                                                                      @ApiParam(value = "授权码ID") @RequestParam(value = "authorizationCodeId", required = false) String authorizationCodeId,
                                                                      @ApiParam(value = "授权码") @RequestParam(value = "code", required = false) String code)
            throws URISyntaxException {
        MongoOAuth2AuthorizationCode probe = new MongoOAuth2AuthorizationCode();
        probe.setId(authorizationCodeId);
        probe.setCode(code);
        Page<MongoOAuth2AuthorizationCode> codes = oAuth2AuthorizationCodeRepository.findAll(Example.of(probe), pageable);
        List<MongoOAuth2AuthorizationCodeDTO> DTOs = codes.getContent().stream().map(MongoOAuth2AuthorizationCode::toDTO)
                .collect(Collectors.toList());
        HttpHeaders headers = generatePageHeaders(codes);
        return ResponseEntity.ok().headers(headers).body(DTOs);
    }

    @ApiOperation("根据ID检索授权码")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "授权码不存在")})
    @GetMapping("/api/oauth2-authorization-code/codes/{id}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<MongoOAuth2AuthorizationCodeDTO> findById(
            @ApiParam(value = "授权码ID", required = true) @PathVariable String id) {
        MongoOAuth2AuthorizationCode entity = oAuth2AuthorizationCodeRepository.findById(id).orElseThrow(() -> new NoDataException(id));
        return ResponseEntity.ok(entity.toDTO());
    }

    @ApiOperation(value = "根据ID删除授权码", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "授权码不存在")})
    @DeleteMapping("/api/oauth2-authorization-code/codes/{id}")
    @Secured(Authority.ADMIN)
    public ResponseEntity<Void> delete(@ApiParam(value = "授权码ID", required = true) @PathVariable String id) {
        log.debug("REST request to delete oauth2 authorization code: {}", id);
        oAuth2AuthorizationCodeRepository.findById(id).orElseThrow(() -> new NoDataException(id));
        oAuth2AuthorizationCodeRepository.deleteById(id);
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("notification.oauth2.authorization.code.deleted", id))
                .build();
    }
}

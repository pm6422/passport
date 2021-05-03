package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.MongoOAuth2Approval;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.OAuth2ApprovalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

@RestController
@Api(tags = "登录授权")
@Slf4j
public class OAuth2ApprovalController {

    private final OAuth2ApprovalRepository oAuth2ApprovalRepository;

    private final MongoTemplate mongoTemplate;

    private final HttpHeaderCreator httpHeaderCreator;

    public OAuth2ApprovalController(OAuth2ApprovalRepository oAuth2ApprovalRepository,
                                    MongoTemplate mongoTemplate,
                                    HttpHeaderCreator httpHeaderCreator) {
        this.oAuth2ApprovalRepository = oAuth2ApprovalRepository;
        this.mongoTemplate = mongoTemplate;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("分页检索登录授权列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/oauth2-approvals")
    @Secured(Authority.ADMIN)
    public ResponseEntity<List<MongoOAuth2Approval>> find(Pageable pageable,
                                                          @ApiParam(value = "授权ID") @RequestParam(value = "approvalId", required = false) String approvalId,
                                                          @ApiParam(value = "客户端ID") @RequestParam(value = "clientId", required = false) String clientId,
                                                          @ApiParam(value = "用户名") @RequestParam(value = "userName", required = false) String userName) {
        Query query = new Query();
        if (StringUtils.isNotEmpty(approvalId)) {
            query.addCriteria(Criteria.where("id").is(approvalId));
        }
        if (StringUtils.isNotEmpty(clientId)) {
            query.addCriteria(Criteria.where("clientId").is(clientId));
        }
        if (StringUtils.isNotEmpty(userName)) {
            query.addCriteria(Criteria.where("userId").is(userName));
        }
        long totalCount = mongoTemplate.count(query, MongoOAuth2Approval.class);
        query.with(pageable);
        Page<MongoOAuth2Approval> approvals = new PageImpl<>(mongoTemplate.find(query, MongoOAuth2Approval.class), pageable, totalCount);
        HttpHeaders headers = generatePageHeaders(approvals);
        return ResponseEntity.ok().headers(headers).body(approvals.getContent());
    }

    @ApiOperation("根据ID检索授权")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "授权不存在")})
    @GetMapping("/api/oauth2-approvals/{id}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<MongoOAuth2Approval> findById(
            @ApiParam(value = "授权ID", required = true) @PathVariable String id) {
        MongoOAuth2Approval domain = oAuth2ApprovalRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        return ResponseEntity.ok(domain);
    }

    @ApiOperation(value = "根据ID删除授权", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "授权不存在")})
    @DeleteMapping("/api/oauth2-approvals/{id}")
    @Secured(Authority.ADMIN)
    public ResponseEntity<Void> delete(@ApiParam(value = "授权ID", required = true) @PathVariable String id) {
        log.debug("REST request to delete oauth2 approval: {}", id);
        oAuth2ApprovalRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        oAuth2ApprovalRepository.deleteById(id);
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("SM1003", id)).build();
    }
}

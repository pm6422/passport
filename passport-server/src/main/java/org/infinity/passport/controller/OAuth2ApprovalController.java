package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.MongoOAuth2Approval;
import org.infinity.passport.dto.MongoOAuth2ApprovalDTO;
import org.infinity.passport.exception.NoDataException;
import org.infinity.passport.repository.OAuth2ApprovalRepository;
import org.infinity.passport.utils.HttpHeaderCreator;
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

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

@RestController
@Api(tags = "登录授权信息")
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

    @ApiOperation("获取授权信息信息分页列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功获取")})
    @GetMapping("/api/oauth2-approval/approvals")
    @Secured(Authority.ADMIN)
    public ResponseEntity<List<MongoOAuth2ApprovalDTO>> find(Pageable pageable,
                                                             @ApiParam(value = "授权ID") @RequestParam(value = "approvalId", required = false) String approvalId,
                                                             @ApiParam(value = "客户端ID") @RequestParam(value = "clientId", required = false) String clientId,
                                                             @ApiParam(value = "用户名") @RequestParam(value = "userName", required = false) String userName)
            throws URISyntaxException {
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
        //        oAuth2ApprovalRepository.findAll(Example.of(probe), pageable); I dont know why this statement does not work
        Page<MongoOAuth2Approval> approvals = new PageImpl<>(
                mongoTemplate.find(query, MongoOAuth2Approval.class), pageable, totalCount);

        List<MongoOAuth2ApprovalDTO> DTOs = approvals.getContent().stream().map(MongoOAuth2Approval::asDTO)
                .collect(Collectors.toList());
        HttpHeaders headers = generatePageHeaders(approvals, "/api/oauth2-approval/approvals");
        return ResponseEntity.ok().headers(headers).body(DTOs);
    }

    @ApiOperation("根据授权信息ID检索授权信息信息")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功获取"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "授权信息信息不存在")})
    @GetMapping("/api/oauth2-approval/approvals/{id}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<MongoOAuth2ApprovalDTO> findById(
            @ApiParam(value = "授权信息ID", required = true) @PathVariable String id) {
        MongoOAuth2Approval entity = oAuth2ApprovalRepository.findById(id).orElseThrow(() -> new NoDataException(id));
        return ResponseEntity.ok(entity.asDTO());
    }

    @ApiOperation(value = "根据授权信息ID删除授权信息信息", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "授权信息信息不存在")})
    @DeleteMapping("/api/oauth2-approval/approvals/{id}")
    @Secured(Authority.ADMIN)
    public ResponseEntity<Void> delete(@ApiParam(value = "授权信息ID", required = true) @PathVariable String id) {
        log.debug("REST request to delete oauth2 approval: {}", id);
        oAuth2ApprovalRepository.findById(id).orElseThrow(() -> new NoDataException(id));
        oAuth2ApprovalRepository.deleteById(id);
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("notification.oauth2.approval.deleted", id)).build();
    }
}

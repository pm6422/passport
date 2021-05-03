package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.HttpSession;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.HttpSessionRepository;
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

/**
 * REST controller for managing http sessions.
 */
@RestController
@Api(tags = "Http会话")
@Slf4j
public class HttpSessionController {

    private final HttpSessionRepository httpSessionRepository;
    private final HttpHeaderCreator     httpHeaderCreator;

    public HttpSessionController(HttpSessionRepository httpSessionRepository, HttpHeaderCreator httpHeaderCreator) {
        this.httpSessionRepository = httpSessionRepository;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("分页检索Http会话列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/http-sessions")
    @Secured({Authority.DEVELOPER})
    public ResponseEntity<List<HttpSession>> find(Pageable pageable,
                                                  @ApiParam(value = "用户名称") @RequestParam(value = "principal", required = false) String principal) {
        Page<HttpSession> sessions = StringUtils.isEmpty(principal) ? httpSessionRepository.findAll(pageable) : httpSessionRepository.findByPrincipal(pageable, principal);
        HttpHeaders headers = generatePageHeaders(sessions);
        return ResponseEntity.ok().headers(headers).body(sessions.getContent());
    }

    @ApiOperation(value = "根据ID删除Http会话", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"), @ApiResponse(code = SC_BAD_REQUEST, message = "Http会话信息不存在")})
    @DeleteMapping("/api/http-sessions/{id}")
    @Secured({Authority.DEVELOPER})
    public ResponseEntity<Void> delete(@ApiParam(value = "Http会话ID", required = true) @PathVariable String id) {
        log.debug("REST request to delete http session: {}", id);
        httpSessionRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        httpSessionRepository.deleteById(id);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1003", id)).build();
    }
}

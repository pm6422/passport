package org.infinity.passport.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.PersistentAuditEvent;
import org.infinity.passport.repository.PersistenceAuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

/**
 * REST controller for managing the user audit events.
 */
@RestController
@Api(tags = "用户审计")
public class UserAuditEventController {

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    public UserAuditEventController(PersistenceAuditEventRepository persistenceAuditEventRepository) {
        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
    }

    /**
     * 分页检索用户审计事件列表
     *
     * @param pageable 分页信息
     * @param from     开始日期 Instant反序列化会发生错误，所以使用LocalDate
     * @param to       结束日期 Instant反序列化会发生错误，所以使用LocalDate
     * @return 分页信息
     */
    @ApiOperation("分页检索用户审计事件列表")
    @GetMapping("/api/user-audit-events")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<List<PersistentAuditEvent>> getUserAuditEvents(Pageable pageable,
                                                                         @ApiParam(value = "开始日期，例：2020-10-01") @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                         @ApiParam(value = "结束日期，例：2020-10-02") @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Page<PersistentAuditEvent> userAuditEvents = persistenceAuditEventRepository.findByAuditEventDateBetween(pageable, from, to);
        HttpHeaders headers = generatePageHeaders(userAuditEvents);
        return ResponseEntity.ok().headers(headers).body(userAuditEvents.getContent());
    }
}

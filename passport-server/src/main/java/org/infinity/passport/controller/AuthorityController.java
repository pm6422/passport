package org.infinity.passport.controller;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.exception.DuplicationException;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.AuthorityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

/**
 * REST controller for managing authorities.
 */
@RestController
@Api(tags = "权限管理")
@Slf4j
public class AuthorityController {

    private final AuthorityRepository authorityRepository;
    private final HttpHeaderCreator   httpHeaderCreator;

    public AuthorityController(AuthorityRepository authorityRepository, HttpHeaderCreator httpHeaderCreator) {
        this.authorityRepository = authorityRepository;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("创建权限")
    @ApiResponses(value = {@ApiResponse(code = SC_CREATED, message = "成功创建")})
    @PostMapping("/api/authorities")
    public ResponseEntity<Void> create(
            @ApiParam(value = "权限", required = true) @Valid @RequestBody Authority domain) {
        log.debug("REST request to create authority: {}", domain);
        authorityRepository.findById(domain.getName()).ifPresent(app -> {
            throw new DuplicationException(ImmutableMap.of("name", domain.getName()));
        });
        authorityRepository.insert(domain);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(httpHeaderCreator.createSuccessHeader("SM1001", domain.getName()))
                .build();
    }

    @ApiOperation("分页检索权限列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/authorities")
    public ResponseEntity<List<Authority>> find(Pageable pageable) {
        Page<Authority> authorities = authorityRepository.findAll(pageable);
        HttpHeaders headers = generatePageHeaders(authorities);
        return ResponseEntity.ok().headers(headers).body(authorities.getContent());
    }

    @ApiOperation("根据权限名称检索权限")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "权限不存在")})
    @GetMapping("/api/authorities/{name}")
    public ResponseEntity<Authority> findById(
            @ApiParam(value = "权限名称", required = true) @PathVariable String name) {
        Authority domain = authorityRepository.findById(name).orElseThrow(() -> new NoDataFoundException(name));
        return ResponseEntity.ok(domain);
    }

    @ApiOperation("更新权限")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功更新"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "权限不存在")})
    @PutMapping("/api/authorities")
    public ResponseEntity<Void> update(
            @ApiParam(value = "新的权限", required = true) @Valid @RequestBody Authority domain) {
        log.debug("REST request to update authority: {}", domain);
        authorityRepository.findById(domain.getName()).orElseThrow(() -> new NoDataFoundException(domain.getName()));
        authorityRepository.save(domain);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1002", domain.getName())).build();
    }

    @ApiOperation(value = "根据名称删除权限", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "权限不存在")})
    @DeleteMapping("/api/authorities/{name}")
    public ResponseEntity<Void> delete(@ApiParam(value = "权限名称", required = true) @PathVariable String name) {
        log.debug("REST request to delete authority: {}", name);
        authorityRepository.findById(name).orElseThrow(() -> new NoDataFoundException(name));
        authorityRepository.deleteById(name);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1003", name)).build();
    }
}

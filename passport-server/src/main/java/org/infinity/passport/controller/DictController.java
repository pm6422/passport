package org.infinity.passport.controller;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.Dict;
import org.infinity.passport.exception.DuplicationException;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.DictRepository;
import org.infinity.passport.service.DictService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

@RestController
@Api(tags = "数据字典")
@Slf4j
public class DictController {

    private final DictRepository    dictRepository;
    private final DictService       dictService;
    private final HttpHeaderCreator httpHeaderCreator;

    public DictController(DictRepository dictRepository,
                          DictService dictService,
                          HttpHeaderCreator httpHeaderCreator) {
        this.dictRepository = dictRepository;
        this.dictService = dictService;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("创建数据字典")
    @ApiResponses(value = {@ApiResponse(code = SC_CREATED, message = "成功创建"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "字典名已存在")})
    @PostMapping("/api/dicts")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<Void> create(@ApiParam(value = "数据字典", required = true) @Valid @RequestBody Dict domain) {
        log.debug("REST request to create dict: {}", domain);
        dictRepository.findOneByDictCode(domain.getDictCode()).ifPresent((existingEntity) -> {
            throw new DuplicationException(ImmutableMap.of("dictCode", domain.getDictCode()));
        });
        dictRepository.insert(domain);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(httpHeaderCreator.createSuccessHeader("SM1001", domain.getDictName())).build();
    }

    @ApiOperation("分页检索数据字典列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/dicts")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<List<Dict>> find(Pageable pageable,
                                           @ApiParam(value = "字典名称") @RequestParam(value = "dictName", required = false) String dictName,
                                           @ApiParam(value = "是否可用,null代表全部", allowableValues = "false,true,null") @RequestParam(value = "enabled", required = false) Boolean enabled) {
        Page<Dict> dicts = dictService.find(pageable, dictName, enabled);
        HttpHeaders headers = generatePageHeaders(dicts);
        return ResponseEntity.ok().headers(headers).body(dicts.getContent());
    }

    @ApiOperation("根据ID检索数据字典")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "数据字典不存在")})
    @GetMapping("/api/dicts/{id}")
    @Secured({Authority.DEVELOPER, Authority.USER})
    public ResponseEntity<Dict> findById(@ApiParam(value = "字典编号", required = true) @PathVariable String id) {
        Dict domain = dictRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        return ResponseEntity.ok(domain);
    }

    @ApiOperation("更新数据字典")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功更新"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "数据字典不存在")})
    @PutMapping("/api/dicts")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<Void> update(@ApiParam(value = "新的数据字典", required = true) @Valid @RequestBody Dict domain) {
        log.debug("REST request to update dict: {}", domain);
        dictRepository.findById(domain.getId()).orElseThrow(() -> new NoDataFoundException(domain.getId()));
        dictRepository.save(domain);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1002", domain.getDictName())).build();
    }

    @ApiOperation(value = "根据ID删除数据字典", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "数据字典不存在")})
    @DeleteMapping("/api/dicts/{id}")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<Void> delete(@ApiParam(value = "字典编号", required = true) @PathVariable String id) {
        log.debug("REST request to delete dict: {}", id);
        Dict dict = dictRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        dictRepository.deleteById(id);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1003", dict.getDictName())).build();
    }
}

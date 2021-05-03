package org.infinity.passport.controller;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.DictItem;
import org.infinity.passport.exception.DuplicationException;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.DictItemRepository;
import org.infinity.passport.repository.DictRepository;
import org.infinity.passport.service.DictItemService;
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
import java.util.Map;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.*;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

@RestController
@Api(tags = "数据字典项")
@Slf4j
public class DictItemController {

    private final DictRepository dictRepository;

    private final DictService dictService;

    private final DictItemRepository dictItemRepository;

    private final DictItemService dictItemService;

    private final HttpHeaderCreator httpHeaderCreator;

    public DictItemController(DictRepository dictRepository,
                              DictService dictService,
                              DictItemRepository dictItemRepository,
                              DictItemService dictItemService,
                              HttpHeaderCreator httpHeaderCreator) {
        this.dictRepository = dictRepository;
        this.dictService = dictService;
        this.dictItemRepository = dictItemRepository;
        this.dictItemService = dictItemService;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("创建数据字典项")
    @ApiResponses(value = {@ApiResponse(code = SC_CREATED, message = "成功创建"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "字典code不存在或相同的字典code和字典项已存在")})
    @PostMapping("/api/dict-items")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<Void> create(
            @ApiParam(value = "数据字典项", required = true) @Valid @RequestBody DictItem domain) {
        log.debug("REST request to create dict item: {}", domain);
        DictItem dictItem = dictItemService.insert(domain);
        return ResponseEntity.status(HttpStatus.CREATED).headers(
                httpHeaderCreator.createSuccessHeader("SM1001", dictItem.getDictItemName()))
                .build();
    }

    @ApiOperation("分页检索数据字典项列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/dict-items")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<List<DictItem>> find(Pageable pageable,
                                               @ApiParam(value = "字典代码") @RequestParam(value = "dictCode", required = false) String dictCode,
                                               @ApiParam(value = "字典项名称") @RequestParam(value = "dictItemName", required = false) String dictItemName) {
        Page<DictItem> dictItems = dictItemService.find(pageable, dictCode, dictItemName);
        Map<String, String> dictCodeDictNameMap = dictService.findDictCodeDictNameMap();
        List<DictItem> domains = dictItems.getContent().stream().map(domain -> {
            domain.setDictName(dictCodeDictNameMap.get(domain.getDictCode()));
            return domain;
        }).collect(Collectors.toList());
        HttpHeaders headers = generatePageHeaders(dictItems);
        return ResponseEntity.ok().headers(headers).body(domains);
    }

    @ApiOperation("根据ID检索数据字典项")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "字典项不存在")})
    @GetMapping("/api/dict-items/{id}")
    @Secured({Authority.USER})
    public ResponseEntity<DictItem> findById(
            @ApiParam(value = "数据字典项ID", required = true) @PathVariable String id) {
        log.debug("REST request to get dict item : {}", id);
        DictItem domain = dictItemRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        return ResponseEntity.ok(domain);
    }

    @ApiOperation("更新数据字典项")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功更新"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "字典项不存在")})
    @PutMapping("/api/dict-items")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<Void> update(
            @ApiParam(value = "新的数据字典项", required = true) @Valid @RequestBody DictItem domain) {
        log.debug("REST request to update dict item: {}", domain);
        dictItemService.update(domain);
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("SM1002", domain.getDictItemName()))
                .build();
    }

    @ApiOperation(value = "根据ID删除数据字典项", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "字典项不存在")})
    @DeleteMapping("/api/dict-items/{id}")
    @Secured(Authority.DEVELOPER)
    public ResponseEntity<Void> delete(@ApiParam(value = "数据字典项ID", required = true) @PathVariable String id) {
        log.debug("REST request to delete dict item: {}", id);
        DictItem dictItem = dictItemRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        dictItemRepository.deleteById(id);
        return ResponseEntity.ok().headers(
                httpHeaderCreator.createSuccessHeader("SM1003", dictItem.getDictItemName()))
                .build();
    }
}

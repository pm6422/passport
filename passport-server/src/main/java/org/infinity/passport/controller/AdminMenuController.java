package org.infinity.passport.controller;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.AdminMenu;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.exception.DuplicationException;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.AdminMenuRepository;
import org.infinity.passport.service.AdminMenuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

/**
 * REST controller for managing the admin menu.
 */
@RestController
@Api(tags = "管理菜单")
@Slf4j
public class AdminMenuController {

    private final AdminMenuRepository adminMenuRepository;
    private final AdminMenuService    adminMenuService;
    private final HttpHeaderCreator   httpHeaderCreator;

    public AdminMenuController(AdminMenuRepository adminMenuRepository,
                               AdminMenuService adminMenuService,
                               HttpHeaderCreator httpHeaderCreator) {
        this.adminMenuRepository = adminMenuRepository;
        this.adminMenuService = adminMenuService;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("创建菜单")
    @ApiResponses(value = {@ApiResponse(code = SC_CREATED, message = "成功创建")})
    @PostMapping("/api/admin-menus")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> create(
            @ApiParam(value = "菜单", required = true) @Valid @RequestBody AdminMenu entity) {
        log.debug("REST request to create admin menu: {}", entity);
        adminMenuRepository.findOneByAppNameAndLevelAndSequence(entity.getAppName(), entity.getLevel(), entity.getSequence())
                .ifPresent((existingEntity) -> {
                    throw new DuplicationException(ImmutableMap.of("appName", entity.getAppName(), "level", entity.getLevel(), "sequence", entity.getSequence()));
                });
        adminMenuRepository.insert(entity);
        return ResponseEntity.status(HttpStatus.CREATED).headers(
                httpHeaderCreator.createSuccessHeader("SM1001", entity.getName()))
                .build();
    }

    @ApiOperation("分页检索菜单列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/admin-menus")
    @Secured({Authority.ADMIN})
    public ResponseEntity<List<AdminMenu>> find(Pageable pageable,
                                                @ApiParam(value = "应用名称") @RequestParam(value = "appName", required = false) String appName) {
        Page<AdminMenu> adminMenus = adminMenuService.find(pageable, appName);
        HttpHeaders headers = generatePageHeaders(adminMenus);
        return ResponseEntity.ok().headers(headers).body(adminMenus.getContent());
    }

    @ApiOperation("根据ID检索菜单")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "菜单不存在")})
    @GetMapping("/api/admin-menus/{id}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<AdminMenu> findById(@ApiParam(value = "菜单ID", required = true) @PathVariable String id) {
        AdminMenu domain = adminMenuRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        return ResponseEntity.ok(domain);
    }

    @ApiOperation("更新菜单")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功更新"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "菜单不存在")})
    @PutMapping("/api/admin-menus")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> update(
            @ApiParam(value = "新的菜单", required = true) @Valid @RequestBody AdminMenu domain) {
        log.debug("REST request to update admin menu: {}", domain);
        adminMenuRepository.findById(domain.getId()).orElseThrow(() -> new NoDataFoundException(domain.getId()));
        adminMenuRepository.save(domain);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1002", domain.getName())).build();
    }

    @ApiOperation("根据ID删除管理菜单")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "菜单不存在")})
    @DeleteMapping("/api/admin-menus/{id}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> delete(@ApiParam(value = "菜单ID", required = true) @PathVariable String id) {
        log.debug("REST request to delete admin menu: {}", id);
        AdminMenu adminMenu = adminMenuRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        adminMenuRepository.deleteById(id);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1003", adminMenu.getName())).build();
    }

    @ApiOperation("检索父类菜单")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/admin-menus/parents")
    @Secured({Authority.ADMIN})
    public ResponseEntity<List<AdminMenu>> findParents(
            @ApiParam(value = "应用名称", required = true) @RequestParam(value = "appName") String appName,
            @ApiParam(value = "菜单级别", required = true) @RequestParam(value = "level") Integer level) {
        List<AdminMenu> all = adminMenuRepository.findByAppNameAndLevel(appName, level);
        return ResponseEntity.ok(all);
    }

    @ApiOperation("根据ID提升管理菜单顺序")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功操作")})
    @PutMapping("/api/admin-menus/move-up/{id}")
    @Secured({Authority.ADMIN})
    public void moveUp(@ApiParam(value = "菜单ID", required = true) @PathVariable String id) {
        adminMenuService.moveUp(id);
    }

    @ApiOperation("根据ID降低管理菜单顺序")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功操作")})
    @PutMapping("/api/admin-menus/move-down/{id}")
    @Secured({Authority.ADMIN})
    public void moveDown(@ApiParam(value = "菜单ID", required = true) @PathVariable String id) {
        adminMenuService.moveDown(id);
    }

    @ApiOperation("复制管理菜单")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功复制")})
    @GetMapping("/api/admin-menus/copy")
    @Secured({Authority.ADMIN})
    public void copyMenus(@ApiParam(value = "源应用名称", required = true, defaultValue = "DeepBrainPassport") @RequestParam(value = "sourceAppName") String sourceAppName,
                          @ApiParam(value = "目标应用名称", required = true) @RequestParam(value = "targetAppName") String targetAppName) {
        List<AdminMenu> sourceMenus = adminMenuRepository.findByAppName(sourceAppName);
        sourceMenus.forEach(menu -> {
            menu.setAppName(targetAppName);
            menu.setId(null);
        });
        adminMenuRepository.saveAll(sourceMenus);
    }

    @ApiOperation(value = "导入管理菜单", notes = "输入文件格式：每行先后appName,name,label,level,url,sequence数列，列之间使用tab分隔，行之间使用回车换行")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功导入")})
    @PostMapping(value = "/api/admin-menus/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({Authority.ADMIN})
    public void importData(@ApiParam(value = "文件", required = true) @RequestPart MultipartFile file) throws IOException {
        List<String> lines = IOUtils.readLines(file.getInputStream(), StandardCharsets.UTF_8);
        List<AdminMenu> list = new ArrayList<>();
        for (String line : lines) {
            if (StringUtils.isNotEmpty(line)) {
                String[] lineParts = line.split("\t");
                AdminMenu entity = new AdminMenu(lineParts[0], lineParts[1], lineParts[2],
                        Integer.parseInt(lineParts[3]), lineParts[4], Integer.parseInt(lineParts[5]), null);
                list.add(entity);
            }
        }
        adminMenuRepository.insert(list);
    }
}

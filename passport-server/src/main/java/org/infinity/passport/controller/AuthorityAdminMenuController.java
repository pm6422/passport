package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.infinity.passport.domain.AdminMenu;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.AuthorityAdminMenu;
import org.infinity.passport.dto.AdminAuthorityMenusDTO;
import org.infinity.passport.entity.MenuTreeNode;
import org.infinity.passport.repository.AdminMenuRepository;
import org.infinity.passport.repository.AuthorityAdminMenuRepository;
import org.infinity.passport.service.AdminMenuService;
import org.infinity.passport.service.AuthorityService;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * REST controller for managing the authority admin menu.
 */
@RestController
@Api(tags = "权限管理菜单")
@Slf4j
public class AuthorityAdminMenuController {

    private final AuthorityAdminMenuRepository authorityAdminMenuRepository;
    private final AdminMenuRepository          adminMenuRepository;
    private final AdminMenuService             adminMenuService;
    private final AuthorityService             authorityService;
    private final HttpHeaderCreator            httpHeaderCreator;

    public AuthorityAdminMenuController(AuthorityAdminMenuRepository authorityAdminMenuRepository,
                                        AdminMenuRepository adminMenuRepository,
                                        AdminMenuService adminMenuService,
                                        AuthorityService authorityService,
                                        HttpHeaderCreator httpHeaderCreator) {
        this.authorityAdminMenuRepository = authorityAdminMenuRepository;
        this.adminMenuRepository = adminMenuRepository;
        this.adminMenuService = adminMenuService;
        this.authorityService = authorityService;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("检索当前用户权限关联的菜单")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/authority-admin-menu/authority-menus")
    @Secured({Authority.USER})
    public ResponseEntity<List<MenuTreeNode>> findByAppName(
            @ApiParam(value = "应用名称", required = true) @RequestParam(value = "appName") String appName) {
        List<String> allEnabledAuthorities = authorityService.findAllAuthorityNames(true);
        List<String> userEnabledAuthorities = SecurityUtils.getCurrentUserRoles().parallelStream()
                .filter(userAuthority -> allEnabledAuthorities.contains(userAuthority.getAuthority()))
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        List<MenuTreeNode> results = adminMenuService.getAuthorityMenus(appName, userEnabledAuthorities);
        return ResponseEntity.ok(results);
    }

    @ApiOperation("检索当前用户权限关联的链接")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/authority-admin-menu/authority-links")
    @Secured({Authority.USER})
    public ResponseEntity<List<AdminMenu>> findAuthorityLinks(
            @ApiParam(value = "应用名称", required = true) @RequestParam(value = "appName") String appName) {
        List<String> allEnabledAuthorities = authorityService.findAllAuthorityNames(true);
        List<String> userEnabledAuthorities = SecurityUtils.getCurrentUserRoles().parallelStream()
                .filter(userAuthority -> allEnabledAuthorities.contains(userAuthority.getAuthority()))
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        List<AdminMenu> results = adminMenuService.getAuthorityLinks(appName, userEnabledAuthorities);
        return ResponseEntity.ok(results);
    }

    @ApiOperation("根据权限名称检索菜单信息")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/authority-admin-menu/menu-info")
    @Secured({Authority.ADMIN})
    public ResponseEntity<List<MenuTreeNode>> findMenus(
            @ApiParam(value = "应用名称", required = true) @RequestParam(value = "appName") String appName,
            @ApiParam(value = "权限名称", required = true) @RequestParam(value = "authorityName") String authorityName) {
        List<MenuTreeNode> results = adminMenuService.getAllAuthorityMenus(appName, authorityName);
        return ResponseEntity.ok(results);
    }

    @ApiOperation("更新权限菜单")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功更新"), @ApiResponse(code = SC_BAD_REQUEST, message = "权限信息不存在")})
    @PutMapping("/api/authority-admin-menu/update-authority-menus")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> update(
            @ApiParam(value = "新的权限菜单信息", required = true) @Valid @RequestBody AdminAuthorityMenusDTO dto) {
        log.debug("REST request to update admin authority menus: {}", dto);
        // 删除当前权限下的所有菜单
        Set<String> appAdminMenuIds = adminMenuRepository.findByAppName(dto.getAppName()).stream().map(AdminMenu::getId)
                .collect(Collectors.toSet());
        authorityAdminMenuRepository.deleteByAuthorityNameAndAdminMenuIdIn(dto.getAuthorityName(),
                new ArrayList<>(appAdminMenuIds));

        // 构建权限映射集合
        if (CollectionUtils.isNotEmpty(dto.getAdminMenuIds())) {
            List<AuthorityAdminMenu> adminAuthorityMenus = dto.getAdminMenuIds().stream()
                    .map(adminMenuId -> new AuthorityAdminMenu(dto.getAuthorityName(), adminMenuId))
                    .collect(Collectors.toList());
            // 批量插入
            authorityAdminMenuRepository.saveAll(adminAuthorityMenus);
        }
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("notification.admin.authority.menu.updated")).build();
    }
}

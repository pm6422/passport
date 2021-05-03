package org.infinity.passport.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.infinity.passport.domain.AdminMenu;
import org.infinity.passport.dto.AdminMenuTreeDTO;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.AdminMenuRepository;
import org.infinity.passport.service.AdminMenuService;
import org.infinity.passport.service.AuthorityAdminMenuService;
import org.infinity.passport.service.AuthorityService;
import org.infinity.passport.utils.SecurityUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class AdminMenuServiceImpl implements AdminMenuService {

    private final AdminMenuRepository       adminMenuRepository;
    private final AuthorityService          authorityService;
    private final AuthorityAdminMenuService authorityAdminMenuService;

    public AdminMenuServiceImpl(AdminMenuRepository adminMenuRepository,
                                AuthorityService authorityService,
                                AuthorityAdminMenuService authorityAdminMenuService) {
        this.adminMenuRepository = adminMenuRepository;
        this.authorityService = authorityService;
        this.authorityAdminMenuService = authorityAdminMenuService;
    }

    @Override
    public Page<AdminMenu> find(Pageable pageable, String appName) {
        // Ignore query parameter if it has a null value
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
        Example<AdminMenu> queryExample = Example.of(new AdminMenu(appName), matcher);
        return adminMenuRepository.findAll(queryExample, pageable);
    }

    @Override
    public List<AdminMenu> getUserAuthorityLinks(String appName) {
        Set<String> adminMenuIds = getAdminMenuIds(getEnabledUserAuthorities());
        if (CollectionUtils.isEmpty(adminMenuIds)) {
            return Collections.emptyList();
        }
        // 检索二级及以上级别菜单
        return adminMenuRepository.findByAppNameAndIdInAndParentIdNotNull(appName, adminMenuIds);
    }

    @Override
    public List<AdminMenuTreeDTO> getUserAuthorityMenus(String appName) {
        Set<String> adminMenuIds = getAdminMenuIds(getEnabledUserAuthorities());
        if (CollectionUtils.isEmpty(adminMenuIds)) {
            return Collections.emptyList();
        }
        List<AdminMenu> adminMenus = adminMenuRepository.findByAppNameAndIdIn(appName, adminMenuIds);
        return generateTree(adminMenus);
    }

    @Override
    public List<AdminMenuTreeDTO> getAuthorityMenus(String appName, String authorityName) {
        Set<String> authorityAdminMenuIds = getAdminMenuIds(Collections.singletonList(authorityName));
        if (CollectionUtils.isEmpty(authorityAdminMenuIds)) {
            return Collections.emptyList();
        }
        // 检索所有菜单并将已赋权菜单的checked字段设置为true
        List<AdminMenu> allAdminMenus = adminMenuRepository.findByAppName(appName).stream().peek(menu -> {
            if (authorityAdminMenuIds.contains(menu.getId())) {
                menu.setChecked(true);
            }
        }).collect(Collectors.toList());
        return generateTree(allAdminMenus);
    }

    private List<String> getEnabledUserAuthorities() {
        List<String> allEnabledAuthorities = authorityService.findAllAuthorityNames(true);
        return SecurityUtils.getCurrentUserRoles().parallelStream()
                .map(GrantedAuthority::getAuthority)
                .filter(allEnabledAuthorities::contains).collect(Collectors.toList());
    }

    private Set<String> getAdminMenuIds(List<String> authorityNames) {
        if (CollectionUtils.isEmpty(authorityNames)) {
            return Collections.emptySet();
        }
        return authorityAdminMenuService.findAdminMenuIds(authorityNames);
    }

    private List<AdminMenuTreeDTO> generateTree(List<AdminMenu> menus) {
        // 根节点
        List<AdminMenuTreeDTO> rootMenus = menus.stream()
                .filter(menu -> StringUtils.isEmpty(menu.getParentId()))
                .map(AdminMenu::toTreeDTO)
                .sorted(Comparator.comparing(AdminMenuTreeDTO::getSequence))
                .collect(Collectors.toList());
        rootMenus.forEach(rootMenu -> {
            // 给根节点设置子节点
            rootMenu.setChildren(getChildren(rootMenu.getId(), menus));
        });
        return rootMenus;
    }

    private List<AdminMenuTreeDTO> getChildren(String parentId, List<AdminMenu> menus) {
        // 子菜单
        List<AdminMenuTreeDTO> childMenus = menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(AdminMenu::toTreeDTO)
                .sorted(Comparator.comparing(AdminMenuTreeDTO::getSequence))
                .collect(Collectors.toList());
        // 递归
        for (AdminMenuTreeDTO childMenu : childMenus) {
            childMenu.setChildren(getChildren(childMenu.getId(), menus));
        }
        return childMenus;
    }

    @Override
    public void moveUp(String id) {
        this.adjustSeq(id, -1, this::isNotHead);
    }

    private boolean isNotHead(LinkedList<AdminMenu> linkedList, AdminMenu current) {
        return !linkedList.getFirst().equals(current);
    }

    @Override
    public void moveDown(String id) {
        this.adjustSeq(id, 1, this::isNotTail);
    }

    private boolean isNotTail(LinkedList<AdminMenu> linkedList, AdminMenu current) {
        return !linkedList.getLast().equals(current);
    }

    private void adjustSeq(String id, int moveIndex, BiFunction<LinkedList<AdminMenu>, AdminMenu, Boolean> func) {
        AdminMenu current = adminMenuRepository.findById(id).orElseThrow(() -> new NoDataFoundException(id));
        List<AdminMenu> existingList = adminMenuRepository.findByAppNameAndLevelOrderBySequenceAsc(current.getAppName(), current.getLevel());
        if (CollectionUtils.isNotEmpty(existingList) && existingList.size() == 1) {
            return;
        }
        LinkedList<AdminMenu> linkedList = new LinkedList<>(existingList);
        int currentIndex = linkedList.indexOf(current);

        if (func.apply(linkedList, current)) {
            linkedList.remove(currentIndex);
            linkedList.add(currentIndex + moveIndex, current);
        }

        // Re-order the sequences
        for (int i = 0; i < linkedList.size(); i++) {
            linkedList.get(i).setSequence(i + 1);
        }
        adminMenuRepository.saveAll(linkedList);
    }
}
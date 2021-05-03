package org.infinity.passport.service;

import org.infinity.passport.domain.AdminMenu;
import org.infinity.passport.dto.AdminMenuTreeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminMenuService {

    Page<AdminMenu> find(Pageable pageable, String appName);

    List<AdminMenu> getUserAuthorityLinks(String appName);

    List<AdminMenuTreeDTO> getUserAuthorityMenus(String appName);

    List<AdminMenuTreeDTO> getAuthorityMenus(String appName, String authorityName);

    void moveUp(String id);

    void moveDown(String id);
}
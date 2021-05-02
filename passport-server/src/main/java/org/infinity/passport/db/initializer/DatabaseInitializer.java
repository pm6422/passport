package org.infinity.passport.db.initializer;//package org.infinity.passport.setup;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import org.infinity.passport.domain.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Creates the initial database
 */
@ChangeLog(order = "01")
public class DatabaseInitializer {

    private static final String APP_NAME = "passport-server";

    @ChangeSet(order = "01", author = "Louis", id = "addApps", runAlways = true)
    public void addApps(MongockTemplate mongoTemplate) {
        App app = new App(APP_NAME, true);
        mongoTemplate.save(app);
    }

    @ChangeSet(order = "02", author = "Louis", id = "addAuthorities", runAlways = true)
    public void addAuthorities(MongockTemplate mongoTemplate) {
        mongoTemplate.save(new Authority(Authority.USER, true, true));
        mongoTemplate.save(new Authority(Authority.ADMIN, true, true));
        mongoTemplate.save(new Authority(Authority.DEVELOPER, true, true));
        mongoTemplate.save(new Authority(Authority.ANONYMOUS, true, true));

        mongoTemplate.save(new AppAuthority(APP_NAME, Authority.USER));
        mongoTemplate.save(new AppAuthority(APP_NAME, Authority.ADMIN));
        mongoTemplate.save(new AppAuthority(APP_NAME, Authority.DEVELOPER));
        mongoTemplate.save(new AppAuthority(APP_NAME, Authority.ANONYMOUS));
    }

    @ChangeSet(order = "03", author = "Louis", id = "addUserAndAuthorities", runAlways = true)
    public void addUserAndAuthorities(MongockTemplate mongoTemplate) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // Creates 'user' user and corresponding authorities
        User userRoleUser = new User();
        userRoleUser.setUserName("user");
        userRoleUser.setFirstName("User");
        userRoleUser.setLastName("User");
        userRoleUser.setEmail("user@localhost");
        userRoleUser.setMobileNo("15000899479");
        // Raw password: user
        userRoleUser.setPasswordHash(passwordEncoder.encode("user"));
        userRoleUser.setActivated(true);
        userRoleUser.setActivationKey(null);
        userRoleUser.setResetKey(null);
        userRoleUser.setResetTime(null);
        userRoleUser.setEnabled(true);
        mongoTemplate.save(userRoleUser);

        mongoTemplate.save(new UserAuthority(userRoleUser.getId(), Authority.USER));

        // Creates 'admin' user and corresponding authorities
        User adminRoleUser = new User();
        adminRoleUser.setUserName("admin");
        adminRoleUser.setFirstName("Admin");
        adminRoleUser.setLastName("Admin");
        adminRoleUser.setEmail("admin@localhost");
        adminRoleUser.setMobileNo("15000899477");
        // Raw password: admin
        adminRoleUser.setPasswordHash(passwordEncoder.encode("admin"));
        adminRoleUser.setActivated(true);
        adminRoleUser.setActivationKey(null);
        adminRoleUser.setResetKey(null);
        adminRoleUser.setResetTime(null);
        adminRoleUser.setEnabled(true);
        mongoTemplate.save(adminRoleUser);

        mongoTemplate.save(new UserAuthority(adminRoleUser.getId(), Authority.USER));
        mongoTemplate.save(new UserAuthority(adminRoleUser.getId(), Authority.ADMIN));

        // Creates 'system' user and corresponding authorities
        User adminRoleSystemUser = new User();
        adminRoleSystemUser.setUserName("system");
        adminRoleSystemUser.setFirstName("System");
        adminRoleSystemUser.setLastName("System");
        adminRoleSystemUser.setEmail("system@localhost");
        adminRoleSystemUser.setMobileNo("15000899422");
        // Raw password: system
        adminRoleSystemUser.setPasswordHash(passwordEncoder.encode("system"));
        adminRoleSystemUser.setActivated(true);
        adminRoleSystemUser.setActivationKey(null);
        adminRoleSystemUser.setResetKey(null);
        adminRoleSystemUser.setResetTime(null);
        adminRoleSystemUser.setEnabled(true);
        mongoTemplate.save(adminRoleSystemUser);

        mongoTemplate.save(new UserAuthority(adminRoleSystemUser.getId(), Authority.USER));
        mongoTemplate.save(new UserAuthority(adminRoleSystemUser.getId(), Authority.ADMIN));

        // Creates 'louis' user and corresponding authorities
        User developerRoleUser = new User();
        developerRoleUser.setUserName("louis");
        developerRoleUser.setFirstName("Louis");
        developerRoleUser.setLastName("Liu");
        developerRoleUser.setEmail("louis@localhost");
        developerRoleUser.setMobileNo("15000899488");
        // Raw password: louis
        developerRoleUser.setPasswordHash(passwordEncoder.encode("louis"));
        developerRoleUser.setActivated(true);
        developerRoleUser.setActivationKey(null);
        developerRoleUser.setResetKey(null);
        developerRoleUser.setResetTime(null);
        developerRoleUser.setEnabled(true);
        mongoTemplate.save(developerRoleUser);

        mongoTemplate.save(new UserAuthority(developerRoleUser.getId(), Authority.USER));
        mongoTemplate.save(new UserAuthority(developerRoleUser.getId(), Authority.ADMIN));
        mongoTemplate.save(new UserAuthority(developerRoleUser.getId(), Authority.DEVELOPER));
    }

    @ChangeSet(order = "04", author = "Louis", id = "addAuthorityAdminMenu", runAlways = true)
    public void addAuthorityAdminMenu(MongockTemplate mongoTemplate) {

        AdminMenu userAuthority = new AdminMenu(APP_NAME, "user-authority", "用户权限", 1, "user-authority", 100, null);
        mongoTemplate.save(userAuthority);

        AdminMenu authorityList = new AdminMenu(APP_NAME, "authority-list", "权限", 2, "user-authority.authority-list",
                101, userAuthority.getId());
        mongoTemplate.save(authorityList);

        AdminMenu userList = new AdminMenu(APP_NAME, "user-list", "用户", 2, "user-authority.user-list", 102,
                userAuthority.getId());
        mongoTemplate.save(userList);

        AdminMenu app = new AdminMenu(APP_NAME, "app", "应用系统", 1, "app", 200, null);
        mongoTemplate.save(app);

        AdminMenu appList = new AdminMenu(APP_NAME, "app-list", "应用", 2, "app.app-list", 201, app.getId());
        mongoTemplate.save(appList);

        AdminMenu adminMenuAuthority = new AdminMenu(APP_NAME, "admin-menu-authority", "菜单权限", 1,
                "admin-menu-authority", 300, null);
        mongoTemplate.save(adminMenuAuthority);

        AdminMenu adminMenuList = new AdminMenu(APP_NAME, "admin-menu-list", "管理菜单", 2,
                "admin-menu-authority.admin-menu-list", 301, adminMenuAuthority.getId());
        mongoTemplate.save(adminMenuList);

        AdminMenu authorityAdminMenu = new AdminMenu(APP_NAME, "authority-admin-menu", "权限管理菜单", 2,
                "admin-menu-authority.authority-admin-menu", 302, adminMenuAuthority.getId());
        mongoTemplate.save(authorityAdminMenu);

        AdminMenu security = new AdminMenu(APP_NAME, "security", "安全信息", 1, "security", 400, null);
        mongoTemplate.save(security);

        AdminMenu oAuth2ClientDetails = new AdminMenu(APP_NAME, "oauth2-client-list", "单点登录客户端", 2,
                "security.oauth2-client-list", 401, security.getId());
        mongoTemplate.save(oAuth2ClientDetails);

        AdminMenu oAuth2AccessTokenDetails = new AdminMenu(APP_NAME, "oauth2-access-token-list", "访问令牌", 2,
                "security.oauth2-access-token-list", 402, security.getId());
        mongoTemplate.save(oAuth2AccessTokenDetails);

        AdminMenu oAuth2RefreshTokenDetails = new AdminMenu(APP_NAME, "oauth2-refresh-token-list", "刷新令牌", 2,
                "security.oauth2-refresh-token-list", 403, security.getId());
        mongoTemplate.save(oAuth2RefreshTokenDetails);

        AdminMenu oAuth2ApprovalDetails = new AdminMenu(APP_NAME, "oauth2-approval-list", "登录授权", 2,
                "security.oauth2-approval-list", 404, security.getId());
        mongoTemplate.save(oAuth2ApprovalDetails);

        //AuthorityAdminMenu
        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, userAuthority.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, authorityList.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, userList.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, app.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, appList.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, adminMenuAuthority.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, adminMenuList.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, authorityAdminMenu.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, security.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, oAuth2ClientDetails.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, oAuth2AccessTokenDetails.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, oAuth2RefreshTokenDetails.getId()));

        mongoTemplate.save(AuthorityAdminMenu.of(Authority.ADMIN, oAuth2ApprovalDetails.getId()));
    }

    @ChangeSet(order = "05", author = "Louis", id = "addOAuth2ClientDetails", runAlways = true)
    public void addOAuth2ClientDetails(MongockTemplate mongoTemplate) {
        MongoOAuth2ClientDetails oAuth2ClientDetails = new MongoOAuth2ClientDetails();
        oAuth2ClientDetails.setClientId(MongoOAuth2ClientDetails.INTERNAL_CLIENT_ID);
        oAuth2ClientDetails.setRawClientSecret(MongoOAuth2ClientDetails.INTERNAL_RAW_CLIENT_SECRET);
        oAuth2ClientDetails.setClientSecret(
                new BCryptPasswordEncoder().encode(MongoOAuth2ClientDetails.INTERNAL_RAW_CLIENT_SECRET));
        oAuth2ClientDetails.setScope(Arrays.asList("read", "write"));
        // It will auto approve if autoApproveScopes exactly match the scopes.
        oAuth2ClientDetails.setAutoApproveScopes(Collections.singletonList("read"));
        oAuth2ClientDetails.setAuthorizedGrantTypes(
                Arrays.asList("password", "authorization_code", "refresh_token", "client_credentials"));
        // Note: localhost and 127.0.0.1 must be save twice.
        oAuth2ClientDetails.setRegisteredRedirectUri(
                new HashSet<>(Arrays.asList("http://127.0.0.1:8010/login", "http://localhost:8010/login")));
        oAuth2ClientDetails.setAccessTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(7));
        oAuth2ClientDetails.setRefreshTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(7));
        // 这个authority还不知道其作用
        oAuth2ClientDetails.setAuthorities(Arrays.asList(new SimpleGrantedAuthority(Authority.DEVELOPER),
                new SimpleGrantedAuthority(Authority.ADMIN), new SimpleGrantedAuthority(Authority.USER),
                new SimpleGrantedAuthority(Authority.ANONYMOUS)));
        mongoTemplate.save(oAuth2ClientDetails);
    }
}

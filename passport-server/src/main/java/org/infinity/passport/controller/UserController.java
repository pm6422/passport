package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.User;
import org.infinity.passport.domain.UserAuthority;
import org.infinity.passport.domain.UserProfilePhoto;
import org.infinity.passport.dto.ManagedUserDTO;
import org.infinity.passport.dto.UserNameAndPasswordDTO;
import org.infinity.passport.event.LogoutEvent;
import org.infinity.passport.exception.NoAuthorityException;
import org.infinity.passport.repository.UserAuthorityRepository;
import org.infinity.passport.repository.UserProfilePhotoRepository;
import org.infinity.passport.service.MailService;
import org.infinity.passport.service.UserService;
import org.infinity.passport.utils.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.*;
import static org.infinity.passport.domain.User.DEFAULT_PASSWORD;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;
import static org.infinity.passport.utils.NetworkUtils.getRequestUrl;

/**
 * REST controller for managing users.
 */
@RestController
@Api(tags = "用户管理")
@Slf4j
public class UserController {

    private final UserProfilePhotoRepository userProfilePhotoRepository;
    private final UserAuthorityRepository    userAuthorityRepository;
    private final UserService                userService;
    private final MailService                mailService;
    private final ApplicationEventPublisher  applicationEventPublisher;
    private final HttpHeaderCreator          httpHeaderCreator;

    public UserController(UserProfilePhotoRepository userProfilePhotoRepository,
                          UserAuthorityRepository userAuthorityRepository,
                          UserService userService,
                          MailService mailService,
                          ApplicationEventPublisher applicationEventPublisher,
                          HttpHeaderCreator httpHeaderCreator) {
        this.userProfilePhotoRepository = userProfilePhotoRepository;
        this.userAuthorityRepository = userAuthorityRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation(value = "创建新用户并发送激活邮件")
    @ApiResponses(value = {@ApiResponse(code = SC_CREATED, message = "成功创建"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "账号已注册")})
    @PostMapping("/api/users")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> create(@ApiParam(value = "用户", required = true) @Valid @RequestBody User domain,
                                       HttpServletRequest request) {
        log.debug("REST request to create user: {}", domain);
        User newUser = userService.insert(domain, DEFAULT_PASSWORD);
        mailService.sendCreationEmail(newUser, getRequestUrl(request));
        HttpHeaders headers = httpHeaderCreator.createSuccessHeader("NM2011", DEFAULT_PASSWORD);
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).build();
    }

    @ApiOperation("分页检索用户列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/users")
    @Secured({Authority.ADMIN})
    public ResponseEntity<List<User>> find(Pageable pageable,
                                           @ApiParam(value = "检索条件") @RequestParam(value = "login", required = false) String login) {
        Page<User> users = userService.findByLogin(pageable, login);
        return ResponseEntity.ok().headers(generatePageHeaders(users)).body(users.getContent());
    }

    @ApiOperation("根据用户名检索用户")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "用户不存在或账号无权限")})
    @GetMapping("/api/users/{userName:[a-zA-Z0-9-]+}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<ManagedUserDTO> findByName(@ApiParam(value = "用户名", required = true) @PathVariable String userName) {
        User domain = userService.findOneByUserName(userName);
        List<UserAuthority> userAuthorities = Optional.ofNullable(userAuthorityRepository.findByUserId(domain.getId()))
                .orElseThrow(() -> new NoAuthorityException(userName));
        Set<String> authorities = userAuthorities.stream().map(UserAuthority::getAuthorityName).collect(Collectors.toSet());
        return ResponseEntity.ok(new ManagedUserDTO(domain, authorities));
    }

    @ApiOperation("更新用户")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功更新"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "用户不存在"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "账号已注册"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "用户不存在"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "已激活用户无法变成未激活状态")})
    @PutMapping("/api/users")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> update(@ApiParam(value = "新的用户", required = true) @Valid @RequestBody User domain) {
        log.debug("REST request to update user: {}", domain);
        userService.update(domain);
        if (domain.getUserName().equals(SecurityUtils.getCurrentUserName())) {
            // Logout if current user were changed
            applicationEventPublisher.publishEvent(new LogoutEvent(this));
        }
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1002", domain.getUserName())).build();
    }

    @ApiOperation(value = "根据用户名删除用户", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "用户不存在")})
    @DeleteMapping("/api/users/{userName:[a-zA-Z0-9-]+}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> delete(@ApiParam(value = "用户名", required = true) @PathVariable String userName) {
        log.debug("REST request to delete user: {}", userName);
        userService.deleteByUserName(userName);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("SM1003", userName)).build();
    }

    @ApiOperation("根据用户名重置密码")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功重置"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "用户不存在或账号无权限")})
    @PutMapping("/api/users/{userName:[a-zA-Z0-9-]+}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<String> resetPassword(@ApiParam(value = "用户名", required = true) @PathVariable String userName) {
        log.debug("REST reset the password of user: {}", userName);
        userService.changePassword(UserNameAndPasswordDTO.builder().userName(userName).newPassword(DEFAULT_PASSWORD).build());
        HttpHeaders headers = httpHeaderCreator.createSuccessHeader("NM2012", DEFAULT_PASSWORD);
        return ResponseEntity.ok().headers(headers).body(DEFAULT_PASSWORD);
    }

    public static final String GET_PROFILE_PHOTO_URL = "/api/users/profile-photo/";

    @ApiOperation("检索用户头像")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping(GET_PROFILE_PHOTO_URL + "{userName:[a-zA-Z0-9-]+}")
    @Secured({Authority.USER})
    public ResponseEntity<byte[]> getProfilePhoto(@ApiParam(value = "用户名", required = true) @PathVariable String userName) {
        User user = userService.findOneByUserName(userName);
        Optional<UserProfilePhoto> userProfilePhoto = userProfilePhotoRepository.findByUserId(user.getId());
        return userProfilePhoto.map(photo -> ResponseEntity.ok(photo.getProfilePhoto().getData())).orElse(null);
    }
}

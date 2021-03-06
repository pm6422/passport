package org.infinity.passport.service.impl;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.infinity.passport.component.MessageCreator;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.User;
import org.infinity.passport.domain.UserAuthority;
import org.infinity.passport.dto.UserNameAndPasswordDTO;
import org.infinity.passport.exception.DuplicationException;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.UserAuthorityRepository;
import org.infinity.passport.repository.UserRepository;
import org.infinity.passport.service.UserService;
import org.infinity.passport.utils.RandomUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.thymeleaf.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository          userRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final PasswordEncoder         passwordEncoder;
    private final MessageCreator          messageCreator;

    public UserServiceImpl(UserRepository userRepository,
                           UserAuthorityRepository userAuthorityRepository,
                           PasswordEncoder passwordEncoder,
                           MessageCreator messageCreator) {
        this.userRepository = userRepository;
        this.userAuthorityRepository = userAuthorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageCreator = messageCreator;
    }

    // private void removeUserToken(User user) {
    // String clientId =
    // applicationProperties.getSecurity().getAuthentication().getOauth().getClientId();
    // tokenStore.findTokensByClientIdAndUserName(clientId,
    // user.getUserName()).stream()
    // .forEach(token -> tokenStore.removeAccessToken(token));
    // }

    @Override
    public void changePassword(UserNameAndPasswordDTO dto) {
        userRepository.findOneByUserName(dto.getUserName()).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);
            log.debug("Changed password for user: {}", user);
        });
    }

    @Override
    public User insert(User user, String rawPassword) {
        Optional<User> existingUser = userRepository.findOneByUserName(user.getUserName().toLowerCase(Locale.ENGLISH));
        if (existingUser.isPresent()) {
            throw new DuplicationException(ImmutableMap.of("userName", user.getUserName()));
        }
        if (findOneByEmail(user.getEmail()).isPresent()) {
            throw new DuplicationException(ImmutableMap.of("email", user.getEmail()));
        }
        if (findOneByMobileNo(user.getMobileNo()).isPresent()) {
            throw new DuplicationException(ImmutableMap.of("mobileNo", user.getMobileNo()));
        }

        user.setUserName(user.getUserName().toLowerCase());
        user.setEmail(user.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setActivationKey(RandomUtils.generateActivationKey());
        user.setActivated(false);
        user.setResetKey(RandomUtils.generateResetKey());
        user.setResetTime(Instant.now());
        user.setEnabled(true);
        userRepository.save(user);

        if (CollectionUtils.isNotEmpty(user.getAuthorities())) {
            if (!user.getAuthorities().contains(Authority.USER)) {
                throw new IllegalArgumentException("[ROLE_USER] authority must be specified!");
            }
            user.getAuthorities().forEach(authorityName -> userAuthorityRepository.insert(new UserAuthority(user.getId(), authorityName)));
        }

        log.debug("Created information for user: {}", user);
        return user;
    }

    @Override
    public void update(User user) {
        // 因为其他表的创建者和更新者使用的是userName，所以不能更新
        userRepository.findById(user.getId()).map(u -> {
            Optional<User> existingUser = findOneByEmail(user.getEmail());
            if (existingUser.isPresent() && (!existingUser.get().getId().equalsIgnoreCase(user.getId()))) {
                throw new DuplicationException(ImmutableMap.of("email", user.getEmail()));
            }
            existingUser = findOneByMobileNo(user.getMobileNo());
            if (existingUser.isPresent() && (!existingUser.get().getId().equalsIgnoreCase(user.getId()))) {
                throw new DuplicationException(ImmutableMap.of("mobileNo", user.getMobileNo()));
            }
            if (existingUser.isPresent() && !Boolean.TRUE.equals(user.getActivated()) && Boolean.TRUE.equals(existingUser.get().getActivated())) {
                throw new IllegalArgumentException(messageCreator.getMessage("EP5021"));
            }

            u.setFirstName(user.getFirstName());
            u.setLastName(user.getLastName());
            u.setEmail(user.getEmail().toLowerCase());
            u.setMobileNo(user.getMobileNo());
            u.setEnabled(user.getEnabled());
            u.setRemarks(user.getRemarks());
            userRepository.save(u);
            log.debug("Updated user: {}", user);

            if (CollectionUtils.isNotEmpty(user.getAuthorities())) {
                if (!user.getAuthorities().contains(Authority.USER)) {
                    throw new IllegalArgumentException("[ROLE_USER] authority must be specified!");
                }
                userAuthorityRepository.deleteByUserId(user.getId());
                user.getAuthorities().forEach(authorityName -> userAuthorityRepository.insert(new UserAuthority(user.getId(), authorityName)));
                log.debug("Updated user authorities");
            }
            return u;
        }).orElseThrow(() -> new NoDataFoundException(user.getId()));
    }

    @Override
    public User findOneByUserName(String userName) {
        Assert.hasText(userName, "it must not be null, empty, or blank");
        return userRepository.findOneByUserName(userName.toLowerCase(Locale.ENGLISH))
                .orElseThrow(() -> new NoDataFoundException(userName));
    }

    @Override
    public Optional<User> findOneByEmail(String email) {
        Assert.hasText(email, "it must not be null, empty, or blank");
        return userRepository.findOneByEmail(email.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Optional<User> findOneByMobileNo(String mobileNo) {
        Assert.hasText(mobileNo, "it must not be null, empty, or blank");
        return userRepository.findOneByMobileNo(mobileNo.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Optional<User> findOneByLogin(String login) {
        Assert.hasText(login, "it must not be null, empty, or blank");
        return userRepository.findOneByUserNameOrEmailOrMobileNo(login.toLowerCase(Locale.ENGLISH),
                login.toLowerCase(Locale.ENGLISH), login.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Page<User> findByLogin(Pageable pageable, String login) {
        if (StringUtils.isEmpty(login)) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByUserNameOrEmailOrMobileNo(pageable, login, login, login);
    }

    @Override
    public Optional<User> activateRegistration(String activationKey) {
        return userRepository.findOneByActivationKey(activationKey).map(user -> {
            user.setActivated(true);
            user.setActivationKey(null);
            userRepository.save(user);
            log.debug("Activated user: {}", user);
            return user;
        });
    }

    @Override
    public User requestPasswordReset(String email, String resetKey) {
        User user = userRepository.findOneByEmailAndActivatedIsTrue(email)
                .orElseThrow(() -> new NoDataFoundException(messageCreator.getMessage("email")));
        user.setResetKey(RandomUtils.generateResetKey());
        user.setResetTime(Instant.now());
        userRepository.save(user);
        log.debug("Requested reset user password for reset key {}", resetKey);
        return user;
    }

    @Override
    public User completePasswordReset(String newRawPassword, String resetKey) {
        User user = userRepository.findOneByResetKey(resetKey)
                .filter(u -> u.getResetTime().isAfter(Instant.now().minusSeconds(TimeUnit.DAYS.toSeconds(1))))
                .orElseThrow(() -> new NoDataFoundException(messageCreator.getMessage("resetKey")));
        user.setPasswordHash(passwordEncoder.encode(newRawPassword));
        user.setResetKey(null);
        user.setResetTime(null);
        userRepository.save(user);
        log.debug("Reset user password for reset key {}", resetKey);
        return user;
    }

    @Override
    public void deleteByUserName(String userName) {
        User user = findOneByUserName(userName);
        userRepository.deleteById(user.getId());
        userAuthorityRepository.deleteByUserId(user.getId());
    }
}
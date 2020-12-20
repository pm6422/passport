package org.infinity.passport.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.component.HttpHeaderCreator;
import org.infinity.passport.domain.App;
import org.infinity.passport.domain.AppAuthority;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.dto.AppDTO;
import org.infinity.passport.exception.NoDataFoundException;
import org.infinity.passport.repository.AppAuthorityRepository;
import org.infinity.passport.repository.AppRepository;
import org.infinity.passport.service.AppService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.*;
import static org.infinity.passport.utils.HttpHeaderUtils.generatePageHeaders;

/**
 * REST controller for managing apps.
 */
@RestController
@Api(tags = "应用管理")
@Slf4j
public class AppController {

    private final AppRepository          appRepository;
    private final AppAuthorityRepository appAuthorityRepository;
    private final AppService             appService;
    private final HttpHeaderCreator      httpHeaderCreator;

    public AppController(AppRepository appRepository,
                         AppAuthorityRepository appAuthorityRepository,
                         AppService appService,
                         HttpHeaderCreator httpHeaderCreator) {
        this.appRepository = appRepository;
        this.appAuthorityRepository = appAuthorityRepository;
        this.appService = appService;
        this.httpHeaderCreator = httpHeaderCreator;
    }

    @ApiOperation("创建应用")
    @ApiResponses(value = {@ApiResponse(code = SC_CREATED, message = "成功创建")})
    @PostMapping("/api/app/apps")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> create(@ApiParam(value = "应用", required = true) @Valid @RequestBody AppDTO dto) {
        log.debug("REST request to create app: {}", dto);
        appService.insert(dto.getName(), dto.getEnabled(), dto.getAuthorities());
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(httpHeaderCreator.createSuccessHeader("SM1001", dto.getName())).build();
    }

    @ApiOperation("分页检索应用列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索")})
    @GetMapping("/api/app/apps")
    @Secured({Authority.ADMIN})
    public ResponseEntity<List<AppDTO>> find(Pageable pageable) {
        Page<App> apps = appRepository.findAll(pageable);
        List<AppDTO> DTOs = apps.getContent().stream().map(App::toDTO).collect(Collectors.toList());
        HttpHeaders headers = generatePageHeaders(apps);
        return ResponseEntity.ok().headers(headers).body(DTOs);
    }

    @ApiOperation("根据名称检索应用")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功检索"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "应用不存在")})
    @GetMapping("/api/app/apps/{name}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<AppDTO> findById(@ApiParam(value = "应用名称", required = true) @PathVariable String name) {
        App app = appRepository.findById(name).orElseThrow(() -> new NoDataFoundException(name));
        List<AppAuthority> appAuthorities = appAuthorityRepository.findByAppName(name);
        Set<String> authorities = appAuthorities.stream().map(AppAuthority::getAuthorityName).collect(Collectors.toSet());
        return ResponseEntity.ok(new AppDTO(name, app.getEnabled(), authorities));
    }

    @ApiOperation("更新应用")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功更新"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "应用不存在")})
    @PutMapping("/api/app/apps")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> update(@ApiParam(value = "新的应用", required = true) @Valid @RequestBody AppDTO dto) {
        log.debug("REST request to update app: {}", dto);
        appRepository.findById(dto.getName()).orElseThrow(() -> new NoDataFoundException(dto.getName()));
        appService.update(dto.getName(), dto.getEnabled(), dto.getAuthorities());
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("SM1002", dto.getName())).build();
    }

    @ApiOperation(value = "根据名称删除应用", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "应用不存在")})
    @DeleteMapping("/api/app/apps/{name}")
    @Secured({Authority.ADMIN})
    public ResponseEntity<Void> delete(@ApiParam(value = "应用名称", required = true) @PathVariable String name) {
        log.debug("REST request to delete app: {}", name);
        appRepository.findById(name).orElseThrow(() -> new NoDataFoundException(name));
        appRepository.deleteById(name);
        appAuthorityRepository.deleteByAppName(name);
        return ResponseEntity.ok()
                .headers(httpHeaderCreator.createSuccessHeader("SM1003", name)).build();
    }
}

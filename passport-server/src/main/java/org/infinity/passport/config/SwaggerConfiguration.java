package org.infinity.passport.config;

import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.utils.TraceIdUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Springfox Swagger configuration.
 * <p>
 * Warning! When having a lot of REST endpoints, Springfox can become a
 * performance issue. In that case, you can use a specific Spring profile for
 * this class, so that only front-end developers have access to the Swagger
 * view.
 * <p>
 * https://blog.csdn.net/qq_21948951/article/details/90443723
 */
@Configuration
@EnableSwagger2
@ConditionalOnProperty(prefix = "application.swagger", value = "enabled", havingValue = "true")
@Slf4j
public class SwaggerConfiguration {
    private static final String                DEFAULT_API_INCLUDE_PATTERN      = "/api/.*";
    private static final String                DEFAULT_OPEN_API_INCLUDE_PATTERN = "/open-api/.*";
    private static final String                SECURITY_TOKEN_NAME              = "Authorization";
    private final        ApplicationProperties applicationProperties;

    public SwaggerConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public Docket apiDocket() {
        return createDocket("api-group", DEFAULT_API_INCLUDE_PATTERN);
    }

    @Bean
    public Docket openApiDocket() {
        return createDocket("open-api-group", DEFAULT_OPEN_API_INCLUDE_PATTERN);
    }

    private Docket createDocket(String groupName, String path) {
        log.debug("Building Swagger API docket with group [{}]", groupName);
        Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName(groupName).apiInfo(apiInfo())
                .forCodeGeneration(true);
        if (System.getProperty("specified.uri.scheme.host") != null
                && "true".equals(System.getProperty("specified.uri.scheme.host"))) {
            docket.host(applicationProperties.getSwagger().getHost());
        }

        docket.genericModelSubstitutes(ResponseEntity.class)
                .ignoredParameterTypes(java.sql.Date.class)
                .directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
                .directModelSubstitute(java.time.LocalDateTime.class, Date.class)
                .select()
                .paths(regex(path))
                .build()
//                .securitySchemes(securitySchemes())
//                .securityContexts(securityContexts())
                .globalOperationParameters(createGlobalParameters());
        log.debug("Built Swagger API docket with group [{}]", groupName);
        return docket;
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact(applicationProperties.getSwagger().getContactName(),
                null,
                applicationProperties.getSwagger().getContactEmail());

        return new ApiInfoBuilder()
                .title(applicationProperties.getSwagger().getApi().getTitle())
                .description(applicationProperties.getSwagger().getApi().getDescription())
                .version(applicationProperties.getSwagger().getVersion())
                .contact(contact)
                .build();
    }

    /**
     * Add global parameters for all the API
     *
     * @return global operation parameters
     */
    private List<Parameter> createGlobalParameters() {
        ParameterBuilder requesterParamBuilder = new ParameterBuilder();
        requesterParamBuilder.name(TraceIdUtils.TRACE_ID)
                .description("Trace ID(全局参数)")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();

//        ParameterBuilder tokenParameterBuilder = new ParameterBuilder();
//        tokenParameterBuilder.name("Authorization")
//                .defaultValue("Bearer ")
//                .description("访问令牌")
//                .modelRef(new ModelRef("string"))
//                .parameterType("header")
//                .required(false)
//                .build();

        return Collections.singletonList(requesterParamBuilder.build());
    }

    private List<ApiKey> securitySchemes() {
        List<ApiKey> apiKeyList = new ArrayList<>();
        apiKeyList.add(new ApiKey(SECURITY_TOKEN_NAME, SECURITY_TOKEN_NAME, "header"));
        return apiKeyList;
    }

    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        // 输入令牌对于除上文所述的包含auth的接口结果都会带上token。
        //通过PathSelectors.regex("^(?!auth).*$")，排除包含"auth"的接口不需要使用securitySchemes
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
//                        .forPaths(PathSelectors.regex("^(?!api).*$"))
                        .build());
        return securityContexts;
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference(SECURITY_TOKEN_NAME, authorizationScopes));
        return securityReferences;
    }
}

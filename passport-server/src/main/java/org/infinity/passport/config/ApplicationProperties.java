package org.infinity.passport.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Properties specific to Application.
 *
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Validated
@Getter
public class ApplicationProperties {
    private final Http               http               = new Http();
    private final Swagger            swagger            = new Swagger();
    private final AopLogging         aopLogging         = new AopLogging();
    private final ElapsedTimeLogging elapsedTimeLogging = new ElapsedTimeLogging();
    private final Redis              redis              = new Redis();
    private final Ribbon             ribbon             = new Ribbon();

    @Data
    public static class Http {
        private final Cache cache = new Cache();

        @Data
        public static class Cache {
            /**
             * Expired days
             */
            private Long expiredAfter = 31L;
        }
    }

    @Data
    public static class Swagger {
        private       boolean enabled;
        private       String  version;
        private       String  contactName;
        private       String  contactEmail;
        private       String  host;
        private final Api     api     = new Api();
        private final OpenApi openApi = new OpenApi();

        @Data
        public static class Api {
            private String title;
            private String description;
        }

        @Data
        public static class OpenApi {
            private String title;
            private String description;
        }
    }

    @Data
    public static class AopLogging {
        private boolean      enabled;
        private boolean      methodWhitelistMode;
        private List<String> methodWhitelist;
    }

    @Data
    public static class ElapsedTimeLogging {
        private boolean enabled;
        private int     slowExecutionThreshold;
    }

    @Data
    public static class Dubbo {
        private String adminUrl;
        private String monitorUrl;
    }

    @Data
    public static class Redis {
        private String adminUrl;
    }

    @Data
    public static class Ribbon {
        private String[] displayOnActiveProfiles;
    }
}

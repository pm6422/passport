package org.infinity.passport.config;

import com.github.cloudyrock.spring.v5.EnableMongock;
import io.changock.runner.core.ChangockBase;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.config.oauth2.OAuth2AccessTokenReadConverter;
import org.infinity.passport.config.oauth2.OAuth2AuthenticationReadConverter;
import org.infinity.passport.config.oauth2.OAuth2GrantedAuthorityTokenReadConverter;
import org.infinity.passport.config.oauth2.OAuth2RefreshTokenReadConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.BasicMongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Using @EnableMongock with minimal configuration only requires changeLog package to scan
 * in property file
 */
@EnableMongoAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableMongock
@Configuration
@Slf4j
public class MongoConfiguration {

    private final MongoMappingContext       mongoMappingContext;
    private final MongoDatabaseFactory      mongoDatabaseFactory;
    private final LocalValidatorFactoryBean validator;

    /**
     * Use @Lazy to fix dependencies problems
     *
     * @param mongoMappingContext  mongo mapping context
     * @param mongoDatabaseFactory mongo db factory
     * @param validator            bean validator
     */
    public MongoConfiguration(@Lazy MongoMappingContext mongoMappingContext,
                              MongoDatabaseFactory mongoDatabaseFactory,
                              LocalValidatorFactoryBean validator) {
        this.mongoMappingContext = mongoMappingContext;
        this.mongoDatabaseFactory = mongoDatabaseFactory;
        this.validator = validator;
    }

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
        return new ValidatingMongoEventListener(validator);
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new OAuth2AccessTokenReadConverter());
        converters.add(new OAuth2RefreshTokenReadConverter());
        converters.add(new OAuth2AuthenticationReadConverter());
        converters.add(new OAuth2GrantedAuthorityTokenReadConverter());
        return new MongoCustomConversions(converters);
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter() {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        converter.setCustomConversions(customConversions());
        // remove _class field
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDatabaseFactory, mappingMongoConverter());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndicesAfterStartup() {
        if (mongoMappingContext != null) {
            for (BasicMongoPersistentEntity<?> persistentEntity : mongoMappingContext.getPersistentEntities()) {
                Class<?> clazz = persistentEntity.getType();
                if (clazz.isAnnotationPresent(Document.class)) {
                    MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
                    IndexOperations indexOps = mongoTemplate().indexOps(clazz);
                    resolver.resolveIndexFor(clazz).forEach(indexOps::ensureIndex);
                }
            }
        }
    }
}

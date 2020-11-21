package org.infinity.passport.config;

import com.github.mongobee.Mongobee;
import com.mongodb.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.infinity.passport.config.oauth2.OAuth2AccessTokenReadConverter;
import org.infinity.passport.config.oauth2.OAuth2AuthenticationReadConverter;
import org.infinity.passport.config.oauth2.OAuth2GrantedAuthorityTokenReadConverter;
import org.infinity.passport.config.oauth2.OAuth2RefreshTokenReadConverter;
import org.infinity.passport.setup.DatabaseInitialSetup;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDbFactory;
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

@Configuration
@EnableMongoAuditing(auditorAwareRef = "springSecurityAuditorAware")
@Slf4j
public class MongoConfiguration {

    private final MongoMappingContext mongoMappingContext;
    private final MongoDbFactory      mongoDbFactory;

    // Use @Lazy to fix dependencies problems
    public MongoConfiguration(@Lazy MongoMappingContext mongoMappingContext, MongoDbFactory mongoDbFactory) {
        this.mongoMappingContext = mongoMappingContext;
        this.mongoDbFactory = mongoDbFactory;
    }

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
        return new ValidatingMongoEventListener(validator());
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
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
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        converter.setCustomConversions(customConversions());
        // remove _class field
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter());
    }

    @Bean
    public Mongobee mongobee(MongoClient mongoClient, MongoTemplate mongoTemplate) {
        log.debug("Configuring Mongobee");
        Mongobee mongobee = new Mongobee(mongoClient);
        // For embedded mongo
        mongobee.setDbName(mongoClient.listDatabaseNames().first());
        mongobee.setMongoTemplate(mongoTemplate);
        mongobee.setChangeLogsScanPackage(DatabaseInitialSetup.class.getPackage().getName());
        mongobee.setEnabled(true);
        log.debug("Configured Mongobee");
        return mongobee;
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

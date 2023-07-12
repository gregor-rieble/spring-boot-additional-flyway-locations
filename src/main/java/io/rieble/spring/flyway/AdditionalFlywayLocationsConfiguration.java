package io.rieble.spring.flyway;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.FlywayConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@ConditionalOnBean({FlywayAutoConfiguration.class, FlywayConfiguration.class})
public class AdditionalFlywayLocationsConfiguration {
    @Bean
    public static BeanFactoryPostProcessor additionalFlywayLocationsInjector(
            FlywayProperties flywayProperties,
            ConfigurableEnvironment environment
    ) {
        return new AdditionalFlywayLocationsInjector(flywayProperties, environment);
    }
}

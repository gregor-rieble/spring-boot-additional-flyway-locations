package de.gcoding.spring.flyway;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ImportAutoConfiguration(AdditionalFlywayLocationsConfiguration.class)
public @interface AdditionalFlywayLocations {
    String[] value() default {};
}

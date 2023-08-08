package de.gcoding.spring.flyway;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used on spring {@link Configuration} classes. When the application is started, it will
 * add the provided locations to the search path of flyway. For example, if the default flyway locations are configured
 * like so:
 * <pre>spring.flyway.locations=classpath:db/migration</pre>
 * and there is an annotated configuration class:
 * <pre>
 * &#064;Configuration
 * &#064;AdditionalFlywayLocations("classpath:db/additional-location")
 * class MyFlywayConfiguration {
 * }
 * </pre>
 * This would result in flyway to search for scripts in both locations. In other words the application would behave
 * as if the flyway locations were configured as follows:
 * <pre>spring.flyway.locations=classpath:db/migration,classpath:db/additional-location</pre>
 * Locations specified through this annotation will be added at the end of the existing list of locations
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ImportAutoConfiguration(AdditionalFlywayLocationsConfiguration.class)
public @interface AdditionalFlywayLocations {
    /**
     * Returns additional flyway migration script locations that should be searched by flyway on application start
     *
     * @return The additional flyway migration script locations
     */
    String[] value() default {};
}

package io.rieble.spring.flyway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdditionalFlywayLocationsInjectorTest {
    final static List<String> DEFAULT_LOCATIONS = List.of(
            "classpath:/db/migration/generic",
            "classpath:/db/migration/{vendor}"
    );
    final static String ADDITIONAL_LOCATION_1 = "classpath:/additional1";
    final static String ADDITIONAL_LOCATION_2 = "classpath:/additional2/{vendor}";
    final static String ADDITIONAL_LOCATION_3 = "classpath:/additional3";
    final static List<String> EXPECTED_LOCATIONS = Stream.concat(
            DEFAULT_LOCATIONS.stream(),
            Stream.of(ADDITIONAL_LOCATION_1, ADDITIONAL_LOCATION_2, ADDITIONAL_LOCATION_3)
    ).toList();

    ConfigurableEnvironment environment;
    FlywayProperties flywayProperties;
    AdditionalFlywayLocationsInjector injector;
    @Mock
    ConfigurableListableBeanFactory beanFactory;

    @BeforeEach
    void beforeEach() {
        environment = new MockEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource(
                "default",
                Map.of("spring.flyway.locations", DEFAULT_LOCATIONS)
        ));

        flywayProperties = new FlywayProperties();
        flywayProperties.setLocations(DEFAULT_LOCATIONS);

        injector = new AdditionalFlywayLocationsInjector(flywayProperties, environment);

        when(beanFactory.getBeanNamesForAnnotation(AdditionalFlywayLocations.class)).thenReturn(new String[]{"bean1", "bean2"});
        when(beanFactory.getBeanDefinition("bean1")).thenReturn(new AnnotatedGenericBeanDefinition(AnnotatedTestBean1.class));
        when(beanFactory.getBeanDefinition("bean2")).thenReturn(new AnnotatedGenericBeanDefinition(AnnotatedTestBean2.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAdditionalLocationsAdded() {
        injector.postProcessBeanFactory(beanFactory);

        assertThat(flywayProperties.getLocations()).containsExactlyElementsOf(EXPECTED_LOCATIONS);
        assertThat(environment.getProperty("spring.flyway.locations", List.class))
                .containsExactlyElementsOf(EXPECTED_LOCATIONS);
    }

    @AdditionalFlywayLocations(ADDITIONAL_LOCATION_1)
    static class AnnotatedTestBean1 {
    }

    @AdditionalFlywayLocations({ADDITIONAL_LOCATION_2, ADDITIONAL_LOCATION_3})
    static class AnnotatedTestBean2 {
    }
}

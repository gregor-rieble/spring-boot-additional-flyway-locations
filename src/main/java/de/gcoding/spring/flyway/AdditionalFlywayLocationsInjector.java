package de.gcoding.spring.flyway;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link BeanFactoryPostProcessor} that will modify the result of {@link FlywayProperties#getLocations()}
 * by adding additional flyway locations to the list. This is done by scanning for beans annotated with
 * {@link AdditionalFlywayLocations}. To achieve the desired result, the existing {@link FlywayProperties} bean
 * is modified directly and a new {@link PropertySource} is added with the highest priority that overwrites the
 * result of looking up the {@code spring.flyway.locations} property
 */
@Slf4j
public class AdditionalFlywayLocationsInjector implements BeanFactoryPostProcessor {
    private final FlywayProperties flywayProperties;
    private final ConfigurableEnvironment environment;

    /**
     * Creates a new {@link AdditionalFlywayLocationsInjector}
     *
     * @param flywayProperties The existing {@link FlywayProperties} that will be modified
     * @param environment      The {@link ConfigurableEnvironment} that will also be modified by adding an additional
     *                         {@link PropertySource} with highest priority
     */
    public AdditionalFlywayLocationsInjector(FlywayProperties flywayProperties, ConfigurableEnvironment environment) {
        this.flywayProperties = flywayProperties;
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final var newLocations = new LinkedList<>(flywayProperties.getLocations());

        final var annotatedBeanNames = beanFactory.getBeanNamesForAnnotation(AdditionalFlywayLocations.class);
        addLocationsFoundOnAnnotatedBeans(beanFactory, annotatedBeanNames, newLocations);

        flywayProperties.setLocations(newLocations);
        environment.getPropertySources().addFirst(new MapPropertySource(
                AdditionalFlywayLocationsInjector.class.getName(),
                Map.of("spring.flyway.locations", newLocations)
        ));
    }

    private void addLocationsFoundOnAnnotatedBeans(
            @NonNull ConfigurableListableBeanFactory beanFactory,
            @NonNull String[] annotatedBeanNames,
            @NonNull LinkedList<String> targetList
    ) {
        for (final var beanName : annotatedBeanNames) {
            final var beanDefinition = beanFactory.getBeanDefinition(beanName);
            if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
                addAdditionalLocationsFromAnnotation(annotatedBeanDefinition, targetList);
            } else {
                log.warn("Found @AdditionalFlywayLocations annotation on bean {}, but bean definition does not implement"
                        + " the AnnotatedBeanDefinition interface", beanName);
            }
        }
    }

    private void addAdditionalLocationsFromAnnotation(
            @NonNull AnnotatedBeanDefinition annotatedBeanDefinition,
            @NonNull List<String> targetList
    ) {
        final var annotationMetadata = annotatedBeanDefinition.getMetadata();
        final var additionalLocations = annotationMetadata.getAnnotations()
                .get(AdditionalFlywayLocations.class)
                .synthesize()
                .value();

        for (final var additionalLocation : additionalLocations) {
            if (!targetList.contains(additionalLocation)) {
                log.info("Adding additional flyway location: {}", additionalLocation);
                targetList.add(additionalLocation);
            }
        }
    }
}

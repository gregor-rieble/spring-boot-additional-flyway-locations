package io.rieble.spring.flyway;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AdditionalFlywayLocationsInjectorIT {
    @Configuration
    @AdditionalFlywayLocations("classpath:db/additional-migration")
    static class NoFlywayConfiguredTestConfiguration {
    }

    @Configuration
    @EnableAutoConfiguration
    @ImportAutoConfiguration(FlywayAutoConfiguration.class)
    @AdditionalFlywayLocations("classpath:db/additional-migration")
    static class FlywayConfiguredWithAdditionalLocationConfiguration {
    }

    @ActiveProfiles("it")
    static class AdditionalFlywayLocationsTestBase {
        @Autowired
        Environment environment;
        @Autowired(required = false)
        FlywayProperties flywayProperties;
        @Autowired(required = false)
        DataSource dataSource;
    }

    @Nested
    @SpringBootTest(classes = NoFlywayConfiguredTestConfiguration.class)
    class TestThatAnnotationHasNoEffectWithoutFlyway extends AdditionalFlywayLocationsTestBase {
        @Test
        void testAdditionalLocationsAreNotConfigured() {
            assertThat(flywayProperties).isNull();

            final var flywayLocations = environment.getProperty("spring.flyway.locations");
            assertThat(flywayLocations).isNull();
        }
    }

    @Nested
    @DataJpaTest
    @ContextConfiguration(classes = FlywayConfiguredWithAdditionalLocationConfiguration.class)
    class TestThatAdditionalFlywayLocationsAreConfigured extends AdditionalFlywayLocationsTestBase {
        @Test
        void testAdditionalLocationsAreConfiguredInProperties() {
            assertThat(flywayProperties).isNotNull();

            final var flywayLocations = flywayProperties.getLocations();
            assertThat(flywayLocations).containsExactly("classpath:db/migration", "classpath:db/additional-migration");
        }

        @Test
        void testAdditionalLocationsAreConfiguredInEnvironment() {
            final var flywayLocations = environment.getProperty("spring.flyway.locations");
            assertThat(flywayLocations).contains("db/migration").contains("db/additional-migration");
        }

        @Test
        void testAllMigrationsHaveBeenExecuted() throws SQLException {
            final var migrations = fetchExecutedMigrations();
            assertThat(migrations).containsExactlyInAnyOrder("V1__Initial.sql", "V2__Additional.sql");
        }

        private Set<String> fetchExecutedMigrations() throws SQLException {
            final var migrations = new HashSet<String>();

            try (
                    final var connection = dataSource.getConnection();
                    final var resultSet = connection.createStatement().executeQuery("SELECT * FROM \"flyway_schema_history\";")
            ) {
                while (resultSet.next()) {
                    final var migration = resultSet.getString("script");
                    if (StringUtils.hasText(migration)) {
                        migrations.add(migration);
                    }
                }
            }

            return migrations;
        }
    }
}

package com.team7.agora.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class ProductionSchemaConfigTest {

    @Test
    void flywayIsAvailableForProductionSchemaMigrations() throws Exception {
        assertThat(Class.forName("org.flywaydb.core.Flyway")).isNotNull();
    }

    @Test
    void prodProfileUsesFlywayAndValidatesHibernateSchema() throws Exception {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
            .load("application", new ClassPathResource("application.yaml"));

        PropertySource<?> prod = sources.stream()
            .filter(source -> "prod".equals(source.getProperty("spring.config.activate.on-profile")))
            .findFirst()
            .orElseThrow();

        assertThat(prod.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(prod.getProperty("spring.flyway.enabled")).isEqualTo(true);
        assertThat(prod.getProperty("spring.flyway.locations")).isEqualTo("classpath:db/migration");
    }
}

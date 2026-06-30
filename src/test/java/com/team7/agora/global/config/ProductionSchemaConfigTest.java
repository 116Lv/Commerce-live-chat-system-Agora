package com.team7.agora.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

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

    @Test
    void paymentConfirmingAtColumnIsCoveredByFlywayMigration() throws Exception {
        ClassPathResource migration = new ClassPathResource("db/migration/V3__add_payment_confirming_at.sql");

        assertThat(migration.exists()).isTrue();

        String sql = StreamUtils.copyToString(migration.getInputStream(), StandardCharsets.UTF_8)
            .toLowerCase();
        assertThat(sql).contains("alter table payments");
        assertThat(sql).contains("add column confirming_at");
    }

    @Test
    void tradeCreatedAtColumnKeepsOriginalV4FlywayMigration() throws Exception {
        ClassPathResource migration = new ClassPathResource("db/migration/V4__add_trade_created_at.sql");

        assertThat(migration.exists()).isTrue();

        String sql = StreamUtils.copyToString(migration.getInputStream(), StandardCharsets.UTF_8)
            .toLowerCase();
        assertThat(sql).contains("alter table trades");
        assertThat(sql).contains("add column created_at");
    }

    @Test
    void flywayMigrationVersionsAreUnique() throws Exception {
        Pattern versionPattern = Pattern.compile("^V\\d+__.*\\.sql$");
        List<String> versions;
        try (var paths = Files.list(Path.of("src/main/resources/db/migration"))) {
            versions = paths
                .map(path -> path.getFileName().toString())
                .filter(name -> versionPattern.matcher(name).matches())
                .map(name -> name.substring(0, name.indexOf("__")))
                .toList();
        }

        assertThat(versions)
            .doesNotHaveDuplicates();
    }

    @Test
    void adminPermissionsElementCollectionIsCoveredByFlywayMigration() throws Exception {
        String combinedSql;
        try (var paths = Files.list(Path.of("src/main/resources/db/migration"))) {
            combinedSql = paths
                .filter(path -> path.getFileName().toString().endsWith(".sql"))
                .map(path -> {
                    try {
                        return Files.readString(path, StandardCharsets.UTF_8);
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                })
                .collect(Collectors.joining("\n"))
                .toLowerCase();
        }

        assertThat(combinedSql).contains("create table if not exists admin_permissions");
        assertThat(combinedSql).contains("admin_id bigint not null");
        assertThat(combinedSql).contains("permission varchar(40) not null");
        assertThat(combinedSql).contains("foreign key (admin_id) references admins (id)");
    }
}

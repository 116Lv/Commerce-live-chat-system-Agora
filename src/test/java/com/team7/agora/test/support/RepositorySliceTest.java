package com.team7.agora.test.support;

import com.team7.agora.global.config.QuerydslConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Repository slice tests share Querydsl infrastructure and disable default
 * {@code data.sql} loading so each test can own its fixture setup.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest(properties = "spring.sql.init.mode=never")
@Import(QuerydslConfig.class)
public @interface RepositorySliceTest {
}

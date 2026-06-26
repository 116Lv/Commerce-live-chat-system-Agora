// QueryDSL의 JPAQueryFactory를 Spring Bean으로 등록하는 설정
package com.team7.agora.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Querydsl 사용에 필요한 인프라를 설정한다.
 */
@Configuration
public class QuerydslConfig {

    /**
     * 현재 EntityManager를 사용하는 JPAQueryFactory를 생성한다.
     * @param entityManager 엔티티 매니저
     * @return JPAQueryFactory
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}

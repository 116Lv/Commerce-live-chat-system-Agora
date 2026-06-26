package com.team7.agora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Agora 애플리케이션의 Spring Boot 진입점이다.
 */
@SpringBootApplication
@EnableScheduling
public class AgoraApplication {

    /**
     * 스프링 부트 애플리케이션을 실행하는 시작 지점이다.
     * @param args 애플리케이션 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(AgoraApplication.class, args);
    }

}

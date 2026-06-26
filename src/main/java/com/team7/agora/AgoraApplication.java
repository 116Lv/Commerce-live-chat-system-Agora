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
     * 요청한 동작을 처리한다.
     * @param args 입력 값
     */
    public static void main(String[] args) {
        SpringApplication.run(AgoraApplication.class, args);
    }

}

package com.deol.deolspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.deol.deolspring.entity") // 엔티티가 위치한 패키지 경로
@EnableJpaRepositories(basePackages = "com.deol.deolspring.repository") // JPA 리포지토리가 위치한 패키지 경로

public class DeolSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeolSpringApplication.class, args);
    }

}

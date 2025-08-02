package com.javarush.jira;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
abstract class BaseTests {

    // @ActiveProfiles("test") - данная аннотация активирует профайл "test"

    @Container
    static PostgreSQLContainer<?> postreSQL = new PostgreSQLContainer<>("postgres:latest");

    // Добавление аннотаций @Testcontainers к классу и @Container к полю объявления контейнера позволит JUnit
    // автоматически запустить контейнер перед выполнением тестов и автоматически уничтожить его после их выполнения.

//    static {
//        postreSQL.start();
//    }
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postreSQL::getJdbcUrl);
//        registry.add("spring.datasource.username", postreSQL::getUsername);
//        registry.add("spring.datasource.password", postreSQL::getPassword);
//    }

}

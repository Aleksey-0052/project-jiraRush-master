package com.javarush.jira.common.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

//@Configuration
//public class DataSourceConfig {
//
//    @Bean
//    @Primary
//    @Profile("!test")
//    @ConfigurationProperties("spring.datasource.postgres")
//    public DataSource postgresDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//
//    @Bean
//    @Profile("test")
//    @ConfigurationProperties("spring.datasource.h2")
//    public DataSource h2DataSource() {
//        return DataSourceBuilder.create().build();
//    }
//}

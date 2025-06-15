package com.example.AWMI;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DBCheckConfig {

    @Bean
    public CommandLineRunner testDataSource(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("成功连接数据库: " + conn.getMetaData().getURL());
            } catch (Exception e) {
                System.out.println("数据库连接失败！");
                e.printStackTrace();
            }
        };
    }
}

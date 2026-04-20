package com.dormrepair;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.SpringApplication;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DormRepairApplication {

    public static void main(String[] args) {
        SpringApplication.run(DormRepairApplication.class, args);
    }
}

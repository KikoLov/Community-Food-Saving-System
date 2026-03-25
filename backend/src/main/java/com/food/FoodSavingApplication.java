package com.food;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.food.mapper")
public class FoodSavingApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoodSavingApplication.class, args);
    }
}

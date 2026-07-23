package com.lifeequation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lifeequation.mapper")
public class LifeEquationApplication {
    public static void main(String[] args) {
        SpringApplication.run(LifeEquationApplication.class, args);
    }
}

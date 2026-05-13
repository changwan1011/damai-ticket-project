package com.damai.damaiticket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.damai.damaiticket.mapper")
public class DamaiTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(DamaiTicketApplication.class, args);
    }
}

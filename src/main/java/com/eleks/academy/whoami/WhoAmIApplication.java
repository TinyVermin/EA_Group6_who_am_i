package com.eleks.academy.whoami;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class WhoAmIApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhoAmIApplication.class, args);
    }

}
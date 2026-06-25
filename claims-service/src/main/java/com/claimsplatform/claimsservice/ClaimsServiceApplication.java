package com.claimsplatform.claimsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.claimsplatform.claimsservice", "com.claimsplatform.common"})
public class ClaimsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimsServiceApplication.class, args);
    }
}

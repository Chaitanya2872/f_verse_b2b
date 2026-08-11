package com.acs.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.acs.crm.security.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class B2bCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2bCrmApplication.class, args);
    }
}

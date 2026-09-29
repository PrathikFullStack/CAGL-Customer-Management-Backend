package com.iexceed.appzillonbanking.cagl.cm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = { "com.iexceed.appzillonbanking.*" })
public class AppzillonBankingCustomerManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppzillonBankingCustomerManagementApplication.class, args);
    }
}

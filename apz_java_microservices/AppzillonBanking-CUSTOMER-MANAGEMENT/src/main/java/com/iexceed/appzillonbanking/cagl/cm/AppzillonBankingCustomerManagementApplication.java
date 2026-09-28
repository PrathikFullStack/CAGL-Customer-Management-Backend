package com.iexceed.appzillonbanking.cagl.cm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.iexceed.appzillonbanking.cagl.cm"
})
@EntityScan(basePackages = {
    "com.iexceed.appzillonbanking.cagl.cm.entity"
})
public class AppzillonBankingCustomerManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppzillonBankingCustomerManagementApplication.class, args);
    }
}

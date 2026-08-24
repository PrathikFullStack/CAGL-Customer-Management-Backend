package com.iexceed.appzillonbanking.cagl.cob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.iexceed.appzillonbanking.*" })
public class AppzillonBankingCobApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppzillonBankingCobApplication.class, args);
	}
}

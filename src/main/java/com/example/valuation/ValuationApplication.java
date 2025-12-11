package com.example.valuation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// @SpringBootApplication(scanBasePackages = {
//         "com.example.valuation",   
//         "io.github.sushmithashiva04ops.centraleventpublisher"    
// })
//@EnableConfigurationProperties(OutboxProperties.class)
//@EnableScheduling
//@EnableJms
@SpringBootApplication
public class ValuationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValuationApplication.class, args);
	}

}

package jp.co.company.sqas.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("==============================================");
        System.out.println("  SQAS Demo Sandbox Started!");
        System.out.println("  Web UI: http://localhost:8080");
        System.out.println("  MailHog: http://localhost:8025");
        System.out.println("==============================================");
    }
}

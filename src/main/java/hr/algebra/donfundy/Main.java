package hr.algebra.donfundy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Main {

    // TODO: https://medium.com/@AlexanderObregon/how-to-implement-internationalization-i18n-in-spring-boot-aea2c62c1bfa

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}

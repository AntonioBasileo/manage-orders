package it.manage.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class ManageOrdersApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageOrdersApplication.class, args);
    }
}

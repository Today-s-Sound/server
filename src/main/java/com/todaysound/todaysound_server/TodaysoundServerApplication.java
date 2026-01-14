package com.todaysound.todaysound_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TodaysoundServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodaysoundServerApplication.class, args);
    }

}

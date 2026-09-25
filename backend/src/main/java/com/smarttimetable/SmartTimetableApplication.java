package com.smarttimetable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartTimetableApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTimetableApplication.class, args);
        System.out.println("======================================================================");
        System.out.println(" Smart Automatic Timetable Generator Backend Started Successfully!");
        System.out.println(" Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("======================================================================");
    }
}

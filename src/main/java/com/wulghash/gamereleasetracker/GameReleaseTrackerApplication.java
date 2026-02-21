package com.wulghash.gamereleasetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GameReleaseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameReleaseTrackerApplication.class, args);
    }

}

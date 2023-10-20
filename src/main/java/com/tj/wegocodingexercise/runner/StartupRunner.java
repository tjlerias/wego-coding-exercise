package com.tj.wegocodingexercise.runner;

import com.tj.wegocodingexercise.service.CarparkService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@Profile("!test")
@Component
public class StartupRunner implements ApplicationRunner {

    private final CarparkService carparkService;

    public StartupRunner(CarparkService carparkService) {
        this.carparkService = carparkService;
    }

    @Override
    public void run(ApplicationArguments args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        carparkService.loadCarparkData();
    }
}

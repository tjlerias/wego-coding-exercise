package com.tj.wegocodingexercise.runner;

import com.tj.wegocodingexercise.service.CarParkService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@Profile("!test")
@Component
public class StartupRunner implements ApplicationRunner {

    private final CarParkService carParkService;

    public StartupRunner(CarParkService carParkService) {
        this.carParkService = carParkService;
    }

    @Override
    public void run(ApplicationArguments args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        carParkService.loadCarParkData();
    }
}

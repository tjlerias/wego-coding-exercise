package com.tj.wegocodingexercise.scheduler;

import com.tj.wegocodingexercise.dto.CarParkAvailabilityDTO;
import com.tj.wegocodingexercise.service.DataGovSGService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tj.wegocodingexercise.config.CacheConfig.CAR_PARK_AVAILABILITY_SECONDARY_KEY;

/**
 * We can use Spring Boot's @Scheduler, but we will experiment on this approach for now.
 */
@Component
public class CarParkAvailabilityScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CarParkAvailabilityScheduler.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DataGovSGService dataGovSGService;
    private final RedisTemplate<String, Object> redisTemplate;

    public CarParkAvailabilityScheduler(DataGovSGService dataGovSGService, RedisTemplate<String, Object> redisTemplate) {
        this.dataGovSGService = dataGovSGService;
        this.redisTemplate = redisTemplate;
        scheduler.scheduleAtFixedRate(this::fetchCarParkAvailability, 0, 1, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void preDestroy() {
        scheduler.shutdown();
    }

    private void fetchCarParkAvailability() {
        Map<String, CarParkAvailabilityDTO> availabilityPerCarPark = dataGovSGService.getCarParkAvailability();
        redisTemplate.opsForValue().set(CAR_PARK_AVAILABILITY_SECONDARY_KEY, availabilityPerCarPark);
        logger.info("Updating car park availability cache success.");
    }
}

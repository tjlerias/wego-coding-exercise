package com.tj.wegocodingexercise.controller;

import com.tj.wegocodingexercise.dto.CarParkDetails;
import com.tj.wegocodingexercise.dto.NearestCarParksRequest;
import com.tj.wegocodingexercise.service.CarParkService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/carparks")
@Validated
public class CarParkController {

    private final CarParkService carParkService;

    public CarParkController(CarParkService carParkService) {
        this.carParkService = carParkService;
    }

    @GetMapping("/nearest")
    public List<CarParkDetails> getNearestCarParks(
        @Valid NearestCarParksRequest request,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, name = "per_page", defaultValue = "10") Integer pageSize
    ) {
        return carParkService.getNearestCarParks(
            request,
            PageRequest.of(page - 1, pageSize)
        );
    }
}

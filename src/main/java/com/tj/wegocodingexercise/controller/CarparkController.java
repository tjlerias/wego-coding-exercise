package com.tj.wegocodingexercise.controller;

import com.tj.wegocodingexercise.dto.CarparkDetails;
import com.tj.wegocodingexercise.dto.NearestCarparksRequest;
import com.tj.wegocodingexercise.service.CarparkService;
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
public class CarparkController {

    private final CarparkService carparkService;

    public CarparkController(CarparkService carparkService) {
        this.carparkService = carparkService;
    }

    @GetMapping("/nearest")
    public List<CarparkDetails> getNearestCarparks(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam(required = false, defaultValue = "500") Integer distance,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, name = "per_page", defaultValue = "10") Integer pageSize
    ) {
        return carparkService.getNearestCarParks(
            new NearestCarparksRequest(latitude, longitude, distance),
            PageRequest.of(page - 1, pageSize)
        );
    }
}

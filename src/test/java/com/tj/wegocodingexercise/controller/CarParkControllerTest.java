package com.tj.wegocodingexercise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tj.wegocodingexercise.dto.CarParkDetails;
import com.tj.wegocodingexercise.dto.Error;
import com.tj.wegocodingexercise.dto.ErrorResponse;
import com.tj.wegocodingexercise.dto.NearestCarParksRequest;
import com.tj.wegocodingexercise.service.CarParkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarParkController.class)
class CarParkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarParkService carParkService;

    @Test
    void getNearestCarParks_withDefaultParamValues_success() throws Exception {
        double latitude = 1.37326;
        double longitude = 103.897;
        NearestCarParksRequest request = new NearestCarParksRequest(latitude, longitude, 500);
        PageRequest pageable = PageRequest.of(0, 10);
        CarParkDetails carParkDetails1 = new CarParkDetails("Address 1", 1.3732, 103.8969, 100, 25);
        CarParkDetails carParkDetails2 = new CarParkDetails("Address 2", 1.3742, 103.8958, 123, 1);
        List<CarParkDetails> expected = List.of(carParkDetails1, carParkDetails2);

        when(carParkService.getNearestCarParks(request, pageable)).thenReturn(expected);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("latitude", Double.toString(latitude))
                    .param("longitude", Double.toString(longitude)))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verify(carParkService).getNearestCarParks(request, pageable);
        verifyNoMoreInteractions(carParkService);
    }

    @Test
    void getNearestCarParks_withCustomParamValues_success() throws Exception {
        double latitude = 1.37326;
        double longitude = 103.897;
        int distance = 1000;
        NearestCarParksRequest request = new NearestCarParksRequest(latitude, longitude, distance);
        PageRequest pageable = PageRequest.of(1, 3);
        CarParkDetails carParkDetails1 = new CarParkDetails("Address 1", 1.3732, 103.8969, 100, 25);
        CarParkDetails carParkDetails2 = new CarParkDetails("Address 2", 1.3742, 103.8958, 123, 1);
        List<CarParkDetails> expected = List.of(carParkDetails1, carParkDetails2);

        when(carParkService.getNearestCarParks(request, pageable)).thenReturn(expected);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("latitude", Double.toString(latitude))
                    .param("longitude", Double.toString(longitude))
                    .param("distance", Integer.toString(distance))
                    .param("page", "2")
                    .param("per_page", "3"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verify(carParkService).getNearestCarParks(request, pageable);
        verifyNoMoreInteractions(carParkService);
    }

    @Test
    void getNearestCarParks_nullLatitude_badRequest() throws Exception {
        Error error = new Error("latitude", "Latitude is required");
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), null, List.of(error));

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("longitude", "103.897"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carParkService);
    }

    @Test
    void getNearestCarParks_nullLongitude_badRequest() throws Exception {
        Error error = new Error("longitude", "Longitude is required");
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), null, List.of(error));

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("latitude", "1.37326"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carParkService);
    }

    @Test
    void getNearestCarParks_invalidDistance_badRequest() throws Exception {
        Error error = new Error("distance", "Distance must be greater than or equal to 500");
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), null, List.of(error));

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("longitude", "103.897")
                    .param("latitude", "1.37326")
                    .param("distance", "100"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carParkService);
    }

    @Test
    void getNearestCarParks_negativePage_badRequest() throws Exception {
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), "Page index must not be less than zero", null);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("longitude", "103.897")
                    .param("latitude", "1.37326")
                    .param("page", "0"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carParkService);
    }

    @Test
    void getNearestCarParks_negativePageSize_badRequest() throws Exception {
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), "Page size must not be less than one", null);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("longitude", "103.897")
                    .param("latitude", "1.37326")
                    .param("per_page", "-1"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carParkService);
    }
}
package com.tj.wegocodingexercise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tj.wegocodingexercise.dto.CarparkDetails;
import com.tj.wegocodingexercise.dto.Error;
import com.tj.wegocodingexercise.dto.ErrorResponse;
import com.tj.wegocodingexercise.dto.NearestCarparksRequest;
import com.tj.wegocodingexercise.service.CarparkService;
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

@WebMvcTest(CarparkController.class)
class CarparkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarparkService carparkService;

    @Test
    void getNearestCarparks_withDefaultParamValues_success() throws Exception {
        double latitude = 1.37326;
        double longitude = 103.897;
        NearestCarparksRequest request = new NearestCarparksRequest(latitude, longitude, 500);
        PageRequest pageable = PageRequest.of(0, 10);
        CarparkDetails carparkDetails1 = new CarparkDetails("Address 1", 1.3732, 103.8969, 100, 25);
        CarparkDetails carparkDetails2 = new CarparkDetails("Address 2", 1.3742, 103.8958, 123, 1);
        List<CarparkDetails> expected = List.of(carparkDetails1, carparkDetails2);

        when(carparkService.getNearestCarParks(request, pageable)).thenReturn(expected);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("latitude", Double.toString(latitude))
                    .param("longitude", Double.toString(longitude)))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verify(carparkService).getNearestCarParks(request, pageable);
        verifyNoMoreInteractions(carparkService);
    }

    @Test
    void getNearestCarparks_withCustomParamValues_success() throws Exception {
        double latitude = 1.37326;
        double longitude = 103.897;
        int distance = 1000;
        NearestCarparksRequest request = new NearestCarparksRequest(latitude, longitude, distance);
        PageRequest pageable = PageRequest.of(1, 3);
        CarparkDetails carparkDetails1 = new CarparkDetails("Address 1", 1.3732, 103.8969, 100, 25);
        CarparkDetails carparkDetails2 = new CarparkDetails("Address 2", 1.3742, 103.8958, 123, 1);
        List<CarparkDetails> expected = List.of(carparkDetails1, carparkDetails2);

        when(carparkService.getNearestCarParks(request, pageable)).thenReturn(expected);

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

        verify(carparkService).getNearestCarParks(request, pageable);
        verifyNoMoreInteractions(carparkService);
    }

    @Test
    void getNearestCarparks_nullLatitude_badRequest() throws Exception {
        Error error = new Error("latitude", "Latitude is required");
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), null, List.of(error));

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("longitude", "103.897"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carparkService);
    }

    @Test
    void getNearestCarparks_nullLongitude_badRequest() throws Exception {
        Error error = new Error("longitude", "Longitude is required");
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), null, List.of(error));

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("latitude", "1.37326"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carparkService);
    }

    @Test
    void getNearestCarparks_negativePage_badRequest() throws Exception {
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), "Page index must not be less than zero", null);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("longitude", "103.897")
                    .param("latitude", "1.37326")
                    .param("page", "0"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carparkService);
    }

    @Test
    void getNearestCarparks_negativePageSize_badRequest() throws Exception {
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST.value(), "Page size must not be less than one", null);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/carparks/nearest")
                    .param("longitude", "103.897")
                    .param("latitude", "1.37326")
                    .param("per_page", "-1"))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expected));

        verifyNoMoreInteractions(carparkService);
    }
}
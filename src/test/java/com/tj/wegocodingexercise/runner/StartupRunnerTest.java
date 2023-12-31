package com.tj.wegocodingexercise.runner;

import com.tj.wegocodingexercise.service.CarParkService;
import com.tj.wegocodingexercise.service.DataGovSGService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class StartupRunnerTest {

    @Mock
    private CarParkService carParkService;

    @Mock
    private DataGovSGService dataGovSGService;

    private StartupRunner startupRunner;

    @BeforeEach
    void setUp() {
        startupRunner = new StartupRunner(carParkService);
    }

    @Test
    void run_success() {
        startupRunner.run(null);

        assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT+8"));

        verify(carParkService).loadCarParkData();
        verifyNoMoreInteractions(carParkService);
    }
}
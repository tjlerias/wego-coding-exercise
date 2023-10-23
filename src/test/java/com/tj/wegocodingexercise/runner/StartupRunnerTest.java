package com.tj.wegocodingexercise.runner;

import com.tj.wegocodingexercise.service.CarparkService;
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
    private CarparkService carparkService;

    private StartupRunner startupRunner;

    @BeforeEach
    void setUp() {
        startupRunner = new StartupRunner(carparkService);
    }

    @Test
    void run_success() {
        startupRunner.run(null);

        assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT+8"));

        verify(carparkService).loadCarparkData();
        verifyNoMoreInteractions(carparkService);
    }
}
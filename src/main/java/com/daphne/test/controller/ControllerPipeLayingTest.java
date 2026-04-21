package com.geowebframework.pipeLaying.controller;

import com.geowebframework.pipeLaying.service.ServicePipeLaying;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ControllerPipeLayingTest {

    @Mock
    private ServicePipeLaying servicePipeLaying;

    @InjectMocks
    private ControllerPipeLaying controllerPipeLaying;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controllerPipeLaying = new ControllerPipeLaying(servicePipeLaying);
    }

    @Test
    public void pipeLayingByDrawing_shouldReturn200() {
        when(servicePipeLaying.executePipeLaying()).thenReturn("OK");

        ResponseEntity<String> response = controllerPipeLaying.pipeLayingByDrawing();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void pipeLayingByDrawing_shouldReturnServiceResult() {
        String expected = "Under piping eseguito: 42 elementi processati";
        when(servicePipeLaying.executePipeLaying()).thenReturn(expected);
        ResponseEntity<String> response = controllerPipeLaying.pipeLayingByDrawing();
        assertEquals(response.getBody(), expected);
    }

    @Test
    public void pipeLayingByDrawing_shouldReturnEmptyBody() {
        when(servicePipeLaying.executePipeLaying()).thenReturn("");
        ResponseEntity<String> response = controllerPipeLaying.pipeLayingByDrawing();
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), "");
    }

    @Test
    public void pipeLayingByDrawing_shouldCallServiceExactlyOnce() {
        when(servicePipeLaying.executePipeLaying()).thenReturn("done");
        controllerPipeLaying.pipeLayingByDrawing();
        verify(servicePipeLaying, times(1)).executePipeLaying();
        verifyNoMoreInteractions(servicePipeLaying);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void pipeLayingByDrawing_shouldPropagateServiceException() {
        when(servicePipeLaying.executePipeLaying())
                .thenThrow(new RuntimeException("Errore interno"));
        controllerPipeLaying.pipeLayingByDrawing();
    }

    @Test
    public void pipeLayingByDrawing_bodyNotNull() {
        when(servicePipeLaying.executePipeLaying()).thenReturn("risultato");
        ResponseEntity<String> response = controllerPipeLaying.pipeLayingByDrawing();
        assertNotNull(response.getBody());
    }
}
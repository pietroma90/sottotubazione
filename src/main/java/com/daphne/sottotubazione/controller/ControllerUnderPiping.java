package com.geowebframework.underPiping.controller;

import com.geowebframework.underPiping.service.ServiceUnderPiping;
import it.eagleprojects.gisfocommons.annotations.GisfoRestController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@GisfoRestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ControllerUnderPiping {

    private final ServiceUnderPiping serviceUnderPiping;

    @PostMapping(value = "underPipingByDrawing", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> underPipingByDrawing() {
        return ResponseEntity.ok(serviceUnderPiping.executeUnderPiping());
    }
}
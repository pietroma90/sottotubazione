package com.geowebframework.pipeLaying.controller;

import com.geowebframework.pipeLaying.service.ServicePipeLaying;
import it.eagleprojects.gisfocommons.annotations.GisfoRestController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@GisfoRestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ControllerPipeLaying {

    private final ServicePipeLaying servicePipeLaying;

    @PostMapping(value = "pipeLayingByDrawing", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> pipeLayingByDrawing() {
        return ResponseEntity.ok(servicePipeLaying.executePipeLaying());
    }
}
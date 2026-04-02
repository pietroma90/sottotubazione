package com.geowebframework.underPiping.controller;


import com.geowebframework.procedureOutput.ProcedureOutputException;
import com.geowebframework.underPiping.service.ServiceUnderPiping;
import it.eagleprojects.gisfocommons.annotations.GisfoRestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Endpoint REST per la procedura di sotto-tubazione automatica.
 * Corrisponde al tasto "Assegna Sotto-tubazioni" nella UI.
 */
@GisfoRestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Log4j
public class ControllerUnderPiping {

    private final ServiceUnderPiping serviceUnderPiping;

    @PostMapping("underPipingByDrawing")
    public ResponseEntity<String> underPipingByDrawing() throws ProcedureOutputException {
        return ResponseEntity.ok(serviceUnderPiping.executeUnderPiping());
    }
}
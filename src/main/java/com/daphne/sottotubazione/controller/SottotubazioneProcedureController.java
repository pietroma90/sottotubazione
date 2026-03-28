package com.geowebframework.sottotubazione.controller;


import com.geowebframework.sottotubazione.domain.ProcedureResult;
import com.geowebframework.sottotubazione.service.SottotubazioneProcedureService;
import com.geowebframework.transfer.objects.JsonServerResponse;
import it.eagleprojects.gisfocommons.service.ServiceCommonsMultiutenza;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint REST per la procedura di sotto-tubazione automatica.
 * Corrisponde al tasto "Assegna Sotto-tubazioni" nella UI.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SottotubazioneProcedureController {

    private final SottotubazioneProcedureService procedureService;
    private final ServiceCommonsMultiutenza serviceCommonsMultiutenza;

    /**
     * POST /api/v1/sottotubazione/assegna/{projectId}
     */
    @PostMapping("assegnaSottotubazioni")
    public ResponseEntity<ProcedureResult> assegnaSottotubazioni() {
        Long projectId = serviceCommonsMultiutenza.getCorrectDrawing();
        log.info("Richiesta assegnazione sotto-tubazioni per progetto {}", projectId);
        ProcedureResult result = procedureService.lanciaPerProgetto(projectId);
        return ResponseEntity.ok(result);
    }
}

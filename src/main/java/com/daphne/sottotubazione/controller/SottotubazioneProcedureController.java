package com.daphne.sottotubazione.controller;

import com.daphne.sottotubazione.domain.ProcedureResult;
import com.daphne.sottotubazione.dto.ProcedureResponseDto;
import com.daphne.sottotubazione.service.SottotubazioneProcedureService;
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
@RequestMapping("/api/v1/sottotubazione")
@RequiredArgsConstructor
public class SottotubazioneProcedureController {

    private final SottotubazioneProcedureService procedureService;

    /**
     * POST /api/v1/sottotubazione/assegna/{projectId}
     */
    @PostMapping("/assegna/{projectId}")
    public ResponseEntity<ProcedureResponseDto> assegnaSottotubazioni(@PathVariable Long projectId) {
        log.info("Richiesta assegnazione sotto-tubazioni per progetto {}", projectId);
        ProcedureResult result = procedureService.lanciaPerProgetto(projectId);
        ProcedureResponseDto response = ProcedureResponseDto.builder()
                .success(true)
                .totalAssigned(result.getTotalAssigned())
                .totalSkipped(result.getTotalSkipped())
                .hasWarnings(result.hasWarnings())
                .warnings(result.getAllLogs().stream()
                    .map(l -> ProcedureResponseDto.WarningDto.builder()
                        .fkTratta(l.getFkTratta())
                        .pkParent(l.getPkParent())
                        .parentDescr(l.getParentDescr())
                        .pkTarget(l.getPkTarget())
                        .targetDescr(l.getTargetDescr())
                        .message(l.getMessage())
                        .build())
                    .toList())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/sottotubazione/log/{projectId}
     */
    @GetMapping("/log/{projectId}")
    public ResponseEntity<?> getLog(@PathVariable Long projectId) {
        return ResponseEntity.ok(procedureService.getLogs(projectId));
    }
}

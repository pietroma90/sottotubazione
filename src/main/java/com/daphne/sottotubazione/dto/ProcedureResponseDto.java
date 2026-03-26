package com.daphne.sottotubazione.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO di risposta per l'endpoint REST della procedura.
 */
@Data
@Builder
public class ProcedureResponseDto {

    private boolean success;
    private int totalAssigned;
    private int totalSkipped;
    private boolean hasWarnings;
    private List<WarningDto> warnings;

    @Data
    @Builder
    public static class WarningDto {
        private Long fkTratta;
        private Long pkParent;
        private String parentDescr;
        private Long pkTarget;
        private String targetDescr;
        private String message;
    }
}

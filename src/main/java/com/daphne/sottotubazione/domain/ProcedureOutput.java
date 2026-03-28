package com.geowebframework.sottotubazione.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Mappa la tabella logging.procedure_output.
 * Colleziona i log prodotti dalla procedura di sotto-tubazione.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureOutput {

    private Long id;
    private String entityTableName;
    private Long fkEntity;
    private Long fkTratta;
    private Long pkParent;
    private String parentDescr;
    private Long pkTarget;
    private String targetDescr;
    private String message;
    private Instant createdAt;

    public void prePersist() {
        this.createdAt = Instant.now();
        this.entityTableName = "projects";
    }
}

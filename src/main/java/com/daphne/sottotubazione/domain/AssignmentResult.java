package com.daphne.sottotubazione.domain;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Risultato dell'assegnazione per una singola tratta.
 */
@Data
@Builder
public class AssignmentResult {

    private Long fkTratta;

    @Builder.Default
    private List<ProcedureOutput> logs = new ArrayList<>();

    private int assignedCount;
    private int skippedCount;
    private boolean assigned;
    private boolean parentFull;

    public void addLog(ProcedureOutput log) {
        logs.add(log);
    }

    public void merge(AssignmentResult other) {
        logs.addAll(other.getLogs());
        assignedCount += other.getAssignedCount();
        skippedCount  += other.getSkippedCount();
    }
}

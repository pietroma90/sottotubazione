package com.daphne.sottotubazione.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Risultato aggregato dell'intera procedura su tutte le tratte.
 */
@Data
public class ProcedureResult {

    private final List<ProcedureOutput> allLogs = new ArrayList<>();
    private int totalAssigned;
    private int totalSkipped;

    public static ProcedureResult of(List<ProcedureOutput> logs) {
        ProcedureResult r = new ProcedureResult();
        r.getAllLogs().addAll(logs);
        return r;
    }

    public void merge(AssignmentResult result) {
        allLogs.addAll(result.getLogs());
        totalAssigned += result.getAssignedCount();
        totalSkipped  += result.getSkippedCount();
    }

    public boolean hasWarnings() {
        return !allLogs.isEmpty();
    }
}

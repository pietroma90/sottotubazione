package com.geowebframework.sottotubazione.domain;

import com.geowebframework.sottotubazione.RowUpdateData;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Risultato dell'assegnazione per una singola tratta.
 * Contiene i log, i contatori e il batch di update da applicare.
 */
@Data
@Builder
public class AssignmentResult {

    private Long fkTratta;

    @Builder.Default
    private List<ProcedureOutput> logs = new ArrayList<>();

    @Builder.Default
    private int assignedCount = 0;

    @Builder.Default
    private int skippedCount = 0;

    @Builder.Default
    private Map<String, List<RowUpdateData>> batchUpdates = new HashMap<>();

    public void addLog(ProcedureOutput log) {
        logs.add(log);
    }

    public void incrementAssigned() {
        assignedCount++;
    }

    public void incrementSkipped() {
        skippedCount++;
    }

    public void merge(AssignmentResult other) {
        logs.addAll(other.getLogs());
        assignedCount += other.getAssignedCount();
        skippedCount  += other.getSkippedCount();
        other.getBatchUpdates().forEach((table, rows) ->
                batchUpdates.computeIfAbsent(table, k -> new ArrayList<>()).addAll(rows)
        );
    }
}

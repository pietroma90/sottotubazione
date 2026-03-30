package com.geowebframework.sottotubazione.procedure;

import com.geowebframework.sottotubazione.RowUpdateData;
import it.eagleprojects.gisfocommons.utils.Message;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Output mutabile accumulato durante la Chain of Responsibility.
 * Raccoglie batch updates, messaggi di warning e contatori.
 * Separato da AssignmentInput per rispettare SRP.
 */
@Data
public class AssignmentOutput {

    private final Map<String, List<RowUpdateData>> batchUpdates = new HashMap<>();
    private final Message message = new Message();
    private int assignedCount;
    private int skippedCount;

    public void addWarning(String msg) {
        message.addToWarning(msg);
    }

    public void incrementAssigned() {
        assignedCount++;
    }

    public void incrementSkipped() {
        skippedCount++;
    }

    public void addBatchRow(String tableName, RowUpdateData row) {
        batchUpdates.computeIfAbsent(tableName, k -> new ArrayList<>()).add(row);
    }
}

package com.geowebframework.underPiping.domain;

import it.eagleprojects.gisfocommons.utils.Message;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Risultato aggregato dell'intera procedura su tutte le tratte.
 */
@Data
public class PipeInPipeRoutingProcedureResult {

    private final Message message = new Message();
    private int totalAssigned = 0;
    private int totalSkipped = 0;
    protected HashMap<String, List<RowUpdateData>> massiveValueToUpdate = new HashMap<>();

    public void merge(AssignmentResult result) {
        this.message.addToWarning(result.getMessage().getWarning());
        this.totalAssigned += result.getAssignedCount();
        this.totalSkipped  += result.getSkippedCount();
        if (!CollectionUtils.isEmpty(result.getMassiveValueToUpdate())) {
            result.getMassiveValueToUpdate().forEach((key, list) ->
                    massiveValueToUpdate
                            .computeIfAbsent(key, k -> new ArrayList<>())
                            .addAll(list)
            );
        }
    }
}

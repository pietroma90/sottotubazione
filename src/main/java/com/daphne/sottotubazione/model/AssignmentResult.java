package com.geowebframework.pipeLaying.model;

import it.eagleprojects.gisfocommons.utils.Message;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class AssignmentResult {

    private Message message = new Message();
    private int assignedCount = 0;
    private int skippedCount = 0;
    private Map<String, List<RowUpdateData>> massiveValueToUpdate = new HashMap<>();

    public void addLog(String log) {
        message.addToWarning(log);
    }

    public void incrementAssigned() {
        assignedCount++;
    }

    public void merge(AssignmentResult other) {
        this.message.addToWarning(other.getMessage().getWarning());
        this.assignedCount += other.getAssignedCount();
        if (!CollectionUtils.isEmpty(other.getMassiveValueToUpdate())) {
            other.getMassiveValueToUpdate().forEach((key, list) ->
                    massiveValueToUpdate
                            .computeIfAbsent(key, k -> new ArrayList<>())
                            .addAll(list)
            );
        }
    }
}
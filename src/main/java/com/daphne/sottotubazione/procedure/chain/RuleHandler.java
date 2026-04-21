package com.geowebframework.pipeLaying.procedure.chain;

import com.geowebframework.pipeLaying.model.AssignmentResult;
import com.geowebframework.pipeLaying.model.ConfigRule;
import com.geowebframework.pipeLaying.model.DuctTube;
import com.geowebframework.pipeLaying.model.UndergroundRoute;
import com.geowebframework.webclient.model.serverDbEntity.tubi.RLinesProducts;
import com.geowebframework.webclient.model.serverDbEntity.tubi.TubiEsistenti;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RuleHandler {

    private RuleHandler next;
    private final ConfigRule rule;

    public RuleHandler setNext(RuleHandler next) {
        this.next = next;
        return next;
    }

    protected Optional<AssignmentResult> passToNext(UndergroundRoute route) {
        return next != null ? next.handle(route) : Optional.empty();
    }

    public Optional<AssignmentResult> handle(UndergroundRoute route) {
        Set<DuctTube> parentDucts = route.getDuctTubes().stream()
                .filter(rule::matchesParent)
                .collect(Collectors.toSet());
        Set<DuctTube> targetDucts = route.getDuctTubes().stream()
                .filter(rule::matchesTarget)
                .collect(Collectors.toSet());

        if (parentDucts.isEmpty() || targetDucts.isEmpty()) {
            return passToNext(route);
        }

        AssignmentResult myResult = new AssignmentResult();

        parentDucts.forEach(parent -> {
            targetDucts.forEach(target -> processAssignment(parent, target, myResult));
            if (parent.getChildCount() == rule.getMax_duct_number()) {
                parent.setFull(true);
            }
        });
        Optional<AssignmentResult> nextResult = passToNext(route);
        nextResult.ifPresent(myResult::merge);
        return Optional.of(myResult);
    }

    private void processAssignment(DuctTube parent, DuctTube target, AssignmentResult result) {
        target.setProcessedChild(true);
        if (target.is_child()
                || parent.getChildCount() >= rule.getMax_duct_number()
                || parent.isFull()) {
            return;
        }
        target.setParent_id(parent.getId());
        target.set_child(true);
        parent.incrementChildCount();
        addBatchUpdate(target.getId(), parent.getId(), target.is_new(), parent.is_new(), result);
        result.incrementAssigned();
    }

    private void addBatchUpdate(Long id, Long parentId, boolean isNewTarget, boolean isNewParent, AssignmentResult result) {
        String tableName = isNewTarget ? RLinesProducts.S_DB_TABLE_NAME : TubiEsistenti.S_TABLE_NAME;

        RowUpdateData rowData = new RowUpdateData();
        if (isNewTarget && isNewParent) {
            rowData.addValue(RLinesProducts.S_FK_PARENT_NEW_DUCT, parentId);
        } else if (isNewTarget) {
            rowData.addValue(RLinesProducts.S_FK_PARENT_EXI_DUCT, parentId);
        } else if (!isNewParent) {
            rowData.addValue(TubiEsistenti.S_FK_PARENT_EXI_DUCT, parentId);
        }
        rowData.addFilter(isNewTarget ? RLinesProducts.S_PK_COLUMN_NAME : TubiEsistenti.S_PK_COLUMN, id);

        result.getMassiveValueToUpdate()
                .computeIfAbsent(tableName, s -> new ArrayList<>())
                .add(rowData);
    }
}
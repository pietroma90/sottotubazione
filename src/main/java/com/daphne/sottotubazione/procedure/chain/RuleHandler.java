package com.geowebframework.underPiping.procedure.chain;


import com.geowebframework.underPiping.domain.AssignmentResult;
import com.geowebframework.underPiping.domain.ConfigRule;
import com.geowebframework.underPiping.domain.DuctTube;
import com.geowebframework.underPiping.procedure.AssignmentContext;
import com.geowebframework.webclient.model.serverDbEntity.tubi.RLinesProducts;
import com.geowebframework.webclient.model.serverDbEntity.tubi.TubiEsistenti;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handler base per la Chain of Responsibility delle regole di configurazione.
 */
@RequiredArgsConstructor
public class RuleHandler {

    private RuleHandler next;
    private final ConfigRule rule;

    public RuleHandler setNext(RuleHandler next) {
        this.next = next;
        return next;
    }

    protected Optional<AssignmentResult> passToNext(AssignmentContext ctx) {
        return next != null ? next.handle(ctx) : Optional.empty();
    }

    public Optional<AssignmentResult> handle(AssignmentContext ctx) {
        Set<DuctTube> parentDucts = ctx.getTratta().getDuctTubes().stream()
                .filter(rule::matchesParent)
                .collect(Collectors.toSet());
        Set<DuctTube> targetDucts = ctx.getTratta().getDuctTubes().stream()
                .filter(rule::matchesTarget)
                .collect(Collectors.toSet());

        if (parentDucts.isEmpty() || targetDucts.isEmpty()) {
            return passToNext(ctx);
        }

        AssignmentResult myResult = new AssignmentResult();

        parentDucts.forEach(parent -> {
            targetDucts.forEach(target -> processAssignment(parent, target, myResult));
            if (parent.getChildCount() == rule.getMat_duct_max_number_usable()) {
                parent.setFull(true);
            }
        });
        Optional<AssignmentResult> nextResult = passToNext(ctx);
        nextResult.ifPresent(myResult::merge);
        return Optional.of(myResult);
    }

    /**
     * Tenta di assegnare un target a un parent, aggiornando il risultato se l'assegnazione va a buon fine.
     */
    private void processAssignment(DuctTube parent, DuctTube target, AssignmentResult result) {
        target.setProcessedChild(true);
        if (target.is_child()
                || parent.getChildCount() >= rule.getMat_duct_max_number_usable()
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
package com.geowebframework.sottotubazione.procedure.chain;

import com.geowebframework.sottotubazione.RowUpdateData;
import com.geowebframework.sottotubazione.domain.AssignmentResult;
import com.geowebframework.sottotubazione.domain.ConfigRule;
import com.geowebframework.sottotubazione.domain.DuctTube;
import com.geowebframework.sottotubazione.procedure.AssignmentContext;
import com.geowebframework.webclient.model.serverDbEntity.tubi.RLinesProducts;
import com.geowebframework.webclient.model.serverDbEntity.tubi.TubiEsistenti;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Handler concreto: verifica se la regola di configurazione
 * è applicabile al contesto (parent + target) e gestisce l'assegnazione.
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigRuleHandler extends RuleHandler {

    private final ConfigRule rule;


    @Override
    public Optional<AssignmentResult> handle(AssignmentContext ctx) {
        Set<DuctTube> parentDuct = ctx.getDuctTube().stream().filter(rule::matchesParent).collect(Collectors.toSet());
        Set<DuctTube> targetDuct = ctx.getDuctTube().stream().filter(rule::matchesTarget).collect(Collectors.toSet());


        if (parentDuct.isEmpty() || targetDuct.isEmpty()) {
            return passToNext(ctx);
        }
        parentDuct.forEach(parentDuctItem -> {
            if (parentDuctItem.getChildCount() == rule.getMat_duct_max_number_usable())
                return;
            AtomicInteger processedRemaining = new AtomicInteger(targetDuct.size());
            while (processedRemaining.intValue() > 0) {
                targetDuct.forEach(targetDuctItem -> {
                    if (targetDuctItem.is_child())
                        return;
                    if (parentDuctItem.getChildCount() == rule.getMat_duct_max_number_usable()) {
                        ctx.getMessage().addToWarning("Per la seguente tratta (" + ctx.getTratta().getPk_prj_lines_trenches() + "), " +
                                "all'interno dell'elemento (" + parentDuctItem.getId() + ", " + parentDuctItem.getShort_desc_name() + ") " +
                                "non è stato possibile sotto-tubare il seguente elemento  (" + targetDuctItem.getId() + ", " + targetDuctItem.getShort_desc_name() + ").");
                            return;
                            }
                    targetDuctItem.setParent_id(parentDuctItem.getId());
                    targetDuctItem.set_child(true);
                    parentDuctItem.incrementChildCount();
                    processedRemaining.decrementAndGet();
                    addBatchUpdate(targetDuctItem.getId(), parentDuctItem.getId(), targetDuctItem.is_new(), parentDuctItem.is_new(), ctx);
                });
                processedRemaining.set(0);
            }
        });
        return passToNext(ctx);
    }


    public void addBatchUpdate(Long id, Long parent_id, boolean isNewTarget, boolean isNewParent, AssignmentContext context) {
        String tableName;
        if (isNewTarget)
            tableName = RLinesProducts.S_DB_TABLE_NAME;
        else tableName = TubiEsistenti.S_TABLE_NAME;

        context.getMassiveValueToUpdate().computeIfAbsent(tableName, s -> new ArrayList<>());

        RowUpdateData rowData = new RowUpdateData();

        if (isNewTarget && isNewParent) {
            rowData.addValue(RLinesProducts.S_FK_PARENT_NEW_DUCT, parent_id);
        }
        if (isNewTarget && !isNewParent) {
            rowData.addValue(RLinesProducts.S_FK_PARENT_EXI_DUCT, parent_id);
        }
        if (!isNewTarget && !isNewParent) {
            rowData.addValue(TubiEsistenti.S_FK_PARENT_EXI_DUCT, parent_id);
        }

        rowData.addFilter(isNewTarget ? RLinesProducts.S_PK_COLUMN_NAME : TubiEsistenti.S_PK_COLUMN, id);

        context.getMassiveValueToUpdate().get(tableName).add(rowData);
    }
}

package com.geowebframework.sottotubazione.procedure.chain.strategy;

import com.geowebframework.sottotubazione.RowUpdateData;
import com.geowebframework.sottotubazione.procedure.chain.DuctUpdateStrategy;
import com.geowebframework.webclient.model.serverDbEntity.tubi.RLinesProducts;
import org.springframework.stereotype.Component;

/**
 * Strategy: target nuovo, parent esistente → aggiorna FK_PARENT_EXI_DUCT su r_lines_products.
 */
@Component
public class NewTargetExistingParentStrategy implements DuctUpdateStrategy {

    @Override
    public boolean matches(boolean isNewTarget, boolean isNewParent) {
        return isNewTarget && !isNewParent;
    }

    @Override
    public String getTableName() {
        return RLinesProducts.S_DB_TABLE_NAME;
    }

    @Override
    public RowUpdateData buildUpdate(Long targetId, Long parentId) {
        return RowUpdateData.of(RLinesProducts.S_PK_COLUMN_NAME, targetId)
                .set(RLinesProducts.S_FK_PARENT_EXI_DUCT, parentId);
    }
}

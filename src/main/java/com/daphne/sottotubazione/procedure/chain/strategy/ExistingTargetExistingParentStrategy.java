package com.geowebframework.sottotubazione.procedure.chain.strategy;

import com.geowebframework.sottotubazione.RowUpdateData;
import com.geowebframework.sottotubazione.procedure.chain.DuctUpdateStrategy;
import com.geowebframework.webclient.model.serverDbEntity.tubi.TubiEsistenti;
import org.springframework.stereotype.Component;

/**
 * Strategy: target esistente, parent esistente → aggiorna FK_PARENT_EXI_DUCT su tubi_esistenti.
 */
@Component
public class ExistingTargetExistingParentStrategy implements DuctUpdateStrategy {

    @Override
    public boolean matches(boolean isNewTarget, boolean isNewParent) {
        return !isNewTarget && !isNewParent;
    }

    @Override
    public String getTableName() {
        return TubiEsistenti.S_TABLE_NAME;
    }

    @Override
    public RowUpdateData buildUpdate(Long targetId, Long parentId) {
        return RowUpdateData.of(TubiEsistenti.S_PK_COLUMN, targetId)
                .set(TubiEsistenti.S_FK_PARENT_EXI_DUCT, parentId);
    }
}

package com.geowebframework.sottotubazione.procedure.chain;

import com.geowebframework.sottotubazione.RowUpdateData;

/**
 * Strategy per la costruzione del RowUpdateData in base alla combinazione
 * target (nuovo/esistente) e parent (nuovo/esistente).
 * Separa la logica di persistenza dall'handler della catena (SRP).
 */
public interface DuctUpdateStrategy {

    /**
     * Costruisce il RowUpdateData da accodare nel batch update.
     *
     * @param targetId ID del tubo target
     * @param parentId ID del tubo parent
     * @return RowUpdateData pronto per il batch
     */
    RowUpdateData buildUpdate(Long targetId, Long parentId);

    /**
     * Nome della tabella su cui eseguire l'update.
     */
    String getTableName();

    /**
     * Indica se questa strategy e' applicabile alla combinazione isNewTarget/isNewParent.
     */
    boolean matches(boolean isNewTarget, boolean isNewParent);
}

package com.geowebframework.sottotubazione.procedure;

import com.geowebframework.sottotubazione.RowUpdateData;
import com.geowebframework.sottotubazione.domain.UndergroundRoute;
import com.geowebframework.sottotubazione.domain.DuctTube;
import it.eagleprojects.gisfocommons.utils.Message;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Oggetto di contesto passato lungo la Chain of Responsibility.
 * Evita il parameter drilling tra i vari handler.
 */
@Data
@Builder
public class AssignmentContext {
    private UndergroundRoute tratta;
    private Set<DuctTube> ductTube;
    private Long projectId;
    private HashMap<String, List<RowUpdateData>> massiveValueToUpdate;
    @Builder.Default
    private Message message = new Message();
}

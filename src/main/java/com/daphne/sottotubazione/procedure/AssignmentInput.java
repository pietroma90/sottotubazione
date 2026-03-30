package com.geowebframework.sottotubazione.procedure;

import com.geowebframework.sottotubazione.domain.ConfigRule;
import com.geowebframework.sottotubazione.domain.DuctTube;
import com.geowebframework.sottotubazione.domain.UndergroundRoute;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

/**
 * Input immutabile passato lungo la Chain of Responsibility.
 * Contiene tutti i dati di sola lettura necessari agli handler.
 * Separato da AssignmentOutput per rispettare SRP.
 */
@Value
@Builder
public class AssignmentInput {
    UndergroundRoute tratta;
    Set<DuctTube> ductTubes;
    Long projectId;
    List<ConfigRule> rules;
}

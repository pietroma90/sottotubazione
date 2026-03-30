package com.geowebframework.sottotubazione.procedure;

import lombok.Value;

/**
 * Oggetto di contesto passato lungo la Chain of Responsibility.
 * Aggrega input (immutabile) e output (accumulabile) separando le responsabilita'.
 */
@Value
public class AssignmentContext {
    AssignmentInput input;
    AssignmentOutput output;

    public static AssignmentContext of(AssignmentInput input) {
        return new AssignmentContext(input, new AssignmentOutput());
    }
}

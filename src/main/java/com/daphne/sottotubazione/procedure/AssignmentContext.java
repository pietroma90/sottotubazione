package com.daphne.sottotubazione.procedure;

import com.daphne.sottotubazione.domain.TrattaInterrata;
import com.daphne.sottotubazione.domain.TuboParent;
import com.daphne.sottotubazione.domain.TuboTarget;
import lombok.Builder;
import lombok.Data;

/**
 * Oggetto di contesto passato lungo la Chain of Responsibility.
 * Evita il parameter drilling tra i vari handler.
 */
@Data
@Builder
public class AssignmentContext {
    private TrattaInterrata tratta;
    private TuboParent parent;
    private TuboTarget target;
    private Long projectId;
}

package com.geowebframework.sottotubazione.procedure.chain;

import com.geowebframework.sottotubazione.domain.AssignmentResult;
import com.geowebframework.sottotubazione.procedure.AssignmentContext;

import java.util.Optional;

/**
 * Handler base per la Chain of Responsibility delle regole di configurazione.
 * Ogni handler processa il contesto e passa al successivo,
 * aggregando il proprio AssignmentResult con quello restituito dalla catena.
 */
public abstract class RuleHandler {

    private RuleHandler next;

    public RuleHandler setNext(RuleHandler next) {
        this.next = next;
        return next;
    }

    public abstract Optional<AssignmentResult> handle(AssignmentContext ctx);

    /**
     * Passa il contesto al prossimo handler e restituisce il risultato aggregato.
     * Il chiamante e' responsabile di fare merge del proprio risultato
     * con quello restituito da questo metodo.
     */
    protected Optional<AssignmentResult> passToNext(AssignmentContext ctx) {
        return next != null ? next.handle(ctx) : Optional.empty();
    }
}

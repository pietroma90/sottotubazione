package com.daphne.sottotubazione.procedure.chain;

import com.daphne.sottotubazione.domain.AssignmentResult;
import com.daphne.sottotubazione.procedure.AssignmentContext;

import java.util.Optional;

/**
 * Handler base per la Chain of Responsibility delle regole di configurazione.
 */
public abstract class RuleHandler {

    private RuleHandler next;

    public RuleHandler setNext(RuleHandler next) {
        this.next = next;
        return next;
    }

    public abstract Optional<AssignmentResult> handle(AssignmentContext ctx);

    protected Optional<AssignmentResult> passToNext(AssignmentContext ctx) {
        return next != null ? next.handle(ctx) : Optional.empty();
    }
}

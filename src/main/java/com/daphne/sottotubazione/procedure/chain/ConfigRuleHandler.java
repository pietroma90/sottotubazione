package com.daphne.sottotubazione.procedure.chain;

import com.daphne.sottotubazione.domain.AssignmentResult;
import com.daphne.sottotubazione.domain.ConfigRule;
import com.daphne.sottotubazione.procedure.AssignmentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

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
        if (!rule.appliesTo(ctx.getTratta())) return passToNext(ctx);
        if (!rule.matchesParent(ctx.getParent()) || !rule.matchesTarget(ctx.getTarget())) return passToNext(ctx);

        int currentCount = ctx.getParent().getCurrentAssignedCount();
        int maxAllowed   = rule.getMatDuctMaxNumberUsable();

        AssignmentResult result = AssignmentResult.builder()
                .fkTratta(ctx.getTratta().getPkLinesTrenches())
                .build();

        if (currentCount < maxAllowed) {
            log.debug("Regola #{} applicata: parent {} -> target {}",
                rule.getId(), ctx.getParent().getPkLinesProducts(), ctx.getTarget().getPkLinesProducts());
            result.setAssigned(true);
            result.setParentFull(currentCount + 1 >= maxAllowed);
            result.setAssignedCount(1);
        } else {
            log.debug("Regola #{}: parent {} gia' pieno (max {})",
                rule.getId(), ctx.getParent().getPkLinesProducts(), maxAllowed);
            result.setAssigned(false);
            result.setParentFull(true);
        }
        return Optional.of(result);
    }
}

package com.daphne.sottotubazione.procedure.chain;

import com.daphne.sottotubazione.domain.ConfigRule;
import com.daphne.sottotubazione.domain.TrattaInterrata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Costruisce la catena di RuleHandler a partire dalle ConfigRule ordinate per priority_rules_order.
 */
@Component
public class RuleChainBuilder {

    public Optional<RuleHandler> build(List<ConfigRule> rules, TrattaInterrata tratta) {
        List<RuleHandler> handlers = rules.stream()
            .filter(r -> !r.isDeleted() && r.appliesTo(tratta))
            .sorted((a, b) -> Integer.compare(a.getPriorityRulesOrder(), b.getPriorityRulesOrder()))
            .map(ConfigRuleHandler::new)
            .map(h -> (RuleHandler) h)
            .toList();

        if (handlers.isEmpty()) return Optional.empty();

        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
        return Optional.of(handlers.get(0));
    }
}

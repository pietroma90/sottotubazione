package com.geowebframework.sottotubazione.procedure.chain;

import com.geowebframework.sottotubazione.domain.ConfigRule;
import com.geowebframework.sottotubazione.domain.UndergroundRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Costruisce la catena di RuleHandler a partire dalle ConfigRule ordinate per priority_rules_order.
 * Inietta le DuctUpdateStrategy disponibili in ogni ConfigRuleHandler (Strategy pattern).
 */
@Component
@RequiredArgsConstructor
public class RuleChainBuilder {

    private final List<DuctUpdateStrategy> updateStrategies;

    public Optional<RuleHandler> build(List<ConfigRule> rules, UndergroundRoute tratta) {
        List<RuleHandler> handlers = rules.stream()
                .filter(r -> !r.is_deleted() && r.appliesTo(tratta))
                .sorted(Comparator.comparingInt(ConfigRule::getPriority_rules_order))
                .map(rule -> (RuleHandler) new ConfigRuleHandler(rule, updateStrategies))
                .collect(Collectors.toList());

        if (handlers.isEmpty()) return Optional.empty();

        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
        return Optional.of(handlers.get(0));
    }
}

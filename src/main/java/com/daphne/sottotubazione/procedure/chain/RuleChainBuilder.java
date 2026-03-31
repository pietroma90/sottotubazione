package com.geowebframework.underPiping.procedure.chain;

import com.geowebframework.underPiping.domain.ConfigRule;
import com.geowebframework.underPiping.domain.UndergroundRoute;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Costruisce la catena di RuleHandler a partire dalle ConfigRule ordinate per priority_rules_order.
 */
@Component
public class RuleChainBuilder {

    public Optional<RuleHandler> build(List<ConfigRule> rules, UndergroundRoute tratta) {
        List<RuleHandler> handlers = rules.stream()
                .filter(r -> !r.is_deleted() && r.appliesTo(tratta))
                .sorted(Comparator.comparingInt(ConfigRule::getPriority_rules_order))
                .map(RuleHandler::new).collect(Collectors.toList());

        if (handlers.isEmpty()) return Optional.empty();
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
        return Optional.of(handlers.get(0));
    }
}

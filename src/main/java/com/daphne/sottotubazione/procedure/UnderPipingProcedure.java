package com.geowebframework.underPiping.procedure;

import com.geowebframework.underPiping.model.AssignmentResult;
import com.geowebframework.underPiping.model.ConfigRule;
import com.geowebframework.underPiping.model.UndergroundRoute;
import com.geowebframework.underPiping.procedure.chain.RuleChainBuilder;
import com.geowebframework.underPiping.procedure.chain.RuleHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UnderPipingProcedure {

    protected final RuleChainBuilder ruleChainBuilder;

    public Optional<AssignmentResult> execute(UndergroundRoute tratta, List<ConfigRule> rules) {
        Optional<RuleHandler> chainHead = ruleChainBuilder.build(rules, tratta);
        if (!chainHead.isPresent()) {
            return Optional.empty();
        }
        AssignmentContext ctx = AssignmentContext.builder()
                .tratta(tratta)
                .build();
        return chainHead.get().handle(ctx);
    }
}
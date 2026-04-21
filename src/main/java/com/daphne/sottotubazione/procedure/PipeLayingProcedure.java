package com.geowebframework.pipeLaying.procedure;

import com.geowebframework.pipeLaying.model.AssignmentResult;
import com.geowebframework.pipeLaying.model.ConfigRule;
import com.geowebframework.pipeLaying.model.UndergroundRoute;
import com.geowebframework.pipeLaying.procedure.chain.RuleChainBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PipeLayingProcedure {

    protected final RuleChainBuilder ruleChainBuilder;

    public Optional<AssignmentResult> execute(UndergroundRoute tratta, List<ConfigRule> rules) {
        return ruleChainBuilder.build(rules, tratta)
                .flatMap(head -> head.handle(tratta));
    }
}
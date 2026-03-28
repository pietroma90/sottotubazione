package com.geowebframework.sottotubazione.procedure;

import com.geowebframework.sottotubazione.RowUpdateData;
import com.geowebframework.sottotubazione.domain.*;
import com.geowebframework.sottotubazione.procedure.chain.RuleChainBuilder;
import com.geowebframework.sottotubazione.procedure.chain.RuleHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Template Method: definisce lo scheletro dell'algoritmo di sotto-tubazione.
 * I metodi loadParents() e loadTargets() sono gli hook differenti tra le Strategy.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SottotubazioneProcedure {

    protected final RuleChainBuilder ruleChainBuilder;

    public void execute(UndergroundRoute tratta, List<ConfigRule> rules, Long projectId, HashMap<String, List<RowUpdateData>> massiveValueToUpdate) {
        Optional<RuleHandler> chainHead = ruleChainBuilder.build(rules, tratta);
        if (!chainHead.isPresent()) {
            log.info("Nessuna regola applicabile per tratta {}", tratta.getPk_prj_lines_trenches());
            return;
        }
        AssignmentContext ctx = AssignmentContext.builder()
                .tratta(tratta)
                .ductTube(tratta.getDuctTubes())
                .projectId(projectId)
                .massiveValueToUpdate(massiveValueToUpdate)
                .build();
        chainHead.get().handle(ctx);
    }
}

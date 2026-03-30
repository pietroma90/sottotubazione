package com.geowebframework.sottotubazione.procedure;

import com.geowebframework.sottotubazione.RowUpdateData;
import com.geowebframework.sottotubazione.domain.*;
import com.geowebframework.sottotubazione.procedure.chain.RuleChainBuilder;
import com.geowebframework.sottotubazione.procedure.chain.RuleHandler;
import it.eagleprojects.gisfocommons.utils.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Template Method: definisce lo scheletro dell'algoritmo di sotto-tubazione.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SottotubazioneProcedure {

    protected final RuleChainBuilder ruleChainBuilder;

    public AssignmentResult execute(UndergroundRoute tratta, List<ConfigRule> rules, Long projectId,
                                    HashMap<String, List<RowUpdateData>> massiveValueToUpdate, Message message) {
        Optional<RuleHandler> chainHead = ruleChainBuilder.build(rules, tratta);
        if (!chainHead.isPresent()) {
            log.info("Nessuna regola applicabile per tratta {}", tratta.getPk_prj_lines_trenches());
            return null;
        }
        AssignmentContext ctx = AssignmentContext.builder()
                .tratta(tratta)
                .ductTube(tratta.getDuctTubes())
                .projectId(projectId)
                .massiveValueToUpdate(massiveValueToUpdate)
                .message(message)
                .build();
        return chainHead.get().handle(ctx).orElse(null);
    }
}

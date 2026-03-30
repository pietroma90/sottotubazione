package com.geowebframework.sottotubazione.procedure;

import com.geowebframework.sottotubazione.domain.AssignmentResult;
import com.geowebframework.sottotubazione.domain.ConfigRule;
import com.geowebframework.sottotubazione.domain.DuctTube;
import com.geowebframework.sottotubazione.domain.UndergroundRoute;
import com.geowebframework.sottotubazione.procedure.chain.RuleChainBuilder;
import com.geowebframework.sottotubazione.procedure.chain.RuleHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Esegue la procedura di sotto-tubazione per una singola UndergroundRoute.
 * Costruisce la Chain of Responsibility tramite RuleChainBuilder,
 * crea il contesto (input immutabile + output accumulabile) e
 * restituisce l'AssignmentResult aggregato da tutta la catena.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SottotubazioneProcedure {

    private final RuleChainBuilder ruleChainBuilder;

    public Optional<AssignmentResult> execute(UndergroundRoute tratta, List<ConfigRule> rules, Long projectId) {
        Optional<RuleHandler> chainHead = ruleChainBuilder.build(rules, tratta);
        if (chainHead.isEmpty()) {
            log.info("Nessuna regola applicabile per tratta {}", tratta.getPk_prj_lines_trenches());
            return Optional.empty();
        }

        Set<DuctTube> ductTubes = tratta.getDuctTubes() != null
                ? tratta.getDuctTubes()
                : new HashSet<>();

        AssignmentInput input = AssignmentInput.builder()
                .tratta(tratta)
                .ductTubes(ductTubes)
                .projectId(projectId)
                .rules(rules)
                .build();

        AssignmentContext ctx = AssignmentContext.of(input);
        Optional<AssignmentResult> result = chainHead.get().handle(ctx);

        // Propaga il batch di update nel result per il service
        result.ifPresent(r -> r.setBatchUpdates(ctx.getOutput().getBatchUpdates()));
        return result;
    }
}

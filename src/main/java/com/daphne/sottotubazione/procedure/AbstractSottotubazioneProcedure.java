package com.daphne.sottotubazione.procedure;

import com.daphne.sottotubazione.domain.*;
import com.daphne.sottotubazione.domain.enums.TuboStatus;
import com.daphne.sottotubazione.procedure.chain.RuleChainBuilder;
import com.daphne.sottotubazione.procedure.chain.RuleHandler;
import com.daphne.sottotubazione.repository.TuboRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Template Method: definisce lo scheletro dell'algoritmo di sotto-tubazione.
 * I metodi loadParents() e loadTargets() sono gli hook differenti tra le Strategy.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSottotubazioneProcedure implements SottotubazioneProcedure {

    protected final TuboRepository tuboRepository;
    protected final RuleChainBuilder ruleChainBuilder;

    @Override
    public AssignmentResult execute(TrattaInterrata tratta, List<ConfigRule> rules, Long projectId) {
        AssignmentResult result = AssignmentResult.builder()
                .fkTratta(tratta.getPkLinesTrenches())
                .build();

        Optional<RuleHandler> chainHead = ruleChainBuilder.build(rules, tratta);
        if (chainHead.isEmpty()) {
            log.info("Nessuna regola applicabile per tratta {}", tratta.getPkLinesTrenches());
            return result;
        }

        List<TuboParent> parents = loadParents(tratta, rules);
        Set<Long> processedParentIds = new HashSet<>();

        for (TuboParent parent : parents) {
            if (processedParentIds.contains(parent.getPkLinesProducts())) continue;
            if (parent.isOccupato()) continue;

            List<TuboTarget> targets = loadTargets(tratta, rules);
            Set<Long> processedTargetIds = new HashSet<>();

            for (TuboTarget target : targets) {
                if (processedTargetIds.contains(target.getPkLinesProducts())) continue;
                if (target.isGiaAssegnato()) continue;

                AssignmentContext ctx = AssignmentContext.builder()
                        .tratta(tratta)
                        .parent(parent)
                        .target(target)
                        .projectId(projectId)
                        .build();

                Optional<AssignmentResult> handlerResult = chainHead.get().handle(ctx);
                handlerResult.ifPresent(r -> {
                    result.merge(r);
                    if (r.isAssigned()) {
                        processedTargetIds.add(target.getPkLinesProducts());
                        parent.setCurrentAssignedCount(parent.getCurrentAssignedCount() + 1);
                        tuboRepository.updateParentStatus(
                            parent.getPkLinesProducts(),
                            TuboStatus.SOTTOTUBATO.name(),
                            parent.getCurrentAssignedCount()
                        );
                        target.setStatus(TuboStatus.ASSEGNATO);
                    } else {
                        result.addLog(buildWarningLog(tratta, parent, target, projectId));
                        result.setSkippedCount(result.getSkippedCount() + 1);
                    }
                    if (r.isParentFull()) {
                        processedParentIds.add(parent.getPkLinesProducts());
                        parent.setStatus(TuboStatus.OCCUPATO);
                        tuboRepository.updateParentStatus(
                            parent.getPkLinesProducts(),
                            TuboStatus.OCCUPATO.name(),
                            parent.getCurrentAssignedCount()
                        );
                    }
                });
            }
        }
        return result;
    }

    protected abstract List<TuboParent> loadParents(TrattaInterrata tratta, List<ConfigRule> rules);
    protected abstract List<TuboTarget> loadTargets(TrattaInterrata tratta, List<ConfigRule> rules);

    private ProcedureOutput buildWarningLog(TrattaInterrata tratta, TuboParent parent,
                                             TuboTarget target, Long projectId) {
        return ProcedureOutput.builder()
                .entityTableName("projects")
                .fkEntity(projectId)
                .fkTratta(tratta.getPkLinesTrenches())
                .pkParent(parent.getPkLinesProducts())
                .parentDescr(parent.getShortDescript())
                .pkTarget(target.getPkLinesProducts())
                .targetDescr(target.getShortDescript())
                .message(String.format(
                    "Per la tratta (%s), all'interno dell'elemento (%s, %s) " +
                    "non e' stato possibile sotto-tubare il seguente elemento (%s, %s).",
                    tratta.getPkLinesTrenches(),
                    parent.getPkLinesProducts(), parent.getShortDescript(),
                    target.getPkLinesProducts(), target.getShortDescript()
                ))
                .build();
    }
}

package com.geowebframework.sottotubazione.procedure.chain;

import com.geowebframework.sottotubazione.domain.AssignmentResult;
import com.geowebframework.sottotubazione.domain.ConfigRule;
import com.geowebframework.sottotubazione.domain.DuctTube;
import com.geowebframework.sottotubazione.domain.ProcedureOutput;
import com.geowebframework.sottotubazione.procedure.AssignmentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handler concreto della Chain of Responsibility.
 * Verifica se la regola e' applicabile al contesto e gestisce l'assegnazione.
 *
 * Delega la logica di persistenza a DuctUpdateStrategy (SRP).
 * Accumula il proprio AssignmentResult e lo aggrega con il risultato
 * del resto della catena prima di restituirlo (CoR corretto).
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigRuleHandler extends RuleHandler {

    private final ConfigRule rule;
    private final List<DuctUpdateStrategy> updateStrategies;

    @Override
    public Optional<AssignmentResult> handle(AssignmentContext ctx) {
        Set<DuctTube> parentDucts = ctx.getInput().getDuctTubes().stream()
                .filter(rule::matchesParent)
                .collect(Collectors.toSet());
        Set<DuctTube> targetDucts = ctx.getInput().getDuctTubes().stream()
                .filter(rule::matchesTarget)
                .collect(Collectors.toSet());

        if (parentDucts.isEmpty() || targetDucts.isEmpty()) {
            return passToNext(ctx);
        }

        AssignmentResult myResult = AssignmentResult.builder()
                .fkTratta(ctx.getInput().getTratta().getPk_prj_lines_trenches())
                .build();

        for (DuctTube parent : parentDucts) {
            for (DuctTube target : targetDucts) {
                if (target.is_child()) continue;

                if (parent.getChildCount() >= rule.getMat_duct_max_number_usable()) {
                    ctx.getOutput().addWarning(
                            "Per la tratta (" + ctx.getInput().getTratta().getPk_prj_lines_trenches() + "), " +
                            "l'elemento parent (" + parent.getId() + ", " + parent.getShort_desc_name() + ") " +
                            "ha raggiunto il limite massimo: impossibile sotto-tubare (" +
                            target.getId() + ", " + target.getShort_desc_name() + ")."
                    );
                    myResult.addLog(ProcedureOutput.builder()
                            .fkTratta(ctx.getInput().getTratta().getPk_prj_lines_trenches())
                            .pkParent(parent.getId())
                            .parentDescr(parent.getShort_desc_name())
                            .pkTarget(target.getId())
                            .targetDescr(target.getShort_desc_name())
                            .message("Limite massimo raggiunto")
                            .build());
                    myResult.incrementSkipped();
                    continue;
                }

                // Aggiorna stato in memoria
                target.setParent_id(parent.getId());
                target.set_child(true);
                parent.incrementChildCount();

                // Delega la costruzione del batch update alla strategy corretta
                updateStrategies.stream()
                        .filter(s -> s.matches(target.is_new(), parent.is_new()))
                        .findFirst()
                        .ifPresent(strategy -> ctx.getOutput().addBatchRow(
                                strategy.getTableName(),
                                strategy.buildUpdate(target.getId(), parent.getId())
                        ));

                myResult.incrementAssigned();
            }
        }

        // Accumulo CoR corretto: merge del mio risultato con quello della catena successiva
        Optional<AssignmentResult> nextResult = passToNext(ctx);
        nextResult.ifPresent(myResult::merge);
        return Optional.of(myResult);
    }
}

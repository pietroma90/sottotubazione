package com.daphne.sottotubazione.procedure;

import com.daphne.sottotubazione.domain.ConfigRule;
import com.daphne.sottotubazione.domain.TrattaInterrata;
import com.daphne.sottotubazione.domain.TuboParent;
import com.daphne.sottotubazione.domain.TuboTarget;
import com.daphne.sottotubazione.procedure.chain.RuleChainBuilder;
import com.daphne.sottotubazione.repository.TuboRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy per tratte di SCAVO NUOVO.
 * Parent: TUBI NUOVI SU TRATTA (P.T. partenza).
 * Target: NUOVI (P.T. target).
 */
@Component
public class NuovoScavoStrategy extends AbstractSottotubazioneProcedure {

    public NuovoScavoStrategy(TuboRepository tuboRepository, RuleChainBuilder ruleChainBuilder) {
        super(tuboRepository, ruleChainBuilder);
    }

    @Override
    protected List<TuboParent> loadParents(TrattaInterrata tratta, List<ConfigRule> rules) {
        return tuboRepository.findNuoviNonOccupatiByTratta(tratta.getPkLinesTrenches());
    }

    @Override
    protected List<TuboTarget> loadTargets(TrattaInterrata tratta, List<ConfigRule> rules) {
        return rules.stream()
            .filter(r -> !r.isDeleted() && r.appliesTo(tratta) && r.getFkMatDuctTarget() != null)
            .flatMap(r -> tuboRepository
                .findTargetNuoviByTrattaAndMat(tratta.getPkLinesTrenches(), r.getFkMatDuctTarget())
                .stream())
            .distinct()
            .toList();
    }
}

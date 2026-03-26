package com.daphne.sottotubazione.procedure;

import com.daphne.sottotubazione.domain.ConfigRule;
import com.daphne.sottotubazione.domain.TrattaInterrata;
import com.daphne.sottotubazione.domain.TuboParent;
import com.daphne.sottotubazione.domain.TuboTarget;
import com.daphne.sottotubazione.procedure.chain.RuleChainBuilder;
import com.daphne.sottotubazione.repository.TuboRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy per tratte ESISTENTE INTERRATO.
 * Parent: TUBI ESISTENTI NON OCCUPATI (P.T. partenza), filtrati per diametro.
 * Target: ESISTENTI (P.T. target) o NUOVI (P.T. target).
 */
@Component
public class EsistenteInterratoStrategy extends AbstractSottotubazioneProcedure {

    public EsistenteInterratoStrategy(TuboRepository tuboRepository, RuleChainBuilder ruleChainBuilder) {
        super(tuboRepository, ruleChainBuilder);
    }

    @Override
    protected List<TuboParent> loadParents(TrattaInterrata tratta, List<ConfigRule> rules) {
        List<TuboParent> parents = new ArrayList<>();
        for (ConfigRule rule : rules) {
            if (rule.isDeleted() || !rule.appliesTo(tratta)) continue;
            parents.addAll(tuboRepository.findEsistentiNonOccupatiByTratta(
                tratta.getPkLinesTrenches(),
                rule.getTubiEsistentiExtMaxDiamParent(),
                rule.getTubiEsistentiExtMinDiamParent()
            ));
        }
        return parents.stream().distinct().toList();
    }

    @Override
    protected List<TuboTarget> loadTargets(TrattaInterrata tratta, List<ConfigRule> rules) {
        List<TuboTarget> targets = new ArrayList<>();
        for (ConfigRule rule : rules) {
            if (rule.isDeleted() || !rule.appliesTo(tratta) || rule.getFkMatDuctTarget() == null) continue;
            targets.addAll(tuboRepository.findTargetNuoviByTrattaAndMat(
                tratta.getPkLinesTrenches(), rule.getFkMatDuctTarget()
            ));
        }
        return targets.stream().distinct().toList();
    }
}

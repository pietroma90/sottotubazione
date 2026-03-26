package com.daphne.sottotubazione.service;

import com.daphne.sottotubazione.domain.*;
import com.daphne.sottotubazione.procedure.SottotubazioneProcedure;
import com.daphne.sottotubazione.procedure.SottotubazioneProcedureFactory;
import com.daphne.sottotubazione.repository.ConfigRuleRepository;
import com.daphne.sottotubazione.repository.ProcedureOutputRepository;
import com.daphne.sottotubazione.repository.TrattaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orchestratore principale della procedura di sotto-tubazione automatica.
 * @Transactional garantisce rollback completo in caso di errore.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SottotubazioneProcedureService {

    private final TrattaRepository trattaRepository;
    private final ConfigRuleRepository configRuleRepository;
    private final ProcedureOutputRepository procedureOutputRepository;
    private final SottotubazioneProcedureFactory strategyFactory;
    private final ProcedureLogService logService;

    @Transactional
    public ProcedureResult lanciaPerProgetto(Long projectId) {
        log.info("Avvio procedura sotto-tubazione per progetto {}", projectId);

        List<TrattaInterrata> tratte = trattaRepository.findInterrateByProject(projectId);
        List<ConfigRule> rules       = configRuleRepository.findActiveOrderedByPriority();

        if (rules.isEmpty()) {
            log.warn("Nessuna regola attiva. Procedura terminata.");
            return new ProcedureResult();
        }

        ProcedureResult globalResult = new ProcedureResult();

        for (TrattaInterrata tratta : tratte) {
            log.debug("Elaborazione tratta {} ({})", tratta.getPkLinesTrenches(), tratta.getType());
            SottotubazioneProcedure strategy = strategyFactory.getStrategy(tratta);
            AssignmentResult assignmentResult = strategy.execute(tratta, rules, projectId);
            globalResult.merge(assignmentResult);
        }

        logService.saveAll(globalResult.getAllLogs(), projectId);

        log.info("Procedura completata. Assegnati: {}, Skippati: {}",
            globalResult.getTotalAssigned(), globalResult.getTotalSkipped());

        return globalResult;
    }

    public List<ProcedureOutput> getLogs(Long projectId) {
        return logService.findByProject(projectId);
    }
}

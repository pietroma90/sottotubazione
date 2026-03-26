package com.daphne.sottotubazione.service;

import com.daphne.sottotubazione.domain.ProcedureOutput;
import com.daphne.sottotubazione.repository.ProcedureOutputRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gestisce la scrittura e la lettura dei log su logging.procedure_output.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcedureLogService {

    private final ProcedureOutputRepository repository;

    public void saveAll(List<ProcedureOutput> logs, Long projectId) {
        if (logs.isEmpty()) return;
        logs.forEach(l -> {
            l.setEntityTableName("projects");
            l.setFkEntity(projectId);
        });
        repository.saveAll(logs);
        log.info("Salvati {} log per progetto {}", logs.size(), projectId);
    }

    public List<ProcedureOutput> findByProject(Long projectId) {
        return repository.findByFkEntityOrderByCreatedAtDesc(projectId);
    }
}

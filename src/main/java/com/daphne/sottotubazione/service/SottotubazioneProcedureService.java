package com.geowebframework.sottotubazione.service;

import com.geowebframework.sottotubazione.domain.AssignmentResult;
import com.geowebframework.sottotubazione.domain.ConfigRule;
import com.geowebframework.sottotubazione.domain.DuctTube;
import com.geowebframework.sottotubazione.domain.ProcedureResult;
import com.geowebframework.sottotubazione.domain.UndergroundRoute;
import com.geowebframework.sottotubazione.procedure.SottotubazioneProcedure;
import com.geowebframework.sottotubazione.repository.DuctCommandMapper;
import com.geowebframework.sottotubazione.repository.DuctQueryMapper;
import com.geowebframework.sottotubazione.repository.MapperConfigRule;
import com.geowebframework.sottotubazione.repository.TableNameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Orchestratore principale della procedura di sotto-tubazione automatica.
 * Usa DuctQueryMapper (letture) e DuctCommandMapper (scritture) separati (CQRS).
 *
 * @Transactional garantisce rollback completo in caso di errore.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SottotubazioneProcedureService {

    private final MapperConfigRule mapperConfigRule;
    private final DuctQueryMapper ductQueryMapper;
    private final DuctCommandMapper ductCommandMapper;
    private final TableNameValidator tableNameValidator;
    private final SottotubazioneProcedure sottotubazioneProcedure;

    @Transactional(rollbackFor = Exception.class)
    public ProcedureResult lanciaPerProgetto(Long projectId) {
        log.info("Avvio procedura sotto-tubazione per progetto {}", projectId);
        ProcedureResult globalResult = new ProcedureResult();

        List<ConfigRule> rules = mapperConfigRule.findActiveOrderedByPriority();
        if (rules.isEmpty()) {
            log.warn("Nessuna regola attiva. Procedura terminata.");
            return globalResult;
        }

        List<UndergroundRoute> routes = ductQueryMapper.findRoutesByDrawing(projectId);
        List<DuctTube> ductTubes = ductQueryMapper.findDuctsByDrawing(projectId);
        if (routes.isEmpty() || ductTubes.isEmpty()) {
            return globalResult;
        }

        // Associazione O(n+m): raggruppa i DuctTube per tratta con una Map
        Map<Long, List<DuctTube>> ductsByTratta = ductTubes.stream()
                .collect(Collectors.groupingBy(DuctTube::getFk_lines_trenches));
        routes.forEach(route -> route.getDuctTubes().addAll(
                ductsByTratta.getOrDefault(route.getPk_prj_lines_trenches(), Collections.emptyList())
        ));

        // Esecuzione per ogni tratta con merge del risultato
        routes.forEach(route -> {
            Optional<AssignmentResult> result = sottotubazioneProcedure.execute(route, rules, projectId);
            result.ifPresent(r -> {
                globalResult.merge(r);
                executeBatchUpdates(r);
            });
        });

        log.info("Procedura completata. Assegnati: {}, Saltati: {}",
                globalResult.getTotalAssigned(), globalResult.getTotalSkipped());
        return globalResult;
    }

    private void executeBatchUpdates(AssignmentResult result) {
        result.getBatchUpdates().forEach((tableName, rows) -> {
            tableNameValidator.validate(tableName);  // protezione SQL injection
            ductCommandMapper.batchUpdateByTable(tableName, rows);
        });
    }
}

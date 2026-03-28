package com.geowebframework.sottotubazione.service;

import com.geowebframework.sottotubazione.RowUpdateData;
import com.geowebframework.sottotubazione.domain.*;
import com.geowebframework.sottotubazione.procedure.SottotubazioneProcedure;
import com.geowebframework.sottotubazione.repository.MapperConfigRule;
import com.geowebframework.sottotubazione.repository.MapperDuct;
import com.geowebframework.webclient.dao.prj.DaoPrjLinesTrenches;
import it.eagleprojects.gisfocommons.utils.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestratore principale della procedura di sotto-tubazione automatica.
 *
 * @Transactional garantisce rollback completo in caso di errore.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SottotubazioneProcedureService {

    private final MapperConfigRule mapperConfigRule;
    private final MapperDuct mapperDuct;
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
        List<UndergroundRoute> undergroundRoutes = mapperDuct.retrieveUndergroundRoutesByDrawing(projectId);
        List<DuctTube> ductTubes = mapperDuct.findNuoviNonOccupatiByTratta(projectId);
        if (undergroundRoutes.isEmpty() && ductTubes.isEmpty()) {
            return globalResult;
        }

        // Raggruppa i DuctTube per tratta con una Map per evitare il join O(n*m) in memoria
        Map<Long, List<DuctTube>> ductTubesByTratta = ductTubes.stream()
                .collect(Collectors.groupingBy(DuctTube::getFk_lines_trenches));
        for (UndergroundRoute undergroundRoute : undergroundRoutes) {
            List<DuctTube> associated = ductTubesByTratta.getOrDefault(
                    undergroundRoute.getPk_prj_lines_trenches(), Collections.emptyList());
            undergroundRoute.getDuctTubes().addAll(associated);
        }

        HashMap<String, List<RowUpdateData>> massiveValueToUpdate = new HashMap<>();
        Message message = new Message();
        processRoute(undergroundRoutes, projectId, rules, massiveValueToUpdate, message, globalResult);
        executeBatchUpdates(massiveValueToUpdate);
        log.info("Procedura completata. Assegnati: {}, Saltati: {}, Warning: {}",
                globalResult.getTotalAssigned(), globalResult.getTotalSkipped(), message.getWarning());
        return globalResult;
    }

    private void processRoute(List<UndergroundRoute> routeList, Long projectId, List<ConfigRule> rules,
                              HashMap<String, List<RowUpdateData>> massiveValueToUpdate, Message message,
                              ProcedureResult globalResult) {
        for (UndergroundRoute route : routeList) {
            AssignmentResult result = sottotubazioneProcedure.execute(route, rules, projectId, massiveValueToUpdate, message);
            if (result != null) {
                globalResult.merge(result);
            }
        }
    }

    private void executeBatchUpdates(HashMap<String, List<RowUpdateData>> massiveValueToUpdate) {
        massiveValueToUpdate.forEach(mapperDuct::massiveUpdateEntityValuesByFilterValuesBatch);
    }
}

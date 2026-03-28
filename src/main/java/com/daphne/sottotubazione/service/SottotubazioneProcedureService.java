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
    private final SottotubazioneProcedure SottotubazioneProcedure;

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
        for (UndergroundRoute undergroundRoute : undergroundRoutes) {
            for (DuctTube ductTube : ductTubes) {
                if (Objects.equals(ductTube.getFk_lines_trenches(), undergroundRoute.getPk_prj_lines_trenches()))
                    undergroundRoute.getDuctTubes().add(ductTube);
            }
        }
        HashMap<String, List<RowUpdateData>> massiveValueToUpdate = new HashMap<>();
        Message message = new Message();
        processRoute(undergroundRoutes, projectId, rules , massiveValueToUpdate,message);
        diocane(massiveValueToUpdate);
        log.info("Procedura completata. Warning: {}",
                message.getWarning());
        return globalResult;
    }

    private void processRoute(List<UndergroundRoute> routeList, Long projectId, List<ConfigRule> rules, HashMap<String, List<RowUpdateData>> massiveValueToUpdate, Message message) {
        for (UndergroundRoute route : routeList) {
            SottotubazioneProcedure.execute(route, rules, projectId, massiveValueToUpdate,message);
        }
    }

    private void diocane(HashMap<String, List<RowUpdateData>> massiveValueToUpdate){
        massiveValueToUpdate.forEach(mapperDuct::massiveUpdateEntityValuesByFilterValuesBatch);
    }
}
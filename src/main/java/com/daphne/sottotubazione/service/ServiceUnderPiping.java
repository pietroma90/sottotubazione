package com.geowebframework.underPiping.service;

import com.geowebframework.underPiping.dao.DaoUnderPiping;
import com.geowebframework.underPiping.model.*;
import com.geowebframework.underPiping.message.UnderPipingMessage;
import com.geowebframework.underPiping.procedure.UnderPipingProcedure;
import it.eagleprojects.gisfocommons.service.ServiceCommonsMultiutenza;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class ServiceUnderPiping {

    private final DaoUnderPiping daoUnderPiping;
    private final UnderPipingProcedure underPipingProcedure;
    private final ServiceCommonsMultiutenza serviceCommonsMultiutenza;
    private final UnderPipingMessage underPipingMessage;

    public String executeUnderPiping() {
        List<ConfigRule> rules = loadAndValidateRules();
        List<UndergroundRoute> routes = loadAndPrepareRoutes();
        if (rules == null || routes == null)
            return underPipingMessage.getWarningMessage("warning-under-piping.standard-log");
        PipeInPipeRoutingProcedureResult result = runProcedure(routes, rules);
        executeBatchUpdates(result.getMassiveValueToUpdate());
        return getSuccessStringResult(result);
    }

    private String getSuccessStringResult(PipeInPipeRoutingProcedureResult result) {
        return underPipingMessage.getWarningMessage("warning-under-piping.end-procedure", result.getTotalAssigned(), result.getTotalSkipped(), result.getMessage().getWarning());
    }

    private List<ConfigRule> loadAndValidateRules() {
        List<ConfigRule> rules = daoUnderPiping.findActiveRules();
        if (CollectionUtils.isEmpty(rules)) {
            return null;
        }
        return rules;
    }

    private List<UndergroundRoute> loadAndPrepareRoutes() {
        Long projectId = serviceCommonsMultiutenza.getCorrectDrawing();
        List<UndergroundRoute> routes = daoUnderPiping.retrieveUndergroundRoutesByDrawing(projectId);
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        List<DuctTube> ductTubes = daoUnderPiping.findNuoviNonOccupatiByTratta(projectId);
        linkDuctTubesToUndergroundRoutes(ductTubes, routes);
        return routes;
    }

    private PipeInPipeRoutingProcedureResult runProcedure(List<UndergroundRoute> routes, List<ConfigRule> rules) {
        PipeInPipeRoutingProcedureResult result = new PipeInPipeRoutingProcedureResult();
        routes.forEach(undergroundRoute -> processPipelineAssignments(rules, undergroundRoute, result));
        return result;
    }

    private void processPipelineAssignments(List<ConfigRule> rules, UndergroundRoute undergroundRoute, PipeInPipeRoutingProcedureResult procedureResult) {
        underPipingProcedure.execute(undergroundRoute, rules).ifPresent(assignmentResult -> collectAndMergeResults(undergroundRoute, assignmentResult, procedureResult));
    }

    private void collectAndMergeResults(UndergroundRoute undergroundRoute, AssignmentResult assignmentResult, PipeInPipeRoutingProcedureResult result) {
        collectSkipped(undergroundRoute, assignmentResult);
        result.merge(assignmentResult);
    }

    private void collectSkipped(UndergroundRoute undergroundRoute, AssignmentResult result) {
        Set<DuctTube> skipped = undergroundRoute.getDuctTubes().stream()
                .filter(t -> t.isProcessedChild() && !t.is_child())
                .collect(Collectors.toSet());
        result.setSkippedCount(skipped.size());
        skipped.forEach(t -> result.addLog(
                underPipingMessage.getWarningMessage("warning-under-piping.pipe-not-under-pipe", t.getFk_lines_trenches(), t.getId(), t.getShort_desc_name())));
    }

    private void linkDuctTubesToUndergroundRoutes(List<DuctTube> ductTubes, List<UndergroundRoute> undergroundRoutes) {
        Map<Long, List<DuctTube>> byTrenchId = ductTubes.stream().collect(Collectors.groupingBy(DuctTube::getFk_lines_trenches));
        undergroundRoutes.forEach(route ->
                route.getDuctTubes().addAll(byTrenchId.getOrDefault(route.getPk_prj_lines_trenches(), Collections.emptyList()))
        );
    }

    private void executeBatchUpdates(Map<String, List<RowUpdateData>> massiveValueToUpdate) {
        massiveValueToUpdate.forEach(daoUnderPiping::massiveUpdateEntityValuesByFilterValuesBatch);
    }
}
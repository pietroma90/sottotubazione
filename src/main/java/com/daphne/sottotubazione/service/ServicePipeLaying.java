package com.geowebframework.pipeLaying.service;

import com.geowebframework.pipeLaying.dao.DaoPipeLaying;
import com.geowebframework.pipeLaying.message.PipeLayingMessage;
import com.geowebframework.pipeLaying.model.*;
import com.geowebframework.pipeLaying.procedure.PipeLayingProcedure;
import it.eagleprojects.gisfocommons.service.ServiceCommonsMultiutenza;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class ServicePipeLaying {

    private final DaoPipeLaying daoPipeLaying;
    private final PipeLayingProcedure pipeLayingProcedure;
    private final ServiceCommonsMultiutenza serviceCommonsMultiutenza;
    private final PipeLayingMessage pipeLayingMessage;

    public String executePipeLaying() {
        List<ConfigRule> rules = loadAndValidateRules();
        List<UndergroundRoute> routes = loadAndPrepareRoutes();
        if (rules == null || routes == null)
            return pipeLayingMessage.getWarningMessage("warning.pipe-laying.standard-log");
        PipeLayingProcedureResult result = runProcedure(routes, rules);
        executeBatchUpdates(result.getMassiveValueToUpdate());
        return getSuccessStringResult(result);
    }

    private String getSuccessStringResult(PipeLayingProcedureResult result) {
        return pipeLayingMessage.getWarningMessage("warning.pipe-laying.end-procedure", result.getTotalAssigned(), result.getTotalSkipped(), result.getMessage().getWarning());
    }

    private List<ConfigRule> loadAndValidateRules() {
        List<ConfigRule> rules = daoPipeLaying.findActiveRules();
        if (CollectionUtils.isEmpty(rules)) {
            return null;
        }
        return rules;
    }

    private List<UndergroundRoute> loadAndPrepareRoutes() {
        Long projectId = serviceCommonsMultiutenza.getCorrectDrawing();
        List<UndergroundRoute> routes = daoPipeLaying.retrieveUndergroundRoutesByDrawing(projectId);
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        List<DuctTube> ductTubes = daoPipeLaying.getDuctTubeByDrawing(projectId);
        linkDuctTubesToUndergroundRoutes(ductTubes, routes);
        return routes;
    }

    private PipeLayingProcedureResult runProcedure(List<UndergroundRoute> routes, List<ConfigRule> rules) {
        PipeLayingProcedureResult result = new PipeLayingProcedureResult();
        routes.forEach(undergroundRoute -> processPipelineAssignments(rules, undergroundRoute, result));
        return result;
    }

    private void processPipelineAssignments(List<ConfigRule> rules, UndergroundRoute undergroundRoute, PipeLayingProcedureResult procedureResult) {
        pipeLayingProcedure.execute(undergroundRoute, rules).ifPresent(assignmentResult -> collectAndMergeResults(undergroundRoute, assignmentResult, procedureResult));
    }

    private void collectAndMergeResults(UndergroundRoute undergroundRoute, AssignmentResult assignmentResult, PipeLayingProcedureResult result) {
        collectSkipped(undergroundRoute, assignmentResult);
        result.accumulateResults(assignmentResult);
    }

    private void collectSkipped(UndergroundRoute undergroundRoute, AssignmentResult result) {
        Set<DuctTube> skipped = undergroundRoute.getDuctTubes().stream()
                .filter(t -> t.isProcessedChild() && !t.is_child())
                .collect(Collectors.toSet());
        result.setSkippedCount(skipped.size());
        skipped.forEach(t -> result.addLog(
                pipeLayingMessage.getWarningMessage("warning.pipe-laying.pipe-not-under-pipe", t.getFk_lines_trenches(), t.getId(), t.getShort_desc_name())));
    }

    private void linkDuctTubesToUndergroundRoutes(List<DuctTube> ductTubes, List<UndergroundRoute> undergroundRoutes) {
        Map<Long, List<DuctTube>> ductTubesByTrench = ductTubes.stream().collect(Collectors.groupingBy(DuctTube::getFk_lines_trenches));
        undergroundRoutes.forEach(route ->
                route.getDuctTubes().addAll(ductTubesByTrench.getOrDefault(route.getPk_prj_lines_trenches(), Collections.emptyList()))
        );
    }

    private void executeBatchUpdates(Map<String, List<RowUpdateData>> massiveValueToUpdate) {
        massiveValueToUpdate.forEach(daoPipeLaying::massiveUpdateEntityValuesByFilterValuesBatch);
    }
}
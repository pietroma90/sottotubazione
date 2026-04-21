package com.geowebframework.pipeLaying.service;

import com.geowebframework.pipeLaying.dao.DaoPipeLaying;
import com.geowebframework.pipeLaying.model.AssignmentResult;
import com.geowebframework.pipeLaying.model.ConfigRule;
import com.geowebframework.pipeLaying.model.DuctTube;
import com.geowebframework.pipeLaying.model.UndergroundRoute;
import com.geowebframework.pipeLaying.message.PipeLayingMessage;
import com.geowebframework.pipeLaying.procedure.PipeLayingProcedure;
import it.eagleprojects.gisfocommons.service.ServiceCommonsMultiutenza;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ServicePipeLayingTest {

    @Mock private DaoPipeLaying daoPipeLaying;
    @Mock private PipeLayingProcedure pipeLayingProcedure;
    @Mock private ServiceCommonsMultiutenza serviceCommonsMultiutenza;
    @Mock private PipeLayingMessage pipeLayingMessage;

    @InjectMocks
    private ServicePipeLaying servicePipeLaying;

    private static final Long PROJECT_ID = 42L;
    private static final String STANDARD_LOG_KEY = "warning.pipe-laying.standard-log";
    private static final String END_PROCEDURE_KEY = "warning.pipe-laying.end-procedure";

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(serviceCommonsMultiutenza.getCorrectDrawing()).thenReturn(PROJECT_ID);
        servicePipeLaying = new ServicePipeLaying(daoPipeLaying, pipeLayingProcedure,serviceCommonsMultiutenza, pipeLayingMessage);
    }

    @Test(description = "Nessuna regola attiva: deve ritornare il messaggio standard senza interrogare le tratte")
    public void executePipeLaying_noRules_returnsStandardMessage() {
        when(daoPipeLaying.findActiveRules()).thenReturn(Collections.emptyList());
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(pipeLayingMessage.getWarningMessage(STANDARD_LOG_KEY)).thenReturn("Nessuna regola.");

        String result = servicePipeLaying.executePipeLaying();

        Assert.assertEquals(result,"Nessuna regola.");
        verify(pipeLayingProcedure, never()).execute(any(), any());
    }

    @Test(description = "Regole presenti ma nessuna tratta: deve ritornare il messaggio standard")
    public void executePipeLaying_noRoutes_returnsStandardMessage() {
        when(daoPipeLaying.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(pipeLayingMessage.getWarningMessage(STANDARD_LOG_KEY)).thenReturn("Nessuna tratta.");

        String result = servicePipeLaying.executePipeLaying();

        Assert.assertEquals(result,"Nessuna tratta.");
        verify(pipeLayingProcedure, never()).execute(any(), any());
    }

    @Test(description = "Flusso completo: la procedura viene eseguita e si ottiene il messaggio di fine procedura")
    public void executePipeLaying_fullFlow_callsProcedureAndReturnsEndMessage() {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        AssignmentResult assignmentResult = new AssignmentResult();

        when(daoPipeLaying.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoPipeLaying.getDuctTubeByDrawing(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(pipeLayingProcedure.execute(eq(route), anyList())).thenReturn(Optional.of(assignmentResult));
        when(pipeLayingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("Procedura completata.");

        String result = servicePipeLaying.executePipeLaying();

        Assert.assertEquals(result,"Procedura completata.");
    }

    @Test(description = "La procedura non produce risultati (Optional.empty): nessun merge e il totale rimane a zero")
    public void executePipeLaying_procedureReturnsEmpty_noMergeOccurs() {
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        when(daoPipeLaying.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoPipeLaying.getDuctTubeByDrawing(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(pipeLayingProcedure.execute(any(), any())).thenReturn(Optional.empty());
        when(pipeLayingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), eq(0), eq(0), any()))
                .thenReturn("Procedura OK: 0 assegnati.");

        String result = servicePipeLaying.executePipeLaying();

        Assert.assertEquals(result,"Procedura OK: 0 assegnati.");
    }

    @Test(description = "I DuctTube vengono collegati correttamente alle tratte tramite fk_lines_trenches")
    public void executePipeLaying_linksDuctTubesCorrectlyToRoutes() {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(10L);

        DuctTube tube = new DuctTube();
        tube.setFk_lines_trenches(10L);

        when(daoPipeLaying.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoPipeLaying.getDuctTubeByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(tube));
        when(pipeLayingProcedure.execute(eq(route), anyList())).thenReturn(Optional.empty());
        when(pipeLayingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("OK");

        servicePipeLaying.executePipeLaying();

        Assert.assertTrue((route.getDuctTubes()).contains(tube));
    }

    @Test(description = "collectSkipped: tubo processato ma non figlio → skippedCount > 0 e warning loggato")
    public void executePipeLaying_skippedTubesAreLogged() {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        DuctTube skippedTube = new DuctTube();
        skippedTube.setFk_lines_trenches(1L);
        skippedTube.setProcessedChild(true);
        skippedTube.set_child(false); // processato ma non assegnato

        AssignmentResult assignmentResult = new AssignmentResult();
        // Il servizio chiama collectSkipped internamente; il mock deve restituire
        // un Optional con un AssignmentResult vuoto

        when(daoPipeLaying.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(route));
        when(daoPipeLaying.getDuctTubeByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(skippedTube));
        when(pipeLayingProcedure.execute(eq(route), anyList()))
                .thenReturn(Optional.of(assignmentResult));
        when(pipeLayingMessage.getWarningMessage(
                eq("warning-under-piping.pipe-not-under-pipe"), anyLong(), anyLong(), any()))
                .thenReturn("Tubo non assegnato");
        when(pipeLayingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), eq(0), eq(1), any()))
                .thenReturn("Fine con skipped.");

        String result = servicePipeLaying.executePipeLaying();
        Assert.assertEquals(result, "Fine con skipped.");
    }

    @Test(description = "executeBatchUpdates: se massiveValueToUpdate non è vuoto, il dao viene chiamato")
    public void executePipeLaying_withBatchUpdates_callsDao() {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        AssignmentResult assignmentResult = new AssignmentResult();
        assignmentResult.getMassiveValueToUpdate()
                .put("some_table", Collections.singletonList(new RowUpdateData()));

        when(daoPipeLaying.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(route));
        when(daoPipeLaying.getDuctTubeByDrawing(PROJECT_ID))
                .thenReturn(Collections.emptyList());
        when(pipeLayingProcedure.execute(eq(route), anyList()))
                .thenReturn(Optional.of(assignmentResult));
        when(pipeLayingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("OK con batch");

        servicePipeLaying.executePipeLaying();

        verify(daoPipeLaying).massiveUpdateEntityValuesByFilterValuesBatch(eq("some_table"), anyList());
    }

    // 1. collectSkipped: tubo processedChild=true e is_child=false → skippedCount e warning
    @Test(description = "collectSkipped: tubo processato ma non assegnato → skippedCount=1 e warning nel log")
    public void executePipeLaying_skippedTube_isCountedAndLogged() {
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        DuctTube skipped = new DuctTube();
        skipped.setFk_lines_trenches(1L);
        skipped.setId(99L);
        skipped.setShort_desc_name("TUBO_TEST");
        skipped.setProcessedChild(true);
        skipped.set_child(false);

        AssignmentResult assignmentResult = new AssignmentResult();
        when(daoPipeLaying.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(route));
        when(daoPipeLaying.getDuctTubeByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(skipped));
        when(pipeLayingProcedure.execute(eq(route), anyList()))
                .thenReturn(Optional.of(assignmentResult));
        when(pipeLayingMessage.getWarningMessage(
                eq("warning.pipe-laying.pipe-not-under-pipe"), anyLong(), anyLong(), any()))
                .thenReturn("Tubo non assegnato");
        when(pipeLayingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), eq(0), eq(1), any()))
                .thenReturn("Fine con skipped.");

        String result = servicePipeLaying.executePipeLaying();
        Assert.assertEquals(result, "Fine con skipped.");
        verify(pipeLayingMessage).getWarningMessage(
                eq("warning.pipe-laying.pipe-not-under-pipe"), anyLong(), anyLong(), any());
    }

    // 2. executeBatchUpdates: mappa non vuota → dao.massiveUpdateEntityValuesByFilterValuesBatch invocato
    @Test(description = "executeBatchUpdates: mappa aggiornamenti non vuota → il DAO viene invocato")
    public void executePipeLaying_withBatchUpdates_invokesDao() {
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        AssignmentResult ar = new AssignmentResult();
        ar.getMassiveValueToUpdate().put("some_table",
                Collections.singletonList(new RowUpdateData()));
                when(daoPipeLaying.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoPipeLaying.retrieveUndergroundRoutesByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(route));
        when(daoPipeLaying.getDuctTubeByDrawing(PROJECT_ID))
                .thenReturn(Collections.emptyList());
        when(pipeLayingProcedure.execute(eq(route), anyList()))
                .thenReturn(Optional.of(ar));
        when(pipeLayingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("OK");

        servicePipeLaying.executePipeLaying();

        verify(daoPipeLaying)
                .massiveUpdateEntityValuesByFilterValuesBatch(eq("some_table"), anyList());
    }
}

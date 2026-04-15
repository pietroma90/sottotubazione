package com.geowebframework.underPiping.service;

import com.geowebframework.underPiping.dao.DaoUnderPiping;
import com.geowebframework.underPiping.model.AssignmentResult;
import com.geowebframework.underPiping.model.ConfigRule;
import com.geowebframework.underPiping.model.DuctTube;
import com.geowebframework.underPiping.model.UndergroundRoute;
import com.geowebframework.underPiping.message.UnderPipingMessage;
import com.geowebframework.underPiping.procedure.UnderPipingProcedure;
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

public class ServiceUnderPipingTest {

    @Mock private DaoUnderPiping daoUnderPiping;
    @Mock private UnderPipingProcedure underPipingProcedure;
    @Mock private ServiceCommonsMultiutenza serviceCommonsMultiutenza;
    @Mock private UnderPipingMessage underPipingMessage;

    @InjectMocks
    private ServiceUnderPiping serviceUnderPiping;

    private static final Long PROJECT_ID = 42L;
    private static final String STANDARD_LOG_KEY = "warning-under-piping.standard-log";
    private static final String END_PROCEDURE_KEY = "warning-under-piping.end-procedure";

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(serviceCommonsMultiutenza.getCorrectDrawing()).thenReturn(PROJECT_ID);
        serviceUnderPiping = new ServiceUnderPiping(daoUnderPiping,underPipingProcedure,serviceCommonsMultiutenza,underPipingMessage);
    }

    @Test(description = "Nessuna regola attiva: deve ritornare il messaggio standard senza interrogare le tratte")
    public void executeUnderPiping_noRules_returnsStandardMessage() {
        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.emptyList());
        when(underPipingMessage.getWarningMessage(STANDARD_LOG_KEY)).thenReturn("Nessuna regola.");

        String result = serviceUnderPiping.executeUnderPiping();

        Assert.assertEquals(result,"Nessuna regola.");
        verify(daoUnderPiping, never()).retrieveUndergroundRoutesByDrawing(anyLong());
        verify(underPipingProcedure, never()).execute(any(), any());
    }

    @Test(description = "Regole presenti ma nessuna tratta: deve ritornare il messaggio standard")
    public void executeUnderPiping_noRoutes_returnsStandardMessage() {
        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingMessage.getWarningMessage(STANDARD_LOG_KEY)).thenReturn("Nessuna tratta.");

        String result = serviceUnderPiping.executeUnderPiping();

        Assert.assertEquals(result,"Nessuna tratta.");
        verify(underPipingProcedure, never()).execute(any(), any());
    }

    @Test(description = "Flusso completo: la procedura viene eseguita e si ottiene il messaggio di fine procedura")
    public void executeUnderPiping_fullFlow_callsProcedureAndReturnsEndMessage() {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        AssignmentResult assignmentResult = new AssignmentResult();

        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingProcedure.execute(eq(route), anyList())).thenReturn(Optional.of(assignmentResult));
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("Procedura completata.");

        String result = serviceUnderPiping.executeUnderPiping();

        Assert.assertEquals(result,"Procedura completata.");
    }

    @Test(description = "La procedura non produce risultati (Optional.empty): nessun merge e il totale rimane a zero")
    public void executeUnderPiping_procedureReturnsEmpty_noMergeOccurs() {
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingProcedure.execute(any(), any())).thenReturn(Optional.empty());
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), eq(0), eq(0), any()))
                .thenReturn("Procedura OK: 0 assegnati.");

        String result = serviceUnderPiping.executeUnderPiping();

        Assert.assertEquals(result,"Procedura OK: 0 assegnati.");
    }

    @Test(description = "I DuctTube vengono collegati correttamente alle tratte tramite fk_lines_trenches")
    public void executeUnderPiping_linksDuctTubesCorrectlyToRoutes() {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(10L);

        DuctTube tube = new DuctTube();
        tube.setFk_lines_trenches(10L);

        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(Collections.singletonList(tube));
        when(underPipingProcedure.execute(eq(route), anyList())).thenReturn(Optional.empty());
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("OK");

        serviceUnderPiping.executeUnderPiping();

        Assert.assertTrue((route.getDuctTubes()).contains(tube));
    }

    @Test(description = "collectSkipped: tubo processato ma non figlio → skippedCount > 0 e warning loggato")
    public void executeUnderPiping_skippedTubesAreLogged() {
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

        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID))
                .thenReturn(Collections.singletonList(skippedTube));
        when(underPipingProcedure.execute(eq(route), anyList()))
                .thenReturn(Optional.of(assignmentResult));
        when(underPipingMessage.getWarningMessage(
                eq("warning-under-piping.pipe-not-under-pipe"), anyLong(), anyLong(), any()))
                .thenReturn("Tubo non assegnato");
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), eq(0), eq(1), any()))
                .thenReturn("Fine con skipped.");

        String result = serviceUnderPiping.executeUnderPiping();
        Assert.assertEquals(result, "Fine con skipped.");
    }

    @Test(description = "executeBatchUpdates: se massiveValueToUpdate non è vuoto, il dao viene chiamato")
    public void executeUnderPiping_withBatchUpdates_callsDao() {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        AssignmentResult assignmentResult = new AssignmentResult();
        assignmentResult.getMassiveValueToUpdate()
                .put("some_table", Collections.singletonList(new RowUpdateData()));

        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID))
                .thenReturn(Collections.emptyList());
        when(underPipingProcedure.execute(eq(route), anyList()))
                .thenReturn(Optional.of(assignmentResult));
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("OK con batch");

        serviceUnderPiping.executeUnderPiping();

        verify(daoUnderPiping).massiveUpdateEntityValuesByFilterValuesBatch(eq("some_table"), anyList());
    }

    // 1. collectSkipped: tubo processedChild=true e is_child=false → skippedCount e warning
    @Test(description = "collectSkipped: tubo processato ma non assegnato → skippedCount=1 e warning nel log")
    public void executeUnderPiping_skippedTube_isCountedAndLogged() {
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        DuctTube skipped = new DuctTube();
        skipped.setFk_lines_trenches(1L);
        skipped.setId(99L);
        skipped.setShort_desc_name("TUBO_TEST");
        skipped.setProcessedChild(true);
        skipped.set_child(false);

        AssignmentResult assignmentResult = new AssignmentResult();
        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID))
                .thenReturn(Collections.singletonList(skipped));
        when(underPipingProcedure.execute(eq(route), anyList()))
                .thenReturn(Optional.of(assignmentResult));
        when(underPipingMessage.getWarningMessage(
                eq("warning-under-piping.pipe-not-under-pipe"), anyLong(), anyLong(), any()))
                .thenReturn("Tubo non assegnato");
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), eq(0), eq(1), any()))
                .thenReturn("Fine con skipped.");

        String result = serviceUnderPiping.executeUnderPiping();
        Assert.assertEquals(result, "Fine con skipped.");
        verify(underPipingMessage).getWarningMessage(
                eq("warning-under-piping.pipe-not-under-pipe"), anyLong(), anyLong(), any());
    }

    // 2. executeBatchUpdates: mappa non vuota → dao.massiveUpdateEntityValuesByFilterValuesBatch invocato
    @Test(description = "executeBatchUpdates: mappa aggiornamenti non vuota → il DAO viene invocato")
    public void executeUnderPiping_withBatchUpdates_invokesDao() {
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        AssignmentResult ar = new AssignmentResult();
        ar.getMassiveValueToUpdate().put("some_table",
                Collections.singletonList(new RowUpdateData()));
                when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID))
                .thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID))
                .thenReturn(Collections.emptyList());
        when(underPipingProcedure.execute(eq(route), anyList()))
                .thenReturn(Optional.of(ar));
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("OK");

        serviceUnderPiping.executeUnderPiping();

        verify(daoUnderPiping)
                .massiveUpdateEntityValuesByFilterValuesBatch(eq("some_table"), anyList());
    }
}

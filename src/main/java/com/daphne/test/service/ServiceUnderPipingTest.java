package com.geowebframework.underPiping.service;

import com.geowebframework.procedureOutput.ProcedureOutput;
import com.geowebframework.procedureOutput.ProcedureOutputException;
import com.geowebframework.procedureOutput.ServiceProcedureOutput;
import com.geowebframework.underPiping.dao.DaoUnderPiping;
import com.geowebframework.underPiping.domain.AssignmentResult;
import com.geowebframework.underPiping.domain.ConfigRule;
import com.geowebframework.underPiping.domain.DuctTube;
import com.geowebframework.underPiping.domain.UndergroundRoute;
import com.geowebframework.underPiping.message.UnderPipingMessage;
import com.geowebframework.underPiping.procedure.UnderPipingProcedure;
import it.eagleprojects.gisfocommons.service.ServiceCommonsMultiutenza;
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
    @Mock private ServiceProcedureOutput serviceProcedureOutput;
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
        serviceUnderPiping = new ServiceUnderPiping(daoUnderPiping,underPipingProcedure,serviceCommonsMultiutenza,serviceProcedureOutput,underPipingMessage);
    }

    @Test(description = "Nessuna regola attiva: deve ritornare il messaggio standard senza interrogare le tratte")
    public void executeUnderPiping_noRules_returnsStandardMessage() throws ProcedureOutputException {
        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.emptyList());
        when(underPipingMessage.getWarningMessage(STANDARD_LOG_KEY)).thenReturn("Nessuna regola.");

        String result = serviceUnderPiping.executeUnderPiping();

        Assert.assertEquals(result,"Nessuna regola.");
        verify(daoUnderPiping, never()).retrieveUndergroundRoutesByDrawing(anyLong());
        verify(underPipingProcedure, never()).execute(any(), any());
    }

    @Test(description = "Regole presenti ma nessuna tratta: deve ritornare il messaggio standard")
    public void executeUnderPiping_noRoutes_returnsStandardMessage() throws ProcedureOutputException {
        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingMessage.getWarningMessage(STANDARD_LOG_KEY)).thenReturn("Nessuna tratta.");

        String result = serviceUnderPiping.executeUnderPiping();

        Assert.assertEquals(result,"Nessuna tratta.");
        verify(underPipingProcedure, never()).execute(any(), any());
    }

    @Test(description = "Flusso completo: la procedura viene eseguita e si ottiene il messaggio di fine procedura")
    public void executeUnderPiping_fullFlow_callsProcedureAndReturnsEndMessage() throws ProcedureOutputException {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        AssignmentResult assignmentResult = new AssignmentResult();
        ProcedureOutput procedureOutput = mock(ProcedureOutput.class);

        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingProcedure.execute(eq(route), anyList())).thenReturn(Optional.of(assignmentResult));
        when(serviceProcedureOutput.insertAtStart(anyLong(), anyString(), anyLong())).thenReturn(procedureOutput);
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("Procedura completata.");

        String result = serviceUnderPiping.executeUnderPiping();

        Assert.assertEquals(result,"Procedura completata.");
        verify(serviceProcedureOutput).writeFileAndUpdateTheEnd(eq(procedureOutput), anyString(), eq(true));
    }

    @Test(description = "La procedura non produce risultati (Optional.empty): nessun merge e il totale rimane a zero")
    public void executeUnderPiping_procedureReturnsEmpty_noMergeOccurs() throws ProcedureOutputException {
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);
        ProcedureOutput procedureOutput = mock(ProcedureOutput.class);

        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(new ConfigRule()));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingProcedure.execute(any(), any())).thenReturn(Optional.empty());
        when(serviceProcedureOutput.insertAtStart(anyLong(), anyString(), anyLong())).thenReturn(procedureOutput);
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), eq(0), eq(0), any()))
                .thenReturn("Procedura OK: 0 assegnati.");

        String result = serviceUnderPiping.executeUnderPiping();

        Assert.assertEquals(result,"Procedura OK: 0 assegnati.");
    }

    @Test(description = "I DuctTube vengono collegati correttamente alle tratte tramite fk_lines_trenches")
    public void executeUnderPiping_linksDuctTubesCorrectlyToRoutes() throws ProcedureOutputException {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(10L);

        DuctTube tube = new DuctTube();
        tube.setFk_lines_trenches(10L);

        ProcedureOutput procedureOutput = mock(ProcedureOutput.class);

        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.singletonList(rule));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.singletonList(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(Collections.singletonList(tube));
        when(underPipingProcedure.execute(eq(route), anyList())).thenReturn(Optional.empty());
        when(serviceProcedureOutput.insertAtStart(anyLong(), anyString(), anyLong())).thenReturn(procedureOutput);
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("OK");

        serviceUnderPiping.executeUnderPiping();

        Assert.assertTrue((route.getDuctTubes()).contains(tube));
    }
}

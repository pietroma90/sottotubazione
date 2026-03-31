package com.daphne.sottotubazione.service;

import com.geowebframework.underPiping.dao.DaoUnderPiping;
import com.geowebframework.underPiping.domain.*;
import com.geowebframework.underPiping.message.UnderPipingMessage;
import com.geowebframework.underPiping.procedure.UnderPipingProcedure;
import com.geowebframework.underPiping.service.ServiceUnderPiping;
import com.geowebframework.procedureOutput.ProcedureOutput;
import com.geowebframework.procedureOutput.ProcedureOutputException;
import com.geowebframework.procedureOutput.ServiceProcedureOutput;
import it.eagleprojects.gisfocommons.service.ServiceCommonsMultiutenza;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceUnderPipingTest {

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

    @BeforeEach
    void setUp() {
        when(serviceCommonsMultiutenza.getCorrectDrawing()).thenReturn(PROJECT_ID);
    }

    @Test
    @DisplayName("Nessuna regola attiva: deve ritornare il messaggio standard senza interrogare le tratte")
    void executeUnderPiping_noRules_returnsStandardMessage() throws ProcedureOutputException {
        when(daoUnderPiping.findActiveRules()).thenReturn(Collections.emptyList());
        when(underPipingMessage.getWarningMessage(STANDARD_LOG_KEY)).thenReturn("Nessuna regola.");

        String result = serviceUnderPiping.executeUnderPiping();

        assertThat(result).isEqualTo("Nessuna regola.");
        verify(daoUnderPiping, never()).retrieveUndergroundRoutesByDrawing(anyLong());
        verify(underPipingProcedure, never()).execute(any(), any());
    }

    @Test
    @DisplayName("Regole presenti ma nessuna tratta: deve ritornare il messaggio standard")
    void executeUnderPiping_noRoutes_returnsStandardMessage() throws ProcedureOutputException {
        when(daoUnderPiping.findActiveRules()).thenReturn(List.of(new ConfigRule()));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingMessage.getWarningMessage(STANDARD_LOG_KEY)).thenReturn("Nessuna tratta.");

        String result = serviceUnderPiping.executeUnderPiping();

        assertThat(result).isEqualTo("Nessuna tratta.");
        verify(underPipingProcedure, never()).execute(any(), any());
    }

    @Test
    @DisplayName("Flusso completo: la procedura viene eseguita e si ottiene il messaggio di fine procedura")
    void executeUnderPiping_fullFlow_callsProcedureAndReturnsEndMessage() throws ProcedureOutputException {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);

        AssignmentResult assignmentResult = new AssignmentResult();
        ProcedureOutput procedureOutput = mock(ProcedureOutput.class);

        when(daoUnderPiping.findActiveRules()).thenReturn(List.of(rule));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(List.of(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingProcedure.execute(eq(route), anyList())).thenReturn(Optional.of(assignmentResult));
        when(serviceProcedureOutput.insertAtStart(anyLong(), anyString(), anyLong())).thenReturn(procedureOutput);
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("Procedura completata.");

        String result = serviceUnderPiping.executeUnderPiping();

        assertThat(result).isEqualTo("Procedura completata.");
        verify(serviceProcedureOutput).writeFileAndUpdateTheEnd(eq(procedureOutput), anyString(), eq(true));
    }

    @Test
    @DisplayName("La procedura non produce risultati (Optional.empty): nessun merge e il totale rimane a zero")
    void executeUnderPiping_procedureReturnsEmpty_noMergeOccurs() throws ProcedureOutputException {
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(1L);
        ProcedureOutput procedureOutput = mock(ProcedureOutput.class);

        when(daoUnderPiping.findActiveRules()).thenReturn(List.of(new ConfigRule()));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(List.of(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(underPipingProcedure.execute(any(), any())).thenReturn(Optional.empty());
        when(serviceProcedureOutput.insertAtStart(anyLong(), anyString(), anyLong())).thenReturn(procedureOutput);
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), eq(0), eq(0), any()))
                .thenReturn("Procedura OK: 0 assegnati.");

        String result = serviceUnderPiping.executeUnderPiping();

        assertThat(result).isEqualTo("Procedura OK: 0 assegnati.");
    }

    @Test
    @DisplayName("I DuctTube vengono collegati correttamente alle tratte tramite fk_lines_trenches")
    void executeUnderPiping_linksDuctTubesCorrectlyToRoutes() throws ProcedureOutputException {
        ConfigRule rule = new ConfigRule();
        UndergroundRoute route = new UndergroundRoute();
        route.setPk_prj_lines_trenches(10L);

        DuctTube tube = new DuctTube();
        tube.setFk_lines_trenches(10L);

        ProcedureOutput procedureOutput = mock(ProcedureOutput.class);

        when(daoUnderPiping.findActiveRules()).thenReturn(List.of(rule));
        when(daoUnderPiping.retrieveUndergroundRoutesByDrawing(PROJECT_ID)).thenReturn(List.of(route));
        when(daoUnderPiping.findNuoviNonOccupatiByTratta(PROJECT_ID)).thenReturn(List.of(tube));
        when(underPipingProcedure.execute(eq(route), anyList())).thenReturn(Optional.empty());
        when(serviceProcedureOutput.insertAtStart(anyLong(), anyString(), anyLong())).thenReturn(procedureOutput);
        when(underPipingMessage.getWarningMessage(eq(END_PROCEDURE_KEY), anyInt(), anyInt(), any()))
                .thenReturn("OK");

        serviceUnderPiping.executeUnderPiping();

        assertThat(route.getDuctTubes()).contains(tube);
    }
}

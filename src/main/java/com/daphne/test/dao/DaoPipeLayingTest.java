package com.geowebframework.pipeLaying.dao;

import com.geowebframework.pipeLaying.mapper.MapperPipeLaying;
import com.geowebframework.pipeLaying.model.ConfigRule;
import com.geowebframework.pipeLaying.model.DuctTube;
import com.geowebframework.pipeLaying.model.UndergroundRoute;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class DaoPipeLayingTest {

    @Mock
    private MapperPipeLaying mapperPipeLaying;

    @InjectMocks
    private DaoPipeLaying daoPipeLaying;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        daoPipeLaying = new DaoPipeLaying(mapperPipeLaying);
    }

    @Test
    public void findActiveRules_shouldReturnRulesFromMapper() {
        List<ConfigRule> expected = Arrays.asList(new ConfigRule(), new ConfigRule());
        when(mapperPipeLaying.findActiveRules()).thenReturn(expected);

        List<ConfigRule> result = daoPipeLaying.findActiveRules();

        assertEquals(result, expected);
        verify(mapperPipeLaying, times(1)).findActiveRules();
    }

    @Test
    public void findActiveRules_shouldReturnEmptyList() {
        when(mapperPipeLaying.findActiveRules()).thenReturn(Collections.emptyList());

        List<ConfigRule> result = daoPipeLaying.findActiveRules();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void findActiveRules_shouldDelegateExceptionFromMapper() {
        when(mapperPipeLaying.findActiveRules())
                .thenThrow(new RuntimeException("DB error"));

        try {
            daoPipeLaying.findActiveRules();
            fail("Attesa RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(e.getMessage(), "DB error");
        }

        verify(mapperPipeLaying, times(1)).findActiveRules();
    }

    @Test
    public void retrieveUndergroundRoutesByDrawing_shouldReturnRoutesFromMapper() {
        Long projectId = 42L;
        List<UndergroundRoute> expected = Arrays.asList(new UndergroundRoute(), new UndergroundRoute());
        when(mapperPipeLaying.retrieveUndergroundRoutesByDrawing(projectId)).thenReturn(expected);

        List<UndergroundRoute> result = daoPipeLaying.retrieveUndergroundRoutesByDrawing(projectId);

        assertEquals(result, expected);
        verify(mapperPipeLaying, times(1)).retrieveUndergroundRoutesByDrawing(projectId);
    }

    @Test
    public void retrieveUndergroundRoutesByDrawing_shouldPassCorrectProjectId() {
        Long projectId = 99L;
        when(mapperPipeLaying.retrieveUndergroundRoutesByDrawing(projectId))
                .thenReturn(Collections.emptyList());

        daoPipeLaying.retrieveUndergroundRoutesByDrawing(projectId);

        verify(mapperPipeLaying).retrieveUndergroundRoutesByDrawing(99L);
        verify(mapperPipeLaying, never()).retrieveUndergroundRoutesByDrawing(argThat(id -> !id.equals(99L)));
    }

    @Test
    public void retrieveUndergroundRoutesByDrawing_shouldReturnEmptyList() {
        when(mapperPipeLaying.retrieveUndergroundRoutesByDrawing(anyLong()))
                .thenReturn(Collections.emptyList());

        List<UndergroundRoute> result = daoPipeLaying.retrieveUndergroundRoutesByDrawing(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void findNuoviNonOccupatiByTratta_shouldReturnDuctTubesFromMapper() {
        Long projectId = 7L;
        List<DuctTube> expected = Arrays.asList(new DuctTube(), new DuctTube(), new DuctTube());
        when(mapperPipeLaying.getDuctTubeByDrawing(projectId)).thenReturn(expected);

        List<DuctTube> result = daoPipeLaying.getDuctTubeByDrawing(projectId);

        assertEquals(result, expected);
        assertEquals(result.size(), 3);
        verify(mapperPipeLaying, times(1)).getDuctTubeByDrawing(projectId);
    }

    @Test
    public void findNuoviNonOccupatiByTratta_shouldReturnEmptyList() {
        when(mapperPipeLaying.getDuctTubeByDrawing(anyLong()))
                .thenReturn(Collections.emptyList());

        List<DuctTube> result = daoPipeLaying.getDuctTubeByDrawing(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void findNuoviNonOccupatiByTratta_shouldPassCorrectProjectId() {
        Long projectId = 55L;
        when(mapperPipeLaying.getDuctTubeByDrawing(projectId))
                .thenReturn(Collections.emptyList());

        daoPipeLaying.getDuctTubeByDrawing(projectId);

        verify(mapperPipeLaying).getDuctTubeByDrawing(55L);
    }

    @Test
    public void massiveUpdateEntityValuesByFilterValuesBatch_shouldDelegateToMapper() {
        String tableName = "duct_tube";
        List<RowUpdateData> rowUpdateData = Arrays.asList(new RowUpdateData(), new RowUpdateData());

        daoPipeLaying.massiveUpdateEntityValuesByFilterValuesBatch(tableName, rowUpdateData);

        verify(mapperPipeLaying, times(1))
                .massiveUpdateEntityValuesByFilterValuesBatch(tableName, rowUpdateData);
    }

    @Test
    public void massiveUpdateEntityValuesByFilterValuesBatch_shouldPassCorrectArguments() {
        String tableName = "underground_route";
        List<RowUpdateData> rowUpdateData = Collections.singletonList(new RowUpdateData());

        daoPipeLaying.massiveUpdateEntityValuesByFilterValuesBatch(tableName, rowUpdateData);

        verify(mapperPipeLaying).massiveUpdateEntityValuesByFilterValuesBatch(
                eq("underground_route"),
                eq(rowUpdateData)
        );
        verifyNoMoreInteractions(mapperPipeLaying);
    }

    @Test
    public void massiveUpdateEntityValuesByFilterValuesBatch_shouldWorkWithEmptyList() {
        daoPipeLaying.massiveUpdateEntityValuesByFilterValuesBatch("table", Collections.emptyList());

        verify(mapperPipeLaying, times(1))
                .massiveUpdateEntityValuesByFilterValuesBatch("table", Collections.emptyList());
    }
}
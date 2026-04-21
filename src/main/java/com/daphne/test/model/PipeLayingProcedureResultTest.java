package com.geowebframework.pipeLaying.model;

import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

public class PipeLayingProcedureResultTest {

    @Test(description = "Stato iniziale: tutti i contatori a zero e mappa vuota")
    public void initialState_allZeroAndEmpty() {
        PipeLayingProcedureResult result = new PipeLayingProcedureResult();
        Assert.assertEquals(result.getTotalAssigned(), 0);
        Assert.assertEquals(result.getTotalSkipped(), 0);
        Assert.assertTrue(result.getMassiveValueToUpdate().isEmpty());
    }

    @Test(description = "merge: somma correttamente assigned e skipped da AssignmentResult")
    public void accumulateResults_sumsAssignedAndSkipped() {
        PipeLayingProcedureResult total = new PipeLayingProcedureResult();

        AssignmentResult r1 = new AssignmentResult();
        r1.incrementAssigned();
        r1.incrementAssigned();
        r1.setSkippedCount(1);

        AssignmentResult r2 = new AssignmentResult();
        r2.incrementAssigned();
        r2.setSkippedCount(3);

        total.accumulateResults(r1);
        total.accumulateResults(r2);

        Assert.assertEquals(total.getTotalAssigned(), 3);
        Assert.assertEquals(total.getTotalSkipped(), 4);
    }

    @Test(description = "merge: unisce i massiveValueToUpdate accumulando le liste per chiave")
    public void accumulateResults_accumulatesMassiveValueToUpdate() {
        PipeLayingProcedureResult total = new PipeLayingProcedureResult();

        AssignmentResult r1 = new AssignmentResult();
        r1.getMassiveValueToUpdate().put("tbl", new ArrayList<>(Collections.singletonList(new RowUpdateData())));

        AssignmentResult r2 = new AssignmentResult();
        r2.getMassiveValueToUpdate().put("tbl", new ArrayList<>(Collections.singletonList(new RowUpdateData())));

        total.accumulateResults(r1);
        total.accumulateResults(r2);

        Assert.assertEquals(total.getMassiveValueToUpdate().get("tbl").size(), 2);
    }

    @Test(description = "merge con warning: i messaggi vengono accumulati")
    public void accumulateResults_accumulatesWarningMessages() {
        PipeLayingProcedureResult total = new PipeLayingProcedureResult();
        AssignmentResult r = new AssignmentResult();
        r.addLog("Tubo 123 non assegnato");

        total.accumulateResults(r);

        Assert.assertTrue(total.getMessage().getWarning().contains("Tubo 123 non assegnato"));
    }
}

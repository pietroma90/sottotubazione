package com.geowebframework.underPiping.domain;

import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;


public class AssignmentResultTest {

    @Test(description = "incrementAssigned: il contatore cresce di 1 ad ogni chiamata")
    public void incrementAssigned_incrementsCounter() {
        AssignmentResult r = new AssignmentResult();
        r.incrementAssigned();
        r.incrementAssigned();
        Assert.assertEquals(r.getAssignedCount(), 2);
    }

    @Test(description = "addLog: il messaggio di warning viene aggiunto")
    public void addLog_appendsWarning() {
        AssignmentResult r = new AssignmentResult();
        r.addLog("warning 1");
        Assert.assertTrue(r.getMessage().getWarning().contains("warning 1"));
    }

    @Test(description = "merge: somma gli assigned e unisce i massiveValueToUpdate")
    public void merge_combinesCountsAndUpdates() {
        AssignmentResult base = new AssignmentResult();
        base.incrementAssigned();

        AssignmentResult other = new AssignmentResult();
        other.incrementAssigned();
        other.incrementAssigned();
        RowUpdateData row = new RowUpdateData();
        other.getMassiveValueToUpdate().put("table_x", Collections.singletonList((row)));

        base.merge(other);

        Assert.assertEquals(base.getAssignedCount(), 3);
        Assert.assertTrue(base.getMassiveValueToUpdate().containsKey("table_x"));
        Assert.assertEquals(base.getMassiveValueToUpdate().get("table_x").size(), 1);
    }

    @Test(description = "merge con massiveValueToUpdate vuoto: non causa errori e non aggiunge chiavi")
    public void merge_emptyMassiveUpdate_doesNotAddKeys() {
        AssignmentResult base = new AssignmentResult();
        AssignmentResult other = new AssignmentResult();
        base.merge(other);
        Assert.assertTrue(base.getMassiveValueToUpdate().isEmpty());
    }

    @Test(description = "merge: stessa chiave già presente → le liste vengono concatenate")
    public void merge_sameKeyAlreadyPresent_appendsToList() {
        AssignmentResult base = new AssignmentResult();
        RowUpdateData row1 = new RowUpdateData();
        base.getMassiveValueToUpdate().put("tbl", new ArrayList<>(Collections.singletonList(row1)));

        AssignmentResult other = new AssignmentResult();
        RowUpdateData row2 = new RowUpdateData();
        other.getMassiveValueToUpdate().put("tbl", Collections.singletonList(row2));

        base.merge(other);

        Assert.assertEquals(base.getMassiveValueToUpdate().get("tbl").size(), 2);
    }
}

package com.daphne.sottotubazione.domain;

import com.geowebframework.underPiping.domain.AssignmentResult;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentResultTest {

    @Test
    @DisplayName("incrementAssigned: il contatore cresce di 1 ad ogni chiamata")
    void incrementAssigned_incrementsCounter() {
        AssignmentResult r = new AssignmentResult();
        r.incrementAssigned();
        r.incrementAssigned();
        assertThat(r.getAssignedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("addLog: il messaggio di warning viene aggiunto")
    void addLog_appendsWarning() {
        AssignmentResult r = new AssignmentResult();
        r.addLog("warning 1");
        assertThat(r.getMessage().getWarning()).contains("warning 1");
    }

    @Test
    @DisplayName("merge: somma gli assigned e unisce i massiveValueToUpdate")
    void merge_combinesCountsAndUpdates() {
        AssignmentResult base = new AssignmentResult();
        base.incrementAssigned();

        AssignmentResult other = new AssignmentResult();
        other.incrementAssigned();
        other.incrementAssigned();
        RowUpdateData row = new RowUpdateData();
        other.getMassiveValueToUpdate().put("table_x", new ArrayList<>(List.of(row)));

        base.merge(other);

        assertThat(base.getAssignedCount()).isEqualTo(3);
        assertThat(base.getMassiveValueToUpdate()).containsKey("table_x");
        assertThat(base.getMassiveValueToUpdate().get("table_x")).hasSize(1);
    }

    @Test
    @DisplayName("merge con massiveValueToUpdate vuoto: non causa errori e non aggiunge chiavi")
    void merge_emptyMassiveUpdate_doesNotAddKeys() {
        AssignmentResult base = new AssignmentResult();
        AssignmentResult other = new AssignmentResult();
        base.merge(other);
        assertThat(base.getMassiveValueToUpdate()).isEmpty();
    }
}

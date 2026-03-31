package com.daphne.sottotubazione.domain;

import com.geowebframework.underPiping.domain.AssignmentResult;
import com.geowebframework.underPiping.domain.PipeInPipeRoutingProcedureResult;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PipeInPipeRoutingProcedureResultTest {

    @Test
    @DisplayName("Stato iniziale: tutti i contatori a zero e mappa vuota")
    void initialState_allZeroAndEmpty() {
        PipeInPipeRoutingProcedureResult result = new PipeInPipeRoutingProcedureResult();
        assertThat(result.getTotalAssigned()).isZero();
        assertThat(result.getTotalSkipped()).isZero();
        assertThat(result.getMassiveValueToUpdate()).isEmpty();
    }

    @Test
    @DisplayName("merge: somma correttamente assigned e skipped da AssignmentResult")
    void merge_sumsAssignedAndSkipped() {
        PipeInPipeRoutingProcedureResult total = new PipeInPipeRoutingProcedureResult();

        AssignmentResult r1 = new AssignmentResult();
        r1.incrementAssigned();
        r1.incrementAssigned();
        r1.setSkippedCount(1);

        AssignmentResult r2 = new AssignmentResult();
        r2.incrementAssigned();
        r2.setSkippedCount(3);

        total.merge(r1);
        total.merge(r2);

        assertThat(total.getTotalAssigned()).isEqualTo(3);
        assertThat(total.getTotalSkipped()).isEqualTo(4);
    }

    @Test
    @DisplayName("merge: unisce i massiveValueToUpdate accumulando le liste per chiave")
    void merge_accumulatesMassiveValueToUpdate() {
        PipeInPipeRoutingProcedureResult total = new PipeInPipeRoutingProcedureResult();

        AssignmentResult r1 = new AssignmentResult();
        r1.getMassiveValueToUpdate().put("tbl", new ArrayList<>(List.of(new RowUpdateData())));

        AssignmentResult r2 = new AssignmentResult();
        r2.getMassiveValueToUpdate().put("tbl", new ArrayList<>(List.of(new RowUpdateData())));

        total.merge(r1);
        total.merge(r2);

        assertThat(total.getMassiveValueToUpdate().get("tbl")).hasSize(2);
    }

    @Test
    @DisplayName("merge con warning: i messaggi vengono accumulati")
    void merge_accumulatesWarningMessages() {
        PipeInPipeRoutingProcedureResult total = new PipeInPipeRoutingProcedureResult();
        AssignmentResult r = new AssignmentResult();
        r.addLog("Tubo 123 non assegnato");

        total.merge(r);

        assertThat(total.getMessage().getWarning()).contains("Tubo 123 non assegnato");
    }
}

package com.daphne.sottotubazione.procedure.chain;

import com.geowebframework.underPiping.domain.AssignmentResult;
import com.geowebframework.underPiping.domain.ConfigRule;
import com.geowebframework.underPiping.domain.DuctTube;
import com.geowebframework.underPiping.domain.UndergroundRoute;
import com.geowebframework.underPiping.procedure.AssignmentContext;
import com.geowebframework.underPiping.procedure.chain.RuleHandler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleHandlerTest {

    private ConfigRule rule;

    @BeforeMethod
    public void setUp() {
        rule = new ConfigRule();
        rule.setFk_mat_duct_parent(10L);
        rule.setFk_mat_duct_target(20L);
        rule.setMat_duct_max_number_usable(2);
    }

    private DuctTube makeParent(Long id, Long matDuct) {
        DuctTube t = new DuctTube();
        t.setId(id);
        t.setFk_mat_duct(matDuct);
        t.set_new(true);
        return t;
    }

    private DuctTube makeTarget(Long id, Long matDuct) {
        DuctTube t = new DuctTube();
        t.setId(id);
        t.setFk_mat_duct(matDuct);
        t.set_new(true);
        return t;
    }

    private AssignmentContext buildCtx(Set<DuctTube> tubes) {
        UndergroundRoute route = new UndergroundRoute();
        route.setDuctTubes(tubes);
        return AssignmentContext.builder().tratta(route).build();
    }

    @Test(description = "Nessun parent tube: passa al prossimo handler (null → empty)")
    public void handle_noParent_passesToNext() {
        DuctTube target = makeTarget(2L, 20L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Set.of(target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        assertThat(result).isEmpty();
    }

    @Test(description = "Nessun target tube: passa al prossimo handler (null → empty)")
    public void handle_noTarget_passesToNext() {
        DuctTube parent = makeParent(1L, 10L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Set.of(parent)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        assertThat(result).isEmpty();
    }

    @Test(description = "Parent e target validi: l'assegnazione incrementa il contatore assigned")
    public void handle_validParentAndTarget_incrementsAssignedCount() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Set.of(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        assertThat(result).isPresent();
        assertThat(result.get().getAssignedCount()).isEqualTo(1);
    }

    @Test(description = "Target già figlio (is_child=true): non viene riassegnato")
    public void handle_targetAlreadyChild_notReassigned() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        target.set_child(true);
        AssignmentContext ctx = buildCtx(new HashSet<>(Set.of(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        assertThat(result).isPresent();
        assertThat(result.get().getAssignedCount()).isEqualTo(0);
    }

    @Test(description = "Parent raggiunge max_number_usable: non accetta ulteriori figli")
    public void handle_parentAtMaxCapacity_doesNotAssignMore() {
        rule.setMat_duct_max_number_usable(1);
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target1 = makeTarget(2L, 20L);
        DuctTube target2 = makeTarget(3L, 20L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Set.of(parent, target1, target2)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        assertThat(result).isPresent();
        assertThat(result.get().getAssignedCount()).isEqualTo(1);
    }

    @Test(description = "Il batch update viene popolato con i dati corretti")
    public void handle_validAssignment_populatesMassiveValueToUpdate() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Set.of(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        assertThat(result).isPresent();
        assertThat(result.get().getMassiveValueToUpdate()).isNotEmpty();
    }
}

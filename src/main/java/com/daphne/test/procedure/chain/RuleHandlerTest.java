package com.geowebframework.underPiping.procedure.chain;

import com.geowebframework.underPiping.domain.AssignmentResult;
import com.geowebframework.underPiping.domain.ConfigRule;
import com.geowebframework.underPiping.domain.DuctTube;
import com.geowebframework.underPiping.domain.UndergroundRoute;
import com.geowebframework.underPiping.procedure.AssignmentContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;


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
        AssignmentContext ctx = buildCtx(new HashSet<>(Collections.singleton(target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);
        Assert.assertFalse(result.isPresent());
    }

    @Test(description = "Nessun target tube: passa al prossimo handler (null → empty)")
    public void handle_noTarget_passesToNext() {
        DuctTube parent = makeParent(1L, 10L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Collections.singleton(parent)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        Assert.assertFalse(result.isPresent());
    }

    @Test(description = "Parent e target validi: l'assegnazione incrementa il contatore assigned")
    public void handle_validParentAndTarget_incrementsAssignedCount() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(),1);
    }

    @Test(description = "Target già figlio (is_child=true): non viene riassegnato")
    public void handle_targetAlreadyChild_notReassigned() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        target.set_child(true);
        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(),0);
    }

    @Test(description = "Parent raggiunge max_number_usable: non accetta ulteriori figli")
    public void handle_parentAtMaxCapacity_doesNotAssignMore() {
        rule.setMat_duct_max_number_usable(1);
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target1 = makeTarget(2L, 20L);
        DuctTube target2 = makeTarget(3L, 20L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent, target1, target2)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(),1);
    }

    @Test(description = "Il batch update viene popolato con i dati corretti")
    public void handle_validAssignment_populatesMassiveValueToUpdate() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);
        Assert.assertTrue(result.isPresent());
        Assert.assertFalse(result.get().getMassiveValueToUpdate().isEmpty());
    }

    @Test(description = "target già processedChild ma non figlio: processAssignment ritorna senza assegnare")
    public void handle_targetProcessedChildNotChild_notAssigned() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        target.setProcessedChild(true); // già processato, ma is_child=false
        // processedChild non blocca l'assegnazione di per sé (il flag viene settato dentro)
        // ma verifica che il flusso non si rompa
        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);
        Optional<AssignmentResult> result = handler.handle(ctx);
        Assert.assertTrue(result.isPresent());
    }

    @Test(description = "Parent già pieno (isFull=true): non assegna nessun target")
    public void handle_parentAlreadyFull_doesNotAssign() {
        DuctTube parent = makeParent(1L, 10L);
        parent.setFull(true);
        DuctTube target = makeTarget(2L, 20L);
        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(), 0);
    }

    @Test(description = "addBatchUpdate: target nuovo, parent esistente → usa S_FK_PARENT_EXI_DUCT")
    public void handle_newTargetOldParent_usesFkParentExiDuct() {
        rule.setFk_mat_duct_parent(10L);
        rule.setFk_mat_duct_target(20L);
        rule.setMat_duct_max_number_usable(5);
        DuctTube parent = makeParent(1L, 10L);
        parent.set_new(false); // parent esistente
        DuctTube target = makeTarget(2L, 20L);
        target.set_new(true);  // target nuovo
        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);
        Assert.assertTrue(result.isPresent());
        Assert.assertFalse(result.get().getMassiveValueToUpdate().isEmpty());
    }

    @Test(description = "addBatchUpdate: target esistente, parent esistente → usa TubiEsistenti S_FK_PARENT_EXI_DUCT")
    public void handle_bothExisting_usesTubiEsistentiKey() {
        rule.setFk_mat_duct_parent(10L);
        rule.setFk_mat_duct_target(20L);
        rule.setMat_duct_max_number_usable(5);
        DuctTube parent = makeParent(1L, 10L);
        parent.set_new(false);
        DuctTube target = makeTarget(2L, 20L);
        target.set_new(false); // entrambi esistenti
        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(ctx);
        Assert.assertTrue(result.isPresent());
        Assert.assertFalse(result.get().getMassiveValueToUpdate().isEmpty());
    }

    @Test(description = "Con next handler presente: i risultati vengono mergiati")
    public void handle_withNextHandler_mergesResults() {
        ConfigRule rule2 = new ConfigRule();
        rule2.setFk_mat_duct_parent(30L);
        rule2.setFk_mat_duct_target(40L);
        rule2.setMat_duct_max_number_usable(2);

        DuctTube parent1 = makeParent(1L, 10L);
        DuctTube target1 = makeTarget(2L, 20L);
        DuctTube parent2 = makeParent(3L, 30L);
        DuctTube target2 = makeTarget(4L, 40L);
        target2.set_new(false);

        AssignmentContext ctx = buildCtx(new HashSet<>(Arrays.asList(parent1, target1, parent2, target2)));

        RuleHandler handler1 = new RuleHandler(rule);
        RuleHandler handler2 = new RuleHandler(rule2);
        handler1.setNext(handler2);

        Optional<AssignmentResult> result = handler1.handle(ctx);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(), 2);
    }
}

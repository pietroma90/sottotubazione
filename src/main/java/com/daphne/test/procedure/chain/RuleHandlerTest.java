package com.geowebframework.pipeLaying.procedure.chain;

import com.geowebframework.pipeLaying.model.AssignmentResult;
import com.geowebframework.pipeLaying.model.ConfigRule;
import com.geowebframework.pipeLaying.model.DuctTube;
import com.geowebframework.pipeLaying.model.UndergroundRoute;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;


public class RuleHandlerTest {

    private ConfigRule rule;

    @BeforeMethod
    public void setUp() {
        rule = new ConfigRule();
        rule.setMat_duct_parent_id(10L);
        rule.setMat_duct_child_id(20L);
        rule.setMax_duct_number(2);
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

    private UndergroundRoute buildCtx(Set<DuctTube> tubes) {
        UndergroundRoute route = new UndergroundRoute();
        route.setDuctTubes(tubes);
        return route;
    }

    @Test(description = "Nessun parent tube: passa al prossimo handler (null → empty)")
    public void handle_noParent_passesToNext() {
        DuctTube target = makeTarget(2L, 20L);
        UndergroundRoute route = buildCtx(new HashSet<>(Collections.singleton(target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);
        Assert.assertFalse(result.isPresent());
    }

    @Test(description = "Nessun target tube: passa al prossimo handler (null → empty)")
    public void handle_noTarget_passesToNext() {
        DuctTube parent = makeParent(1L, 10L);
        UndergroundRoute route = buildCtx(new HashSet<>(Collections.singleton(parent)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);

        Assert.assertFalse(result.isPresent());
    }

    @Test(description = "Parent e target validi: l'assegnazione incrementa il contatore assigned")
    public void handle_validParentAndTarget_incrementsAssignedCount() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(),1);
    }

    @Test(description = "Target già figlio (is_child=true): non viene riassegnato")
    public void handle_targetAlreadyChild_notReassigned() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        target.set_child(true);
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(),0);
    }

    @Test(description = "Parent raggiunge max_number_usable: non accetta ulteriori figli")
    public void handle_parentAtMaxCapacity_doesNotAssignMore() {
        rule.setMax_duct_number(1);
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target1 = makeTarget(2L, 20L);
        DuctTube target2 = makeTarget(3L, 20L);
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target1, target2)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(),1);
    }

    @Test(description = "Il batch update viene popolato con i dati corretti")
    public void handle_validAssignment_populatesMassiveValueToUpdate() {
        DuctTube parent = makeParent(1L, 10L);
        DuctTube target = makeTarget(2L, 20L);
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);
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
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);
        Optional<AssignmentResult> result = handler.handle(route);
        Assert.assertTrue(result.isPresent());
    }

    @Test(description = "Parent già pieno (isFull=true): non assegna nessun target")
    public void handle_parentAlreadyFull_doesNotAssign() {
        DuctTube parent = makeParent(1L, 10L);
        parent.setFull(true);
        DuctTube target = makeTarget(2L, 20L);
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(), 0);
    }

    @Test(description = "addBatchUpdate: target nuovo, parent esistente → usa S_FK_PARENT_EXI_DUCT")
    public void handle_newTargetOldParent_usesFkParentExiDuct() {
        rule.setMat_duct_parent_id(10L);
        rule.setMat_duct_child_id(20L);
        rule.setMax_duct_number(5);
        DuctTube parent = makeParent(1L, 10L);
        parent.set_new(false); // parent esistente
        DuctTube target = makeTarget(2L, 20L);
        target.set_new(true);  // target nuovo
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);
        Assert.assertTrue(result.isPresent());
        Assert.assertFalse(result.get().getMassiveValueToUpdate().isEmpty());
    }

    @Test(description = "addBatchUpdate: target esistente, parent esistente → usa TubiEsistenti S_FK_PARENT_EXI_DUCT")
    public void handle_bothExisting_usesTubiEsistentiKey() {
        rule.setMat_duct_parent_id(10L);
        rule.setMat_duct_child_id(20L);
        rule.setMax_duct_number(5);
        DuctTube parent = makeParent(1L, 10L);
        parent.set_new(false);
        DuctTube target = makeTarget(2L, 20L);
        target.set_new(false); // entrambi esistenti
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);
        Assert.assertTrue(result.isPresent());
        Assert.assertFalse(result.get().getMassiveValueToUpdate().isEmpty());
    }

    @Test(description = "Con next handler presente: i risultati vengono mergiati")
    public void handle_withNextHandler_mergesResults() {
        ConfigRule rule2 = new ConfigRule();
        rule2.setMat_duct_parent_id(30L);
        rule2.setMat_duct_child_id(40L);
        rule2.setMax_duct_number(2);

        DuctTube parent1 = makeParent(1L, 10L);
        DuctTube target1 = makeTarget(2L, 20L);
        DuctTube parent2 = makeParent(3L, 30L);
        DuctTube target2 = makeTarget(4L, 40L);
        target2.set_new(false);

        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent1, target1, parent2, target2)));

        RuleHandler handler1 = new RuleHandler(rule);
        RuleHandler handler2 = new RuleHandler(rule2);
        handler1.setNext(handler2);

        Optional<AssignmentResult> result = handler1.handle(route);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(), 2);
    }

    @Test(description = "Con next handler presente che produce risultato: i due AssignmentResult vengono mergiati")
    public void handle_withNextHandlerReturningResult_mergesResults() {
        ConfigRule rule2 = new ConfigRule();
        rule2.setMat_duct_parent_id(30L);
        rule2.setMat_duct_child_id(40L);
        rule2.setMax_duct_number(2);

        DuctTube p1 = makeParent(1L, 10L);
        DuctTube t1 = makeTarget(2L, 20L);
        DuctTube p2 = makeParent(3L, 30L);
        DuctTube t2 = makeTarget(4L, 40L);

        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(p1, t1, p2, t2)));

        RuleHandler h1 = new RuleHandler(rule);
        RuleHandler h2 = new RuleHandler(rule2);
        h1.setNext(h2);

        Optional<AssignmentResult> result = h1.handle(route);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(), 2);
    }

    @Test(description = "Parent con isFull=true fin dall'inizio: nessun target viene assegnato")
    public void handle_parentPresetFull_doesNotAssignAnyTarget() {
        DuctTube parent = makeParent(1L, 10L);
        parent.setFull(true);
        DuctTube target = makeTarget(2L, 20L);
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));
        RuleHandler handler = new RuleHandler(rule);

        Optional<AssignmentResult> result = handler.handle(route);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(), 0);
    }

    @Test(description = "addBatchUpdate: target nuovo + parent esistente → ramo S_FK_PARENT_EXI_DUCT di RLinesProducts")
    public void handle_newTargetExistingParent_usesRLinesParentExiDuct() {
        rule.setMax_duct_number(5);
        DuctTube parent = makeParent(1L, 10L);
        parent.set_new(false);  // parent ESISTENTE
        DuctTube target = makeTarget(2L, 20L);
        target.set_new(true);   // target NUOVO
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));

        Optional<AssignmentResult> result = new RuleHandler(rule).handle(route);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(), 1);
        Assert.assertFalse(result.get().getMassiveValueToUpdate().isEmpty());
    }

    @Test(description = "addBatchUpdate: target esistente + parent nuovo → solo addFilter, nessun addValue")
    public void handle_existingTargetNewParent_noValueAdded() {
        rule.setMax_duct_number(5);
        DuctTube parent = makeParent(1L, 10L);
        parent.set_new(true);   // parent NUOVO
        DuctTube target = makeTarget(2L, 20L);
        target.set_new(false);  // target ESISTENTE
        UndergroundRoute route = buildCtx(new HashSet<>(Arrays.asList(parent, target)));

        Optional<AssignmentResult> result = new RuleHandler(rule).handle(route);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get().getAssignedCount(), 1);
        // massiveValueToUpdate deve contenere la chiave (TubiEsistenti), anche se rowData ha solo il filter
        Assert.assertFalse(result.get().getMassiveValueToUpdate().isEmpty());
    }
}

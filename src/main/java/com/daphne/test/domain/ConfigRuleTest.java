package com.geowebframework.underPiping.domain;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class ConfigRuleTest {

    private ConfigRule rule;

    @BeforeMethod
    public void setUp() {
        rule = new ConfigRule();
    }

    // --- matchesParent ---

    @Test(description = "matchesParent: fk_mat_duct_parent settato e corrisponde → true")
    public void matchesParent_byMaterial_match() {
        rule.setFk_mat_duct_parent(5L);
        DuctTube tube = new DuctTube();
        tube.setFk_mat_duct(5L);
        Assert.assertTrue(rule.matchesParent(tube));
    }

    @Test(description = "matchesParent: fk_mat_duct_parent settato ma diverso → false")
    public void matchesParent_byMaterial_noMatch() {
        rule.setFk_mat_duct_parent(5L);
        DuctTube tube = new DuctTube();
        tube.setFk_mat_duct(9L);
        Assert.assertFalse(rule.matchesParent(tube));
    }

    @Test(description = "matchesParent: nessun fk, diametro nel range → true")
    public void matchesParent_byDiameter_inRange() {
        rule.setTubi_esistenti_ext_min_diam_parent(10);
        rule.setTubi_esistenti_ext_max_diam_parent(50);
        DuctTube tube = new DuctTube();
        tube.setExsternal_diameter(30);
        Assert.assertTrue(rule.matchesParent(tube));
    }

    @Test(description = "matchesParent: nessun fk, diametro fuori range → false")
    public void matchesParent_byDiameter_outOfRange() {
        rule.setTubi_esistenti_ext_min_diam_parent(10);
        rule.setTubi_esistenti_ext_max_diam_parent(50);
        DuctTube tube = new DuctTube();
        tube.setExsternal_diameter(60);
        Assert.assertFalse(rule.matchesParent(tube));
    }

    @Test(description = "matchesParent: diametro null trattato come 0, min=0 → true")
    public void matchesParent_nullDiameter_treatedAsZero() {
        rule.setTubi_esistenti_ext_min_diam_parent(0);
        rule.setTubi_esistenti_ext_max_diam_parent(100);
        DuctTube tube = new DuctTube(); // diameter null
        Assert.assertTrue(rule.matchesParent(tube));
    }

    // --- matchesTarget ---

    @Test(description = "matchesTarget: fk_mat_duct_target corrisponde → true")
    public void matchesTarget_byMaterial_match() {
        rule.setFk_mat_duct_target(7L);
        DuctTube tube = new DuctTube();
        tube.setFk_mat_duct(7L);
        Assert.assertTrue(rule.matchesTarget(tube));
    }

    @Test(description = "matchesTarget: diametro nel range → true")
    public void matchesTarget_byDiameter_inRange() {
        rule.setTubi_esistenti_ext_min_diam_target(5);
        rule.setTubi_esistenti_ext_max_diam_target(20);
        DuctTube tube = new DuctTube();
        tube.setExsternal_diameter(10);
        Assert.assertTrue(rule.matchesTarget(tube));
    }

    // --- appliesTo ---

    @Test(description = "appliesTo: trenches_types null → false")
    public void appliesTo_nullTrenchType_returnsFalse() {
        rule.setFk_lines_types_ids(Collections.singletonList(1L));
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(null);
        Assert.assertFalse(rule.appliesTo(route));
    }

    @Test(description = "appliesTo: fk_lines_types_ids vuota → false")
    public void appliesTo_emptyTypeList_returnsFalse() {
        rule.setFk_lines_types_ids(Collections.emptyList());
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(1L);
        Assert.assertFalse(rule.appliesTo(route));
    }

    @Test(description = "appliesTo: type presente nella lista → true")
    public void appliesTo_typeInList_returnsTrue() {
        rule.setFk_lines_types_ids(Arrays.asList(1L, 2L, 3L));
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(2L);
        Assert.assertTrue(rule.appliesTo(route));
    }

    @Test(description = "appliesTo: type NON presente nella lista → false")
    public void appliesTo_typeNotInList_returnsFalse() {
        rule.setFk_lines_types_ids(Arrays.asList(1L, 2L));
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(99L);
        Assert.assertFalse(rule.appliesTo(route));
    }

    @Test(description = "matchesParent: solo max settato, diametro ≤ max → true")
    public void matchesParent_onlyMaxDiam_inRange() {
        rule.setTubi_esistenti_ext_max_diam_parent(50);
        DuctTube tube = new DuctTube();
        tube.setExsternal_diameter(30);
        Assert.assertTrue(rule.matchesParent(tube));
    }

    @Test(description = "matchesTarget: fk_mat_duct_target diverso → false")
    public void matchesTarget_byMaterial_noMatch() {
        rule.setFk_mat_duct_target(7L);
        DuctTube tube = new DuctTube();
        tube.setFk_mat_duct(99L);
        Assert.assertFalse(rule.matchesTarget(tube));
    }

    @Test(description = "matchesTarget: diametro null trattato come 0, min=0 → true")
    public void matchesTarget_nullDiameter_treatedAsZero() {
        rule.setTubi_esistenti_ext_min_diam_target(0);
        rule.setTubi_esistenti_ext_max_diam_target(100);
        DuctTube tube = new DuctTube();
        Assert.assertTrue(rule.matchesTarget(tube));
    }

    @Test(description = "appliesTo: fk_lines_types_ids null → false")
    public void appliesTo_nullTypeList_returnsFalse() {
        rule.setFk_lines_types_ids(null);
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(1L);
        Assert.assertFalse(rule.appliesTo(route));
    }
}

package com.daphne.sottotubazione.domain;

import com.geowebframework.underPiping.domain.ConfigRule;
import com.geowebframework.underPiping.domain.DuctTube;
import com.geowebframework.underPiping.domain.UndergroundRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigRuleTest {

    private ConfigRule rule;

    @BeforeEach
    void setUp() {
        rule = new ConfigRule();
    }

    // --- matchesParent ---

    @Test
    @DisplayName("matchesParent: fk_mat_duct_parent settato e corrisponde → true")
    void matchesParent_byMaterial_match() {
        rule.setFk_mat_duct_parent(5L);
        DuctTube tube = new DuctTube();
        tube.setFk_mat_duct(5L);
        assertThat(rule.matchesParent(tube)).isTrue();
    }

    @Test
    @DisplayName("matchesParent: fk_mat_duct_parent settato ma diverso → false")
    void matchesParent_byMaterial_noMatch() {
        rule.setFk_mat_duct_parent(5L);
        DuctTube tube = new DuctTube();
        tube.setFk_mat_duct(9L);
        assertThat(rule.matchesParent(tube)).isFalse();
    }

    @Test
    @DisplayName("matchesParent: nessun fk, diametro nel range → true")
    void matchesParent_byDiameter_inRange() {
        rule.setTubi_esistenti_ext_min_diam_parent(10);
        rule.setTubi_esistenti_ext_max_diam_parent(50);
        DuctTube tube = new DuctTube();
        tube.setExsternal_diameter(30);
        assertThat(rule.matchesParent(tube)).isTrue();
    }

    @Test
    @DisplayName("matchesParent: nessun fk, diametro fuori range → false")
    void matchesParent_byDiameter_outOfRange() {
        rule.setTubi_esistenti_ext_min_diam_parent(10);
        rule.setTubi_esistenti_ext_max_diam_parent(50);
        DuctTube tube = new DuctTube();
        tube.setExsternal_diameter(60);
        assertThat(rule.matchesParent(tube)).isFalse();
    }

    @Test
    @DisplayName("matchesParent: diametro null trattato come 0, min=0 → true")
    void matchesParent_nullDiameter_treatedAsZero() {
        rule.setTubi_esistenti_ext_min_diam_parent(0);
        rule.setTubi_esistenti_ext_max_diam_parent(100);
        DuctTube tube = new DuctTube(); // diameter null
        assertThat(rule.matchesParent(tube)).isTrue();
    }

    // --- matchesTarget ---

    @Test
    @DisplayName("matchesTarget: fk_mat_duct_target corrisponde → true")
    void matchesTarget_byMaterial_match() {
        rule.setFk_mat_duct_target(7L);
        DuctTube tube = new DuctTube();
        tube.setFk_mat_duct(7L);
        assertThat(rule.matchesTarget(tube)).isTrue();
    }

    @Test
    @DisplayName("matchesTarget: diametro nel range → true")
    void matchesTarget_byDiameter_inRange() {
        rule.setTubi_esistenti_ext_min_diam_target(5);
        rule.setTubi_esistenti_ext_max_diam_target(20);
        DuctTube tube = new DuctTube();
        tube.setExsternal_diameter(10);
        assertThat(rule.matchesTarget(tube)).isTrue();
    }

    // --- appliesTo ---

    @Test
    @DisplayName("appliesTo: trenches_types null → false")
    void appliesTo_nullTrenchType_returnsFalse() {
        rule.setFk_lines_types_ids(List.of(1L));
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(null);
        assertThat(rule.appliesTo(route)).isFalse();
    }

    @Test
    @DisplayName("appliesTo: fk_lines_types_ids vuota → false")
    void appliesTo_emptyTypeList_returnsFalse() {
        rule.setFk_lines_types_ids(List.of());
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(1L);
        assertThat(rule.appliesTo(route)).isFalse();
    }

    @Test
    @DisplayName("appliesTo: type presente nella lista → true")
    void appliesTo_typeInList_returnsTrue() {
        rule.setFk_lines_types_ids(List.of(1L, 2L, 3L));
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(2L);
        assertThat(rule.appliesTo(route)).isTrue();
    }

    @Test
    @DisplayName("appliesTo: type NON presente nella lista → false")
    void appliesTo_typeNotInList_returnsFalse() {
        rule.setFk_lines_types_ids(List.of(1L, 2L));
        UndergroundRoute route = new UndergroundRoute();
        route.setTrenches_types(99L);
        assertThat(rule.appliesTo(route)).isFalse();
    }
}

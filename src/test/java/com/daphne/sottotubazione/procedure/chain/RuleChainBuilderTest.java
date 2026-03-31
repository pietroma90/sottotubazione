package com.daphne.sottotubazione.procedure.chain;

import com.geowebframework.underPiping.domain.ConfigRule;
import com.geowebframework.underPiping.domain.UndergroundRoute;
import com.geowebframework.underPiping.procedure.chain.RuleChainBuilder;
import com.geowebframework.underPiping.procedure.chain.RuleHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RuleChainBuilderTest {

    private RuleChainBuilder builder;
    private UndergroundRoute route;

    @BeforeEach
    void setUp() {
        builder = new RuleChainBuilder();
        route = new UndergroundRoute();
        route.setTrenches_types(1L);
    }

    private ConfigRule makeRule(long typeId, int priority, boolean deleted) {
        ConfigRule r = new ConfigRule();
        r.setFk_lines_types_ids(List.of(typeId));
        r.setPriority_rules_order(priority);
        r.set_deleted(deleted);
        return r;
    }

    @Test
    @DisplayName("Lista regole vuota: ritorna Optional.empty")
    void build_emptyRules_returnsEmpty() {
        Optional<RuleHandler> result = builder.build(List.of(), route);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Tutte le regole sono deleted: ritorna Optional.empty")
    void build_allDeleted_returnsEmpty() {
        ConfigRule r = makeRule(1L, 1, true);
        Optional<RuleHandler> result = builder.build(List.of(r), route);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Regola non applicabile alla tratta: ritorna Optional.empty")
    void build_ruleNotApplicable_returnsEmpty() {
        ConfigRule r = makeRule(99L, 1, false); // type 99 != route type 1
        Optional<RuleHandler> result = builder.build(List.of(r), route);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Una regola valida: ritorna un handler presente")
    void build_oneValidRule_returnsHandler() {
        ConfigRule r = makeRule(1L, 1, false);
        Optional<RuleHandler> result = builder.build(List.of(r), route);
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Più regole valide: ritorna il primo handler della catena")
    void build_multipleValidRules_returnsFirstHandler() {
        ConfigRule r1 = makeRule(1L, 2, false);
        ConfigRule r2 = makeRule(1L, 1, false);
        Optional<RuleHandler> result = builder.build(List.of(r1, r2), route);
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Regola deleted mista a valida: solo la valida viene inclusa")
    void build_mixedDeletedAndValid_returnsOnlyValidHandler() {
        ConfigRule deleted = makeRule(1L, 1, true);
        ConfigRule valid   = makeRule(1L, 2, false);
        Optional<RuleHandler> result = builder.build(List.of(deleted, valid), route);
        assertThat(result).isPresent();
    }
}

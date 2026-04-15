package com.geowebframework.underPiping.procedure.chain;

import com.geowebframework.underPiping.model.ConfigRule;
import com.geowebframework.underPiping.model.UndergroundRoute;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class RuleChainBuilderTest {

    private RuleChainBuilder builder;
    private UndergroundRoute route;

    @BeforeMethod
    public void setUp() {
        builder = new RuleChainBuilder();
        route = new UndergroundRoute();
        route.setTrenches_types(1L);
    }

    private ConfigRule makeRule(long typeId, int priority, boolean deleted) {
        ConfigRule r = new ConfigRule();
        r.setLines_types_ids(Arrays.asList(typeId));
        r.setPriority(priority);
        r.set_deleted(deleted);
        return r;
    }

    @Test(description = "Lista regole vuota: ritorna Optional.empty")
    public void build_emptyRules_returnsEmpty() {
        Optional<RuleHandler> result = builder.build(Collections.emptyList(), route);
        Assert.assertFalse(result.isPresent());
    }

    @Test(description = "Tutte le regole sono deleted: ritorna Optional.empty")
    public void build_allDeleted_returnsEmpty() {
        ConfigRule r = makeRule(1L, 1, true);
        Optional<RuleHandler> result = builder.build(Collections.singletonList(r), route);
        Assert.assertFalse(result.isPresent());
    }

    @Test(description = "Regola non applicabile alla tratta: ritorna Optional.empty")
    public void build_ruleNotApplicable_returnsEmpty() {
        ConfigRule r = makeRule(99L, 1, false);
        Optional<RuleHandler> result = builder.build(Collections.singletonList(r), route);
        Assert.assertFalse(result.isPresent());
    }

    @Test(description = "Una regola valida: ritorna un handler presente")
    public void build_oneValidRule_returnsHandler() {
        ConfigRule r = makeRule(1L, 1, false);
        Optional<RuleHandler> result = builder.build(Collections.singletonList(r), route);
        Assert.assertTrue(result.isPresent());
    }

    @Test(description = "Più regole valide: ritorna il primo handler della catena")
    public void build_multipleValidRules_returnsFirstHandler() {
        ConfigRule r1 = makeRule(1L, 2, false);
        ConfigRule r2 = makeRule(1L, 1, false);
        Optional<RuleHandler> result = builder.build(Arrays.asList(r1, r2), route);
        Assert.assertTrue(result.isPresent());
    }

    @Test(description = "Regola deleted mista a valida: solo la valida viene inclusa")
    public void build_mixedDeletedAndValid_returnsOnlyValidHandler() {
        ConfigRule deleted = makeRule(1L, 1, true);
        ConfigRule valid   = makeRule(1L, 2, false);
        Optional<RuleHandler> result = builder.build(Arrays.asList(deleted, valid), route);
        Assert.assertTrue(result.isPresent());
    }
}

package com.geowebframework.underPiping.procedure;

import com.geowebframework.underPiping.model.AssignmentResult;
import com.geowebframework.underPiping.model.ConfigRule;
import com.geowebframework.underPiping.model.UndergroundRoute;
import com.geowebframework.underPiping.procedure.chain.RuleChainBuilder;
import com.geowebframework.underPiping.procedure.chain.RuleHandler;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UnderPipingProcedureTest {

    @Mock private RuleChainBuilder ruleChainBuilder;
    @InjectMocks private UnderPipingProcedure underPipingProcedure;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        underPipingProcedure = new UnderPipingProcedure(ruleChainBuilder);
    }


    @Test(description = "Nessun handler costruito dalla chain: deve ritornare Optional.empty")
    public void execute_noChainBuilt_returnsEmpty() {
        UndergroundRoute route = new UndergroundRoute();
        when(ruleChainBuilder.build(any(), any())).thenReturn(Optional.empty());

        Optional<AssignmentResult> result = underPipingProcedure.execute(route, Collections.emptyList());

        Assert.assertFalse(result.isPresent());
        verify(ruleChainBuilder).build(Collections.emptyList(), route);
    }

    @Test(description = "Chain presente: delega al primo handler e ritorna il suo risultato")
    public void execute_chainPresent_delegatesToHandler() {
        UndergroundRoute route = new UndergroundRoute();
        ConfigRule rule = new ConfigRule();
        RuleHandler handler = mock(RuleHandler.class);
        AssignmentResult expected = new AssignmentResult();

        when(ruleChainBuilder.build(Collections.singletonList(rule), route)).thenReturn(Optional.of(handler));
        when(handler.handle(any())).thenReturn(Optional.of(expected));

        Optional<AssignmentResult> result = underPipingProcedure.execute(route, Collections.singletonList(rule));

        Assert.assertEquals(result.get(), expected);
        verify(handler).handle(argThat(ctx -> ctx.getTratta() == route));
    }

    @Test(description = "Chain presente ma handler ritorna empty: il risultato è Optional.empty")
    public void execute_chainPresentButHandlerReturnsEmpty_returnsEmpty() {
        UndergroundRoute route = new UndergroundRoute();
        RuleHandler handler = mock(RuleHandler.class);

        when(ruleChainBuilder.build(any(), any())).thenReturn(Optional.of(handler));
        when(handler.handle(any())).thenReturn(Optional.empty());

        Optional<AssignmentResult> result = underPipingProcedure.execute(route, Collections.singletonList(new ConfigRule()));

        Assert.assertFalse(result.isPresent());
    }
}

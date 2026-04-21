package com.geowebframework.pipeLaying.procedure;

import com.geowebframework.pipeLaying.model.AssignmentResult;
import com.geowebframework.pipeLaying.model.ConfigRule;
import com.geowebframework.pipeLaying.model.UndergroundRoute;
import com.geowebframework.pipeLaying.procedure.chain.RuleChainBuilder;
import com.geowebframework.pipeLaying.procedure.chain.RuleHandler;
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

public class PipeLayingProcedureTest {

    @Mock private RuleChainBuilder ruleChainBuilder;
    @InjectMocks private PipeLayingProcedure pipeLayingProcedure;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        pipeLayingProcedure = new PipeLayingProcedure(ruleChainBuilder);
    }


    @Test(description = "Nessun handler costruito dalla chain: deve ritornare Optional.empty")
    public void execute_noChainBuilt_returnsEmpty() {
        UndergroundRoute route = new UndergroundRoute();
        when(ruleChainBuilder.build(any(), any())).thenReturn(Optional.empty());

        Optional<AssignmentResult> result = pipeLayingProcedure.execute(route, Collections.emptyList());

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

        Optional<AssignmentResult> result = pipeLayingProcedure.execute(route, Collections.singletonList(rule));

        Assert.assertEquals(result.get(), expected);
        verify(handler).handle(argThat(ctx -> ctx == route));
    }

    @Test(description = "Chain presente ma handler ritorna empty: il risultato è Optional.empty")
    public void execute_chainPresentButHandlerReturnsEmpty_returnsEmpty() {
        UndergroundRoute route = new UndergroundRoute();
        RuleHandler handler = mock(RuleHandler.class);

        when(ruleChainBuilder.build(any(), any())).thenReturn(Optional.of(handler));
        when(handler.handle(any())).thenReturn(Optional.empty());

        Optional<AssignmentResult> result = pipeLayingProcedure.execute(route, Collections.singletonList(new ConfigRule()));

        Assert.assertFalse(result.isPresent());
    }
}

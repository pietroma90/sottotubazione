package com.daphne.sottotubazione.procedure;

import com.geowebframework.underPiping.domain.AssignmentResult;
import com.geowebframework.underPiping.domain.ConfigRule;
import com.geowebframework.underPiping.domain.UndergroundRoute;
import com.geowebframework.underPiping.procedure.UnderPipingProcedure;
import com.geowebframework.underPiping.procedure.chain.RuleChainBuilder;
import com.geowebframework.underPiping.procedure.chain.RuleHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnderPipingProcedureTest {

    @Mock private RuleChainBuilder ruleChainBuilder;
    @InjectMocks private UnderPipingProcedure underPipingProcedure;

    @Test
    @DisplayName("Nessun handler costruito dalla chain: deve ritornare Optional.empty")
    void execute_noChainBuilt_returnsEmpty() {
        UndergroundRoute route = new UndergroundRoute();
        when(ruleChainBuilder.build(any(), any())).thenReturn(Optional.empty());

        Optional<AssignmentResult> result = underPipingProcedure.execute(route, List.of());

        assertThat(result).isEmpty();
        verify(ruleChainBuilder).build(List.of(), route);
    }

    @Test
    @DisplayName("Chain presente: delega al primo handler e ritorna il suo risultato")
    void execute_chainPresent_delegatesToHandler() {
        UndergroundRoute route = new UndergroundRoute();
        ConfigRule rule = new ConfigRule();
        RuleHandler handler = mock(RuleHandler.class);
        AssignmentResult expected = new AssignmentResult();

        when(ruleChainBuilder.build(List.of(rule), route)).thenReturn(Optional.of(handler));
        when(handler.handle(any())).thenReturn(Optional.of(expected));

        Optional<AssignmentResult> result = underPipingProcedure.execute(route, List.of(rule));

        assertThat(result).isPresent().contains(expected);
        verify(handler).handle(argThat(ctx -> ctx.getTratta() == route));
    }

    @Test
    @DisplayName("Chain presente ma handler ritorna empty: il risultato è Optional.empty")
    void execute_chainPresentButHandlerReturnsEmpty_returnsEmpty() {
        UndergroundRoute route = new UndergroundRoute();
        RuleHandler handler = mock(RuleHandler.class);

        when(ruleChainBuilder.build(any(), any())).thenReturn(Optional.of(handler));
        when(handler.handle(any())).thenReturn(Optional.empty());

        Optional<AssignmentResult> result = underPipingProcedure.execute(route, List.of(new ConfigRule()));

        assertThat(result).isEmpty();
    }
}

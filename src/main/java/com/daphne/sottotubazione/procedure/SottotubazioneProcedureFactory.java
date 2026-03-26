package com.daphne.sottotubazione.procedure;

import com.daphne.sottotubazione.domain.TrattaInterrata;
import com.daphne.sottotubazione.domain.enums.TrattaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory che seleziona la Strategy corretta in base al tipo di tratta.
 */
@Component
@RequiredArgsConstructor
public class SottotubazioneProcedureFactory {

    private final NuovoScavoStrategy nuovoScavoStrategy;
    private final EsistenteInterratoStrategy esistenteInterratoStrategy;

    public SottotubazioneProcedure getStrategy(TrattaInterrata tratta) {
        return tratta.getType() == TrattaType.SCAVO_NUOVO
            ? nuovoScavoStrategy
            : esistenteInterratoStrategy;
    }
}

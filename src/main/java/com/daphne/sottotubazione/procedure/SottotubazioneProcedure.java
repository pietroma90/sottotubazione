package com.daphne.sottotubazione.procedure;

import com.daphne.sottotubazione.domain.AssignmentResult;
import com.daphne.sottotubazione.domain.ConfigRule;
import com.daphne.sottotubazione.domain.TrattaInterrata;

import java.util.List;

/**
 * Contratto per la procedura di sotto-tubazione.
 * Ogni implementazione gestisce un tipo di tratta diverso (Strategy).
 */
public interface SottotubazioneProcedure {
    AssignmentResult execute(TrattaInterrata tratta, List<ConfigRule> rules, Long projectId);
}

package com.daphne.sottotubazione.domain.enums;

/**
 * Tipologia della tratta interrata.
 * SCAVO_NUOVO  → branch sinistra del flusso (tubi nuovi su tratta come parent)
 * ESISTENTE    → branch destra  del flusso (tubi esistenti non occupati come parent)
 */
public enum TrattaType {
    SCAVO_NUOVO,
    ESISTENTE_INTERRATO
}

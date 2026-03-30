package com.geowebframework.sottotubazione;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO per un singolo record da aggiornare nel batch.
 * Usa LinkedHashMap (non ConcurrentHashMap: operazioni sempre single-thread).
 * Espone un'API fluent per costruzione compatta.
 */
@Data
public class RowUpdateData {

    private final Map<String, Object> updateValues = new LinkedHashMap<>();
    private final Map<String, Object> filterValues = new LinkedHashMap<>();

    /**
     * Factory method: crea un RowUpdateData con il filtro (WHERE clause) gia' impostato.
     * Esempio: RowUpdateData.of("pk_id", 42L).set("fk_parent", 7L);
     */
    public static RowUpdateData of(String filterKey, Object filterValue) {
        RowUpdateData row = new RowUpdateData();
        row.filterValues.put(filterKey, filterValue);
        return row;
    }

    /** Aggiunge un campo da aggiornare (SET clause). Fluent. */
    public RowUpdateData set(String key, Object value) {
        this.updateValues.put(key, value);
        return this;
    }

    /** Aggiunge un filtro (WHERE clause). Fluent. */
    public RowUpdateData where(String key, Object value) {
        this.filterValues.put(key, value);
        return this;
    }

    // Manteniamo i vecchi metodi per compatibilita' con codice esistente
    public void addValue(String key, Object value) { set(key, value); }
    public void addFilter(String key, Object value) { where(key, value); }
}

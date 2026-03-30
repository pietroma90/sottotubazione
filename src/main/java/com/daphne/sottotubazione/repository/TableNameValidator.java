package com.geowebframework.sottotubazione.repository;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Whitelist validator per i nomi di tabella usati nel mapper dinamico.
 * Previene SQL injection su ${tableName} in DuctCommandMapper.
 */
@Component
public class TableNameValidator {

    private static final Set<String> ALLOWED_TABLES = Set.of(
            "r_lines_products",
            "tubi_esistenti"
    );

    public void validate(String tableName) {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException(
                    "Tabella non autorizzata per batch update: '" + tableName + "'. " +
                    "Tabelle consentite: " + ALLOWED_TABLES
            );
        }
    }
}

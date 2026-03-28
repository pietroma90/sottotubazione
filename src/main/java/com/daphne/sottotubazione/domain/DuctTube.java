package com.geowebframework.sottotubazione.domain;

import lombok.Data;

/**
 * Tubo "target" che viene infilato dentro il tubo parent.
 * Può essere nuovo (r_lines_products) o esistente.
 */
@Data
public class DuctTube {

    private Long id;
    private Long fk_lines_trenches;
    private Long fk_mat_duct;
    private Integer exsternal_diameter;
    private Long parent_id;
    private int childCount = 0;  // fix: inizializzato a 0 per evitare NullPointerException in incrementChildCount()
    private boolean is_new;
    private boolean is_child;
    private String short_desc_name;

    public void incrementChildCount() {
        this.childCount++;
    }
}

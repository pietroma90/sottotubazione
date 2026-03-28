package com.geowebframework.sottotubazione.domain;


import lombok.Data;

import java.util.List;

/**
 * Mappa la tabella procedure_config.r_config_fk_parent_duct.
 * Contiene le regole di configurazione per la sotto-tubazione automatica.
 */
@Data
public class ConfigRule {

    private Long id;
    private List<Long> fk_lines_types_ids;
    private Long fk_mat_duct_parent;
    private Integer tubi_esistenti_ext_max_diam_parent;
    private Integer tubi_esistenti_ext_min_diam_parent;
    private Long fk_mat_duct_target;
    private Integer tubi_esistenti_ext_max_diam_target;
    private Integer tubi_esistenti_ext_min_diam_target;
    private int mat_duct_max_number_usable;
    private int priority_rules_order;
    private boolean is_deleted;


    public boolean matchesParent(DuctTube parent) {
        if (fk_mat_duct_parent != null) return fk_mat_duct_parent.equals(parent.getFk_mat_duct());
        int diam = parent.getExsternal_diameter() != null ? parent.getExsternal_diameter() : 0;
        boolean maxOk = tubi_esistenti_ext_max_diam_parent == null || diam <= tubi_esistenti_ext_max_diam_parent;
        boolean minOk = tubi_esistenti_ext_min_diam_parent == null || diam >= tubi_esistenti_ext_min_diam_parent;
        return maxOk && minOk;
    }

    public boolean matchesTarget(DuctTube target) {
        if (fk_mat_duct_target != null) return fk_mat_duct_target.equals(target.getFk_mat_duct());
        int diam = target.getExsternal_diameter() != null ? target.getExsternal_diameter() : 0;
        boolean maxOk = tubi_esistenti_ext_max_diam_target == null || diam <= tubi_esistenti_ext_max_diam_target;
        boolean minOk = tubi_esistenti_ext_min_diam_target == null || diam >= tubi_esistenti_ext_min_diam_target;
        return maxOk && minOk;
    }

    public boolean appliesTo(UndergroundRoute tratta) {
        // fix: null-check su fk_lines_types_ids per evitare NullPointerException
        if (tratta.getTrenches_types() == null) return false;
        if (fk_lines_types_ids == null || fk_lines_types_ids.isEmpty()) return false;
        return fk_lines_types_ids.contains(tratta.getTrenches_types());
    }
}

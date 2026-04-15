package com.geowebframework.underPiping.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ConfigRule {

    private Long id;
    private List<Long> lines_types_ids;
    private Long mat_duct_parent_id;
    private Integer max_range_parent_exi_duct;
    private Integer min_range_parent_exi_duct;
    private Long mat_duct_child_id;
    private Integer max_range_child_exi_duct;
    private Integer min_range_child_exi_duct;
    private int max_duct_number;
    private int priority;
    private Date created_at;
    private Date updated_at;
    private boolean is_deleted;

    public boolean matchesParent(DuctTube parent) {
        if (mat_duct_parent_id != null) return mat_duct_parent_id.equals(parent.getFk_mat_duct());
        int diam = parent.getExsternal_diameter() != null ? parent.getExsternal_diameter() : 0;
        boolean maxOk = max_range_parent_exi_duct == null || diam <= max_range_parent_exi_duct;
        boolean minOk = min_range_parent_exi_duct == null || diam >= min_range_parent_exi_duct;
        return maxOk && minOk;
    }

    public boolean matchesTarget(DuctTube target) {
        if (mat_duct_child_id != null) return mat_duct_child_id.equals(target.getFk_mat_duct());
        int diam = target.getExsternal_diameter() != null ? target.getExsternal_diameter() : 0;
        boolean maxOk = max_range_child_exi_duct == null || diam <= max_range_child_exi_duct;
        boolean minOk = min_range_child_exi_duct == null || diam >= min_range_child_exi_duct;
        return maxOk && minOk;
    }

    public boolean appliesTo(UndergroundRoute tratta) {
        if (tratta.getTrenches_types() == null) return false;
        if (lines_types_ids == null || lines_types_ids.isEmpty()) return false;
        return lines_types_ids.contains(tratta.getTrenches_types());
    }
}
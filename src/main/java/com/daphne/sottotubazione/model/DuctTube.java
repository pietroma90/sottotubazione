package com.geowebframework.underPiping.model;

import lombok.Data;

@Data
public class DuctTube {

    private Long id;
    private Long fk_lines_trenches;
    private Long fk_mat_duct;
    private Integer exsternal_diameter;
    private Long parent_id;
    private int childCount = 0;
    private boolean is_new;
    private boolean is_child;
    private String short_desc_name;
    private boolean isFull;
    private boolean isProcessedChild;

    public void incrementChildCount() {
        this.childCount++;
    }
}
package com.daphne.sottotubazione.domain;

import com.daphne.sottotubazione.domain.enums.TuboStatus;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Tubo "padre" che può contenere altri tubi (sotto-tubazione).
 * Può essere un tubo nuovo (r_lines_products) o un tubo esistente (tubi_esistenti).
 */
@Data
@Entity
@Table(name = "r_lines_products")
public class TuboParent {

    @Id
    @Column(name = "pk_lines_products")
    private Long pkLinesProducts;

    @Column(name = "fk_lines_trenches")
    private Long fkLinesTrenches;

    @Column(name = "fk_mat_duct")
    private Long fkMatDuct;

    @Column(name = "short_descript")
    private String shortDescript;

    /** Numero attuale di tubi target già assegnati a questo parent. */
    @Column(name = "current_assigned_count")
    private int currentAssignedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TuboStatus status;

    /** Diametro esterno (rilevante per tubi esistenti). */
    @Column(name = "external_diameter")
    private Integer externalDiameter;

    public boolean isOccupato() {
        return TuboStatus.OCCUPATO.equals(this.status)
            || TuboStatus.SOTTOTUBATO.equals(this.status);
    }
}

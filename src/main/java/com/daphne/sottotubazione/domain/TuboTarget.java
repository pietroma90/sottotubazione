package com.daphne.sottotubazione.domain;

import com.daphne.sottotubazione.domain.enums.TuboKind;
import com.daphne.sottotubazione.domain.enums.TuboStatus;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Tubo "target" che viene infilato dentro il tubo parent.
 * Può essere nuovo (r_lines_products) o esistente.
 */
@Data
@Entity
@Table(name = "r_lines_products")
public class TuboTarget {

    @Id
    @Column(name = "pk_lines_products")
    private Long pkLinesProducts;

    @Column(name = "fk_lines_trenches")
    private Long fkLinesTrenches;

    @Column(name = "fk_mat_duct")
    private Long fkMatDuct;

    @Column(name = "short_descript")
    private String shortDescript;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind")
    private TuboKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TuboStatus status;

    @Column(name = "external_diameter")
    private Integer externalDiameter;

    public boolean isGiaAssegnato() {
        return TuboStatus.ASSEGNATO.equals(this.status);
    }
}

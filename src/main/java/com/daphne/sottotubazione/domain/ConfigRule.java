package com.daphne.sottotubazione.domain;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.List;

/**
 * Mappa la tabella procedure_config.r_config_fk_parent_duct.
 * Contiene le regole di configurazione per la sotto-tubazione automatica.
 */
@Data
@Entity
@Table(name = "r_config_fk_parent_duct", schema = "procedure_config")
public class ConfigRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(ListArrayType.class)
    @Column(name = "fk_lines_types_ids", columnDefinition = "bigint[]")
    private List<Long> fkLinesTypesIds;

    @Column(name = "fk_mat_duct_parent")
    private Long fkMatDuctParent;

    @Column(name = "tubi_esistenti_ext_max_diam_parent")
    private Integer tubiEsistentiExtMaxDiamParent;

    @Column(name = "tubi_esistenti_ext_min_diam_parent")
    private Integer tubiEsistentiExtMinDiamParent;

    @Column(name = "fk_mat_duct_target")
    private Long fkMatDuctTarget;

    @Column(name = "tubi_esistenti_ext_max_diam_target")
    private Integer tubiEsistentiExtMaxDiamTarget;

    @Column(name = "tubi_esistenti_ext_min_diam_target")
    private Integer tubiEsistentiExtMinDiamTarget;

    @Column(name = "mat_duct_max_number_usable")
    private int matDuctMaxNumberUsable;

    @Column(name = "priority_rules_order")
    private int priorityRulesOrder;

    @Column(name = "isdeleted")
    private boolean isDeleted;

    public boolean matchesParent(TuboParent parent) {
        if (fkMatDuctParent != null) return fkMatDuctParent.equals(parent.getFkMatDuct());
        int diam = parent.getExternalDiameter() != null ? parent.getExternalDiameter() : 0;
        boolean maxOk = tubiEsistentiExtMaxDiamParent == null || diam <= tubiEsistentiExtMaxDiamParent;
        boolean minOk = tubiEsistentiExtMinDiamParent == null || diam >= tubiEsistentiExtMinDiamParent;
        return maxOk && minOk;
    }

    public boolean matchesTarget(TuboTarget target) {
        if (fkMatDuctTarget != null) return fkMatDuctTarget.equals(target.getFkMatDuct());
        int diam = target.getExternalDiameter() != null ? target.getExternalDiameter() : 0;
        boolean maxOk = tubiEsistentiExtMaxDiamTarget == null || diam <= tubiEsistentiExtMaxDiamTarget;
        boolean minOk = tubiEsistentiExtMinDiamTarget == null || diam >= tubiEsistentiExtMinDiamTarget;
        return maxOk && minOk;
    }

    public boolean appliesTo(TrattaInterrata tratta) {
        if (fkLinesTypesIds == null || fkLinesTypesIds.isEmpty()) return true;
        if (tratta.getFkLinesTypesIds() == null) return false;
        return tratta.getFkLinesTypesIds().stream().anyMatch(fkLinesTypesIds::contains);
    }
}

package com.daphne.sottotubazione.domain;

import com.daphne.sottotubazione.domain.enums.TrattaType;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.List;

/**
 * Mappa la tabella prj_lines_trenches.
 * Rappresenta una tratta interrata (scavo nuovo o esistente).
 */
@Data
@Entity
@Table(name = "prj_lines_trenches")
public class TrattaInterrata {

    @Id
    @Column(name = "pk_lines_trenches")
    private Long pkLinesTrenches;

    @Column(name = "name")
    private String name;

    /**
     * Tipo tratta: determina quale Strategy viene utilizzata.
     * Derivato dal join con lines_types.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tratta_type")
    private TrattaType type;

    @Column(name = "fk_project")
    private Long fkProject;

    /**
     * Array di ID tipologie tratta (fk_lines_types_ids).
     * Gestito tramite hypersistence-utils per il tipo array PostgreSQL.
     */
    @Type(ListArrayType.class)
    @Column(name = "fk_lines_types_ids", columnDefinition = "bigint[]")
    private List<Long> fkLinesTypesIds;
}

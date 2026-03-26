package com.daphne.sottotubazione.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

/**
 * Mappa la tabella logging.procedure_output.
 * Colleziona i log prodotti dalla procedura di sotto-tubazione.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "procedure_output", schema = "logging")
public class ProcedureOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_table_name")
    private String entityTableName;

    @Column(name = "fk_entity")
    private Long fkEntity;

    @Column(name = "fk_tratta")
    private Long fkTratta;

    @Column(name = "pk_parent")
    private Long pkParent;

    @Column(name = "parent_descr")
    private String parentDescr;

    @Column(name = "pk_target")
    private Long pkTarget;

    @Column(name = "target_descr")
    private String targetDescr;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.entityTableName = "projects";
    }
}

package com.daphne.sottotubazione.repository;

import com.daphne.sottotubazione.domain.TrattaInterrata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrattaRepository extends JpaRepository<TrattaInterrata, Long> {

    @Query("SELECT t FROM TrattaInterrata t WHERE t.fkProject = :projectId")
    List<TrattaInterrata> findInterrateByProject(@Param("projectId") Long projectId);
}

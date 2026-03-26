package com.daphne.sottotubazione.repository;

import com.daphne.sottotubazione.domain.ProcedureOutput;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcedureOutputRepository extends JpaRepository<ProcedureOutput, Long> {

    List<ProcedureOutput> findByFkEntityOrderByCreatedAtDesc(Long projectId);
}

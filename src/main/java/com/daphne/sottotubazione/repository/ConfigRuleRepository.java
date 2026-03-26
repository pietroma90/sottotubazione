package com.daphne.sottotubazione.repository;

import com.daphne.sottotubazione.domain.ConfigRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConfigRuleRepository extends JpaRepository<ConfigRule, Long> {

    @Query("SELECT r FROM ConfigRule r WHERE r.isDeleted = false ORDER BY r.priorityRulesOrder ASC")
    List<ConfigRule> findActiveOrderedByPriority();
}

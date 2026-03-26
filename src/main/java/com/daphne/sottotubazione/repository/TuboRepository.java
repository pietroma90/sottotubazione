package com.daphne.sottotubazione.repository;

import com.daphne.sottotubazione.domain.TuboParent;
import com.daphne.sottotubazione.domain.TuboTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TuboRepository extends JpaRepository<TuboParent, Long> {

    @Query("SELECT t FROM TuboParent t " +
           "WHERE t.fkLinesTrenches = :trattaId " +
           "AND t.status NOT IN ('OCCUPATO','SOTTOTUBATO')")
    List<TuboParent> findNuoviNonOccupatiByTratta(@Param("trattaId") Long trattaId);

    @Query("SELECT t FROM TuboParent t " +
           "WHERE t.fkLinesTrenches = :trattaId " +
           "AND t.status NOT IN ('OCCUPATO','SOTTOTUBATO') " +
           "AND (:maxDiam IS NULL OR t.externalDiameter <= :maxDiam) " +
           "AND (:minDiam IS NULL OR t.externalDiameter >= :minDiam)")
    List<TuboParent> findEsistentiNonOccupatiByTratta(
        @Param("trattaId") Long trattaId,
        @Param("maxDiam") Integer maxDiam,
        @Param("minDiam") Integer minDiam);

    @Query(value = "SELECT t FROM TuboTarget t " +
                   "WHERE t.fkLinesTrenches = :trattaId " +
                   "AND t.fkMatDuct = :matDuctId " +
                   "AND t.status != 'ASSEGNATO'")
    List<TuboTarget> findTargetNuoviByTrattaAndMat(
        @Param("trattaId") Long trattaId,
        @Param("matDuctId") Long matDuctId);

    @Modifying
    @Query("UPDATE TuboParent t SET t.status = :status, t.currentAssignedCount = :count " +
           "WHERE t.pkLinesProducts = :id")
    void updateParentStatus(
        @Param("id") Long id,
        @Param("status") String status,
        @Param("count") int count);
}

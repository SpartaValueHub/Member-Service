package com.sparta.member_service.adaptor.out.mysql.repository;

import com.sparta.member_service.adaptor.out.mysql.entity.TermEntity;
import com.sparta.member_service.domain.enums.TermCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TermJpaRepository extends JpaRepository<TermEntity, Long> {

    List<TermEntity> findByIsActiveTrue();

    @Query("""
            SELECT t FROM TermEntity t
            WHERE t.isActive = true
              AND t.effectiveAt <= :at
              AND (t.expiredAt IS NULL OR t.expiredAt > :at)
            ORDER BY t.termId ASC
            """)
    List<TermEntity> findCurrentlyEffectiveTerms(@Param("at") Instant at);

    Optional<TermEntity> findByTermCode(TermCode termCode);

    boolean existsByTermCode(TermCode termCode);
}

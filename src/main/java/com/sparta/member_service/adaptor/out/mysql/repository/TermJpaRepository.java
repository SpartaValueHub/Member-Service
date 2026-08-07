package com.sparta.member_service.adaptor.out.mysql.repository;

import com.sparta.member_service.adaptor.out.mysql.entity.TermEntity;
import com.sparta.member_service.domain.enums.TermCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TermJpaRepository extends JpaRepository<TermEntity, Long> {

    List<TermEntity> findByActiveTrue();

    /**
     * DB datetime은 KST(serverTimezone) 기준이므로 Instant 파라미터(UTC) 대신 DB 시각으로 비교한다.
     */
    @Query(
            value = """
                    SELECT t.*
                    FROM terms t
                    WHERE t.is_active = 1
                      AND t.effective_at <= CURRENT_TIMESTAMP(6)
                      AND (t.expired_at IS NULL OR t.expired_at > CURRENT_TIMESTAMP(6))
                    ORDER BY t.term_id ASC
                    """,
            nativeQuery = true
    )
    List<TermEntity> findCurrentlyEffectiveTerms();

    Optional<TermEntity> findByTermCode(TermCode termCode);

    boolean existsByTermCode(TermCode termCode);
}

package com.sparta.member_service.config;

import com.sparta.member_service.adaptor.out.mysql.mapper.TermEntityMapper;
import com.sparta.member_service.adaptor.out.mysql.repository.TermJpaRepository;
import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import com.sparta.member_service.domain.model.TermDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * local/dev — terms 테이블이 비어 있으면 회원가입용 기본 약관 4건을 시드한다.
 * prod 에서는 실행하지 않는다 (수동 SQL 또는 BO 반영).
 */
@Slf4j
@Order(100)
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class TermsDevDataInitializer implements ApplicationRunner {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TermJpaRepository termJpaRepository;
    private final TermEntityMapper termEntityMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (termJpaRepository.count() > 0) {
            return;
        }

        Instant effectiveAt = LocalDate.of(2026, 1, 1).atStartOfDay(KST).toInstant();
        List<TermDomain> seeds = List.of(
                TermDomain.create(
                        TermCode.TERMS_OF_SERVICE,
                        "이용약관",
                        TermType.SERVICE,
                        true,
                        "1.0",
                        """
                                제1조 (목적)
                                본 약관은 Value Hub(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리·의무 및 책임사항을 규정함을 목적으로 합니다.

                                제2조 (회원가입)
                                회원은 본인 명의로 가입해야 하며, 허위 정보를 제공해서는 안 됩니다.""",
                        effectiveAt,
                        null
                ),
                TermDomain.create(
                        TermCode.PRIVACY_POLICY,
                        "개인정보 수집 및 이용",
                        TermType.PRIVACY,
                        true,
                        "1.0",
                        """
                                1. 수집 항목
                                - 필수: 이름, 이메일, 로그인 ID, 비밀번호, 휴대전화번호, 닉네임
                                - 선택: 마케팅 수신 동의 시 연락처

                                2. 이용 목적
                                회원 식별, 서비스 제공, 고객 문의 응대

                                3. 보유 기간
                                회원 탈퇴 시까지 (관련 법령에 따른 보존 기간 제외)""",
                        effectiveAt,
                        null
                ),
                TermDomain.create(
                        TermCode.EMAIL_MARKETING,
                        "이메일 마케팅 수신",
                        TermType.MARKETING,
                        false,
                        "1.0",
                        """
                                이메일을 통해 이벤트·프로모션·신규 서비스 안내를 받을 수 있습니다.
                                동의하지 않아도 서비스 이용에 제한은 없으며, 언제든 수신 거부할 수 있습니다.""",
                        effectiveAt,
                        null
                ),
                TermDomain.create(
                        TermCode.SMS_MARKETING,
                        "SMS 마케팅 수신",
                        TermType.MARKETING,
                        false,
                        "1.0",
                        """
                                SMS(문자)를 통해 이벤트·프로모션·신규 서비스 안내를 받을 수 있습니다.
                                동의하지 않아도 서비스 이용에 제한은 없으며, 언제든 수신 거부할 수 있습니다.""",
                        effectiveAt,
                        null
                )
        );

        seeds.forEach(term -> termJpaRepository.save(termEntityMapper.toEntity(term)));
        log.info("terms 테이블이 비어 있어 local/dev 기본 약관 {}건을 시드했습니다.", seeds.size());
    }
}

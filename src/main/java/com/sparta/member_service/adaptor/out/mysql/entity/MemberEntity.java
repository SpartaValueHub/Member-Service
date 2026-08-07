package com.sparta.member_service.adaptor.out.mysql.entity;

import com.sparta.member_service.domain.enums.MemberGrade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** member 테이블 — member_id(PK) 내부용, 외부 식별자는 member_uuid */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_member_uuid", columnNames = "member_uuid"),
                @UniqueConstraint(name = "uk_member_nickname", columnNames = "nickname")
        }
)
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "member_uuid", nullable = false, length = 36)
    private String memberUuid;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_grade", nullable = false, length = 20)
    private MemberGrade memberGrade;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "is_premium", nullable = false)
    private boolean isPremium;

    @Column(name = "is_regions", nullable = false)
    private boolean isRegions;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    private MemberEntity(
            String memberUuid,
            String nickname,
            String profileImageUrl,
            MemberGrade memberGrade,
            String address,
            boolean isPremium,
            boolean isRegions
    ) {
        this.memberUuid = memberUuid;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.memberGrade = memberGrade;
        this.address = address;
        this.isPremium = isPremium;
        this.isRegions = isRegions;
    }

    /** memberUuid는 생성 후 변경 없음 */
    public void updateProfile(
            String nickname,
            String profileImageUrl,
            MemberGrade memberGrade,
            String address,
            boolean isPremium
    ) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.memberGrade = memberGrade;
        this.address = address;
        this.isPremium = isPremium;
    }
}

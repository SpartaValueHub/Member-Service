package com.sparta.member_service.adaptor.out.mysql.entity;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberEntityTest {

    @Test
    void declaresNamedUniqueConstraintsWithoutColumnUniqueFlag() {
        Table table = MemberEntity.class.getAnnotation(Table.class);

        assertThat(table).isNotNull();
        assertThat(table.uniqueConstraints())
                .extracting(UniqueConstraint::name)
                .containsExactlyInAnyOrder(
                        "uk_member_member_uuid",
                        "uk_member_nickname"
                );
    }
}

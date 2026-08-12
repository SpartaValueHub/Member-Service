package com.sparta.member_service.adaptor.out.mysql.entity;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TermEntityTest {

    @Test
    void declaresNamedUniqueConstraintOnTermCode() {
        Table table = TermEntity.class.getAnnotation(Table.class);

        assertThat(table).isNotNull();
        assertThat(table.uniqueConstraints())
                .extracting(UniqueConstraint::name)
                .containsExactly("uk_terms_term_code");
    }
}

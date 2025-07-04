package io.github.honhimw.example.domain.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * @author honhimW
 * @since 2025-06-27
 */

@Entity(name = "test")
@Table(name = "test")
public class TestDO {

    @EmbeddedId
    private Id id;

    @Column(name = "content")
    private String content;

    public static class Id {
        @Column(name = "first")
        private String first;
        @Column(name = "second")
        private Integer second;
        @Column(name = "third")
        private Integer third;
    }

}

package io.github.honhimw.example.domain.jpa;

import jakarta.persistence.*;

/**
 * @author honhimW
 * @since 2025-06-27
 */

@Entity(name = "test2")
@Table(name = "test2")
public class Test2DO {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content")
    private String content;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private Kind kind;

    public enum Kind {
        FOO, BAR
    }

}

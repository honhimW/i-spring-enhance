package io.github.honhimw.example.domain.jpa.player;

import io.github.honhimw.ddd.jpa.domain.AbstractLogicDeleteAR;
import io.github.honhimw.example.domain.jpa.NameDO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Getter
@Setter
@Entity(name = "player")
@Table(name = "player")
public class PlayerDO extends AbstractLogicDeleteAR<PlayerDO, String> {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "full_name_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
        nullable = false,
        updatable = false
    )
    private NameDO fullName;

    @Column(name = "age")
    private Integer age;

    @Embedded
    private SBD sbd;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class SBD {
        @Column(name = "squat")
        private Integer squat;

        @Column(name = "bench_press")
        private Integer benchPress;

        @Column(name = "dead_lift")
        private Integer deadLift;
    }

}

package io.github.honhimw.example.domain.jpa;

import io.github.honhimw.ddd.jpa.domain.AbstractLogicDeleteAR;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Getter
@Setter
@Entity(name = "person")
@Table(name = "person")
public class PersonDO extends AbstractLogicDeleteAR<PersonDO, String> {

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

}

package org.example.domain;

import io.github.honhimw.ddd.jpa.domain.AbstractAR;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Getter
@Setter
@Entity(name = "full_name")
@Table(name = "full_name")
public class NameDO extends AbstractAR<NameDO, String> {

    @Id
    private String id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

}

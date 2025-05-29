package io.github.honhimw.example.domain.jpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameDTO implements Serializable {

    private String id;

    private String firstName;

    private String lastName;

}

package io.github.honhimw.example.domain.jimmer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2025-03-18
 */

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SBDDTO implements Serializable {

    private Integer squat;

    private Integer benchPress;

    private Integer deadLift;

}

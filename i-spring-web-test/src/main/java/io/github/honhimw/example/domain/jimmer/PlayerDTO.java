package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.core.AuditorDTO;
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
public class PlayerDTO implements Serializable {
    
    private String id;
    
    private Integer age;
    
    private NameDTO fullName;

    private SBDDTO sbd;

    private AuditorDTO auditor;
    
}

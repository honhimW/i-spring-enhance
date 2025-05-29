package io.github.honhimw.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author hon_him
 * @since 2022-12-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditorDTO implements Serializable {

    @Schema(description = "created at")
    private LocalDateTime createdAt;

    @Schema(description = "updated at")
    private LocalDateTime updatedAt;

    @Schema(description = "created by")
    private String createdBy;

    @Schema(description = "updated by")
    private String updatedBy;

}

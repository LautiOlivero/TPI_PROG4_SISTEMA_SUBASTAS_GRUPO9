package com.prog.tpi.sistema_subastas.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputaRequestDTO {

    @NotNull(message = "{err.req}")
    private Long subastaId;

    @NotBlank(message = "{err.req}")
    private String motivo;

    @NotBlank(message = "{err.req}")
    private String descripcion;
}

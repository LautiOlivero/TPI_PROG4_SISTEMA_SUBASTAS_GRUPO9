package com.prog.tpi.sistema_subastas.dtos.request;

import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoRequestDTO {

    @NotNull(message = "{err.req}")
    private Long subastaId;

    private EstadoSubasta estadoAnterior;

    @NotNull(message = "{err.req}")
    private EstadoSubasta estadoNuevo;

    private String motivo;
}

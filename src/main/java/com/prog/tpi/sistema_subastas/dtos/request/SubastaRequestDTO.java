package com.prog.tpi.sistema_subastas.dtos.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubastaRequestDTO {

    @NotNull(message = "{err.req}")
    private Long productoId;

    @NotNull(message = "{err.req}")
    @Positive(message = "{err.pos}")
    private BigDecimal precioBase;

    @NotNull(message = "{err.req}")
    @Positive(message = "{err.pos}")
    private BigDecimal incrementoFijo;

    @NotNull(message = "{err.req}")
    private Instant fechaInicio;

    @NotNull(message = "{err.req}")
    @Future(message = "{err.fut}")
    private Instant fechaCierre;

    private String descripcion;
}

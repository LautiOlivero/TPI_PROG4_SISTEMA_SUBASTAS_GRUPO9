package com.prog.tpi.sistema_subastas.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PujaRequestDTO {

    @NotNull(message = "{err.req}")
    private Long subastaId;

    @NotNull(message = "{err.req}")
    @Positive(message = "{err.pos}")
    private BigDecimal montoOfertado;
}

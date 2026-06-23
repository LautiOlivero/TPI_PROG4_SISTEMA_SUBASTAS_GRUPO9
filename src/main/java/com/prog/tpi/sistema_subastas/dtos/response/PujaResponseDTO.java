package com.prog.tpi.sistema_subastas.dtos.response;

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
public class PujaResponseDTO {

    private Long id;
    private Long subastaId;
    private UsuarioResponseDTO usuarioOferente;
    private BigDecimal montoOfertado;
    private Instant fechaPuja;
}

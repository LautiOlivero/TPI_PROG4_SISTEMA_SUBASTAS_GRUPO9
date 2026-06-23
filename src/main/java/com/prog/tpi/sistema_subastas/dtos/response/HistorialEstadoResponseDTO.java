package com.prog.tpi.sistema_subastas.dtos.response;

import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoResponseDTO {

    private Long id;
    private Long subastaId;
    private EstadoSubasta estadoAnterior;
    private EstadoSubasta estadoNuevo;
    private Instant fecha;
    private UsuarioResponseDTO usuarioResponsable;
    private String motivo;
}

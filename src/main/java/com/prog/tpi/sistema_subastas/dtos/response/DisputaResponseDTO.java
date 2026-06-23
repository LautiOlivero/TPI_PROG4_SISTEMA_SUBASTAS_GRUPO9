package com.prog.tpi.sistema_subastas.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputaResponseDTO {

    private Long id;
    private Long subastaId;
    private UsuarioResponseDTO usuarioInicio;
    private String motivo;
    private String descripcion;
    private Instant fechaCreacion;
    private String resolucionAdministrativa;
}

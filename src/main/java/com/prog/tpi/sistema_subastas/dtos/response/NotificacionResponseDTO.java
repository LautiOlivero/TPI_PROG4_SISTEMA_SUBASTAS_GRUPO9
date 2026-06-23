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
public class NotificacionResponseDTO {

    private Long id;
    private Long usuarioDestinoId;
    private String mensaje;
    private Boolean leido;
    private Instant fecha;
}

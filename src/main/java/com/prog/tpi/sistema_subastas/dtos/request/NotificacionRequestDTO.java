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
public class NotificacionRequestDTO {

    @NotNull(message = "{err.req}")
    private Long usuarioDestinoId;

    @NotBlank(message = "{err.req}")
    private String mensaje;
}

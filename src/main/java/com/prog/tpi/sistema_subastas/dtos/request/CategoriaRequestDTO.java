package com.prog.tpi.sistema_subastas.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaRequestDTO {

    @NotBlank(message = "{err.req}")
    private String nombre;

    private String descripcion;
}

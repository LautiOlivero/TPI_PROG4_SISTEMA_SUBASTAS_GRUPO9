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
public class ProductoRequestDTO {

    @NotBlank(message = "{err.req}")
    private String nombre;

    private String descripcion;

    private String imagenUrl;

    @NotNull(message = "{err.req}")
    private Long categoriaId;

    @NotNull(message = "{err.req}")
    private Long vendedorId;
}

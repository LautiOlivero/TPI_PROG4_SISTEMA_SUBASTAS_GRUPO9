package com.prog.tpi.sistema_subastas.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private CategoriaResponseDTO categoria;
    private UsuarioResponseDTO vendedor;
}

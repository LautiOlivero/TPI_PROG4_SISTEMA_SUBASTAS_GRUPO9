package com.prog.tpi.sistema_subastas.dtos.response;

import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
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
public class SubastaResponseDTO {

    private Long id;
    private ProductoResponseDTO producto;
    private BigDecimal precioBase;
    private BigDecimal montoActual;
    private BigDecimal incrementoFijo;
    private Instant fechaInicio;
    private Instant fechaCierre;
    private String descripcion;
    private EstadoSubasta estado;
    private UsuarioResponseDTO vendedor;
    private UsuarioResponseDTO ganadorActual;
    private Long version;
}

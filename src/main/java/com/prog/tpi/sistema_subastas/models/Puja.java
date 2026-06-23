package com.prog.tpi.sistema_subastas.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pujas")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Puja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Subasta subasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_oferente_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuarioOferente;

    @Column(name = "monto_ofertado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoOfertado;

    @Column(name = "fecha_puja", nullable = false)
    private Instant fechaPuja;
}

package com.prog.tpi.sistema_subastas.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subastas")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Subasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", unique = true, nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "monto_actual", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoActual;

    @Column(name = "incremento_fijo", nullable = false, precision = 12, scale = 2)
    private BigDecimal incrementoFijo;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_cierre", nullable = false)
    private Instant fechaCierre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSubasta estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ganador_actual_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario ganadorActual;

    @Version
    @Column(nullable = false)
    private Long version;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "subasta", fetch = FetchType.LAZY)
    private List<Puja> pujas = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "subasta", fetch = FetchType.LAZY)
    private List<HistorialEstado> historiales = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "subasta", fetch = FetchType.LAZY)
    private Disputa disputa;
}

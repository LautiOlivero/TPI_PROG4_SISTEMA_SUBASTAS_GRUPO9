package com.prog.tpi.sistema_subastas.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_destino_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuarioDestino;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Builder.Default
    @Column(nullable = false)
    private Boolean leido = false;

    @Column(nullable = false)
    private Instant fecha;
}

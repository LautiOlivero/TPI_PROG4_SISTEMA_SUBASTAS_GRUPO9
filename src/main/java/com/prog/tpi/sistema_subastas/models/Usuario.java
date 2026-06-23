package com.prog.tpi.sistema_subastas.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username_email", nullable = false, unique = true, length = 150)
    private String usernameEmail;

    @Column(nullable = false, length = 255)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private Boolean bloqueado = false;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarios_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private List<Rol> roles = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "vendedor", fetch = FetchType.LAZY)
    private List<Producto> productosRegistrados = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "vendedor", fetch = FetchType.LAZY)
    private List<Subasta> subastasCreadas = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "ganadorActual", fetch = FetchType.LAZY)
    private List<Subasta> subastasGanadas = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "usuarioOferente", fetch = FetchType.LAZY)
    private List<Puja> pujas = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "usuarioResponsable", fetch = FetchType.LAZY)
    private List<HistorialEstado> historialesEstados = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "usuarioDestino", fetch = FetchType.LAZY)
    private List<Notificacion> notificaciones = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "usuarioInicio", fetch = FetchType.LAZY)
    private List<Disputa> disputasAbiertas = new ArrayList<>();

}

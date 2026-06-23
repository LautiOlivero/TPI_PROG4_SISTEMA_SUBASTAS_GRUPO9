package com.prog.tpi.sistema_subastas.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
}

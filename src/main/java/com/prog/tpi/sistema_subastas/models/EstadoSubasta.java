package com.prog.tpi.sistema_subastas.models;

public enum EstadoSubasta {
    BORRADOR,
    PUBLICADA,
    ACTIVA,
    ADJUDICADA,   // Cerrada con ganador
    FINALIZADA,   // Cerrada sin pujas
    CANCELADA
}

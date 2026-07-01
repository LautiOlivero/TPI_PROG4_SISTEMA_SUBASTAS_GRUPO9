package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
import com.prog.tpi.sistema_subastas.models.HistorialEstado;
import com.prog.tpi.sistema_subastas.models.Subasta;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.HistorialEstadoRepository;
import com.prog.tpi.sistema_subastas.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class HistorialEstadoService {

    private final HistorialEstadoRepository historialEstadoRepository;

    public HistorialEstadoService(HistorialEstadoRepository historialEstadoRepository) {
        this.historialEstadoRepository = historialEstadoRepository;
    }

    @Transactional
    public void registrarCambio(Subasta subasta, EstadoSubasta estadoAnterior, EstadoSubasta estadoNuevo, String motivo) {
        Usuario usuarioActual = null;
        try {
            usuarioActual = SecurityUtils.getUsuarioActual();
        } catch (Exception e) {
            // En caso de que el cambio sea por un job automático en el futuro
        }

        HistorialEstado historial = HistorialEstado.builder()
                .subasta(subasta)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .fecha(Instant.now())
                .usuarioResponsable(usuarioActual)
                .motivo(motivo)
                .build();

        historialEstadoRepository.save(historial);
    }
}

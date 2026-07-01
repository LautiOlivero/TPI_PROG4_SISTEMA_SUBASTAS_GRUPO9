package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.request.DisputaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.DisputaResponseDTO;
import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.*;
import com.prog.tpi.sistema_subastas.repositories.DisputaRepository;
import com.prog.tpi.sistema_subastas.repositories.SubastaRepository;
import com.prog.tpi.sistema_subastas.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisputaService {

    private final DisputaRepository disputaRepository;
    private final SubastaRepository subastaRepository;
    private final NotificacionService notificacionService;

    public DisputaService(DisputaRepository disputaRepository, SubastaRepository subastaRepository, NotificacionService notificacionService) {
        this.disputaRepository = disputaRepository;
        this.subastaRepository = subastaRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public DisputaResponseDTO crearDisputa(DisputaRequestDTO dto, Usuario solicitante) {
        Subasta subasta = subastaRepository.findById(dto.getSubastaId())
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada con ID: " + dto.getSubastaId()));

        // Solo se puede disputar una subasta ADJUDICADA
        if (subasta.getEstado() != EstadoSubasta.ADJUDICADA) {
            throw new ReglaNegocioException(
                    "Solo se pueden crear disputas sobre subastas ADJUDICADAS. Estado actual: " + subasta.getEstado());
        }

        // Solo el ganador o el vendedor pueden abrir una disputa
        boolean esGanador = subasta.getGanadorActual() != null
                && subasta.getGanadorActual().getId().equals(solicitante.getId());
        boolean esVendedor = subasta.getVendedor().getId().equals(solicitante.getId());

        if (!esGanador && !esVendedor) {
            throw new ReglaNegocioException("Solo el ganador o el vendedor pueden abrir una disputa.");
        }

        if (subasta.getDisputa() != null) {
            throw new ReglaNegocioException("Ya existe una disputa abierta para esta subasta.");
        }

        Disputa disputa = Disputa.builder()
                .subasta(subasta)
                .usuarioInicio(solicitante)
                .motivo(dto.getMotivo())
                .descripcion(dto.getDescripcion())
                .fechaCreacion(Instant.now())
                .build();

        Disputa guardada = disputaRepository.save(disputa);

        // Notificar a la otra parte
        Usuario otraParte = esVendedor ? subasta.getGanadorActual() : subasta.getVendedor();
        if (otraParte != null) {
            notificacionService.enviarNotificacion(
                    otraParte,
                    "Se ha abierto una disputa en la subasta '" + subasta.getProducto().getNombre() + "'."
            );
        }

        return DtoMapper.toDisputaDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<DisputaResponseDTO> listarTodas() {
        return disputaRepository.findAll().stream()
                .map(DtoMapper::toDisputaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DisputaResponseDTO obtenerPorId(Long id) {
        return disputaRepository.findById(id)
                .map(DtoMapper::toDisputaDTO)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada con ID: " + id));
    }

    @Transactional
    public DisputaResponseDTO resolver(Long id, String resolucion) {
        Disputa disputa = disputaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada con ID: " + id));

        if (disputa.getResolucionAdministrativa() != null && !disputa.getResolucionAdministrativa().isEmpty()) {
            throw new ReglaNegocioException("Esta disputa ya fue resuelta.");
        }

        disputa.setResolucionAdministrativa(resolucion);
        Disputa guardada = disputaRepository.save(disputa);
        
        // Notificar a ambas partes
        notificacionService.enviarNotificacion(
                disputa.getSubasta().getVendedor(),
                "La disputa de la subasta '" + disputa.getSubasta().getProducto().getNombre() + "' ha sido resuelta por administración."
        );
        if (disputa.getSubasta().getGanadorActual() != null) {
            notificacionService.enviarNotificacion(
                    disputa.getSubasta().getGanadorActual(),
                    "La disputa de la subasta '" + disputa.getSubasta().getProducto().getNombre() + "' ha sido resuelta por administración."
            );
        }
        
        return DtoMapper.toDisputaDTO(guardada);
    }
}

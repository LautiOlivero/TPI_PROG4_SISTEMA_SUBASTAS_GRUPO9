package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.request.DisputaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.DisputaResponseDTO;
import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.Disputa;
import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
import com.prog.tpi.sistema_subastas.models.Subasta;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.DisputaRepository;
import com.prog.tpi.sistema_subastas.repositories.SubastaRepository;
import com.prog.tpi.sistema_subastas.security.SecurityUtils;
import com.prog.tpi.sistema_subastas.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public DisputaResponseDTO crearDisputa(DisputaRequestDTO dto) {
        Subasta subasta = subastaRepository.findById(dto.getSubastaId())
                .orElseThrow(() -> new ReglaNegocioException("Subasta no encontrada."));

        if (subasta.getEstado() != EstadoSubasta.CERRADA) {
            throw new ReglaNegocioException("Solo se pueden abrir disputas en subastas CERRADAS.");
        }

        Usuario usuarioActual = SecurityUtils.getUsuarioActual();
        boolean esVendedor = subasta.getVendedor().getId().equals(usuarioActual.getId());
        boolean esGanador = subasta.getGanadorActual() != null && subasta.getGanadorActual().getId().equals(usuarioActual.getId());

        if (!esVendedor && !esGanador) {
            throw new ReglaNegocioException("Solo el vendedor o el ganador pueden abrir una disputa para esta subasta.");
        }

        if (subasta.getDisputa() != null) {
            throw new ReglaNegocioException("Ya existe una disputa abierta para esta subasta.");
        }

        Disputa disputa = DtoMapper.toDisputa(dto);
        disputa.setSubasta(subasta);
        disputa.setUsuarioInicio(usuarioActual);

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

    @Transactional
    public DisputaResponseDTO resolverDisputa(Long id, String resolucion) {
        Disputa disputa = disputaRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Disputa no encontrada."));

        if (disputa.getResolucionAdministrativa() != null && !disputa.getResolucionAdministrativa().isEmpty()) {
            throw new ReglaNegocioException("La disputa ya fue resuelta.");
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

    @Transactional(readOnly = true)
    public List<DisputaResponseDTO> listarTodas() {
        return disputaRepository.findAll().stream()
                .map(DtoMapper::toDisputaDTO)
                .collect(Collectors.toList());
    }
}

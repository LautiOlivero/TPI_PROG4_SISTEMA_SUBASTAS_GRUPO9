package com.prog.tpi.sistema_subastas.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
import com.prog.tpi.sistema_subastas.security.SecurityUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prog.tpi.sistema_subastas.dtos.request.SubastaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.SubastaResponseDTO;
import com.prog.tpi.sistema_subastas.models.Producto;
import com.prog.tpi.sistema_subastas.models.Subasta;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.ProductoRepository;
import com.prog.tpi.sistema_subastas.repositories.SubastaRepository;
import com.prog.tpi.sistema_subastas.util.DtoMapper;

@Service
public class SubastaService {
    private final SubastaRepository subastaRepository;
    private final ProductoRepository productoRepository;
    private final HistorialEstadoService historialEstadoService;

    public SubastaService(SubastaRepository subastaRepository,
            ProductoRepository productoRepository,
            HistorialEstadoService historialEstadoService) {
        this.subastaRepository = subastaRepository;
        this.productoRepository = productoRepository;
        this.historialEstadoService = historialEstadoService;
    }

    @Transactional
    public SubastaResponseDTO crearSubasta(SubastaRequestDTO dto) {
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + dto.getProductoId()));

        Usuario vendedor = SecurityUtils.getUsuarioActual();

        Subasta subasta = DtoMapper.toSubasta(dto);
        subasta.setProducto(producto);
        subasta.setVendedor(vendedor);
        subasta.setEstado(EstadoSubasta.BORRADOR);
        subasta.setMontoActual(subasta.getPrecioBase());

        Subasta guardada = subastaRepository.save(subasta);

        historialEstadoService.registrarCambio(guardada, null, EstadoSubasta.BORRADOR, "Creación de subasta");

        return DtoMapper.toSubastaDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<SubastaResponseDTO> obtenerSubastas() {
        return subastaRepository.findAllConDetalles().stream()
                .map(DtoMapper::toSubastaDTO)
                .sorted((subasta1, subasta2) -> subasta2.getFechaInicio().compareTo(subasta1.getFechaInicio()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubastaResponseDTO> obtenerMisSubastas() {
        Usuario vendedorActual = SecurityUtils.getUsuarioActual();
        return subastaRepository.findByVendedorId(vendedorActual.getId()).stream()
                .map(DtoMapper::toSubastaDTO)
                .sorted((subasta1, subasta2) -> subasta2.getFechaInicio().compareTo(subasta1.getFechaInicio()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubastaResponseDTO obtenerPorId(Long id) {
        return subastaRepository.findById(id)
                .map(DtoMapper::toSubastaDTO)
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada con ID: " + id));
    }

    @Transactional
    public SubastaResponseDTO publicarSubasta(Long id) {
        Subasta subasta = subastaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada con ID: " + id));

        if (subasta.getEstado() != EstadoSubasta.BORRADOR) {
            throw new ReglaNegocioException("Solo se pueden publicar subastas en estado BORRADOR.");
        }

        Instant minimoInicio = Instant.now().plus(48, ChronoUnit.HOURS);
        if (subasta.getFechaInicio().isBefore(minimoInicio)) {
            throw new ReglaNegocioException(
                    "La fecha de inicio debe ser al menos 48 horas posterior al momento de publicación para permitir su moderación.");
        }

        subasta.setEstado(EstadoSubasta.PUBLICADA);
        Subasta guardada = subastaRepository.save(subasta);

        historialEstadoService.registrarCambio(guardada, EstadoSubasta.BORRADOR, EstadoSubasta.PUBLICADA,
                "Publicación por vendedor");

        return DtoMapper.toSubastaDTO(guardada);
    }

    @Transactional
    public SubastaResponseDTO cancelarSubasta(Long id) {
        Subasta subasta = subastaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada con ID: " + id));

        Usuario usuarioActual = SecurityUtils.getUsuarioActual();
        boolean esAdmin = usuarioActual.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (subasta.getEstado() == EstadoSubasta.ACTIVA && !esAdmin) {
            throw new ReglaNegocioException("La subasta ya está ACTIVA. Solo un administrador puede cancelarla.");
        }

        if (subasta.getEstado() == EstadoSubasta.ADJUDICADA || subasta.getEstado() == EstadoSubasta.FINALIZADA
                || subasta.getEstado() == EstadoSubasta.CANCELADA) {
            throw new ReglaNegocioException(
                    "La subasta ya se encuentra " + subasta.getEstado() + " y no puede ser cancelada.");
        }

        EstadoSubasta estadoAnterior = subasta.getEstado();
        subasta.setEstado(EstadoSubasta.CANCELADA);
        Subasta guardada = subastaRepository.save(subasta);

        historialEstadoService.registrarCambio(guardada, estadoAnterior, EstadoSubasta.CANCELADA,
                "Cancelada por " + (esAdmin ? "Administrador" : "Vendedor"));

        return DtoMapper.toSubastaDTO(guardada);
    }
}

package com.prog.tpi.sistema_subastas.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.EstadoSubasta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prog.tpi.sistema_subastas.dtos.request.SubastaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.SubastaResponseDTO;
import com.prog.tpi.sistema_subastas.models.Producto;
import com.prog.tpi.sistema_subastas.models.Subasta;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.ProductoRepository;
import com.prog.tpi.sistema_subastas.repositories.SubastaRepository;
import com.prog.tpi.sistema_subastas.repositories.UsuarioRepository;
import com.prog.tpi.sistema_subastas.util.DtoMapper;

@Service
public class SubastaService {
    private final SubastaRepository subastaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public SubastaService(SubastaRepository subastaRepository, ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {
        this.subastaRepository = subastaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * MOCK TEMPORAL: Devuelve el Usuario ID = 1 para simular al vendedor
     * autenticado.
     * En la Fase 3, este método leerá el token JWT de Spring Security.
     */
    private Usuario obtenerVendedorMock() {
        return usuarioRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario mock (ID=1). Ejecute el seeder."));
    }

    @Transactional
    public SubastaResponseDTO crearSubasta(SubastaRequestDTO dto) {
        // 1. Validar Producto
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + dto.getProductoId()));

        // 2. Obtener vendedor mockeado
        Usuario vendedor = obtenerVendedorMock();

        // 3. Crear Entidad
        Subasta subasta = DtoMapper.toSubasta(dto);
        subasta.setProducto(producto);
        subasta.setVendedor(vendedor);
        subasta.setEstado(EstadoSubasta.BORRADOR); // Estado inicial forzado
        subasta.setMontoActual(subasta.getPrecioBase()); // El monto inicial de la subasta es su precio base

        // 4. Guardar
        Subasta guardada = subastaRepository.save(subasta);
        return DtoMapper.toSubastaDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<SubastaResponseDTO> obtenerSubastas() {
        return subastaRepository.findAll().stream()
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

        // Validación de fechas para el periodo de moderación
        // La fecha de inicio debe ser al menos 48 hs después de la publicación, para dar un período de moderación
        Instant minimoInicio = Instant.now().plus(48, ChronoUnit.HOURS);
        if (subasta.getFechaInicio().isBefore(minimoInicio)) {
            throw new ReglaNegocioException("La fecha de inicio debe ser al menos 48 horas posterior al momento de publicación para permitir su moderación.");
        }

        subasta.setEstado(EstadoSubasta.PUBLICADA);
        Subasta guardada = subastaRepository.save(subasta);
        return DtoMapper.toSubastaDTO(guardada);
    }

    @Transactional
    public SubastaResponseDTO cancelarSubasta(Long id) {
        Subasta subasta = subastaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada con ID: " + id));

        // TODO: Fase 3 - Integrar validación real de roles desde el token JWT.
        // Si el usuario actual es VENDEDOR y la subasta está ACTIVA, lanzar excepción.
        // Si el usuario es ADMIN, permitir la cancelación en cualquier momento.
        // Por ahora simulamos que el usuario logueado (vendedor) no puede cancelar si está activa.
        if (subasta.getEstado() == EstadoSubasta.ACTIVA) {
            throw new ReglaNegocioException("La subasta ya está ACTIVA. Solo un administrador puede cancelarla.");
        }

        if (subasta.getEstado() == EstadoSubasta.CERRADA || subasta.getEstado() == EstadoSubasta.CANCELADA) {
            throw new ReglaNegocioException("La subasta ya se encuentra " + subasta.getEstado() + " y no puede ser cancelada.");
        }

        subasta.setEstado(EstadoSubasta.CANCELADA);
        Subasta guardada = subastaRepository.save(subasta);
        return DtoMapper.toSubastaDTO(guardada);
    }
}

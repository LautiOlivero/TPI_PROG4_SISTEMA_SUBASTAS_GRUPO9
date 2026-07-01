package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.request.PujaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.PujaResponseDTO;
import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
import com.prog.tpi.sistema_subastas.models.Puja;
import com.prog.tpi.sistema_subastas.models.Subasta;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.PujaRepository;
import com.prog.tpi.sistema_subastas.repositories.SubastaRepository;
import com.prog.tpi.sistema_subastas.security.SecurityUtils;
import com.prog.tpi.sistema_subastas.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PujaService {

    private final PujaRepository pujaRepository;
    private final SubastaRepository subastaRepository;

    public PujaService(PujaRepository pujaRepository, SubastaRepository subastaRepository) {
        this.pujaRepository = pujaRepository;
        this.subastaRepository = subastaRepository;
    }

    @Transactional
    public PujaResponseDTO crearPuja(PujaRequestDTO dto) {
        Subasta subasta = subastaRepository.findById(dto.getSubastaId())
                .orElseThrow(() -> new ReglaNegocioException("Subasta no encontrada."));

        if (subasta.getEstado() != EstadoSubasta.ACTIVA) {
            throw new ReglaNegocioException("Solo se pueden realizar pujas en subastas ACTIVAS.");
        }

        Usuario usuarioOferente = SecurityUtils.getUsuarioActual();

        if (subasta.getVendedor().getId().equals(usuarioOferente.getId())) {
            throw new ReglaNegocioException("El vendedor no puede pujar en su propia subasta.");
        }

        // El nuevo monto se calcula automáticamente
        BigDecimal nuevoMonto = subasta.getMontoActual().add(subasta.getIncrementoFijo());

        subasta.setMontoActual(nuevoMonto);
        subasta.setGanadorActual(usuarioOferente);

        Puja puja = DtoMapper.toPuja(dto);
        puja.setSubasta(subasta);
        puja.setUsuarioOferente(usuarioOferente);
        puja.setMontoOfertado(nuevoMonto);

        Puja guardada = pujaRepository.save(puja);
        subastaRepository.save(subasta);

        // Las notificaciones se reservan para cuando la subasta finalice

        return DtoMapper.toPujaDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<PujaResponseDTO> obtenerPujasPorSubasta(Long subastaId) {
        if (!subastaRepository.existsById(subastaId)) {
            throw new ReglaNegocioException("Subasta no encontrada.");
        }

        return pujaRepository.findBySubastaIdOrderByFechaPujaDesc(subastaId).stream()
                .map(DtoMapper::toPujaDTO)
                .collect(Collectors.toList());
    }
}

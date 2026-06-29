package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.request.CategoriaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.CategoriaResponseDTO;
import com.prog.tpi.sistema_subastas.models.Categoria;
import com.prog.tpi.sistema_subastas.repositories.CategoriaRepository;
import com.prog.tpi.sistema_subastas.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(DtoMapper::toCategoriaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO dto) {
        Categoria categoria = DtoMapper.toCategoria(dto);
        Categoria guardada = categoriaRepository.save(categoria);
        return DtoMapper.toCategoriaDTO(guardada);
    }
}

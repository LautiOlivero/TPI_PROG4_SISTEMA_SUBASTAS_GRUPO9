package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.response.UsuarioResponseDTO;
import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.UsuarioRepository;
import com.prog.tpi.sistema_subastas.security.SecurityUtils;
import com.prog.tpi.sistema_subastas.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPerfilActual() {
        Usuario usuarioActual = SecurityUtils.getUsuarioActual();
        return DtoMapper.toUsuarioDTO(usuarioActual);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(DtoMapper::toUsuarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponseDTO cambiarEstadoBloqueo(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado con ID: " + id));

        Usuario usuarioActual = SecurityUtils.getUsuarioActual();
        if (usuario.getId().equals(usuarioActual.getId())) {
            throw new ReglaNegocioException("Un administrador no puede bloquearse a sí mismo.");
        }

        usuario.setBloqueado(!usuario.getBloqueado());
        Usuario guardado = usuarioRepository.save(usuario);
        
        return DtoMapper.toUsuarioDTO(guardado);
    }
}

package com.prog.tpi.sistema_subastas.util;

import com.prog.tpi.sistema_subastas.dtos.request.*;
import com.prog.tpi.sistema_subastas.dtos.response.*;
import com.prog.tpi.sistema_subastas.models.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {

    public static Rol toRol(RolRequestDTO dto) {
        return Rol.builder()
                .nombre(dto.getNombre())
                .build();
    }

    public static RolResponseDTO toRolDTO(Rol rol) {
        return RolResponseDTO.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .build();
    }

    public static Usuario toUsuario(UsuarioRequestDTO dto) {
        Usuario usuario = Usuario.builder()
                .usernameEmail(dto.getUsernameEmail())
                .password(dto.getPassword())
                .build();

        if (dto.getRolesIds() != null) {
            List<Rol> roles = dto.getRolesIds().stream()
                    .map(id -> Rol.builder().id(id).build())
                    .collect(Collectors.toList());
            usuario.setRoles(roles);
        }

        return usuario;
    }

    public static UsuarioResponseDTO toUsuarioDTO(Usuario usuario) {
        List<RolResponseDTO> roles = Collections.emptyList();
        if (usuario.getRoles() != null) {
            roles = usuario.getRoles().stream()
                    .map(DtoMapper::toRolDTO)
                    .collect(Collectors.toList());
        }

        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .usernameEmail(usuario.getUsernameEmail())
                .bloqueado(usuario.getBloqueado())
                .roles(roles)
                .build();
    }

    public static Categoria toCategoria(CategoriaRequestDTO dto) {
        return Categoria.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
    }

    public static CategoriaResponseDTO toCategoriaDTO(Categoria categoria) {
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }

    public static Producto toProducto(ProductoRequestDTO dto) {
        return Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .categoria(Categoria.builder().id(dto.getCategoriaId()).build())
                .vendedor(Usuario.builder().id(dto.getVendedorId()).build())
                .build();
    }

    public static ProductoResponseDTO toProductoDTO(Producto producto) {
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .categoria(toCategoriaDTO(producto.getCategoria()))
                .vendedor(toUsuarioDTO(producto.getVendedor()))
                .build();
    }

    public static Subasta toSubasta(SubastaRequestDTO dto) {
        return Subasta.builder()
                .producto(Producto.builder().id(dto.getProductoId()).build())
                .precioBase(dto.getPrecioBase())
                .montoActual(dto.getPrecioBase())
                .incrementoFijo(dto.getIncrementoFijo())
                .fechaInicio(dto.getFechaInicio())
                .fechaCierre(dto.getFechaCierre())
                .descripcion(dto.getDescripcion())
                .estado(EstadoSubasta.BORRADOR)
                .version(0L)
                .build();
    }

    public static SubastaResponseDTO toSubastaDTO(Subasta subasta) {
        UsuarioResponseDTO ganadorActual = null;
        if (subasta.getGanadorActual() != null) {
            ganadorActual = toUsuarioDTO(subasta.getGanadorActual());
        }

        return SubastaResponseDTO.builder()
                .id(subasta.getId())
                .producto(toProductoDTO(subasta.getProducto()))
                .precioBase(subasta.getPrecioBase())
                .montoActual(subasta.getMontoActual())
                .incrementoFijo(subasta.getIncrementoFijo())
                .fechaInicio(subasta.getFechaInicio())
                .fechaCierre(subasta.getFechaCierre())
                .descripcion(subasta.getDescripcion())
                .estado(subasta.getEstado())
                .vendedor(toUsuarioDTO(subasta.getVendedor()))
                .ganadorActual(ganadorActual)
                .version(subasta.getVersion())
                .build();
    }

    public static Puja toPuja(PujaRequestDTO dto) {
        return Puja.builder()
                .subasta(Subasta.builder().id(dto.getSubastaId()).build())
                .montoOfertado(dto.getMontoOfertado())
                .fechaPuja(Instant.now())
                .build();
    }

    public static PujaResponseDTO toPujaDTO(Puja puja) {
        return PujaResponseDTO.builder()
                .id(puja.getId())
                .subastaId(puja.getSubasta().getId())
                .usuarioOferente(toUsuarioDTO(puja.getUsuarioOferente()))
                .montoOfertado(puja.getMontoOfertado())
                .fechaPuja(puja.getFechaPuja())
                .build();
    }

    public static HistorialEstado toHistorialEstado(HistorialEstadoRequestDTO dto) {
        return HistorialEstado.builder()
                .subasta(Subasta.builder().id(dto.getSubastaId()).build())
                .estadoAnterior(dto.getEstadoAnterior())
                .estadoNuevo(dto.getEstadoNuevo())
                .fecha(Instant.now())
                .motivo(dto.getMotivo())
                .build();
    }

    public static HistorialEstadoResponseDTO toHistorialEstadoDTO(HistorialEstado historial) {
        return HistorialEstadoResponseDTO.builder()
                .id(historial.getId())
                .subastaId(historial.getSubasta().getId())
                .estadoAnterior(historial.getEstadoAnterior())
                .estadoNuevo(historial.getEstadoNuevo())
                .fecha(historial.getFecha())
                .usuarioResponsable(toUsuarioDTO(historial.getUsuarioResponsable()))
                .motivo(historial.getMotivo())
                .build();
    }

    public static Notificacion toNotificacion(NotificacionRequestDTO dto) {
        return Notificacion.builder()
                .usuarioDestino(Usuario.builder().id(dto.getUsuarioDestinoId()).build())
                .mensaje(dto.getMensaje())
                .leido(false)
                .fecha(Instant.now())
                .build();
    }

    public static NotificacionResponseDTO toNotificacionDTO(Notificacion notificacion) {
        return NotificacionResponseDTO.builder()
                .id(notificacion.getId())
                .usuarioDestinoId(notificacion.getUsuarioDestino().getId())
                .mensaje(notificacion.getMensaje())
                .leido(notificacion.getLeido())
                .fecha(notificacion.getFecha())
                .build();
    }

    public static Disputa toDisputa(DisputaRequestDTO dto) {
        return Disputa.builder()
                .subasta(Subasta.builder().id(dto.getSubastaId()).build())
                .motivo(dto.getMotivo())
                .descripcion(dto.getDescripcion())
                .fechaCreacion(Instant.now())
                .build();
    }

    public static DisputaResponseDTO toDisputaDTO(Disputa disputa) {
        return DisputaResponseDTO.builder()
                .id(disputa.getId())
                .subastaId(disputa.getSubasta().getId())
                .usuarioInicio(toUsuarioDTO(disputa.getUsuarioInicio()))
                .motivo(disputa.getMotivo())
                .descripcion(disputa.getDescripcion())
                .fechaCreacion(disputa.getFechaCreacion())
                .resolucionAdministrativa(disputa.getResolucionAdministrativa())
                .build();
    }
}

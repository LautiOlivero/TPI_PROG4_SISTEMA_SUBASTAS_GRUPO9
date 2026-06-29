package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.request.ProductoRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.ProductoResponseDTO;
import com.prog.tpi.sistema_subastas.models.Categoria;
import com.prog.tpi.sistema_subastas.models.Producto;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.CategoriaRepository;
import com.prog.tpi.sistema_subastas.repositories.ProductoRepository;
import com.prog.tpi.sistema_subastas.repositories.UsuarioRepository;
import com.prog.tpi.sistema_subastas.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * MOCK TEMPORAL: Devuelve el Usuario ID = 1 para simular al vendedor autenticado.
     * En la Fase 3, este método leerá el token JWT de Spring Security.
     */
    private Usuario obtenerVendedorMock() {
        return usuarioRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario mock (ID=1). Ejecute el seeder."));
    }

    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO dto) {
        // 1. Validar Categoría
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getCategoriaId()));

        // 2. Obtener vendedor mockeado
        Usuario vendedor = obtenerVendedorMock();

        // 3. Crear Entidad
        Producto producto = DtoMapper.toProducto(dto);
        producto.setCategoria(categoria);
        producto.setVendedor(vendedor);

        // 4. Guardar
        Producto guardado = productoRepository.save(producto);
        return DtoMapper.toProductoDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoRepository.findAll().stream()
                .map(DtoMapper::toProductoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return DtoMapper.toProductoDTO(producto);
    }
}

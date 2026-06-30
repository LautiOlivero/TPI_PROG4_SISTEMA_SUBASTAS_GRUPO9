package com.prog.tpi.sistema_subastas.controllers;

import com.prog.tpi.sistema_subastas.dtos.request.SubastaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.SubastaResponseDTO;
import com.prog.tpi.sistema_subastas.services.SubastaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subastas")
public class SubastaController {

    private final SubastaService subastaService;

    public SubastaController(SubastaService subastaService) {
        this.subastaService = subastaService;
    }

    @GetMapping
    public ResponseEntity<List<SubastaResponseDTO>> obtenerSubastas() {
        return ResponseEntity.ok(subastaService.obtenerSubastas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubastaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(subastaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SubastaResponseDTO> crearSubasta(@Valid @RequestBody SubastaRequestDTO dto) {
        SubastaResponseDTO creada = subastaService.crearSubasta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/{id}/publicar")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SubastaResponseDTO> publicarSubasta(@PathVariable Long id) {
        SubastaResponseDTO publicada = subastaService.publicarSubasta(id);
        return ResponseEntity.ok(publicada);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<SubastaResponseDTO> cancelarSubasta(@PathVariable Long id) {
        SubastaResponseDTO cancelada = subastaService.cancelarSubasta(id);
        return ResponseEntity.ok(cancelada);
    }
}

package com.prog.tpi.sistema_subastas.controllers;

import com.prog.tpi.sistema_subastas.dtos.request.SubastaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.SubastaResponseDTO;
import com.prog.tpi.sistema_subastas.services.SubastaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subastas")
@CrossOrigin(origins = "*") // Temporal para desarrollo con frontend local
public class SubastaController {

    private final SubastaService subastaService;

    @Autowired
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
    public ResponseEntity<SubastaResponseDTO> crearSubasta(@Valid @RequestBody SubastaRequestDTO dto) {
        SubastaResponseDTO creada = subastaService.crearSubasta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/{id}/publicar")
    public ResponseEntity<SubastaResponseDTO> publicarSubasta(@PathVariable Long id) {
        SubastaResponseDTO publicada = subastaService.publicarSubasta(id);
        return ResponseEntity.ok(publicada);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SubastaResponseDTO> cancelarSubasta(@PathVariable Long id) {
        SubastaResponseDTO cancelada = subastaService.cancelarSubasta(id);
        return ResponseEntity.ok(cancelada);
    }
}

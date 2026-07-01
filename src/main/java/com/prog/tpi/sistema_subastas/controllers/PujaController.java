package com.prog.tpi.sistema_subastas.controllers;

import com.prog.tpi.sistema_subastas.dtos.request.PujaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.PujaResponseDTO;
import com.prog.tpi.sistema_subastas.services.PujaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pujas")
public class PujaController {

    private final PujaService pujaService;

    public PujaController(PujaService pujaService) {
        this.pujaService = pujaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PujaResponseDTO> crearPuja(@Valid @RequestBody PujaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pujaService.crearPuja(dto));
    }

    @GetMapping("/subasta/{subastaId}")
    public ResponseEntity<List<PujaResponseDTO>> obtenerPujasPorSubasta(@PathVariable Long subastaId) {
        return ResponseEntity.ok(pujaService.obtenerPujasPorSubasta(subastaId));
    }
}

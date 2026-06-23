package com.prog.tpi.sistema_subastas.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String usernameEmail;
    private Boolean bloqueado;
    private List<RolResponseDTO> roles;
}

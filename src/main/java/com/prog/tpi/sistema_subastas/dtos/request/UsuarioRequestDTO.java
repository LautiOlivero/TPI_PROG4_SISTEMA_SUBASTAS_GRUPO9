package com.prog.tpi.sistema_subastas.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {

    @NotBlank(message = "{err.req}")
    @Email(message = "{err.email}")
    private String usernameEmail;

    @NotBlank(message = "{err.req}")
    private String password;

    private List<Long> rolesIds;
}

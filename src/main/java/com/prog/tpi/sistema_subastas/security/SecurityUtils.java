package com.prog.tpi.sistema_subastas.security;

import com.prog.tpi.sistema_subastas.models.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static Usuario getUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado en el contexto de seguridad.");
        }
        return (Usuario) authentication.getPrincipal();
    }
}

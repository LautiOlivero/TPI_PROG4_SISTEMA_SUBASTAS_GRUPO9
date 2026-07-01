package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.request.LoginRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.LoginResponseDTO;
import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.Rol;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.RolRepository;
import com.prog.tpi.sistema_subastas.repositories.UsuarioRepository;
import com.prog.tpi.sistema_subastas.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
            JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public LoginResponseDTO register(LoginRequestDTO request) {
        if (usuarioRepository.existsByUsernameEmail(request.getUsernameEmail())) {
            throw new ReglaNegocioException("El email ya se encuentra registrado.");
        }

        Rol rolUser = rolRepository.findByNombre("USER")
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
        Rol rolSeller = rolRepository.findByNombre("SELLER")
                .orElseThrow(() -> new RuntimeException("Rol SELLER no encontrado"));

        Usuario nuevoUsuario = Usuario.builder()
                .usernameEmail(request.getUsernameEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .bloqueado(false)
                .roles(List.of(rolUser, rolSeller))
                .build();

        usuarioRepository.save(nuevoUsuario);

        List<String> roles = nuevoUsuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(nuevoUsuario.getUsername(), roles);

        return buildLoginResponse(nuevoUsuario, token, roles);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken
                .unauthenticated(request.getUsernameEmail(), request.getPassword());

        Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);

        Usuario usuario = (Usuario) authenticationResult.getPrincipal();

        List<String> roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(usuario.getUsername(), roles);

        return buildLoginResponse(usuario, token, roles);
    }

    private LoginResponseDTO buildLoginResponse(Usuario usuario, String token, List<String> roles) {
        LoginResponseDTO.UserSummaryDTO userSummary = LoginResponseDTO.UserSummaryDTO.builder()
                .id(usuario.getId())
                .usernameEmail(usuario.getUsernameEmail())
                .roles(roles)
                .build();

        return LoginResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .user(userSummary)
                .build();
    }
}

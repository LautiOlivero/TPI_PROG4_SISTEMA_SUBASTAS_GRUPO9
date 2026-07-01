package com.prog.tpi.sistema_subastas.config;

import com.prog.tpi.sistema_subastas.models.Rol;
import com.prog.tpi.sistema_subastas.repositories.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

        private final RolRepository rolRepository;

        public DataInitializer(RolRepository rolRepository) {
                this.rolRepository = rolRepository;
        }

        @Override
        @Transactional
        public void run(String... args) {
                crearRolSiNoExiste("USER");
                crearRolSiNoExiste("SELLER");
                crearRolSiNoExiste("ADMIN");
        }

        private void crearRolSiNoExiste(String nombre) {
                if (rolRepository.findByNombre(nombre).isEmpty()) {
                        Rol rol = Rol.builder().nombre(nombre).build();
                        rolRepository.save(rol);
                }
        }
}

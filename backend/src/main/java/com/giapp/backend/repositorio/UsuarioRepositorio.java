package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndRol(String email, Rol rol);

    boolean existsByEmail(String email);

    Optional<Usuario> findByIdAndRol(Long id, Rol rol);
}

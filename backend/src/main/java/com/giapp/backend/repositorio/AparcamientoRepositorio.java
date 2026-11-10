package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AparcamientoRepositorio extends JpaRepository<Aparcamiento, Long> {
    List<Aparcamiento> findByGestor(Usuario gestor);
}

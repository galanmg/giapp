package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.TarifaAlquiler;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifaAlquilerRepositorio extends JpaRepository<TarifaAlquiler, Long> {
}
package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepositorio extends JpaRepository<Vehiculo, Long> {

    List<Vehiculo> findByPropietario(Usuario propietario);

    Optional<Vehiculo> findByMatricula(String matricula);
}
package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Promocion;
import com.giapp.backend.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromocionRepositorio extends JpaRepository<Promocion, Long> {

    List<Promocion> findByAparcamientoOrderByCodigoAsc(Aparcamiento aparcamiento);

    List<Promocion> findByAparcamientoGestorOrderByAparcamientoIdAscCodigoAsc(Usuario gestor);

    // El código es único dentro de cada aparcamiento
    Optional<Promocion> findByAparcamientoAndCodigo(Aparcamiento aparcamiento, String codigo);

    boolean existsByAparcamientoAndCodigo(Aparcamiento aparcamiento, String codigo);

    boolean existsByAparcamientoAndCodigoAndIdNot(Aparcamiento aparcamiento, String codigo, Long id);
}

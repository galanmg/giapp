package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.ClienteAparcamiento;
import com.giapp.backend.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteAparcamientoRepositorio extends JpaRepository<ClienteAparcamiento, Long> {

    Optional<ClienteAparcamiento> findByClienteAndAparcamiento(Usuario cliente, Aparcamiento aparcamiento);

    // ¿Es el usuario cliente activo de ese aparcamiento?
    boolean existsByClienteAndAparcamientoAndActivoTrue(Usuario cliente, Aparcamiento aparcamiento);

    // ¿Es el usuario cliente activo de alguno de los aparcamientos de ese gestor?
    boolean existsByClienteAndAparcamientoGestorAndActivoTrue(Usuario cliente, Usuario gestor);

    // Clientes activos de los aparcamientos de un gestor, sin repetir aunque estén
    // en varios. aparcamientoId es opcional; texto vacío no filtra.
    @Query("""
            select distinct v.cliente from ClienteAparcamiento v
            where v.aparcamiento.gestor = :gestor
              and v.activo = true
              and (:aparcamientoId is null or v.aparcamiento.id = :aparcamientoId)
              and (lower(v.cliente.nombre) like lower(concat('%', :texto, '%'))
                or lower(v.cliente.apellidos) like lower(concat('%', :texto, '%'))
                or lower(v.cliente.email) like lower(concat('%', :texto, '%')))
            order by v.cliente.apellidos, v.cliente.nombre
            """)
    List<Usuario> buscarClientesDeGestor(@Param("gestor") Usuario gestor,
                                         @Param("aparcamientoId") Long aparcamientoId,
                                         @Param("texto") String texto);
}

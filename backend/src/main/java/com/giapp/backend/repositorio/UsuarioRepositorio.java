package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Email repetido en otro usuario distinto del que se está modificando
    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Usuario> findByIdAndRol(Long id, Rol rol);

    List<Usuario> findByRolOrderByApellidosAscNombreAsc(Rol rol);

    // Con nombre derivado haría falta repetir el rol en cada rama del OR
    @Query("""
            select u from Usuario u
            where u.rol = :rol
              and (lower(u.nombre) like lower(concat('%', :texto, '%'))
                or lower(u.apellidos) like lower(concat('%', :texto, '%'))
                or lower(u.email) like lower(concat('%', :texto, '%')))
            order by u.apellidos, u.nombre
            """)
    List<Usuario> buscarPorTexto(@Param("rol") Rol rol, @Param("texto") String texto);
}

package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.modelo.enums.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoRepositorio extends JpaRepository<Contrato, Long> {

    List<Contrato> findByPlazaAndEstado(Plaza plaza, EstadoContrato estado);
}
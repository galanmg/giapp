package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ReciboRepositorio extends JpaRepository<Recibo, Long> {

    // Un mismo periodo de un contrato no se cobra dos veces
    boolean existsByContratoAndPeriodoInicio(Contrato contrato, LocalDate periodoInicio);
}

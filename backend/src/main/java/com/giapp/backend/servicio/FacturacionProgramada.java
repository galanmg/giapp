package com.giapp.backend.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Tarea automática de facturación: cada día a las 6:00, hora de Madrid, genera
 * los recibos pendientes de todos los contratos activos (RF-28).
 *
 * La zona horaria va explícita para que no dependa de la del servidor.
 */
@Component
public class FacturacionProgramada {

    private static final Logger log = LoggerFactory.getLogger(FacturacionProgramada.class);
    private static final String ZONA = "Europe/Madrid";

    private final ReciboServicio reciboServicio;

    public FacturacionProgramada(ReciboServicio reciboServicio) {
        this.reciboServicio = reciboServicio;
    }

    @Scheduled(cron = "0 0 6 * * *", zone = ZONA)
    public void ejecutar() {
        LocalDate hoy = LocalDate.now(ZoneId.of(ZONA));
        int creados = reciboServicio.generarPendientes(hoy);

        log.info("Facturación del {}: {} recibos creados", hoy, creados);
    }
}

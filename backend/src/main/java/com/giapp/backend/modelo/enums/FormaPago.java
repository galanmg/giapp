package com.giapp.backend.modelo.enums;

// RF-32. No hay pagos automáticos ni domiciliaciones: el gestor registra cada pago a mano.
public enum FormaPago {
    EFECTIVO,
    TRANSFERENCIA_BIZUM
}

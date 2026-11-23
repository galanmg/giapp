package com.giapp.backend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Promoción de un aparcamiento (RF-18, RF-19). El código es único dentro de
 * cada aparcamiento, no en toda la aplicación.
 *
 * El contrato no guarda la promoción, solo el precio resultante (RF-23): si la
 * promoción cambia o se borra después, el contrato no se ve afectado.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "promocion",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_promocion_aparcamiento_codigo",
                columnNames = {"aparcamiento_id", "codigo"}))
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false)
    private int porcentajeDescuento;

    @Column(nullable = false)
    private LocalDate fechaCaducidad;

    @Column(nullable = false)
    private boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aparcamiento_id", foreignKey = @ForeignKey(name = "fk_promocion_aparcamiento"))
    private Aparcamiento aparcamiento;

    // Constructor para crear promociones (alta y datos de prueba)
    public Promocion(String codigo, int porcentajeDescuento, LocalDate fechaCaducidad,
                     Aparcamiento aparcamiento) {
        this.codigo = codigo;
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaCaducidad = fechaCaducidad;
        this.aparcamiento = aparcamiento;
        this.activa = true;
    }

    // Vale si está activa y no ha caducado en esa fecha. El día de la caducidad aún vale.
    public boolean esValidaEn(LocalDate fecha) {
        return activa && !fecha.isAfter(fechaCaducidad);
    }

    // Precio con el descuento aplicado, redondeado a 2 decimales con HALF_UP
    public BigDecimal aplicarA(BigDecimal precio) {
        BigDecimal porcentajeQueSePaga = BigDecimal.valueOf(100L - porcentajeDescuento);

        return precio.multiply(porcentajeQueSePaga)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}

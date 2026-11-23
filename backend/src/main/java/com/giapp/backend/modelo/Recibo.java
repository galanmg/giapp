package com.giapp.backend.modelo;

import com.giapp.backend.modelo.enums.EstadoRecibo;
import com.giapp.backend.modelo.enums.FormaPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Recibo de un periodo de facturación (RF-31), sin los campos eléctricos del
 * diagrama de clases (ampliación opcional no implementada).
 *
 * La clave única sobre contrato y periodoInicio impide cobrar dos veces el
 * mismo periodo, aunque la generación se lance varias veces.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "recibo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recibo_contrato_periodo",
                columnNames = {"contrato_id", "periodo_inicio"}))
public class Recibo {

    // Días naturales que tiene el cliente para pagar desde la emisión
    public static final int DIAS_PARA_PAGAR = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", foreignKey = @ForeignKey(name = "fk_recibo_contrato"))
    private Contrato contrato;

    @Column(nullable = false)
    private LocalDate periodoInicio;

    @Column(nullable = false)
    private LocalDate periodoFin;

    @Column(nullable = false)
    private int numeroMeses;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal importeTotal;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRecibo estado = EstadoRecibo.PENDIENTE;

    // Emite el recibo de un periodo: calcula el importe y el vencimiento, y lo deja pendiente
    public Recibo(Contrato contrato, Periodo periodo, LocalDate fechaEmision) {
        this.contrato = contrato;
        this.periodoInicio = periodo.inicio();
        this.periodoFin = periodo.fin();
        this.numeroMeses = periodo.meses();
        this.importeTotal = calcularImporte(contrato.getPrecioMensual(), periodo.meses());
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaEmision.plusDays(DIAS_PARA_PAGAR);
        this.estado = EstadoRecibo.PENDIENTE;
    }

    // Siempre por meses completos, nunca por días: precio mensual × número de meses
    public static BigDecimal calcularImporte(BigDecimal precioMensual, int meses) {
        return precioMensual.multiply(BigDecimal.valueOf(meses));
    }
}

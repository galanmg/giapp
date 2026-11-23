package com.giapp.backend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Vínculo de un cliente con un aparcamiento. Un cliente puede estar vinculado
 * a varios aparcamientos, de gestores distintos, y cada gestor solo ve a los
 * clientes vinculados a sus aparcamientos.
 *
 * La baja en un aparcamiento no borra el vínculo: lo desactiva, para conservar
 * el histórico, y si el cliente vuelve se reactiva el mismo registro.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cliente_aparcamiento",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cliente_aparcamiento_cliente_aparcamiento",
                columnNames = {"cliente_id", "aparcamiento_id"}))
public class ClienteAparcamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id",
            foreignKey = @ForeignKey(name = "fk_cliente_aparcamiento_cliente"))
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aparcamiento_id",
            foreignKey = @ForeignKey(name = "fk_cliente_aparcamiento_aparcamiento"))
    private Aparcamiento aparcamiento;

    @Column(nullable = false)
    private LocalDate fechaAlta;

    @Column(nullable = false)
    private boolean activo = true;

    // Constructor para crear vínculos (alta, vinculación y datos de prueba)
    public ClienteAparcamiento(Usuario cliente, Aparcamiento aparcamiento, LocalDate fechaAlta) {
        this.cliente = cliente;
        this.aparcamiento = aparcamiento;
        this.fechaAlta = fechaAlta;
        this.activo = true;
    }
}

package com.giapp.backend.config;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.ClienteAparcamiento;
import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Periodo;
import com.giapp.backend.modelo.Planta;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.modelo.Promocion;
import com.giapp.backend.modelo.Recibo;
import com.giapp.backend.modelo.TarifaAlquiler;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.Vehiculo;
import com.giapp.backend.modelo.enums.EstadoRecibo;
import com.giapp.backend.modelo.enums.FormaPago;
import com.giapp.backend.modelo.enums.Periodicidad;
import com.giapp.backend.modelo.enums.Rol;
import com.giapp.backend.modelo.enums.TipoVehiculo;
import com.giapp.backend.repositorio.AparcamientoRepositorio;
import com.giapp.backend.repositorio.ClienteAparcamientoRepositorio;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.PlantaRepositorio;
import com.giapp.backend.repositorio.PlazaRepositorio;
import com.giapp.backend.repositorio.PromocionRepositorio;
import com.giapp.backend.repositorio.ReciboRepositorio;
import com.giapp.backend.repositorio.TarifaAlquilerRepositorio;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import com.giapp.backend.repositorio.VehiculoRepositorio;
import com.giapp.backend.servicio.CalculadoraPeriodos;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final AparcamientoRepositorio aparcamientoRepositorio;
    private final PlantaRepositorio plantaRepositorio;
    private final PlazaRepositorio plazaRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;
    private final TarifaAlquilerRepositorio tarifaAlquilerRepositorio;
    private final ContratoRepositorio contratoRepositorio;
    private final ClienteAparcamientoRepositorio clienteAparcamientoRepositorio;
    private final ReciboRepositorio reciboRepositorio;
    private final PromocionRepositorio promocionRepositorio;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepositorio usuarioRepositorio,
                           AparcamientoRepositorio aparcamientoRepositorio,
                           PlantaRepositorio plantaRepositorio,
                           PlazaRepositorio plazaRepositorio,
                           VehiculoRepositorio vehiculoRepositorio,
                           TarifaAlquilerRepositorio tarifaAlquilerRepositorio,
                           ContratoRepositorio contratoRepositorio,
                           ClienteAparcamientoRepositorio clienteAparcamientoRepositorio,
                           ReciboRepositorio reciboRepositorio,
                           PromocionRepositorio promocionRepositorio,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.aparcamientoRepositorio = aparcamientoRepositorio;
        this.plantaRepositorio = plantaRepositorio;
        this.plazaRepositorio = plazaRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.tarifaAlquilerRepositorio = tarifaAlquilerRepositorio;
        this.contratoRepositorio = contratoRepositorio;
        this.clienteAparcamientoRepositorio = clienteAparcamientoRepositorio;
        this.reciboRepositorio = reciboRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {


        if (usuarioRepositorio.count() > 0) {
            System.out.println("La base de datos ya tiene datos. No se cargan los de prueba.");
            return;
        }

        System.out.println("Cargando datos de prueba...");

        String passwordCifrada = passwordEncoder.encode("giapp1234");

        Usuario gestor = usuarioRepositorio.save(
                new Usuario("Andrés", "Moreno", "gestor@giapp.com", passwordCifrada, "600111222", Rol.GESTOR));

        Usuario laura = usuarioRepositorio.save(
                new Usuario("Laura", "Ortega", "laura@giapp.com", passwordCifrada, "600333444", Rol.CLIENTE));

        Usuario miguel = usuarioRepositorio.save(
                new Usuario("Miguel", "Ruiz", "miguel@giapp.com", passwordCifrada, "600555666", Rol.CLIENTE));

        // Aparcamiento
        Aparcamiento juanBosco = aparcamientoRepositorio.save(
                new Aparcamiento("Juan Bosco", "Calle Mayor 10", "Ciudad Real", gestor));

        // Laura y Miguel son clientes de Juan Bosco desde el día en que empiezan sus contratos
        clienteAparcamientoRepositorio.save(
                new ClienteAparcamiento(laura, juanBosco, LocalDate.of(2026, 3, 1)));
        clienteAparcamientoRepositorio.save(
                new ClienteAparcamiento(miguel, juanBosco, LocalDate.of(2026, 4, 1)));

        // Plantas
        Planta sotano = plantaRepositorio.save(new Planta("Sótano 1", -1, juanBosco));
        Planta baja = plantaRepositorio.save(new Planta("Planta baja", 0, juanBosco));

        // Plazas. La capacidad va en medias plazas: 2 es una plaza normal, 1 da hueco para una moto
        Plaza s101 = plazaRepositorio.save(new Plaza("S1-01", 2, true, sotano));
        Plaza s102 = plazaRepositorio.save(new Plaza("S1-02", 2, true, sotano));
        plazaRepositorio.save(new Plaza("S1-03", 2, false, sotano));
        plazaRepositorio.save(new Plaza("S1-04", 1, false, sotano));
        plazaRepositorio.save(new Plaza("PB-01", 2, true, baja));
        plazaRepositorio.save(new Plaza("PB-02", 2, true, baja));

        // Vehículos
        Vehiculo peugeot306 = vehiculoRepositorio.save(
                new Vehiculo("CR 3450 X", "Peugeot", "306", TipoVehiculo.COCHE, laura));

        Vehiculo scooter = vehiculoRepositorio.save(
                new Vehiculo("1234 ABC", "Honda", "PCX 125", TipoVehiculo.MOTO, miguel));

        // No lleva contrato porque es el coche con el que se probará la regla de capacidad
        vehiculoRepositorio.save(
                new Vehiculo("7565 JLM", "Peugeot", "208", TipoVehiculo.COCHE, miguel));

        // Tarifas vigentes desde el 1 de enero
        tarifaAlquilerRepositorio.save(new TarifaAlquiler(
                TipoVehiculo.COCHE, new BigDecimal("45.00"), LocalDate.of(2026, 1, 1), juanBosco));

        tarifaAlquilerRepositorio.save(new TarifaAlquiler(
                TipoVehiculo.MOTO, new BigDecimal("25.00"), LocalDate.of(2026, 1, 1), juanBosco));

        // Promoción para la demostración de RF-23: un 10 % hasta fin de año
        promocionRepositorio.save(new Promocion(
                "BIENVENIDA10", 10, LocalDate.of(2026, 12, 31), juanBosco));

        // Contratos activos
        // S1-01 queda llena: un coche ocupa las 2 medias plazas. Pago semestral.
        Contrato contratoLaura = contratoRepositorio.save(new Contrato(
                LocalDate.of(2026, 3, 1), Periodicidad.SEMESTRAL, new BigDecimal("45.00"),
                laura, s101, peugeot306));

        // S1-02 queda a medias: una moto ocupa 1 de 2, sobra media plaza. Pago mensual.
        Contrato contratoMiguel = contratoRepositorio.save(new Contrato(
                LocalDate.of(2026, 4, 1), Periodicidad.MENSUAL, new BigDecimal("25.00"),
                miguel, s102, scooter));

        // Recibos para demostrar la facturación. Los periodos los calcula
        // CalculadoraPeriodos: no se escriben a mano.
        cargarRecibosLaura(contratoLaura);
        cargarRecibosMiguel(contratoMiguel, LocalDate.now());

        System.out.println("Datos de prueba cargados.");
    }

    // Laura, semestral desde el 1 de marzo: de marzo a junio (4 meses) y de julio a
    // diciembre (6 meses), los dos pagados
    private void cargarRecibosLaura(Contrato contrato) {
        Periodo primero = CalculadoraPeriodos.primerPeriodo(
                contrato.getFechaInicio(), contrato.getPeriodicidad());
        Periodo segundo = CalculadoraPeriodos.periodoFacturable(
                primero.fin().plusDays(1), contrato.getPeriodicidad(), contrato.inicioFacturacion());

        reciboRepositorio.save(pagado(new Recibo(contrato, primero, primero.inicio()), FormaPago.TRANSFERENCIA_BIZUM));
        reciboRepositorio.save(pagado(new Recibo(contrato, segundo, segundo.inicio()), FormaPago.TRANSFERENCIA_BIZUM));
    }

    // Miguel, mensual desde el 1 de abril: un recibo por mes hasta el actual. Todos
    // pagados salvo el del mes pasado, impagado por vencido, y el del mes actual,
    // pendiente. Los pasados se emiten el día 1 de su periodo; el actual, hoy, para
    // que no venza durante la demostración.
    private void cargarRecibosMiguel(Contrato contrato, LocalDate hoy) {
        LocalDate mesActual = hoy.withDayOfMonth(1);
        LocalDate mesPasado = mesActual.minusMonths(1);

        Periodo periodo = CalculadoraPeriodos.primerPeriodo(
                contrato.getFechaInicio(), contrato.getPeriodicidad());

        while (!periodo.inicio().isAfter(mesActual)) {
            if (periodo.inicio().equals(mesActual)) {
                reciboRepositorio.save(new Recibo(contrato, periodo, hoy));
            } else {
                Recibo recibo = new Recibo(contrato, periodo, periodo.inicio());

                if (periodo.inicio().equals(mesPasado)) {
                    recibo.setEstado(EstadoRecibo.IMPAGADO);
                } else {
                    pagado(recibo, FormaPago.EFECTIVO);
                }

                reciboRepositorio.save(recibo);
            }

            periodo = CalculadoraPeriodos.periodoFacturable(
                    periodo.fin().plusDays(1), contrato.getPeriodicidad(), contrato.inicioFacturacion());
        }
    }

    // Pagado dentro de plazo: 5 días después de la emisión, antes de los 10 de vencimiento
    private Recibo pagado(Recibo recibo, FormaPago formaPago) {
        recibo.setEstado(EstadoRecibo.PAGADO);
        recibo.setFechaPago(recibo.getFechaEmision().plusDays(5));
        recibo.setFormaPago(formaPago);

        return recibo;
    }
}

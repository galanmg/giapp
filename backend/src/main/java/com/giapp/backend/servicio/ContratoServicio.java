package com.giapp.backend.servicio;

import com.giapp.backend.dto.ContratoPeticion;
import com.giapp.backend.dto.ContratoRespuesta;
import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.Vehiculo;
import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.repositorio.ClienteAparcamientoRepositorio;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.PlazaRepositorio;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import com.giapp.backend.repositorio.VehiculoRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContratoServicio {

    private final ContratoRepositorio contratoRepositorio;
    private final PlazaRepositorio plazaRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ClienteAparcamientoRepositorio clienteAparcamientoRepositorio;

    public ContratoServicio(ContratoRepositorio contratoRepositorio,
                            PlazaRepositorio plazaRepositorio,
                            VehiculoRepositorio vehiculoRepositorio,
                            UsuarioRepositorio usuarioRepositorio,
                            ClienteAparcamientoRepositorio clienteAparcamientoRepositorio) {
        this.contratoRepositorio = contratoRepositorio;
        this.plazaRepositorio = plazaRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.clienteAparcamientoRepositorio = clienteAparcamientoRepositorio;
    }

    @Transactional
    public ContratoRespuesta crear(ContratoPeticion peticion) {

        Usuario titular = buscarTitular(peticion.titularId());
        Vehiculo vehiculo = buscarVehiculo(peticion.vehiculoId());
        Plaza plaza = buscarPlaza(peticion.plazaId());

        if (!vehiculo.getPropietario().getId().equals(titular.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El vehículo no pertenece al titular del contrato");
        }

        // El titular tiene que ser cliente activo del aparcamiento de la plaza
        Aparcamiento aparcamiento = plaza.getPlanta().getAparcamiento();

        if (!clienteAparcamientoRepositorio.existsByClienteAndAparcamientoAndActivoTrue(
                titular, aparcamiento)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El titular no está dado de alta como cliente en el aparcamiento "
                            + aparcamiento.getNombre());
        }

        int mediasOcupadas = mediasPlazasOcupadas(plaza);

        if (!plaza.admite(vehiculo.getTipo(), mediasOcupadas)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La plaza " + plaza.getNumero() + " no admite este vehículo. Capacidad: "
                            + plaza.getCapacidad() + " medias plazas, ocupadas: " + mediasOcupadas
                            + ", solicitadas: " + vehiculo.getTipo().getMediasPlazas());
        }

        Contrato contrato = new Contrato(
                peticion.fechaInicio(),
                peticion.periodicidad(),
                peticion.precioMensual(),
                titular,
                plaza,
                vehiculo);

        return aRespuesta(contratoRepositorio.save(contrato));
    }

    // Metodos internos

    private int mediasPlazasOcupadas(Plaza plaza) {
        return contratoRepositorio.findByPlazaAndEstado(plaza, EstadoContrato.ACTIVO)
                .stream()
                .mapToInt(Contrato::ocupacion)
                .sum();
    }

    private Usuario buscarTitular(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el usuario con id " + id));
    }

    private Vehiculo buscarVehiculo(Long id) {
        return vehiculoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el vehículo con id " + id));
    }

    private Plaza buscarPlaza(Long id) {
        return plazaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la plaza con id " + id));
    }

    private ContratoRespuesta aRespuesta(Contrato contrato) {
        Usuario titular = contrato.getTitular();

        return new ContratoRespuesta(
                contrato.getId(),
                contrato.getFechaInicio(),
                contrato.getFechaFin(),
                contrato.getPeriodicidad().name(),
                contrato.getPrecioMensual(),
                contrato.getEstado().name(),
                titular.getNombre() + " " + titular.getApellidos(),
                contrato.getPlaza().getNumero(),
                contrato.getVehiculo().getMatricula(),
                contrato.ocupacion());
    }
}
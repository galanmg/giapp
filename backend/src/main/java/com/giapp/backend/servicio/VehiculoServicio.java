package com.giapp.backend.servicio;

import com.giapp.backend.dto.VehiculoPeticion;
import com.giapp.backend.dto.VehiculoRespuesta;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.Vehiculo;
import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.repositorio.ClienteAparcamientoRepositorio;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.VehiculoRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

/**
 * Vehículos. Los gestiona el gestor para los clientes de sus aparcamientos
 * (RF-07, RF-08) y el propio usuario desde su perfil (RF-09).
 */
@Service
public class VehiculoServicio {

    private final VehiculoRepositorio vehiculoRepositorio;
    private final ContratoRepositorio contratoRepositorio;
    private final ClienteAparcamientoRepositorio clienteAparcamientoRepositorio;
    private final ClienteServicio clienteServicio;
    private final UsuarioActualServicio usuarioActualServicio;

    public VehiculoServicio(VehiculoRepositorio vehiculoRepositorio,
                            ContratoRepositorio contratoRepositorio,
                            ClienteAparcamientoRepositorio clienteAparcamientoRepositorio,
                            ClienteServicio clienteServicio,
                            UsuarioActualServicio usuarioActualServicio) {
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.contratoRepositorio = contratoRepositorio;
        this.clienteAparcamientoRepositorio = clienteAparcamientoRepositorio;
        this.clienteServicio = clienteServicio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    // ---- Gestor: vehículos de los clientes de sus aparcamientos ----

    @Transactional(readOnly = true)
    public List<VehiculoRespuesta> listarDeCliente(Long clienteId) {
        return listar(clienteServicio.buscarClienteDeGestor(clienteId));
    }

    @Transactional
    public VehiculoRespuesta crearParaCliente(Long clienteId, VehiculoPeticion peticion) {
        return crear(clienteServicio.buscarClienteDeGestor(clienteId), peticion);
    }

    @Transactional
    public VehiculoRespuesta actualizar(Long id, VehiculoPeticion peticion) {
        return modificar(buscarDeMisClientes(id), peticion);
    }

    @Transactional
    public void eliminar(Long id) {
        borrar(buscarDeMisClientes(id));
    }

    // ---- Usuario autenticado: sus propios vehículos ----

    @Transactional(readOnly = true)
    public List<VehiculoRespuesta> listarPropios() {
        return listar(usuarioActualServicio.obtener());
    }

    @Transactional
    public VehiculoRespuesta crearPropio(VehiculoPeticion peticion) {
        return crear(usuarioActualServicio.obtener(), peticion);
    }

    @Transactional
    public VehiculoRespuesta actualizarPropio(Long id, VehiculoPeticion peticion) {
        return modificar(buscarPropio(id), peticion);
    }

    @Transactional
    public void eliminarPropio(Long id) {
        borrar(buscarPropio(id));
    }

    // Métodos internos

    private List<VehiculoRespuesta> listar(Usuario propietario) {
        return vehiculoRepositorio.findByPropietarioOrderByMatriculaAsc(propietario)
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    private VehiculoRespuesta crear(Usuario propietario, VehiculoPeticion peticion) {
        String matricula = normalizarMatricula(peticion.matricula());

        if (vehiculoRepositorio.existsByPropietarioAndMatricula(propietario, matricula)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya hay un vehículo con la matrícula " + matricula + " en esta cuenta");
        }

        Vehiculo vehiculo = new Vehiculo(
                matricula, peticion.marca(), peticion.modelo(), peticion.tipo(), propietario);

        return aRespuesta(vehiculoRepositorio.save(vehiculo));
    }

    private VehiculoRespuesta modificar(Vehiculo vehiculo, VehiculoPeticion peticion) {
        String matricula = normalizarMatricula(peticion.matricula());

        if (vehiculoRepositorio.existsByPropietarioAndMatriculaAndIdNot(
                vehiculo.getPropietario(), matricula, vehiculo.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya hay un vehículo con la matrícula " + matricula + " en esta cuenta");
        }

        // Cambiar el tipo con un contrato activo podría desbordar la capacidad de la plaza:
        // una moto ocupa 1 media plaza y un coche, 2
        if (vehiculo.getTipo() != peticion.tipo() && tieneContratoActivo(vehiculo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede cambiar el tipo de un vehículo con un contrato activo");
        }

        vehiculo.setMatricula(matricula);
        vehiculo.setMarca(peticion.marca());
        vehiculo.setModelo(peticion.modelo());
        vehiculo.setTipo(peticion.tipo());

        return aRespuesta(vehiculo);
    }

    private void borrar(Vehiculo vehiculo) {
        if (tieneContratoActivo(vehiculo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el vehículo " + vehiculo.getMatricula()
                            + " porque tiene un contrato activo. Finaliza antes el contrato");
        }

        vehiculoRepositorio.delete(vehiculo);
    }

    private boolean tieneContratoActivo(Vehiculo vehiculo) {
        return contratoRepositorio.existsByVehiculoAndEstado(vehiculo, EstadoContrato.ACTIVO);
    }

    private Vehiculo buscarEntidad(Long id) {
        return vehiculoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el vehículo con id " + id));
    }

    // 403 si el propietario no es cliente activo de alguno de mis aparcamientos
    private Vehiculo buscarDeMisClientes(Long id) {
        Vehiculo vehiculo = buscarEntidad(id);

        if (!clienteAparcamientoRepositorio.existsByClienteAndAparcamientoGestorAndActivoTrue(
                vehiculo.getPropietario(), usuarioActualServicio.obtener())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El vehículo con id " + id + " no es de un cliente de tus aparcamientos");
        }

        return vehiculo;
    }

    // 403 si el vehículo es de otro usuario
    private Vehiculo buscarPropio(Long id) {
        Vehiculo vehiculo = buscarEntidad(id);

        if (!vehiculo.getPropietario().getId().equals(usuarioActualServicio.obtener().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El vehículo con id " + id + " no es tuyo");
        }

        return vehiculo;
    }

    // En mayúsculas y sin espacios al principio ni al final
    private String normalizarMatricula(String matricula) {
        return matricula.trim().toUpperCase(Locale.ROOT);
    }

    private VehiculoRespuesta aRespuesta(Vehiculo vehiculo) {
        Usuario propietario = vehiculo.getPropietario();

        return new VehiculoRespuesta(
                vehiculo.getId(),
                vehiculo.getMatricula(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getTipo().name(),
                propietario.getNombre() + " " + propietario.getApellidos());
    }
}

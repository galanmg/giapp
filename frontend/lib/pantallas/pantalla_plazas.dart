import 'package:flutter/material.dart';

import '../modelos/plaza.dart';
import '../servicios/api_servicio.dart';
import 'pantalla_login.dart';

class PantallaPlazas extends StatefulWidget {
  const PantallaPlazas({super.key});

  @override
  State<PantallaPlazas> createState() => _PantallaPlazasState();
}

class _PantallaPlazasState extends State<PantallaPlazas> {
  late Future<List<Plaza>> _plazas;

  @override
  void initState() {
    super.initState();
    _plazas = api.obtenerPlazas();
  }

  void _recargar() {
    setState(() {
      _plazas = api.obtenerPlazas();
    });
  }

  void _salir() {
    api.cerrarSesion();
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const PantallaLogin()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plazas'),
        actions: [
          IconButton(
            onPressed: _recargar,
            icon: const Icon(Icons.refresh),
            tooltip: 'Recargar',
          ),
          IconButton(
            onPressed: _salir,
            icon: const Icon(Icons.logout),
            tooltip: 'Cerrar sesión',
          ),
        ],
      ),
      body: Column(
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            color: Theme.of(context).colorScheme.surfaceContainerHighest,
            child: Text('${api.nombre} · ${api.rol}'),
          ),
          Expanded(
            child: FutureBuilder<List<Plaza>>(
              future: _plazas,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                }

                if (snapshot.hasError) {
                  return Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Text(
                        snapshot.error.toString().replaceFirst('Exception: ', ''),
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: Theme.of(context).colorScheme.error,
                        ),
                      ),
                    ),
                  );
                }

                final plazas = snapshot.data ?? [];

                if (plazas.isEmpty) {
                  return const Center(child: Text('No hay plazas registradas'));
                }

                return ListView.separated(
                  itemCount: plazas.length,
                  separatorBuilder: (_, __) => const Divider(height: 1),
                  itemBuilder: (context, indice) {
                    final plaza = plazas[indice];
                    final fueraDeServicio = plaza.estado != 'DISPONIBLE';

                    return ListTile(
                      leading: Icon(
                        plaza.admiteCoche
                            ? Icons.directions_car
                            : Icons.two_wheeler,
                        color: fueraDeServicio
                            ? Theme.of(context).disabledColor
                            : Theme.of(context).colorScheme.primary,
                      ),
                      title: Text(plaza.numero),
                      subtitle: Text(
                        '${plaza.planta} · ${plaza.capacidad} medias plazas',
                      ),
                      trailing: fueraDeServicio
                          ? const Chip(label: Text('Fuera de servicio'))
                          : null,
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
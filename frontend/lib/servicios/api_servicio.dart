import 'dart:convert';

import 'package:http/http.dart' as http;

import '../modelos/plaza.dart';

class ApiServicio {
  static const String urlBase = 'http://localhost:8080';

  String? _token;
  String? _nombre;
  String? _rol;

  String? get token => _token;
  String? get nombre => _nombre;
  String? get rol => _rol;

  bool get haySesion => _token != null;

  Future<void> login(String email, String password) async {
    final respuesta = await http.post(
      Uri.parse('$urlBase/api/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );

    if (respuesta.statusCode == 200) {
      final datos = jsonDecode(utf8.decode(respuesta.bodyBytes));
      _token = datos['token'];
      _nombre = datos['nombre'];
      _rol = datos['rol'];
      return;
    }

    if (respuesta.statusCode == 401) {
      throw Exception('Email o contraseña incorrectos');
    }

    throw Exception('Error del servidor (${respuesta.statusCode})');
  }

  Future<List<Plaza>> obtenerPlazas() async {
    final respuesta = await http.get(
      Uri.parse('$urlBase/api/plazas'),
      headers: {'Authorization': 'Bearer $_token'},
    );

    if (respuesta.statusCode == 200) {
      final List<dynamic> lista = jsonDecode(utf8.decode(respuesta.bodyBytes));
      return lista.map((json) => Plaza.desdeJson(json)).toList();
    }

    if (respuesta.statusCode == 401) {
      throw Exception('La sesión ha caducado, vuelve a entrar');
    }

    throw Exception(
      'No se han podido cargar las plazas (${respuesta.statusCode})',
    );
  }

  void cerrarSesion() {
    _token = null;
    _nombre = null;
    _rol = null;
  }
}

// Una unica instancia para toda la aplicacion
final ApiServicio api = ApiServicio();

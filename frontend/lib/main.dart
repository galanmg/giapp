import 'package:flutter/material.dart';

import 'pantallas/pantalla_login.dart';

void main() {
  runApp(const AplicacionGiapp());
}

class AplicacionGiapp extends StatelessWidget {
  const AplicacionGiapp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GIAPP',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
      ),
      home: PantallaLogin(),
    );
  }
}
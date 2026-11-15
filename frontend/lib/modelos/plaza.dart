class Plaza {
  final int id;
  final String numero;
  final int capacidad;
  final bool admiteCoche;
  final String estado;
  final String planta;

  Plaza({
    required this.id,
    required this.numero,
    required this.capacidad,
    required this.admiteCoche,
    required this.estado,
    required this.planta,
  });

  factory Plaza.desdeJson(Map<String, dynamic> json) {
    return Plaza(
      id: json['id'],
      numero: json['numero'],
      capacidad: json['capacidad'],
      admiteCoche: json['admiteCoche'],
      estado: json['estado'],
      planta: json['planta'],
    );
  }
}
package pe.edu.cibertec.puntoutil.dto;

import pe.edu.cibertec.puntoutil.entity.Rol;

public record UsuarioResponse(
        Long id,
        String usuario,
        String nombre,
        String email,
        Rol rol,
        boolean activo
) {
}

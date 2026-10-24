package pe.edu.cibertec.puntoutil.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        String usuario,
        String nombre,
        String rol
) {
}

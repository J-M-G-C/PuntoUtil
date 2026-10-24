package pe.edu.cibertec.puntoutil.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.cibertec.puntoutil.entity.Rol;

public record ActualizarUsuarioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 160) String email,
        @NotNull Rol rol,
        Boolean activo,
        @Size(max = 72) String contrasena
) {
}

package pe.edu.cibertec.puntoutil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Maximo 100 caracteres")
        String nombre

) {}
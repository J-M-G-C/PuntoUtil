package pe.edu.cibertec.puntoutil.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoRequest(

        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 150, message = "El nombre del cliente no debe superar los 150 caracteres")
        String nombreCliente,

        @Size(max = 15, message = "El DNI no debe superar los 15 caracteres")
        String dni,

        @Size(max = 20, message = "El telefono no debe superar los 20 caracteres")
        String telefono,

        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 160, message = "El correo no debe superar los 160 caracteres")
        String correo,

        @Size(max = 255, message = "Las observaciones no deben superar los 255 caracteres")
        String observaciones,

        @NotEmpty(message = "Debe agregar al menos un producto al pedido")
        List<@Valid PedidoDetalleRequest> detalles
) {
}

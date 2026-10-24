package pe.edu.cibertec.puntoutil.controller;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.puntoutil.dto.pedido.ActualizarEstadoPedidoRequest;
import pe.edu.cibertec.puntoutil.dto.pedido.PedidoRequest;
import pe.edu.cibertec.puntoutil.dto.pedido.PedidoResponse;
import pe.edu.cibertec.puntoutil.entity.EstadoPedido;
import pe.edu.cibertec.puntoutil.service.PedidoService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> registrarPedido(
            @Valid @RequestBody PedidoRequest request,
            Authentication authentication) {

        PedidoResponse pedido = pedidoService
                .registrarPedido(request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pedido);
    }

    @GetMapping
    public List<PedidoResponse> listarPedidos(
            @RequestParam(required = false) EstadoPedido estado,
            Authentication authentication) {

        return pedidoService.listarPedidos(estado, authentication);
    }

    @GetMapping("/{id}")
    public PedidoResponse obtenerPedido(
            @PathVariable Long id,
            Authentication authentication) {

        return pedidoService.obtenerPedido(id, authentication);
    }

    @GetMapping("/seguimiento/{codigo}")
    public PedidoResponse obtenerPedidoPorCodigo(
            @PathVariable String codigo,
            Authentication authentication) {

        return pedidoService
                .obtenerPedidoPorCodigo(codigo, authentication);
    }

    @PatchMapping("/{id}/estado")
    public PedidoResponse actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoPedidoRequest request) {

        return pedidoService.actualizarEstado(id, request.estado());
    }

    @PatchMapping("/{id}/entregar")
    public PedidoResponse entregarPedido(@PathVariable Long id) {
        return pedidoService.entregarPedido(id);
    }

    @PatchMapping("/{id}/cancelar")
    public PedidoResponse cancelarPedido(@PathVariable Long id) {
        return pedidoService.cancelarPedido(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Long id,
            Authentication authentication) {

        byte[] pdf = pedidoService
                .generarPdfConfirmacion(id, authentication);

        return respuestaPdf(pdf, "pedido-" + id + ".pdf");
    }

    @GetMapping("/seguimiento/{codigo}/pdf")
    public ResponseEntity<byte[]> descargarPdfPorCodigo(
            @PathVariable String codigo,
            Authentication authentication) {

        byte[] pdf = pedidoService
                .generarPdfConfirmacionPorCodigo(codigo, authentication);

        return respuestaPdf(pdf, "pedido-" + codigo + ".pdf");
    }

    private ResponseEntity<byte[]> respuestaPdf(
            byte[] pdf,
            String nombreArchivo) {

        ContentDisposition disposicion = ContentDisposition
                .attachment()
                .filename(nombreArchivo, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposicion.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}

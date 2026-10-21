package pe.edu.cibertec.puntoutil.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.puntoutil.dto.MovimientoInventarioRequest;
import pe.edu.cibertec.puntoutil.dto.MovimientoInventarioResponse;
import pe.edu.cibertec.puntoutil.entity.MovimientoInventario;
import pe.edu.cibertec.puntoutil.entity.Producto;
import pe.edu.cibertec.puntoutil.repository.MovimientoInventarioRepository;
import pe.edu.cibertec.puntoutil.repository.ProductoRepository;

import java.util.List;

@Service
public class InventarioService {

    private final ProductoRepository productoRepository;

    private final MovimientoInventarioRepository
            movimientoRepository;

    public InventarioService(
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoRepository) {

        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // Registrar entrada o salida de inventario
    @Transactional
    public MovimientoInventarioResponse registrarMovimiento(
            MovimientoInventarioRequest request) {

        Producto producto = productoRepository
                .findByIdParaMovimiento(request.productoId())
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado o inactivo"
                ));

        int stockAnterior = producto.getStock();
        int cantidad = request.cantidad();

        int stockNuevo;

        switch (request.tipo()) {

            case ENTRADA -> {

                if (cantidad > Integer.MAX_VALUE - stockAnterior) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La cantidad excede el stock permitido"
                    );
                }

                stockNuevo = stockAnterior + cantidad;
            }

            case SALIDA -> {

                if (cantidad > stockAnterior) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Stock insuficiente"
                    );
                }

                stockNuevo = stockAnterior - cantidad;
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de movimiento no valido"
            );
        }

        // Actualizar existencias del producto
        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        // Registrar el historial de la operacion
        MovimientoInventario movimiento =
                new MovimientoInventario();

        movimiento.setProducto(producto);
        movimiento.setTipo(request.tipo());
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setMotivo(request.motivo().trim());

        MovimientoInventario guardado =
                movimientoRepository.save(movimiento);

        return convertirAResponse(guardado);
    }

    // Consultar historial de un producto
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse>
    listarMovimientosPorProducto(Long productoId) {

        if (!productoRepository.existsById(productoId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado"
            );
        }

        return movimientoRepository
                .findByProducto_IdOrderByFechaDesc(productoId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private MovimientoInventarioResponse convertirAResponse(
            MovimientoInventario movimiento) {

        return new MovimientoInventarioResponse(
                movimiento.getId(),
                movimiento.getProducto().getId(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                movimiento.getMotivo(),
                movimiento.getFecha()
        );
    }
}

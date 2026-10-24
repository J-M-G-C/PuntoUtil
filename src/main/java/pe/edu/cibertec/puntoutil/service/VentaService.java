package pe.edu.cibertec.puntoutil.service;

import pe.edu.cibertec.puntoutil.dto.DetalleVentaRequest;
import pe.edu.cibertec.puntoutil.dto.MovimientoInventarioRequest;
import pe.edu.cibertec.puntoutil.dto.VentaRequest;
import pe.edu.cibertec.puntoutil.entity.TipoMovimiento;
import pe.edu.cibertec.puntoutil.entity.DetalleVenta;
import pe.edu.cibertec.puntoutil.entity.Producto;
import pe.edu.cibertec.puntoutil.entity.Venta;
import pe.edu.cibertec.puntoutil.repository.ProductoRepository;
import pe.edu.cibertec.puntoutil.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioService inventarioService;

    @Transactional
    public Venta registrarVenta(VentaRequest request) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto.");
        }

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        BigDecimal totalAcumulado = BigDecimal.ZERO;

        for (DetalleVentaRequest item : request.getDetalles()) {
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            }

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para '" + producto.getNombre()
                        + "'. Stock disponible: " + producto.getStock()
                        + ", solicitado: " + item.getCantidad());
            }

            // Corrección aplicada: Uso del constructor y registrarMovimiento
            MovimientoInventarioRequest movimiento = new MovimientoInventarioRequest(
                    producto.getId(),
                    TipoMovimiento.SALIDA,
                    item.getCantidad(),
                    "Venta presencial"
            );
            inventarioService.registrarMovimiento(movimiento);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            detalle.setSubtotal(subtotal);

            venta.addDetalle(detalle);
            totalAcumulado = totalAcumulado.add(subtotal);
        }

        venta.setTotal(totalAcumulado);
        return ventaRepository.save(venta);
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }
}

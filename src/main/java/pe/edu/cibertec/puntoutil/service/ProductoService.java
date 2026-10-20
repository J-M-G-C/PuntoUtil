package pe.edu.cibertec.puntoutil.service;
import org.springframework.stereotype.Service;
import pe.edu.cibertec.puntoutil.dto.ProductoResponse;
import pe.edu.cibertec.puntoutil.entity.Producto;
import pe.edu.cibertec.puntoutil.repository.ProductoRepository;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoResponse> listarProductos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private ProductoResponse convertirAResponse(Producto producto) {
        return new ProductoResponse(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio()
        );
    }
}

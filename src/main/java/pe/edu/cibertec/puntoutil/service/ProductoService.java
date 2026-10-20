package pe.edu.cibertec.puntoutil.service;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.puntoutil.dto.ProductoRequest;
import pe.edu.cibertec.puntoutil.dto.ProductoResponse;
import pe.edu.cibertec.puntoutil.entity.Categoria;
import pe.edu.cibertec.puntoutil.entity.Producto;
import pe.edu.cibertec.puntoutil.repository.CategoriaRepository;
import pe.edu.cibertec.puntoutil.repository.ProductoRepository;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos() {
        return productoRepository.findAll()
                .stream()
                .filter(Producto::isActivo)
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional
    public ProductoResponse registrarProducto(ProductoRequest request) {

        if (productoRepository.findByCodigo(request.codigo()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con ese codigo"
            );
        }

        Producto producto = new Producto();

        producto.setCodigo(request.codigo());
        producto.setNombre(request.nombre());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setStockMinimo(request.stockMinimo());
        producto.setActivo(true);

        if (request.categoriaId() != null) {

            Categoria categoria = categoriaRepository
                    .findById(request.categoriaId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La categoria indicada no existe"
                    ));

            producto.setCategoria(categoria);
        }

        Producto guardado = productoRepository.save(producto);

        return convertirAResponse(guardado);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProductoPorId(Long id) {

        Producto producto = productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        return convertirAResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizarProducto(
            Long id, ProductoRequest request) {

        Producto producto = productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        productoRepository.findByCodigo(request.codigo())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe otro producto con ese codigo"
                    );
                });

        producto.setCodigo(request.codigo());
        producto.setNombre(request.nombre());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setStockMinimo(request.stockMinimo());

        if (request.categoriaId() != null) {

            Categoria categoria = categoriaRepository
                    .findById(request.categoriaId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La categoria indicada no existe"
                    ));

            producto.setCategoria(categoria);

        } else {
            producto.setCategoria(null);
        }

        Producto actualizado = productoRepository.save(producto);

        return convertirAResponse(actualizado);
    }

    // Desactivar un producto sin eliminarlo de la base de datos
    @Transactional
    public void desactivarProducto(Long id) {

        Producto producto = productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado o ya desactivado"
                ));

        producto.setActivo(false);

        productoRepository.save(producto);
    }

    private ProductoResponse convertirAResponse(Producto producto) {
        return new ProductoResponse(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio()
        );
    }
}

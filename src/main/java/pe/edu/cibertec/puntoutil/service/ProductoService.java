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
import java.util.Objects;

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

    // 1. Listar todos los productos activos
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos() {

        return productoRepository.findAll()
                .stream()
                .filter(Producto::isActivo)
                .map(this::convertirAResponse)
                .toList();
    }

    // 2. Registrar un producto
    @Transactional
    public ProductoResponse registrarProducto(
            ProductoRequest request) {

        if (productoRepository
                .findByCodigo(request.codigo())
                .isPresent()) {

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

    // 3. Consultar un producto por ID
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

    // 4. Actualizar los datos de un producto
    @Transactional
    public ProductoResponse actualizarProducto(
            Long id,
            ProductoRequest request) {

        // Bloquear el producto durante la actualizacion
        Producto producto = productoRepository
                .findByIdParaMovimiento(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        // Evitar modificaciones directas del stock
        if (!Objects.equals(
                producto.getStock(),
                request.stock())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock debe modificarse desde inventario"
            );
        }

        // Comprobar que el codigo no pertenezca a otro producto
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
        producto.setStockMinimo(request.stockMinimo());

        // Actualizar la categoria
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

    // 5. Desactivar un producto sin borrarlo de MySQL
    @Transactional
    public void desactivarProducto(Long id) {

        Producto producto = productoRepository
                .findByIdParaMovimiento(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado o ya desactivado"
                ));

        producto.setActivo(false);

        productoRepository.save(producto);
    }

    // 6. Convertir la entidad Producto a ProductoResponse
    private ProductoResponse convertirAResponse(Producto producto) {

        Categoria categoria = producto.getCategoria();

        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getStockMinimo(),
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNombre() : null,
                producto.isActivo()
        );
    }
}

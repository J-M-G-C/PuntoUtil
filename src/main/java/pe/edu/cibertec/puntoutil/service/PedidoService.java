package pe.edu.cibertec.puntoutil.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.puntoutil.dto.pedido.PedidoDetalleRequest;
import pe.edu.cibertec.puntoutil.dto.pedido.PedidoDetalleResponse;
import pe.edu.cibertec.puntoutil.dto.pedido.PedidoRequest;
import pe.edu.cibertec.puntoutil.dto.pedido.PedidoResponse;
import pe.edu.cibertec.puntoutil.entity.EstadoPedido;
import pe.edu.cibertec.puntoutil.entity.MovimientoInventario;
import pe.edu.cibertec.puntoutil.entity.Pedido;
import pe.edu.cibertec.puntoutil.entity.PedidoDetalle;
import pe.edu.cibertec.puntoutil.entity.Producto;
import pe.edu.cibertec.puntoutil.entity.TipoMovimiento;
import pe.edu.cibertec.puntoutil.entity.Usuario;
import pe.edu.cibertec.puntoutil.repository.MovimientoInventarioRepository;
import pe.edu.cibertec.puntoutil.repository.PedidoRepository;
import pe.edu.cibertec.puntoutil.repository.ProductoRepository;
import pe.edu.cibertec.puntoutil.repository.UsuarioRepository;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoService {

    private static final int MAX_INTENTOS_CODIGO = 10;
    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            MovimientoInventarioRepository movimientoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // 1. Registrar un pedido en linea para recojo en tienda
    @Transactional
    public PedidoResponse registrarPedido(
            PedidoRequest request,
            Authentication authentication) {

        // Agrupar cantidades cuando el mismo producto se repite en el pedido
        Map<Long, Integer> cantidades = new LinkedHashMap<>();

        for (PedidoDetalleRequest detalle : request.detalles()) {

            if (detalle == null || detalle.productoId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cada detalle debe indicar un producto"
                );
            }

            int cantidad = detalle.cantidad() == null
                    ? 0
                    : detalle.cantidad();

            if (cantidad <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad de cada producto debe ser mayor a 0"
                );
            }

            long cantidadAcumulada = (long) cantidades.getOrDefault(
                    detalle.productoId(), 0) + cantidad;

            if (cantidadAcumulada > Integer.MAX_VALUE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad total por producto excede el maximo permitido"
                );
            }

            cantidades.put(detalle.productoId(), (int) cantidadAcumulada);
        }

        Pedido pedido = new Pedido();
        pedido.setCodigoSeguimiento(generarCodigoUnico());
        pedido.setNombreCliente(request.nombreCliente().trim());
        pedido.setDni(normalizar(request.dni()));
        pedido.setTelefono(normalizar(request.telefono()));
        pedido.setCorreo(normalizar(request.correo()));
        pedido.setObservaciones(normalizar(request.observaciones()));
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setCliente(resolverCliente(authentication));

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {

            Producto producto = productoRepository.findById(entrada.getKey())
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Producto no encontrado o inactivo: " + entrada.getKey()
                    ));

            int cantidad = entrada.getValue();

            if (producto.getStock() < cantidad) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre()
                                + " (disponible: " + producto.getStock() + ")"
                );
            }

            BigDecimal precioUnitario = producto.getPrecio()
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal subtotal = precioUnitario
                    .multiply(BigDecimal.valueOf(cantidad))
                    .setScale(2, RoundingMode.HALF_UP);

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);

            pedido.agregarDetalle(detalle);

            total = total.add(subtotal);
        }

        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));

        Pedido guardado = pedidoRepository.save(pedido);

        return convertirAResponse(guardado);
    }

    // 2. Listar pedidos (el cliente solo ve los suyos)
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidos(
            EstadoPedido estado,
            Authentication authentication) {

        List<Pedido> pedidos;

        if (esCliente(authentication)) {

            pedidos = estado == null
                    ? pedidoRepository
                            .findByCliente_UsuarioOrderByFechaCreacionDesc(
                                    authentication.getName())
                    : pedidoRepository
                            .findByCliente_UsuarioAndEstadoOrderByFechaCreacionDesc(
                                    authentication.getName(), estado);

        } else {

            pedidos = estado == null
                    ? pedidoRepository.findAllByOrderByFechaCreacionDesc()
                    : pedidoRepository
                            .findByEstadoOrderByFechaCreacionDesc(estado);
        }

        return pedidos.stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // 3. Consultar un pedido por ID
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(
            Long id,
            Authentication authentication) {

        Pedido pedido = pedidoRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        verificarAcceso(pedido, authentication);

        return convertirAResponse(pedido);
    }

    // 4. Consultar un pedido por codigo de seguimiento
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedidoPorCodigo(
            String codigo,
            Authentication authentication) {

        Pedido pedido = pedidoRepository
                .findByCodigoSeguimientoWithDetalles(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        verificarAcceso(pedido, authentication);

        return convertirAResponse(pedido);
    }

    // 5. Cambiar el estado del pedido respetando las transiciones validas
    @Transactional
    public PedidoResponse actualizarEstado(
            Long id,
            EstadoPedido nuevoEstado) {

        // Bloqueo pesimista: evita confirmar dos entregas simultaneas
        Pedido pedido = pedidoRepository.findByIdParaActualizacion(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        EstadoPedido estadoActual = pedido.getEstado();

        if (estadoActual == nuevoEstado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El pedido ya se encuentra en estado " + nuevoEstado
            );
        }

        validarTransicion(estadoActual, nuevoEstado);

        if (nuevoEstado == EstadoPedido.ENTREGADO) {
            descontarStockUnaVez(pedido);
            pedido.setFechaEntrega(LocalDateTime.now());
        }

        pedido.setEstado(nuevoEstado);
        pedido.setFechaActualizacion(LocalDateTime.now());

        Pedido actualizado = pedidoRepository.save(pedido);

        return convertirAResponse(actualizado);
    }

    // 6. Confirmar la entrega del pedido
    @Transactional
    public PedidoResponse entregarPedido(Long id) {
        return actualizarEstado(id, EstadoPedido.ENTREGADO);
    }

    // 7. Cancelar el pedido
    @Transactional
    public PedidoResponse cancelarPedido(Long id) {
        return actualizarEstado(id, EstadoPedido.CANCELADO);
    }

    // 8. Generar el PDF de confirmacion (no es boleta SUNAT)
    @Transactional(readOnly = true)
    public byte[] generarPdfConfirmacion(
            Long id,
            Authentication authentication) {

        Pedido pedido = pedidoRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        verificarAcceso(pedido, authentication);

        return construirPdf(pedido);
    }

    // 8b. Generar el PDF usando el codigo de seguimiento
    @Transactional(readOnly = true)
    public byte[] generarPdfConfirmacionPorCodigo(
            String codigo,
            Authentication authentication) {

        Pedido pedido = pedidoRepository
                .findByCodigoSeguimientoWithDetalles(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        verificarAcceso(pedido, authentication);

        return construirPdf(pedido);
    }

    private void validarTransicion(
            EstadoPedido estadoActual,
            EstadoPedido nuevoEstado) {

        boolean permitida = switch (estadoActual) {
            case PENDIENTE ->
                    nuevoEstado == EstadoPedido.LISTO
                            || nuevoEstado == EstadoPedido.CANCELADO;
            case LISTO ->
                    nuevoEstado == EstadoPedido.ENTREGADO
                            || nuevoEstado == EstadoPedido.CANCELADO;
            case ENTREGADO, CANCELADO -> false;
        };

        if (!permitida) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede pasar de " + estadoActual
                            + " a " + nuevoEstado
            );
        }
    }

    /**
     * Descuenta el stock una sola vez. El pedido llega bloqueado con
     * PESSIMISTIC_WRITE y solo se permite desde el estado LISTO.
     */
    private void descontarStockUnaVez(Pedido pedido) {

        List<PedidoDetalle> detalles = new ArrayList<>(pedido.getDetalles());
        detalles.sort(Comparator.comparing(
                detalle -> detalle.getProducto().getId()));

        Map<Long, Producto> productosBloqueados = new LinkedHashMap<>();
        Map<Long, Integer> cantidades = new LinkedHashMap<>();

        // 1. Bloquear todos los productos en orden para evitar deadlocks
        for (PedidoDetalle detalle : detalles) {

            Long productoId = detalle.getProducto().getId();

            Producto producto = productoRepository
                    .findByIdParaMovimiento(productoId)
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "El producto " + detalle.getProducto().getNombre()
                                    + " ya no esta disponible"
                    ));

            productosBloqueados.put(productoId, producto);
            cantidades.merge(productoId, detalle.getCantidad(), Integer::sum);
        }

        // 2. Validar todo el stock antes de modificar existencias
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {

            Producto producto = productosBloqueados.get(entrada.getKey());
            int cantidad = entrada.getValue();

            if (producto.getStock() < cantidad) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre()
                                + " (disponible: " + producto.getStock() + ")"
                );
            }
        }

        // 3. Descontar y registrar el movimiento de inventario
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {

            Producto producto = productosBloqueados.get(entrada.getKey());
            int cantidad = entrada.getValue();

            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior - cantidad;

            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setTipo(TipoMovimiento.SALIDA);
            movimiento.setCantidad(cantidad);
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockNuevo(stockNuevo);
            movimiento.setMotivo(
                    "Pedido " + pedido.getCodigoSeguimiento()
                            + " entregado - recojo en tienda"
            );

            movimientoRepository.save(movimiento);
        }
    }

    private String generarCodigoUnico() {

        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (int intento = 0; intento < MAX_INTENTOS_CODIGO; intento++) {

            StringBuilder sufijo = new StringBuilder(4);

            for (int i = 0; i < 4; i++) {
                sufijo.append(ALFABETO_CODIGO.charAt(
                        RANDOM.nextInt(ALFABETO_CODIGO.length())));
            }

            String codigo = "PU-" + fecha + "-" + sufijo;

            if (!pedidoRepository.existsByCodigoSeguimiento(codigo)) {
                return codigo;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo generar el codigo de seguimiento"
        );
    }

    private Usuario resolverCliente(Authentication authentication) {

        if (!esCliente(authentication)) {
            return null;
        }

        return usuarioRepository.findByUsuario(authentication.getName())
                .orElse(null);
    }

    private boolean esCliente(Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CLIENTE"::equals);
    }

    private void verificarAcceso(
            Pedido pedido,
            Authentication authentication) {

        if (!esCliente(authentication)) {
            return;
        }

        boolean esPropietario = pedido.getCliente() != null
                && authentication.getName()
                        .equals(pedido.getCliente().getUsuario());

        if (!esPropietario) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tiene acceso a este pedido"
            );
        }
    }

    private String normalizar(String valor) {

        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();

        return limpio.isEmpty() ? null : limpio;
    }

    private PedidoResponse convertirAResponse(Pedido pedido) {

        List<PedidoDetalleResponse> detalles = pedido.getDetalles()
                .stream()
                .map(detalle -> new PedidoDetalleResponse(
                        detalle.getId(),
                        detalle.getProducto().getId(),
                        detalle.getProducto().getCodigo(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getCodigoSeguimiento(),
                pedido.getNombreCliente(),
                pedido.getDni(),
                pedido.getTelefono(),
                pedido.getCorreo(),
                pedido.getCliente() == null
                        ? null
                        : pedido.getCliente().getUsuario(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getFechaCreacion(),
                pedido.getFechaActualizacion(),
                pedido.getFechaEntrega(),
                pedido.getObservaciones(),
                detalles
        );
    }

    private byte[] construirPdf(Pedido pedido) {

        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        Document documento = new Document(
                PageSize.A4, 40, 40, 40, 40);

        try {

            PdfWriter.getInstance(documento, salida);
            documento.open();

            Font titulo = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 18);
            Font subtitulo = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(
                    FontFactory.HELVETICA, 10);
            Font negrita = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 10);
            Font nota = FontFactory.getFont(
                    FontFactory.HELVETICA_OBLIQUE, 8);

            Paragraph encabezado = new Paragraph(
                    "PUNTO UTIL - CONFIRMACION DE PEDIDO", titulo);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            documento.add(encabezado);

            Paragraph recojo = new Paragraph(
                    "Pedido en linea para recojo en tienda", normal);
            recojo.setAlignment(Element.ALIGN_CENTER);
            documento.add(recojo);

            documento.add(new Paragraph(" "));

            documento.add(new Paragraph(
                    "Codigo de seguimiento: "
                            + pedido.getCodigoSeguimiento(), subtitulo));

            documento.add(new Paragraph(
                    "Fecha de registro: "
                            + pedido.getFechaCreacion().format(FORMATO_FECHA),
                    normal));

            documento.add(new Paragraph(
                    "Estado: " + pedido.getEstado(), normal));

            if (pedido.getFechaEntrega() != null) {
                documento.add(new Paragraph(
                        "Fecha de entrega: "
                                + pedido.getFechaEntrega().format(FORMATO_FECHA),
                        normal));
            }

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("Datos del cliente", subtitulo));
            documento.add(new Paragraph(
                    "Nombre: " + pedido.getNombreCliente(), normal));

            if (pedido.getDni() != null) {
                documento.add(new Paragraph(
                        "DNI: " + pedido.getDni(), normal));
            }

            if (pedido.getTelefono() != null) {
                documento.add(new Paragraph(
                        "Telefono: " + pedido.getTelefono(), normal));
            }

            if (pedido.getCorreo() != null) {
                documento.add(new Paragraph(
                        "Correo: " + pedido.getCorreo(), normal));
            }

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("Detalle del pedido", subtitulo));

            PdfPTable tabla = new PdfPTable(new float[]{5f, 1.3f, 1.7f, 1.7f});
            tabla.setWidthPercentage(100);

            agregarCeldaTabla(tabla, "Producto", negrita);
            agregarCeldaTabla(tabla, "Cant.", negrita);
            agregarCeldaTabla(tabla, "P. unit.", negrita);
            agregarCeldaTabla(tabla, "Subtotal", negrita);

            for (PedidoDetalle detalle : pedido.getDetalles()) {

                agregarCeldaTabla(tabla,
                        detalle.getProducto().getNombre(), normal);
                agregarCeldaTabla(tabla,
                        String.valueOf(detalle.getCantidad()), normal);
                agregarCeldaTabla(tabla,
                        "S/ " + detalle.getPrecioUnitario(), normal);
                agregarCeldaTabla(tabla,
                        "S/ " + detalle.getSubtotal(), normal);
            }

            documento.add(tabla);

            Paragraph total = new Paragraph(
                    "TOTAL: S/ " + pedido.getTotal(), subtitulo);
            total.setAlignment(Element.ALIGN_RIGHT);
            documento.add(total);

            if (pedido.getObservaciones() != null) {
                documento.add(new Paragraph(" "));
                documento.add(new Paragraph(
                        "Observaciones: " + pedido.getObservaciones(),
                        normal));
            }

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph(
                    "Presente este codigo al momento de recoger su pedido.",
                    normal));

            documento.add(new Paragraph(" "));
            Paragraph leyenda = new Paragraph(
                    "Documento de confirmacion de pedido. "
                            + "No constituye boleta ni factura SUNAT.",
                    nota);
            leyenda.setAlignment(Element.ALIGN_CENTER);
            documento.add(leyenda);

        } finally {
            documento.close();
        }

        return salida.toByteArray();
    }

    private void agregarCeldaTabla(
            PdfPTable tabla,
            String texto,
            Font fuente) {

        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(4);
        tabla.addCell(celda);
    }
}

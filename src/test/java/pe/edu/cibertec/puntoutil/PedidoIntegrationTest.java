package pe.edu.cibertec.puntoutil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.cibertec.puntoutil.entity.MovimientoInventario;
import pe.edu.cibertec.puntoutil.entity.Producto;
import pe.edu.cibertec.puntoutil.entity.Rol;
import pe.edu.cibertec.puntoutil.entity.TipoMovimiento;
import pe.edu.cibertec.puntoutil.entity.Usuario;
import pe.edu.cibertec.puntoutil.repository.MovimientoInventarioRepository;
import pe.edu.cibertec.puntoutil.repository.PedidoRepository;
import pe.edu.cibertec.puntoutil.repository.ProductoRepository;
import pe.edu.cibertec.puntoutil.repository.UsuarioRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PedidoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Producto producto;

    @BeforeEach
    void prepararDatos() {
        movimientoRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        usuarioRepository.deleteAll();

        producto = new Producto();
        producto.setCodigo("CUA-001");
        producto.setNombre("Cuaderno anillado");
        producto.setPrecio(new BigDecimal("5.50"));
        producto.setStock(10);
        producto.setStockMinimo(2);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        Usuario cliente = new Usuario();
        cliente.setUsuario("cliente1");
        cliente.setPasswordHash(passwordEncoder.encode("ClaveCliente2026"));
        cliente.setNombre("Cliente de prueba");
        cliente.setEmail("cliente1@puntoutil.test");
        cliente.setRol(Rol.CLIENTE);
        cliente.setActivo(true);
        usuarioRepository.save(cliente);
    }

    @AfterEach
    void limpiarDatos() {
        movimientoRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void clienteNoPuedeConsultarPedidosDeOtroCliente() throws Exception {
        Long pedidoId = crearPedido();

        Usuario otroCliente = new Usuario();
        otroCliente.setUsuario("cliente2");
        otroCliente.setPasswordHash(passwordEncoder.encode("ClaveCliente2026"));
        otroCliente.setNombre("Otro Cliente");
        otroCliente.setEmail("cliente2@puntoutil.test");
        otroCliente.setRol(Rol.CLIENTE);
        otroCliente.setActivo(true);
        usuarioRepository.save(otroCliente);

        mockMvc.perform(get("/api/pedidos/{id}", pedidoId)
                        .with(user("cliente2").authorities(() -> "ROLE_CLIENTE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pedidos")
                        .with(user("cliente2").authorities(() -> "ROLE_CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void registraPedidoConCodigoDeSeguimientoSinDescontarStock() throws Exception {
        String response = registrarPedido(3)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoSeguimiento").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(16.50))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(3))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("codigoSeguimiento").asText()).startsWith("PU-");

        Producto actualizado = productoRepository.findById(producto.getId()).orElseThrow();
        assertThat(actualizado.getStock()).isEqualTo(10);
    }

    @Test
    void descuentaStockUnaSolaVezAlConfirmarLaEntrega() throws Exception {
        Long pedidoId = crearPedidoYListo(4);

        mockMvc.perform(patch("/api/pedidos/{id}/entregar", pedidoId)
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"))
                .andExpect(jsonPath("$.fechaEntrega").isNotEmpty());

        Producto actualizado = productoRepository.findById(producto.getId()).orElseThrow();
        assertThat(actualizado.getStock()).isEqualTo(6);

        List<MovimientoInventario> movimientos = movimientoRepository.findAll();
        assertThat(movimientos).hasSize(1);
        assertThat(movimientos.get(0).getTipo()).isEqualTo(TipoMovimiento.SALIDA);
        assertThat(movimientos.get(0).getCantidad()).isEqualTo(4);
        assertThat(movimientos.get(0).getStockNuevo()).isEqualTo(6);

        // Confirmar dos veces no debe descontar nuevamente
        mockMvc.perform(patch("/api/pedidos/{id}/entregar", pedidoId)
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR")))
                .andExpect(status().isConflict());

        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock())
                .isEqualTo(6);
        assertThat(movimientoRepository.findAll()).hasSize(1);
    }

    @Test
    void rechazaPedidoCuandoNoHayStockSuficiente() throws Exception {
        registrarPedido(99)
                .andExpect(status().isConflict());
    }

    @Test
    void cancelarPedidoMantieneElStockIntacto() throws Exception {
        Long pedidoId = crearPedido();

        mockMvc.perform(patch("/api/pedidos/{id}/cancelar", pedidoId)
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock())
                .isEqualTo(10);
        assertThat(movimientoRepository.findAll()).isEmpty();
    }

    @Test
    void noPermiteEntregarUnPedidoCancelado() throws Exception {
        Long pedidoId = crearPedido();

        mockMvc.perform(patch("/api/pedidos/{id}/cancelar", pedidoId)
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR")))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/pedidos/{id}/entregar", pedidoId)
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR")))
                .andExpect(status().isConflict());
    }

    @Test
    void generaPdfDeConfirmacionQueNoEsBoletaSunat() throws Exception {
        Long pedidoId = crearPedido();

        byte[] pdf = mockMvc.perform(get("/api/pedidos/{id}/pdf", pedidoId)
                        .with(user("admin").authorities(() -> "ROLE_ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void consultaPedidoPorCodigoDeSeguimiento() throws Exception {
        String response = registrarPedido(1)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String codigo = objectMapper.readTree(response)
                .get("codigoSeguimiento").asText();

        mockMvc.perform(get("/api/pedidos/seguimiento/{codigo}", codigo)
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoSeguimiento").value(codigo));
    }

    private Long crearPedido() throws Exception {
        String response = registrarPedido(2)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long crearPedidoYListo(int cantidad) throws Exception {
        String response = registrarPedido(cantidad)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long pedidoId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/pedidos/{id}/estado", pedidoId)
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"LISTO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LISTO"));

        return pedidoId;
    }

    private org.springframework.test.web.servlet.ResultActions registrarPedido(
            int cantidad) throws Exception {

        String body = """
                {
                  "nombreCliente": "Ana Torres",
                  "dni": "76543210",
                  "telefono": "987654321",
                  "correo": "ana@example.com",
                  "observaciones": "Recojo por la tarde",
                  "detalles": [
                    {"productoId": %d, "cantidad": %d}
                  ]
                }
                """.formatted(producto.getId(), cantidad);

        return mockMvc.perform(post("/api/pedidos")
                .with(user("cliente1").authorities(() -> "ROLE_CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}

package pe.edu.cibertec.puntoutil.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.cibertec.puntoutil.controller.ProductoController;
import pe.edu.cibertec.puntoutil.controller.UsuarioController;
import pe.edu.cibertec.puntoutil.service.ProductoService;
import pe.edu.cibertec.puntoutil.service.UsuarioService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductoController.class, UsuarioController.class})
@Import(SecurityConfig.class)
class PermisosEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void permiteConsultarCatalogoSinAutenticacion() throws Exception {
        when(productoService.listarProductos()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void permiteRegistrarProductoAlAdministrador() throws Exception {
        when(productoService.registrarProducto(any())).thenReturn(null);

        mockMvc.perform(post("/api/productos")
                        .with(user("admin").authorities(() -> "ROLE_ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"CUA-01","nombre":"Cuaderno","precio":5.50,"stock":10,"stockMinimo":2}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void deniegaRegistrarProductoAlVendedor() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"CUA-01","nombre":"Cuaderno","precio":5.50,"stock":10,"stockMinimo":2}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deniegaOperacionesProtegidasSinToken() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permiteGestionarUsuariosAlAdministrador() throws Exception {
        when(usuarioService.listar()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/usuarios")
                        .with(user("admin").authorities(() -> "ROLE_ADMINISTRADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void deniegaGestionDeUsuariosAlVendedorYAlCliente() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios")
                        .with(user("cliente").authorities(() -> "ROLE_CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deniegaAnularVentasAlVendedorYConsultarVentasAlCliente() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/ventas/1")
                        .with(user("vendedor").authorities(() -> "ROLE_VENDEDOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/ventas")
                        .with(user("cliente").authorities(() -> "ROLE_CLIENTE")))
                .andExpect(status().isForbidden());
    }
}

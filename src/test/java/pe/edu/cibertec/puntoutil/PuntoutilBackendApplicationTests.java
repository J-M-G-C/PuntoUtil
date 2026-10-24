package pe.edu.cibertec.puntoutil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.cibertec.puntoutil.entity.Rol;
import pe.edu.cibertec.puntoutil.entity.Usuario;
import pe.edu.cibertec.puntoutil.repository.UsuarioRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PuntoutilBackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void crearAdministradorDePrueba() {
        usuarios.deleteAll();
        Usuario admin = new Usuario();
        admin.setUsuario("admin");
        admin.setPasswordHash(passwordEncoder.encode("ClaveSegura2026"));
        admin.setNombre("Administradora de prueba");
        admin.setEmail("admin@puntoutil.test");
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setActivo(true);
        usuarios.save(admin);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void loginEmiteJwtValidoParaConsultarUsuarioActual() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"admin","contrasena":"ClaveSegura2026"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String token = json.get("token").asText();
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void loginRechazaContrasenaIncorrecta() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"admin","contrasena":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidaTokenSiSeDesactivaLaCuenta() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"admin","contrasena":"ClaveSegura2026"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        Usuario admin = usuarios.findByUsuario("admin").orElseThrow();
        admin.setActivo(false);
        usuarios.save(admin);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}

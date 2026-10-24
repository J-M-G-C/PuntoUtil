package pe.edu.cibertec.puntoutil.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.puntoutil.entity.Rol;
import pe.edu.cibertec.puntoutil.entity.Usuario;
import pe.edu.cibertec.puntoutil.repository.UsuarioRepository;

@Component
public class BootstrapAdminRunner implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String name;
    private final String email;

    public BootstrapAdminRunner(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            @Value("${security.bootstrap-admin.username:}") String username,
            @Value("${security.bootstrap-admin.password:}") String password,
            @Value("${security.bootstrap-admin.nombre:Administrador}") String name,
            @Value("${security.bootstrap-admin.email:}") String email) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    @Override
    public void run(String... args) {
        if (username.isBlank() && password.isBlank() && email.isBlank()) {
            return;
        }
        if (username.isBlank() || password.isBlank() || email.isBlank()) {
            throw new IllegalStateException(
                    "Configure ADMIN_USERNAME, ADMIN_PASSWORD y ADMIN_EMAIL para crear el administrador inicial");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD debe tener al menos 12 caracteres");
        }
        if (!usuarios.existsByUsuarioIgnoreCase(username.trim())
                && !usuarios.existsByEmailIgnoreCase(email.trim())) {
            Usuario admin = new Usuario();
            admin.setUsuario(username.trim());
            admin.setNombre(name.trim());
            admin.setEmail(email.trim().toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setActivo(true);
            usuarios.save(admin);
        }
    }
}

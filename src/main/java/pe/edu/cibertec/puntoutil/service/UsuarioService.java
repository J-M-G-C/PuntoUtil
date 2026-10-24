package pe.edu.cibertec.puntoutil.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.cibertec.puntoutil.dto.ActualizarUsuarioRequest;
import pe.edu.cibertec.puntoutil.dto.CrearUsuarioRequest;
import pe.edu.cibertec.puntoutil.dto.UsuarioResponse;
import pe.edu.cibertec.puntoutil.entity.Usuario;
import pe.edu.cibertec.puntoutil.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    public UsuarioResponse crear(CrearUsuarioRequest request) {
        String usuario = request.usuario().trim();
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByUsuarioIgnoreCase(usuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsuario(usuario);
        nuevo.setNombre(request.nombre().trim());
        nuevo.setEmail(email);
        nuevo.setPasswordHash(passwordEncoder.encode(request.contrasena()));
        nuevo.setRol(request.rol());
        nuevo.setActivo(true);
        return toResponse(usuarioRepository.save(nuevo));
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscar(id);
        String email = request.email().trim().toLowerCase();
        if (!usuario.getEmail().equalsIgnoreCase(email) && usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (request.contrasena() != null && !request.contrasena().isBlank()
                && request.contrasena().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres");
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setRol(request.rol());
        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }
        if (request.contrasena() != null && !request.contrasena().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.contrasena()));
        }
        return toResponse(usuarioRepository.save(usuario));
    }

    public void desactivar(Long id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    public UsuarioResponse obtenerPorUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return toResponse(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsuario(), usuario.getNombre(),
                usuario.getEmail(), usuario.getRol(), usuario.isActivo());
    }
}

package pe.edu.cibertec.puntoutil.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.cibertec.puntoutil.dto.LoginRequest;
import pe.edu.cibertec.puntoutil.dto.LoginResponse;
import pe.edu.cibertec.puntoutil.dto.UsuarioResponse;
import pe.edu.cibertec.puntoutil.service.AutenticacionService;
import pe.edu.cibertec.puntoutil.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;
    private final UsuarioService usuarioService;

    public AutenticacionController(AutenticacionService autenticacionService, UsuarioService usuarioService) {
        this.autenticacionService = autenticacionService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticacionService.login(request);
    }

    @GetMapping("/me")
    public UsuarioResponse usuarioActual(Authentication authentication) {
        return usuarioService.obtenerPorUsuario(authentication.getName());
    }
}

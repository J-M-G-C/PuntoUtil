package pe.edu.cibertec.puntoutil.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import pe.edu.cibertec.puntoutil.dto.LoginRequest;
import pe.edu.cibertec.puntoutil.dto.LoginResponse;
import pe.edu.cibertec.puntoutil.entity.Usuario;
import pe.edu.cibertec.puntoutil.repository.UsuarioRepository;

@Service
public class AutenticacionService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UsuarioRepository usuarioRepository;
    private final long expirationSeconds;

    public AutenticacionService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            UsuarioRepository usuarioRepository,
            @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.usuarioRepository = usuarioRepository;
        this.expirationSeconds = expirationSeconds;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.usuario().trim(), request.contrasena()));
        Usuario usuario = usuarioRepository.findByUsuario(authentication.getName())
                .orElseThrow();

        Instant now = Instant.now();
        String authority = "ROLE_" + usuario.getRol().name();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("puntoutil")
                .issuedAt(now)
                .expiresAt(now.plus(expirationSeconds, ChronoUnit.SECONDS))
                .subject(usuario.getUsuario())
                .claim("nombre", usuario.getNombre())
                .claim("authorities", List.of(authority))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", expirationSeconds,
                usuario.getUsuario(), usuario.getNombre(), usuario.getRol().name());
    }
}

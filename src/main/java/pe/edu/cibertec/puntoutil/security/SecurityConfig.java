package pe.edu.cibertec.puntoutil.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Excepciones temporales de CSRF para pruebas locales
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/productos",
                                "/api/productos/**",
                                "/api/categorias",
                                "/api/categorias/**",
                                "/api/inventario/**"
                        )
                )

                // Autenticacion sin sesiones HTTP
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Permisos de acceso
                .authorizeHttpRequests(auth -> auth

                        // RECURSOS PUBLICOS
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/api/estado"
                        ).permitAll()

                        // PRODUCTOS: CONSULTAR
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/productos",
                                "/api/productos/**"
                        ).permitAll()

                        // PRODUCTOS: REGISTRAR
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/productos"
                        ).authenticated()

                        // PRODUCTOS: ACTUALIZAR
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/productos/**"
                        ).authenticated()

                        // PRODUCTOS: DESACTIVAR
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/productos/**"
                        ).authenticated()

                        // CATEGORIAS: CONSULTAR
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias",
                                "/api/categorias/**"
                        ).permitAll()

                        // CATEGORIAS: REGISTRAR
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categorias"
                        ).authenticated()

                        // CATEGORIAS: ACTUALIZAR
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categorias/**"
                        ).authenticated()

                        // CATEGORIAS: DESACTIVAR
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categorias/**"
                        ).authenticated()

                        // INVENTARIO: REGISTRAR MOVIMIENTOS
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/inventario/movimientos"
                        ).authenticated()

                        // INVENTARIO: CONSULTAR HISTORIAL
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inventario/movimientos/producto/**"
                        ).authenticated()

                        // PROTEGER TODAS LAS DEMAS RUTAS
                        .anyRequest().authenticated()
                )

                // HTTP Basic temporal para desarrollo
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

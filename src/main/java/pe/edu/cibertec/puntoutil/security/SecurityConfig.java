
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
                // Excepcion temporal de CSRF para pruebas de la API
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/productos",
                                "/api/productos/**"
                        )
                )

                // No guardar sesiones para las solicitudes de la API
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configuracion de acceso
                .authorizeHttpRequests(auth -> auth

                        // Recursos publicos
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/api/estado"
                        ).permitAll()

                        // Consultar productos sin autenticacion
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/productos",
                                "/api/productos/**"
                        ).permitAll()

                        // Registrar productos con autenticacion
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/productos"
                        ).authenticated()

                        // Actualizar productos con autenticacion
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/productos/**"
                        ).authenticated()

                        // Desactivar productos con autenticacion
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/productos/**"
                        ).authenticated()

                        // Proteger los demas endpoints
                        .anyRequest().authenticated()
                )

                // Autenticacion HTTP Basic para desarrollo
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

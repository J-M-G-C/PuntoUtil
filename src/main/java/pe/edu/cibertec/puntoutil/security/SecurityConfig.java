
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
                // Excepcion temporal de CSRF para la API de desarrollo
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/productos",
                                "/api/productos/**",
                                "/api/categorias",
                                "/api/categorias/**"
                        )
                )

                // No almacenar sesiones de autenticacion
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Configuracion de permisos
                .authorizeHttpRequests(auth -> auth

                        // Recursos publicos de la aplicacion
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/api/estado"
                        ).permitAll()

                        // PRODUCTOS

                        // Listar y consultar productos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/productos",
                                "/api/productos/**"
                        ).permitAll()

                        // Registrar productos
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/productos"
                        ).authenticated()

                        // Actualizar productos
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/productos/**"
                        ).authenticated()

                        // Desactivar productos
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/productos/**"
                        ).authenticated()

                        // CATEGORIAS

                        // Listar categorias
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias",
                                "/api/categorias/**"
                        ).permitAll()

                        // Registrar categorias
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categorias"
                        ).authenticated()

                        // Actualizar categorias
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categorias/**"
                        ).authenticated()

                        // Desactivar categorias
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categorias/**"
                        ).authenticated()

                        // Proteger todas las demas rutas
                        .anyRequest().authenticated()
                )

                // Autenticacion HTTP Basic para desarrollo
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

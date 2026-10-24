package pe.edu.cibertec.puntoutil.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/error",
                                "/api/estado",
                                "/api/auth/login"
                        ).permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/productos", "/api/productos/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/categorias", "/api/categorias/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/inventario/movimientos")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/inventario/movimientos/producto/**")
                        .hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_VENDEDOR")
                        .requestMatchers(HttpMethod.GET, "/api/ventas/**")
                        .hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_VENDEDOR")
                        .requestMatchers(HttpMethod.POST, "/api/ventas/**")
                        .hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_VENDEDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/ventas/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/ventas/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/ventas/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**")
                        .hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_VENDEDOR", "ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/**")
                        .hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**")
                        .hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_VENDEDOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/**")
                        .hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_VENDEDOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("authorities");
        authorities.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}

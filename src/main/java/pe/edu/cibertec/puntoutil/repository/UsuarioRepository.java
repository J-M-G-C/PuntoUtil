package pe.edu.cibertec.puntoutil.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.puntoutil.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByEmailIgnoreCase(String email);
}

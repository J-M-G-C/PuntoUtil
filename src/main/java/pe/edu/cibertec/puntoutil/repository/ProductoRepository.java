package pe.edu.cibertec.puntoutil.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.cibertec.puntoutil.entity.Producto;

import java.util.Optional;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdParaMovimiento(
            @Param("id") Long id
    );
}

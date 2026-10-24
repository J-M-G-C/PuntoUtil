package pe.edu.cibertec.puntoutil.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.cibertec.puntoutil.entity.EstadoPedido;
import pe.edu.cibertec.puntoutil.entity.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByCodigoSeguimiento(String codigoSeguimiento);

    boolean existsByCodigoSeguimiento(String codigoSeguimiento);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    List<Pedido> findAllByOrderByFechaCreacionDesc();

    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    List<Pedido> findByEstadoOrderByFechaCreacionDesc(EstadoPedido estado);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    List<Pedido> findByCliente_UsuarioOrderByFechaCreacionDesc(String usuario);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    List<Pedido> findByCliente_UsuarioAndEstadoOrderByFechaCreacionDesc(
            String usuario, EstadoPedido estado);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto WHERE p.id = :id")
    Optional<Pedido> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto WHERE p.codigoSeguimiento = :codigo")
    Optional<Pedido> findByCodigoSeguimientoWithDetalles(@Param("codigo") String codigoSeguimiento);

    /**
     * Bloquea la fila del pedido para que dos confirmaciones de entrega
     * simultaneas no puedan descontar el stock dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdParaActualizacion(@Param("id") Long id);
}

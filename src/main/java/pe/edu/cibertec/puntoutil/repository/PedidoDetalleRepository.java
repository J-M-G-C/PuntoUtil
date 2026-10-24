package pe.edu.cibertec.puntoutil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.puntoutil.entity.PedidoDetalle;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Long> {
}

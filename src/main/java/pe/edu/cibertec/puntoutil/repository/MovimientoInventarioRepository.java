package pe.edu.cibertec.puntoutil.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.cibertec.puntoutil.entity.MovimientoInventario;

import java.util.List;

public interface MovimientoInventarioRepository
        extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario>
    findByProducto_IdOrderByFechaDesc(Long productoId);
}

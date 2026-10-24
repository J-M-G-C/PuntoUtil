package pe.edu.cibertec.puntoutil.repository;

import pe.edu.cibertec.puntoutil.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
}
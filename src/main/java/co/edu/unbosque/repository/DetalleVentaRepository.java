package co.edu.unbosque.repository;

import co.edu.unbosque.entity.DetalleVenta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaRepository extends CrudRepository<DetalleVenta, Long> {
	
	
}

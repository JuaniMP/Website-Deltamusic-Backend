package co.edu.unbosque.repository;

import co.edu.unbosque.entity.MetodoPago;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetodoPagoRepository extends CrudRepository<MetodoPago, Long> {
	
}

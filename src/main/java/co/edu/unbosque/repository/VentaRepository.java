package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Venta; 
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends CrudRepository<Venta, Long> { 
	

}

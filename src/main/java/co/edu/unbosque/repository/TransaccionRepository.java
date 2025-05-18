package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Transaccion; 
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionRepository extends CrudRepository<Transaccion, Long> { 

}

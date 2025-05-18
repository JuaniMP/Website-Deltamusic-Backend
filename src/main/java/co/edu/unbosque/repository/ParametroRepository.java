package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Parametro; 
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametroRepository extends CrudRepository<Parametro, Long> { 

}

package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Categoria;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends CrudRepository<Categoria, Long> {
	
	
}

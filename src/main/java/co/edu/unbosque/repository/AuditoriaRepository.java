package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Auditoria;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends CrudRepository<Auditoria, Long> {
	
}
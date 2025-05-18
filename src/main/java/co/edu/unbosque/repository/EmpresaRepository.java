package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Empresa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends CrudRepository<Empresa, Long> {
	
}

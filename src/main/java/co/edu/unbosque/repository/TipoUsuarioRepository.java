package co.edu.unbosque.repository;

import co.edu.unbosque.entity.TipoUsuario; 
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoUsuarioRepository extends CrudRepository<TipoUsuario, Long> { 

}

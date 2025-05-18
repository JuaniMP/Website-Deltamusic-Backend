package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Producto; // Cambié Usuario por Producto
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends CrudRepository<Producto, Long> { // Cambié UsuarioRepository por ProductoRepository

}

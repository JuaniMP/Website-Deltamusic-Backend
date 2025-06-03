// src/main/java/co/edu/unbosque/repository/ClienteRepository.java
package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Cliente;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Long> {

    /**
     * Busca un cliente por su correo (campo "correo_cliente" en la entidad).
     * Devuelve null si no existe ningún Cliente con ese correo.
     */
    Cliente findByCorreoCliente(String correoCliente);

}

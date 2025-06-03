// src/main/java/co/edu/unbosque/repository/ParametroRepository.java
package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Parametro;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametroRepository extends CrudRepository<Parametro, Long> {

    /**
     * Busca un PARAMETRO por descripción y estado.
     * Ej: ("IVA", (byte)1).
     * Retorna Optional.empty() si no existe.
     */
    Optional<Parametro> findByDescripcionAndEstado(String descripcion, byte estado);
}

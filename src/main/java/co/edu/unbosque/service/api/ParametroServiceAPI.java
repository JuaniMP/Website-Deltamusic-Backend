// src/main/java/co/edu/unbosque/service/api/ParametroServiceAPI.java
package co.edu.unbosque.service.api;

import co.edu.unbosque.entity.Parametro;
import co.edu.unbosque.utils.GenericServiceAPI;

import java.util.Optional;

public interface ParametroServiceAPI extends GenericServiceAPI<Parametro, Long> {

    /**
     * Busca un Parametro por su descripción y estado.
     * Retorna Optional.empty() si no existe.
     */
    Optional<Parametro> findByDescripcionAndEstado(String descripcion, byte estado);
}

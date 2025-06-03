// src/main/java/co/edu/unbosque/service/api/ClienteServiceAPI.java
package co.edu.unbosque.service.api;

import co.edu.unbosque.utils.GenericServiceAPI;
import co.edu.unbosque.entity.Cliente;

public interface ClienteServiceAPI extends GenericServiceAPI<Cliente, Long> {

    /**
     * Busca un Cliente por su correo. Devuelve null si no lo encuentra.
     */
    Cliente findByCorreoCliente(String correoCliente);

}

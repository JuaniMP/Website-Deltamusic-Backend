// src/main/java/co/edu/unbosque/service/api/TransaccionServiceAPI.java
package co.edu.unbosque.service.api;

import co.edu.unbosque.utils.GenericServiceAPI;
import co.edu.unbosque.entity.Transaccion;

public interface TransaccionServiceAPI extends GenericServiceAPI<Transaccion, Long>  {
    Transaccion findByIdCompra(Long idCompra);
}

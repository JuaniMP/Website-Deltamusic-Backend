// src/main/java/co/edu/unbosque/service/impl/TransaccionServiceImpl.java
package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.entity.Transaccion; 
import co.edu.unbosque.service.api.TransaccionServiceAPI;
import co.edu.unbosque.repository.TransaccionRepository; 

@Service
public class TransaccionServiceImpl extends GenericServiceImpl<Transaccion, Long> implements TransaccionServiceAPI { 

    @Autowired
    private TransaccionRepository transaccionRepository; 

    @Override
    public CrudRepository<Transaccion, Long> getDao() {
        return transaccionRepository; 
    }

    @Override
    public Transaccion findByIdCompra(Long idCompra) {
        return transaccionRepository.findByIdCompra(idCompra);
    }
}

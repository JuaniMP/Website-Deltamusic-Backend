package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Parametro;
import co.edu.unbosque.repository.ParametroRepository;
import co.edu.unbosque.service.api.ParametroServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class ParametroServiceImpl extends GenericServiceImpl<Parametro, Long> implements ParametroServiceAPI {

    @Autowired
    private ParametroRepository parametroRepository;

    @Override
    public CrudRepository<Parametro, Long> getDao() {
        return parametroRepository;
    }

    @Override
    public Parametro findByDescripcionAndEstado(String descripcion, byte estado) {
        return parametroRepository.findByDescripcionAndEstado(descripcion, estado);
    }
}

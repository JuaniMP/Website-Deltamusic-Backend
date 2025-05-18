package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.entity.Parametro; // Cambié Usuario por Parametro
import co.edu.unbosque.service.api.ParametroServiceAPI; // Cambié UsuarioServiceAPI por ParametroServiceAPI
import co.edu.unbosque.repository.ParametroRepository; // Cambié UsuarioRepository por ParametroRepository

@Service
public class ParametroServiceImpl extends GenericServiceImpl<Parametro, Long> implements ParametroServiceAPI { 

    @Autowired
    private ParametroRepository parametroRepository; 

    @Override
    public CrudRepository<Parametro, Long> getDao() { 
        return parametroRepository; 
    }
}

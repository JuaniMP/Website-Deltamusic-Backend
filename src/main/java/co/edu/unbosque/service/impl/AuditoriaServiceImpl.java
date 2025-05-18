package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.repository.AuditoriaRepository;

@Service
public class AuditoriaServiceImpl extends GenericServiceImpl<Auditoria, Long> implements AuditoriaServiceAPI {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Override
    public CrudRepository<Auditoria, Long> getDao() {
        return auditoriaRepository;
    }
}

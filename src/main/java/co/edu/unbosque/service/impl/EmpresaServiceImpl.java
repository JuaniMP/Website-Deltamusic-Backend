package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.entity.Empresa;
import co.edu.unbosque.service.api.EmpresaServiceAPI;
import co.edu.unbosque.repository.EmpresaRepository;

@Service
public class EmpresaServiceImpl extends GenericServiceImpl<Empresa, Long> implements EmpresaServiceAPI {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Override
    public CrudRepository<Empresa, Long> getDao() {
        return empresaRepository;
    }
}

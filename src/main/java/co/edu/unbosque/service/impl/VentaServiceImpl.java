package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.entity.Venta; 
import co.edu.unbosque.service.api.VentaServiceAPI; 
import co.edu.unbosque.repository.VentaRepository; 

@Service
public class VentaServiceImpl extends GenericServiceImpl<Venta, Long> implements VentaServiceAPI {

    @Autowired
    private VentaRepository ventaRepository;

    @Override
    public CrudRepository<Venta, Long> getDao() { 
        return ventaRepository; 
    }
}

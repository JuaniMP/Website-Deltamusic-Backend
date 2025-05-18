package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.entity.Producto; // Cambié Usuario por Producto
import co.edu.unbosque.service.api.ProductoServiceAPI; // Cambié UsuarioServiceAPI por ProductoServiceAPI
import co.edu.unbosque.repository.ProductoRepository; // Cambié UsuarioRepository por ProductoRepository

@Service
public class ProductoServiceImpl extends GenericServiceImpl<Producto, Long> implements ProductoServiceAPI { 

    @Autowired
    private ProductoRepository productoRepository; 

    @Override
    public CrudRepository<Producto, Long> getDao() { 
        return productoRepository; 
    }
}

package co.edu.unbosque.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.entity.DetalleVenta;
import co.edu.unbosque.service.api.DetalleVentaServiceAPI;
import co.edu.unbosque.repository.DetalleVentaRepository;

@Service
public class DetalleVentaServiceImpl extends GenericServiceImpl<DetalleVenta, Long> implements DetalleVentaServiceAPI {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Override
    public CrudRepository<DetalleVenta, Long> getDao() {
        return detalleVentaRepository;
    }
    

	@Override
	public int totalProductosClientePorFecha(Long idCliente, Date fechaVenta) {
		return detalleVentaRepository.totalProductosClientePorFecha(idCliente, fechaVenta);
	}
}

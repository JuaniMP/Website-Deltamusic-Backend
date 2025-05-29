package co.edu.unbosque.service.api;

import co.edu.unbosque.utils.GenericServiceAPI;

import java.util.Date;

import co.edu.unbosque.entity.DetalleVenta;

public interface DetalleVentaServiceAPI extends GenericServiceAPI<DetalleVenta, Long> {
	
	public int totalProductosClientePorFecha(int idCliente, Date fechaVenta);

    
}

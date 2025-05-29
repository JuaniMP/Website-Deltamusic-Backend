package co.edu.unbosque.repository;

import co.edu.unbosque.entity.DetalleVenta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaRepository extends CrudRepository<DetalleVenta, Long> {
    // Retorna la suma de productos comprados por el cliente en una fecha
	@Query("SELECT COALESCE(SUM(dv.cantComp), 0) FROM DetalleVenta dv " +
		       "WHERE dv.venta.idCliente = :idCliente AND dv.venta.fechaVenta = :fechaVenta")
		int totalProductosClientePorFecha(int idCliente, java.util.Date fechaVenta);
}

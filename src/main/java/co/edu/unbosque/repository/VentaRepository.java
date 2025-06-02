package co.edu.unbosque.repository;

import co.edu.unbosque.entity.Venta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface VentaRepository extends CrudRepository<Venta, Long> {

    // Todas las ventas de un mes dado
    @Query("SELECT v FROM Venta v WHERE FUNCTION('MONTH', v.fechaVenta) = :mes AND FUNCTION('YEAR', v.fechaVenta) = :anio")
    List<Venta> findVentasByMes(@Param("mes") int mes, @Param("anio") int anio);

    // Ventas entre fechas (útil para estadísticas personalizadas)
    @Query("SELECT v FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    List<Venta> findVentasBetween(@Param("inicio") Date inicio, @Param("fin") Date fin);

}

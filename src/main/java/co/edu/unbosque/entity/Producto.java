package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the producto database table.
 * 
 */
@Entity
@Table(name="producto")
public class Producto implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private short id;

	@Column(name="costo_compra")
	private BigDecimal costoCompra;
	
	@Column(name="descripcion")
	private String descripcion;
	
	@Column(name="estado")
	private byte estado;

	private int existencia;

	@Column(name="foto_producto")
	private String fotoProducto;

	@Column(name="id_categoria")
	private short idCategoria;

	@Column(name="precio_venta_actual")
	private BigDecimal precioVentaActual;

	@Column(name="precio_venta_anterior")
	private BigDecimal precioVentaAnterior;
	
	@Column(name="referencia")
	private String referencia;

	@Column(name="stock_maximo")
	private int stockMaximo;

	@Column(name="tiene_iva")
	private byte tieneIva;

	public Producto() {
	}

	public short getId() {
		return this.id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public BigDecimal getCostoCompra() {
		return this.costoCompra;
	}

	public void setCostoCompra(BigDecimal costoCompra) {
		this.costoCompra = costoCompra;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public byte getEstado() {
		return this.estado;
	}

	public void setEstado(byte estado) {
		this.estado = estado;
	}

	public int getExistencia() {
		return this.existencia;
	}

	public void setExistencia(int existencia) {
		this.existencia = existencia;
	}

	public String getFotoProducto() {
		return this.fotoProducto;
	}

	public void setFotoProducto(String fotoProducto) {
		this.fotoProducto = fotoProducto;
	}

	public short getIdCategoria() {
		return this.idCategoria;
	}

	public void setIdCategoria(short idCategoria) {
		this.idCategoria = idCategoria;
	}

	public BigDecimal getPrecioVentaActual() {
		return this.precioVentaActual;
	}

	public void setPrecioVentaActual(BigDecimal precioVentaActual) {
		this.precioVentaActual = precioVentaActual;
	}

	public BigDecimal getPrecioVentaAnterior() {
		return this.precioVentaAnterior;
	}

	public void setPrecioVentaAnterior(BigDecimal precioVentaAnterior) {
		this.precioVentaAnterior = precioVentaAnterior;
	}

	public String getReferencia() {
		return this.referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public int getStockMaximo() {
		return this.stockMaximo;
	}

	public void setStockMaximo(int stockMaximo) {
		this.stockMaximo = stockMaximo;
	}

	public byte getTieneIva() {
		return this.tieneIva;
	}

	public void setTieneIva(byte tieneIva) {
		this.tieneIva = tieneIva;
	}

}
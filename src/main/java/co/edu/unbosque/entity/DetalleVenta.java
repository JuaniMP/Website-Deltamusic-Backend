package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;

/**
 * The persistent class for the detalle_venta database table.
 * 
 */
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "cant_comp")
	private int cantComp;

	@Column(name = "id_producto")
	private short idProducto;

	@ManyToOne
	@JoinColumn(name = "id_venta", referencedColumnName = "id")
	@com.fasterxml.jackson.annotation.JsonIgnore
	private Venta venta;


	@Column(name = "valor_dscto")
	private int valorDscto;

	@Column(name = "valor_iva")
	private int valorIva;

	@Column(name = "valor_unit")
	private int valorUnit;

	public DetalleVenta() {
	}

	public Venta getVenta() {
		return venta;
	}

	public void setVenta(Venta venta) {
		this.venta = venta;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getCantComp() {
		return this.cantComp;
	}

	public void setCantComp(int cantComp) {
		this.cantComp = cantComp;
	}

	public short getIdProducto() {
		return this.idProducto;
	}

	public void setIdProducto(short idProducto) {
		this.idProducto = idProducto;
	}

	public int getValorDscto() {
		return this.valorDscto;
	}

	public void setValorDscto(int valorDscto) {
		this.valorDscto = valorDscto;
	}

	public int getValorIva() {
		return this.valorIva;
	}

	public void setValorIva(int valorIva) {
		this.valorIva = valorIva;
	}

	public int getValorUnit() {
		return this.valorUnit;
	}

	public void setValorUnit(int valorUnit) {
		this.valorUnit = valorUnit;
	}

}
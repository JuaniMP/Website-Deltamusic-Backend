package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;


/**
 * The persistent class for the detalle_venta database table.
 * 
 */
@Entity
@Table(name="detalle_venta")
public class DetalleVenta implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private int id;

	@Column(name="cant_comp")
	private int cantComp;

	@Column(name="id_producto")
	private short idProducto;

	@Column(name="id_venta")
	private int idVenta;

	@Column(name="valor_dscto")
	private int valorDscto;

	@Column(name="valor_iva")
	private int valorIva;

	@Column(name="valor_unit")
	private int valorUnit;

	public DetalleVenta() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
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

	public int getIdVenta() {
		return this.idVenta;
	}

	public void setIdVenta(int idVenta) {
		this.idVenta = idVenta;
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
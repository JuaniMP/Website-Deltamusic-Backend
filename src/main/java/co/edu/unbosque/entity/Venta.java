package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Date;


/**
 * The persistent class for the venta database table.
 * 
 */
@Entity
@Table(name="venta")
public class Venta implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private Long id;

	@Column(name="estado")
	private byte estado;

	@Temporal(TemporalType.DATE)
	@Column(name="fecha_venta")
	private Date fechaVenta;

	@Column(name="id_cliente")
	private int idCliente;

	@Column(name="valor_dscto")
	private int valorDscto;

	@Column(name="valor_iva")
	private int valorIva;

	@Column(name="valor_venta")
	private int valorVenta;

	public Venta() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public byte getEstado() {
		return this.estado;
	}

	public void setEstado(byte estado) {
		this.estado = estado;
	}

	public Date getFechaVenta() {
		return this.fechaVenta;
	}

	public void setFechaVenta(Date fechaVenta) {
		this.fechaVenta = fechaVenta;
	}

	public int getIdCliente() {
		return this.idCliente;
	}

	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
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

	public int getValorVenta() {
		return this.valorVenta;
	}

	public void setValorVenta(int valorVenta) {
		this.valorVenta = valorVenta;
	}

}
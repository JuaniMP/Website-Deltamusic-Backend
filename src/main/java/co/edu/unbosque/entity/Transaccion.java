package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Date;


/**
 * The persistent class for the transaccion database table.
 * 
 */
@Entity
@Table(name="transaccion")
public class Transaccion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private Long id;

	@Column(name="estado")
	private byte estado;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="fecha_hora")
	private Date fechaHora;

	@Column(name="id_banco")
	private String idBanco;

	@Column(name="id_compra")
	private int idCompra;

	@Column(name="id_franquicia")
	private String idFranquicia;

	@Column(name="id_metodo_pago")
	private short idMetodoPago;
	
	@Column(name="identificacion")
	private String identificacion;

	@Column(name="num_tarjeta")
	private String numTarjeta;

	@Column(name="valor_tx")
	private int valorTx;

	public Transaccion() {
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

	public Date getFechaHora() {
		return this.fechaHora;
	}

	public void setFechaHora(Date fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getIdBanco() {
		return this.idBanco;
	}

	public void setIdBanco(String idBanco) {
		this.idBanco = idBanco;
	}

	public int getIdCompra() {
		return this.idCompra;
	}

	public void setIdCompra(int idCompra) {
		this.idCompra = idCompra;
	}

	public String getIdFranquicia() {
		return this.idFranquicia;
	}

	public void setIdFranquicia(String idFranquicia) {
		this.idFranquicia = idFranquicia;
	}

	public short getIdMetodoPago() {
		return this.idMetodoPago;
	}

	public void setIdMetodoPago(short idMetodoPago) {
		this.idMetodoPago = idMetodoPago;
	}

	public String getIdentificacion() {
		return this.identificacion;
	}

	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}

	public String getNumTarjeta() {
		return this.numTarjeta;
	}

	public void setNumTarjeta(String numTarjeta) {
		this.numTarjeta = numTarjeta;
	}

	public int getValorTx() {
		return this.valorTx;
	}

	public void setValorTx(int valorTx) {
		this.valorTx = valorTx;
	}

}
package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;


/**
 * The persistent class for the metodo_pago database table.
 * 
 */
@Entity
@Table(name="metodo_pago")
public class MetodoPago implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private short id;

	@Column(name="descripcion")
	private String descripcion;

	@Column(name="estado")
	private byte estado;

	public MetodoPago() {
	}

	public short getId() {
		return this.id;
	}

	public void setId(short id) {
		this.id = id;
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

}
package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;


/**
 * The persistent class for the cliente database table.
 * 
 */
@Entity
@Table(name="cliente")
public class Cliente implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private Long id;

	@Column(name="correo_cliente")
	private String correoCliente;

	@Column(name="direccion_cliente")
	private String direccionCliente;
	
	@Column(name="estado")
	private byte estado;

	@Column(name="nombre_cliente")
	private String nombreCliente;

	@Column(name="telefono")
	private String telefono;

	public Cliente() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCorreoCliente() {
		return this.correoCliente;
	}

	public void setCorreoCliente(String correoCliente) {
		this.correoCliente = correoCliente;
	}

	public String getDireccionCliente() {
		return this.direccionCliente;
	}

	public void setDireccionCliente(String direccionCliente) {
		this.direccionCliente = direccionCliente;
	}

	public byte getEstado() {
		return this.estado;
	}

	public void setEstado(byte estado) {
		this.estado = estado;
	}

	public String getNombreCliente() {
		return this.nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public String getTelefono() {
		return this.telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

}
package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;


/**
 * The persistent class for the empresa database table.
 * 
 */
@Entity
@Table(name="empresa")
public class Empresa implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private Long id;

	@Column(name="correo_electronico")
	private String correoElectronico;
	
	@Column(name="direccion")
	private String direccion;
	
	@Column(name="estado")
	private byte estado;

	@Column(name="razon_social")
	private String razonSocial;
	
	@Column(name="telefono")
	private String telefono;

	public Empresa() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCorreoElectronico() {
		return this.correoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		this.correoElectronico = correoElectronico;
	}

	public String getDireccion() {
		return this.direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public byte getEstado() {
		return this.estado;
	}

	public void setEstado(byte estado) {
		this.estado = estado;
	}

	public String getRazonSocial() {
		return this.razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getTelefono() {
		return this.telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

}
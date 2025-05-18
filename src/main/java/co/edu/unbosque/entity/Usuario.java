package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Date;


/**
 * The persistent class for the usuario database table.
 * 
 */
@Entity
@Table(name="usuario")
public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private Long id;
	
	@Column(name="clave_usrio")
	private String claveUsrio;

	@Column(name="correo_usuario")
	private String correoUsuario;
	
	@Column(name="estado")
	private byte estado;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="fcha_ultma_clave")
	private Date fchaUltmaClave;


	@Column(name="id_tipo_usuario")
	private String idTipoUsuario;
	
	@Column(name="intentos")
	private int intentos;

	@Column(name="login_usrio")
	private String loginUsrio;

	public Usuario() {
	}

	public String getClaveUsrio() {
		return this.claveUsrio;
	}

	public void setClaveUsrio(String claveUsrio) {
		this.claveUsrio = claveUsrio;
	}

	public String getCorreoUsuario() {
		return this.correoUsuario;
	}

	public void setCorreoUsuario(String correoUsuario) {
		this.correoUsuario = correoUsuario;
	}

	public byte getEstado() {
		return this.estado;
	}

	public void setEstado(byte estado) {
		this.estado = estado;
	}

	public Date getFchaUltmaClave() {
		return this.fchaUltmaClave;
	}

	public void setFchaUltmaClave(Date fchaUltmaClave) {
		this.fchaUltmaClave = fchaUltmaClave;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdTipoUsuario() {
		return this.idTipoUsuario;
	}

	public void setIdTipoUsuario(String idTipoUsuario) {
		this.idTipoUsuario = idTipoUsuario;
	}

	public int getIntentos() {
		return this.intentos;
	}

	public void setIntentos(int intentos) {
		this.intentos = intentos;
	}

	public String getLoginUsrio() {
		return this.loginUsrio;
	}

	public void setLoginUsrio(String loginUsrio) {
		this.loginUsrio = loginUsrio;
	}

}
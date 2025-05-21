package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Date;


/**
 * The persistent class for the auditoria database table.
 * 
 */
@Entity
@Table(name="auditoria")
public class Auditoria implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false)
	private long id;

	@Column(name="accion_audtria")
	private String accionAudtria;

	@Column(name="address_audtria")
	private String addressAudtria;

	@Column(name="comentario_audtria")
	private String comentarioAudtria;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="fcha_audtria")
	private Date fchaAudtria;

	@Column(name="id_tabla")
	private Long idTabla;


	@Column(name="tabla_accion")
	private String tablaAccion;

	@Column(name="usrio_audtria")
	private String usrioAudtria;

	public Auditoria() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAccionAudtria() {
		return this.accionAudtria;
	}

	public void setAccionAudtria(String accionAudtria) {
		this.accionAudtria = accionAudtria;
	}

	public String getAddressAudtria() {
		return this.addressAudtria;
	}

	public void setAddressAudtria(String addressAudtria) {
		this.addressAudtria = addressAudtria;
	}

	public String getComentarioAudtria() {
		return this.comentarioAudtria;
	}

	public void setComentarioAudtria(String comentarioAudtria) {
		this.comentarioAudtria = comentarioAudtria;
	}

	public Date getFchaAudtria() {
		return this.fchaAudtria;
	}

	public void setFchaAudtria(Date fchaAudtria) {
		this.fchaAudtria = fchaAudtria;
	}

	public Long getIdTabla() {
		return this.idTabla;
	}

	public void setIdTabla(Long idTabla) {
		this.idTabla = idTabla;
	}

	public String getTablaAccion() {
		return this.tablaAccion;
	}

	public void setTablaAccion(String tablaAccion) {
		this.tablaAccion = tablaAccion;
	}

	public String getUsrioAudtria() {
		return this.usrioAudtria;
	}

	public void setUsrioAudtria(String usrioAudtria) {
		this.usrioAudtria = usrioAudtria;
	}

}
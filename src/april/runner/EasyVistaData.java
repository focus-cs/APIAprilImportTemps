/*
 * © 2012 Sciforma. Tous droits réservés. 
 */
package april.runner;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EasyVistaData {

	private String numDossier;

	private String centreDeCoutDossier;

	private String modeDeversement;

	private String intervenant;

	private String loginIntervenant;

	private int tempsSaisi;

	private Date dateIntervention;

	private Date dateTraitement;

	private String message;
	
	private Boolean done;

	public EasyVistaData(String numDossier, String centreDeCoutDossier,
			String modeDeversement, String intervenant,
			String loginIntervenant, int tempsSaisi, Date dateIntervention) {
		this.numDossier = numDossier;
		this.centreDeCoutDossier = centreDeCoutDossier;
		this.modeDeversement = modeDeversement;
		this.intervenant = intervenant;
		this.loginIntervenant = loginIntervenant;
		this.tempsSaisi = tempsSaisi;
		this.dateIntervention = dateIntervention;
	}

	public String getNumDossier() {
		return numDossier;
	}

	public void setNumDossier(String numDossier) {
		this.numDossier = numDossier;
	}

	public String getCentreDeCoutDossier() {
		return centreDeCoutDossier;
	}

	public void setCentreDeCoutDossier(String centreDeCoutDossier) {
		this.centreDeCoutDossier = centreDeCoutDossier;
	}

	public String getModeDeversement() {
		return modeDeversement;
	}

	public void setModeDeversement(String modeDeversement) {
		this.modeDeversement = modeDeversement;
	}

	public String getIntervenant() {
		return intervenant;
	}

	public void setIntervenant(String intervenant) {
		this.intervenant = intervenant;
	}

	public String getLoginIntervenant() {
		return loginIntervenant;
	}

	public void setLoginIntervenant(String loginIntervenant) {
		this.loginIntervenant = loginIntervenant;
	}

	public int getTempsSaisi() {
		return tempsSaisi;
	}

	public void setTempsSaisi(int tempsSaisi) {
		this.tempsSaisi = tempsSaisi;
	}

	public Date getDateIntervention() {
		return dateIntervention;
	}

	public void setDateIntervention(Date dateIntervention) {
		this.dateIntervention = dateIntervention;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setDateTraitement(Date dateTraitement) {
		this.dateTraitement = dateTraitement;
	}

	public Date getDateTraitement() {
		return this.dateTraitement;
	}
	
	public void setDone(Boolean done) {
		this.done = done;
	}
	
	public Boolean getDone() {
		return this.done;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}

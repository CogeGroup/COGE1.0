package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@Entity
public class Certificazione extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCertificazione;
	
	@Required
	@MinSize(2)
	@CheckWith(CodiceCheck.class)
	public String codice;
	
	@Required
	public String descrizione;
	
	public Certificazione() {}

	public Certificazione(String codice, String descrizione) {
		this.codice = codice;
		this.descrizione = descrizione;
	}

	static class CodiceCheck extends Check {
		static final String message = "validation.certificazione.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object newCertificazione, Object codice) {
			Certificazione oldCertificazione = Certificazione.find("byCodice", codice).first();
			if(oldCertificazione != null && ((Certificazione) newCertificazione).idCertificazione != oldCertificazione.idCertificazione) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}

}

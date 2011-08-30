package models;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.Risorsa.MatricolaCheck;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class TipoRapportoLavoro extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTipoRapportoLavoro;
	
	@Required
	@CheckWith(CodiceCheck.class)
	@MaxSize(255)
	public String codice;
	
	@Required
	@MaxSize(255)
	public String descrizione;
	
	public TipoRapportoLavoro() {}
	
	public TipoRapportoLavoro(String descrizione, String codice) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
	}
	
	static class CodiceCheck extends Check {
		static final String message = "validation.tipoRapportoLavoro.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object tipoRapportoLavoro, Object codice) {
			TipoRapportoLavoro app = TipoRapportoLavoro.find("byCodice", codice).first();
			if(app != null && ((TipoRapportoLavoro) tipoRapportoLavoro).idTipoRapportoLavoro != app.idTipoRapportoLavoro) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	

}

package models;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class TipoStatoRisorsa extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTipoStatoRisorsa;
	
	@Required
	@CheckWith(CodiceCheck.class)
	@MaxSize(255)
	public String codice;
	
	@Required
	@MaxSize(255)
	public String descrizione;
	
	public TipoStatoRisorsa() {}
	
	public TipoStatoRisorsa(String descrizione, String codice) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
	}
	
	static class CodiceCheck extends Check {
		static final String message = "validation.tipoStatoRisorsa.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object tipoStatoRisorsa, Object codice) {
			TipoStatoRisorsa app = TipoStatoRisorsa.find("byCodice", codice).first();
			if(app != null && ((TipoStatoRisorsa) tipoStatoRisorsa).idTipoStatoRisorsa != app.idTipoStatoRisorsa) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	

}

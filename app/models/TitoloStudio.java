package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@Entity
public class TitoloStudio extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTitoloStudio;
	
	@Required
	@MinSize(2)
	@CheckWith(CodiceCheck.class)
	public String codice;
	
	@Required
	public String descrizione;
	
	@ManyToMany
	public List<Risorsa> listaRisorse;
	
	public TitoloStudio() {}

	public TitoloStudio(String codice, String descrizione) {
		this.codice = codice;
		this.descrizione = descrizione;
		this.listaRisorse = new ArrayList<Risorsa>();
	}
	
	static class CodiceCheck extends Check {
		static final String message = "validation.titoloStudio.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object newTitoloStudio, Object codice) {
			TitoloStudio oldTitoloStudio = TitoloStudio.find("byCodice", codice).first();
			if(oldTitoloStudio != null && ((TitoloStudio) newTitoloStudio).idTitoloStudio != oldTitoloStudio.idTitoloStudio) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
}

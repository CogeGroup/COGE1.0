package models;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class TipoRapportoLavoro extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTipoRapportoLavoro;
	
	public String descrizione;
	public String codice;
	public TipoRapportoLavoro(String descrizione, String codice) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
	}

}

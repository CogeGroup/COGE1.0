package models;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class TipoCommessa extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTipoCommessa;
	
	public String codice;
	
	public String descrizione;
	
	public TipoCommessa() {}
	
	public TipoCommessa(String descrizione, String codice) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
	}

}

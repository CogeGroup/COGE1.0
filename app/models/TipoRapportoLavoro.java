package models;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class TipoRapportoLavoro extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTipoRapportoLavoro;
	
	@Required(message="codice obbligatorio")
	@MaxSize(255)
	public String codice;
	
	@Required(message="descrizione obbligatoria")
	@MaxSize(255)
	public String descrizione;
	
	public TipoRapportoLavoro(String descrizione, String codice) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
	}

}

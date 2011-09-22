package models;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class DettaglioRapportoAttivita extends GenericModel {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idDettaglioRapportoAttivita;
	
	public int oreLavorate;
	
	public int giorno;
	
	@ManyToOne
	public Commessa commessa;
	
	@ManyToOne
	public RapportoAttivita rapportoAttivita;

	public DettaglioRapportoAttivita(int oreLavorate, int giorno,
			Commessa commessa) {
		super();
		this.oreLavorate = oreLavorate;
		this.giorno = giorno;
		this.commessa = commessa;
	}

	
	
	
	
	
}

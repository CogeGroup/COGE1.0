package models;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class Costo extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCosto;
	
	public Float importo;
	
	public Date dataInizio;
	
	public Date dataFine;
	
	@ManyToOne
	public Risorsa risorsa;

	public Costo(Float importo, Date dataInizio) {
		super();
		this.importo = importo;
		this.dataInizio = dataInizio;
	}
	
	

}

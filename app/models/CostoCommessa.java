package models;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.binding.As;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class CostoCommessa extends GenericModel  {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer idCostoCommessa;
	
	@Required
	@Min(0.001)
	public Float importo;
	
	@Required
	@As("dd-MM-yyyy")
	public Date data;
	
	@ManyToOne
	public Commessa commessa;
	
	public CostoCommessa() { }

	public CostoCommessa(Float importo, Date data,CommessaACorpo commessa) {
		this.importo = importo;
		this.data = data;
		this.commessa = commessa;
	}
	
	public CostoCommessa(CommessaACorpo commessa) {
		this.commessa = commessa;
	}
	
}

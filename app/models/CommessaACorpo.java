package models;

import play.data.validation.Min;
import play.data.validation.Required;

@javax.persistence.Entity
public class CommessaACorpo extends Commessa {
	
	@Required
	@Min(0.1)
	public float importo;
	
	public CommessaACorpo() {}

	public CommessaACorpo(String descrizione, String codice,
			boolean fatturabile, float importo) {
		super(descrizione, codice, fatturabile);
		this.importo = importo;
	}

}

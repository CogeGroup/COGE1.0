package models;

@javax.persistence.Entity
public class CommessaACorpo extends Commessa {
	
	public float importo;

	public CommessaACorpo(String descrizione, String codice,
			boolean fatturabile, float importo) {
		super(descrizione, codice, fatturabile);
		this.importo = importo;
	}
	
	

}

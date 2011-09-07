package models;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Min;
import play.data.validation.Required;

@javax.persistence.Entity
public class CommessaACorpo extends Commessa {
	
	@Required
	@Min(value=0.1,message="L'importo deve essere maggiore di 0")
	public float importo;
	
	public CommessaACorpo() {}

	public CommessaACorpo(String descrizione, String codice, boolean calcoloCosti, boolean calcoloRicavi, float importo) {
		super(descrizione, codice, calcoloCosti, calcoloRicavi);
		this.importo = importo;
	}
	
	public static List<CommessaACorpo> findCommesseACorpo(){
		List<Commessa> listaCommesse = Commessa.find("ORDER BY codice asc").fetch();
    	List<CommessaACorpo> listaCommesseACorpo = new ArrayList<CommessaACorpo>();
    	for (Commessa commessa : listaCommesse) {
			if(commessa instanceof CommessaACorpo){
				listaCommesseACorpo.add((CommessaACorpo) commessa);
			}
		}
		return listaCommesseACorpo;
	}
}

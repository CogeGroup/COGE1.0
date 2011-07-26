package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import net.sf.oval.constraint.NotEqual;

import play.data.binding.As;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;


@javax.persistence.Entity
public class Tariffa extends GenericModel{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTariffa;
	
	@Required(message="Data inizio obligatoria")
	@As("dd-MM-yyyy")
	public Date dataInizio;
	
	@As("dd-MM-yyyy")
	public Date dataFine;
	
//	@Required(message="Importo giornaliero obligatorio")
//	@Min(message = "L'importo deve essere maggiore di 0.0",value = 0.1)
	public float importoGiornaliero;

	@ManyToOne
	public Risorsa risorsa;
	
	@ManyToOne
	public Commessa commessa;
	
	public Tariffa() {}

	public Tariffa(Date dataInizio, float importoGiornaliero, Risorsa risorsa,
			Commessa commessa) {
		super();
		this.dataInizio = dataInizio;
		this.importoGiornaliero = importoGiornaliero;
		this.risorsa = risorsa;
		this.commessa = commessa;
	}
	
	public static Tariffa calcolaTariffaRisorsaCommessa(String mese,String anno,Risorsa risorsa,Commessa commessa){
		Tariffa tariffa = null;
		Date dataRapporto;
		try {
			dataRapporto = new SimpleDateFormat("dd/MM/yyyy").parse("01/" + mese + "/" + anno);
			JPAQuery query = Tariffa.find("from Tariffa t where t.risorsa = :risorsa and t.commessa = :commessa and t.dataInizio <= :dataRapporto and (t.dataFine is null or t.dataFine >= :dataRapporto)");
			query.bind("commessa", commessa);
			query.bind("risorsa",risorsa);
			query.bind("dataRapporto", dataRapporto);
			Object t = query.first();
			if (t != null){
		    tariffa = (Tariffa)t;
			}
		} catch (ParseException e) {

			e.printStackTrace();
		}
		return tariffa;
	}
	
	public  float calcolaRicavoTariffa(int oreLavorate){
		return calcolaTariffaOraria() * oreLavorate;
	}
	
	public float calcolaTariffaOraria(){
		return this.importoGiornaliero /8;
	}

	public static List<Commessa> trovaCommessePerRisorsa(String mese,
			String anno, Risorsa risorsa) {
		List<Commessa> listaCommesse = new ArrayList<Commessa>();
		
		try {
			Date dataRapportoFine = new SimpleDateFormat("dd/MM/yyyy").parse("31/" + mese + "/" + anno);
			Date dataRapportoInizio = new SimpleDateFormat("dd/MM/yyyy").parse("01/" + mese + "/" + anno);
			JPAQuery query = Tariffa.find("from Tariffa t where t.risorsa = :risorsa and t.commessa.fatturabile is true and t.dataInizio <= :dataRapportoFine and (t.dataFine is null or t.dataFine >= :dataRapportoInizio)");
			query.bind("dataRapportoFine", dataRapportoFine);
			query.bind("dataRapportoInizio", dataRapportoInizio);
			query.bind("risorsa",risorsa);
			List<Tariffa> listaTariffe = query.fetch();
			if (listaTariffe != null && !listaTariffe.isEmpty()){
			   for(Tariffa t:listaTariffe){
				   if(!(t.commessa instanceof CommessaACorpo)){
					   listaCommesse.add(t.commessa);
				   }
			   }
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} 		
		
		
		return listaCommesse;
				
	}
	
	/* chiude tutte le tariffe associate a una commessa
     * se la data inizio è maggiore di oggi, data fine uguale alla data inizio
     * se la data fine è minore di oggi rimane invariate
     * se la data fine è maggiore di oggi, data fine uguale a oggi
     */
    public static void chiudiTariffeByCommessa(Commessa commessa) {
    	for (Tariffa tariffa : commessa.tariffe) {
    		if (tariffa.dataFine == null) {
    			if(tariffa.dataInizio.after(new Date())){
    				tariffa.dataFine = tariffa.dataInizio;
    			}else{
    				tariffa.dataFine = new Date();
    			}
			} else {
				if(tariffa.dataFine.after(new Date())){
					if(tariffa.dataInizio.after(new Date())){
						tariffa.dataFine = tariffa.dataInizio;
					}else{
						tariffa.dataFine = new Date();
					}
				}
			}
    		tariffa.save();
		}
    }
	    
}

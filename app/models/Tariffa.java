package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;


@javax.persistence.Entity
public class Tariffa extends GenericModel{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTariffa;
	
	public Date dataInizio;
	
	public Date dataFine;
	
	public float importoGiornaliero;

	@ManyToOne
	public Risorsa risorsa;
	
	@ManyToOne
	public Commessa commessa;

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
	
	
	    
}

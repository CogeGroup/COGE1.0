package models;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.binding.As;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import utility.MyUtility;

@javax.persistence.Entity
public class RapportoLavoro extends GenericModel{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRapportoLavoro;

	@Required
	@CheckWith(MyDataInizioCheck.class)
	@As("dd-MM-yyyy")
	public Date dataInizio;
	
	@CheckWith(MyDataFineCheck.class)
	@As("dd-MM-yyyy")
	public Date dataFine;
	
	@Min(0)
	public int giorniAssenzeRetribuite;
	
	@ManyToOne
	public TipoRapportoLavoro tipoRapportoLavoro;
	
	@ManyToOne
	public Risorsa risorsa;
	
	public RapportoLavoro() {}
	
	public RapportoLavoro(Date dataInizio,
			TipoRapportoLavoro tipoRapportoLavoro, Risorsa risorsa) {
		super();
		this.dataInizio = dataInizio;
		this.tipoRapportoLavoro = tipoRapportoLavoro;
		this.risorsa = risorsa;
	}
	
	
	public RapportoLavoro(Risorsa risorsa) {
		this.risorsa = risorsa;		
	}
	
	static class MyDataInizioCheck extends Check {
		
		static final String message = "validation.rapportoLavoro.dataInizio";

		public boolean isSatisfied(Object rl, Object value) {
			if(value != null) {
				RapportoLavoro rapportoLavoro = (RapportoLavoro) rl;
				Date dataInizio = (Date) value;
		    	Risorsa risorsa = rapportoLavoro.risorsa;
		    	Date dataMinima = null;
		    	//index contiene la posizione del rapporto di lavoro nella lista dei rapporti lavoro della risorsa
		    	//- index < 0, stiamo aggiungendo un nuovo rapporto --> effettuo i controlli sull'ultimo rapporto di lavoro della risorsa
		    	//- index = 0, significa che sto provando a modificare il primo rapporto di lavoro --> effettua i controlli sulle date rispetto a quelle della risorsa
		    	//- index > 0, significa che sto provando a modificare un rapporto successivo al primo --> per i controlli tieni conto del rapporto di lavoro precedente
		    	int index = risorsa.rapportiLavoro.indexOf(rapportoLavoro);
		    	if (index < 0) {
		    		RapportoLavoro ultimoRapportoLavoro = risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1);
		        	dataMinima = ultimoRapportoLavoro.dataFine == null ? ultimoRapportoLavoro.dataInizio : ultimoRapportoLavoro.dataFine;
		    	} else if (index == 0) {
					dataMinima = MyUtility.subOneDay(risorsa.dataIn);
				} else {
					dataMinima = risorsa.rapportiLavoro.get(index - 1).dataFine;
				}
		    	if (!dataInizio.after(dataMinima)) {
		    		setMessage(message, MyUtility.dateToString(dataMinima));
		    		return false;
				}
		    	return true;
			}
			return true;
		}
	}
	
	static class MyDataFineCheck extends Check {
		static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.rapportoLavoro.dataFine.lt.dataInizio";
		
		public boolean isSatisfied(Object rl, Object value) {
			RapportoLavoro rapportoLavoro = (RapportoLavoro) rl;
			//effettuo la validazione solo se data inizio è valorizzata
			if(rapportoLavoro.dataInizio == null) {
				return true;
			}
			Date dataFine = (Date) value;
			if (dataFine != null && dataFine.compareTo(rapportoLavoro.dataInizio) < 0) {
				setMessage(DATAFINE_LESS_THAN_DATAINIZIO, MyUtility.dateToString(rapportoLavoro.dataInizio));
				return false;
			}
			return true;
		}
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof RapportoLavoro ? ((RapportoLavoro) other).idRapportoLavoro == this.idRapportoLavoro : false;
	}

   public static List<RapportoLavoro> findByTipoRapportoLavoroAndPeriodo(TipoRapportoLavoro tipoRapLav , int mese, int anno) throws ParseException{
	   Date dataRapporto = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
	   JPAQuery query = RapportoLavoro.find("from RapportoLavoro ral where ral.tipoRapportoLavoro = :tipoRapportoLavoro and ral.dataInizio <= :dataRapporto and (ral.dataFine is null or ral.dataFine >= :dataRapporto)");
	   query.bind("tipoRapportoLavoro", tipoRapLav);
	   query.bind("dataRapporto", dataRapporto);
	   List<RapportoLavoro> list  =  query.fetch();
	   return list;
   }
   
	public static RapportoLavoro findByRisorsaAndPeriodo(Risorsa risorsa , Date dataInizio, Date dataFine) {
		JPAQuery query = RapportoLavoro.find("select ral from RapportoLavoro ral " +
				"where ral.risorsa = :risorsa and (ral.dataInizio <= :dataFine) " +
		   		"and (ral.dataFine is null or (ral.dataFine >= :dataInizio))");
		query.bind("risorsa", risorsa);
		query.bind("dataInizio", dataInizio);
		query.bind("dataFine", dataFine != null ? dataFine : dataInizio);
		System.out.println(query.first());
		return query.first();
	}
	
	public static RapportoLavoro findByRisorsaAndData(Risorsa risorsa , Date data) {
		JPAQuery query = RapportoLavoro.find("select ral from RapportoLavoro ral " +
				"where ral.risorsa = :risorsa " +
				"and ral.dataInizio <= :data " +
				"and ( ral.dataFine is null or ral.dataFine >= :data)");
		query.bind("risorsa", risorsa);
		query.bind("data", data);
		return query.first();
	}
	
	public static List<RapportoLavoro> findByRisorsaAndMeseAndAnno(Risorsa risorsa , int mese, int anno) {
		Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		Date dataFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		JPAQuery query = RapportoLavoro.find("select ral from RapportoLavoro ral " +
				"where ral.risorsa = :risorsa and (ral.dataInizio <= :dataFine " +
		   		"and (ral.dataFine is null or ral.dataFine >= :dataInizio))");
		query.bind("risorsa", risorsa);
		query.bind("dataInizio", dataInizio);
		query.bind("dataFine", dataFine);
		return query.fetch();
	}
	
	public static RapportoLavoro findByRisorsaAndMeseAndAnnoAndTipoRapportoLavoro(Risorsa risorsa , TipoRapportoLavoro trl, int mese, int anno) {
		Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		Date dataFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		JPAQuery query = RapportoLavoro.find("select ral from RapportoLavoro ral " +
				"where ral.risorsa = :risorsa and ral.tipoRapportoLavoro = :trl and (ral.dataInizio <= :dataFine " +
		   		"and (ral.dataFine is null or ral.dataFine >= :dataInizio))");
		query.bind("risorsa", risorsa);
		query.bind("dataInizio", dataInizio);
		query.bind("dataFine", dataFine);
		query.bind("trl", trl);
		return query.first();
	}
	
	public static Integer countRisorse(TipoRapportoLavoro trl){
		List<RapportoLavoro> listaRapportoLavoro = RapportoLavoro.find("byTipoRapportoLavoro", trl).fetch();
		List<Risorsa> listaRisorse = new ArrayList<Risorsa>();
		for(RapportoLavoro ra : listaRapportoLavoro){
			if(!ra.risorsa.tipoStatoRisorsa.codice.equals("CHIUSO")){
				boolean flag = true;
				for(Risorsa r : listaRisorse){
					if(ra.risorsa.idRisorsa == r.idRisorsa)
						flag = false;
				}
				if(flag)
					listaRisorse.add(ra.risorsa);
			}
		}
		return listaRisorse.size();
	}
   
	public static List<Risorsa> findRisorseByTipoRapportoLavoroAndMeseAndAnno(TipoRapportoLavoro trl, int mese, int anno) {
		Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		Date dataFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		JPAQuery query = Risorsa.find("select r from Risorsa r, RapportoLavoro ral " +
				"where ral.risorsa = r and ral.tipoRapportoLavoro = :trl and (ral.dataInizio <= :dataFine " +
		   		"and (ral.dataFine is null or ral.dataFine >= :dataInizio))");
		query.bind("trl", trl);
		
		query.bind("dataInizio", dataInizio);
		query.bind("dataFine", dataFine);
		return query.fetch();
	}
}

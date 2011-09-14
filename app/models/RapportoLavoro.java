package models;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceProperty;
import javax.persistence.Transient;

import play.data.binding.*;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;
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
   
   public static TipoRapportoLavoro findByRisorsaAndPeriodo(Risorsa risorsa , Date dataInizio, Date dataFine) {
	   JPAQuery query = TipoRapportoLavoro.find("select trl from TipoRapportoLavoro trl, RapportoLavoro ral " +
	   		"where ral.risorsa = :risorsa and ral.dataInizio <= :dataFine and (ral.dataFine is null or ral.dataFine >= :dataInizio)" +
	   		"and ral.tipoRapportoLavoro = trl");
	   query.bind("risorsa", risorsa);
	   query.bind("dataInizio", dataInizio);
	   query.bind("dataFine", dataFine);
	   TipoRapportoLavoro trl  =  query.first();
	   return trl;
   }
   
}

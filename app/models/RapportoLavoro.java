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

import models.Costo.MyDataInizioCheck;

import play.data.binding.*;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;
import utility.MyUtility;

@javax.persistence.Entity
public class RapportoLavoro extends GenericModel{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRapportoLavoro;

	@CheckWith(MyDataInizioCheck.class)
	@As("dd-MM-yyyy")
	public Date dataInizio;
	
	@CheckWith(MyDataFineCheck.class)
	@As("dd-MM-yyyy")
	public Date dataFine;
	
	@Transient
	public int meseInizio;
	
	@Transient
	public int annoInizio;
	
	@Transient
	public int meseFine;
	
	@Transient
	public int annoFine;
	
	@ManyToOne
	public TipoRapportoLavoro tipoRapportoLavoro;
	
	
	@ManyToOne
	public Risorsa risorsa;
	
	public RapportoLavoro(Date dataInizio,
			TipoRapportoLavoro tipoRapportoLavoro, Risorsa risorsa) {
		super();
		this.dataInizio = dataInizio;
		this.tipoRapportoLavoro = tipoRapportoLavoro;
		this.risorsa = risorsa;
	}
	
	
	public RapportoLavoro(Risorsa risorsa) {
		this.risorsa = risorsa;
		this.meseInizio = Calendar.getInstance().get(Calendar.MONTH);
		this.annoInizio = Calendar.getInstance().get(Calendar.YEAR);
		this.meseFine= -1;
		this.annoFine= -1;		
	}
	
	static class MyDataInizioCheck extends Check {
		
		static final String message = "validation.rapportoLavoro.dataInizio";

		public boolean isSatisfied(Object rl, Object value) {
			RapportoLavoro rapportoLavoro = (RapportoLavoro) rl;
			Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(rapportoLavoro.meseInizio, rapportoLavoro.annoInizio);
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
				dataMinima = MyUtility.subOneMonth(risorsa.dataIn);
			} else {
				dataMinima = risorsa.rapportiLavoro.get(index - 1).dataFine;
			}
	    	if (!dataInizio.after(dataMinima)) {
	    		setMessage(message, MyUtility.dateToString(dataMinima).substring(3));
	    		return false;
			}
	    	rapportoLavoro.dataInizio = dataInizio;
			return true;
		}
	}
	
	static class MyDataFineCheck extends Check {
		static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.rapportoLavoro.dataFine.lt.dataInizio";
		static final String MESE_ED_ANNO_NON_SELEZIONATE_CONTEMPORANEAMENTE = "validation.rapportoLavoro.meseFine.annoFine.wrongSelection";
		
		public boolean isSatisfied(Object rl, Object value) {
			RapportoLavoro rapportoLavoro = (RapportoLavoro) rl;
			if (rapportoLavoro.meseFine > -1 ^ rapportoLavoro.annoFine > -1) {
				setMessage(MESE_ED_ANNO_NON_SELEZIONATE_CONTEMPORANEAMENTE);
				return false;
			}
			//se arrivo a questo punto meseFine ed annoFine saranno entrambe od uguali a -1 oppure entrambi valorizzati
			//indi faccio solo il controllo meseFine == -1
			Date dataFine = rapportoLavoro.meseFine == -1 ? null : MyUtility.MeseEdAnnoToDataFine(rapportoLavoro.meseFine, rapportoLavoro.annoFine);
			if(dataFine != null){
				if(dataFine.compareTo(rapportoLavoro.dataInizio) <=0 ){
					setMessage(DATAFINE_LESS_THAN_DATAINIZIO, MyUtility.dateToString(rapportoLavoro.dataInizio).substring(3));
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof RapportoLavoro ? ((RapportoLavoro) other).idRapportoLavoro == this.idRapportoLavoro : false;
	}

   public static List<RapportoLavoro> findByTipoRapportoLavoroAndPeriodo(TipoRapportoLavoro tipoRapLav , String mese, String anno) throws ParseException{
	   Date dataRapporto = new SimpleDateFormat("dd/MM/yyyy").parse("01/" + mese + "/" + anno);
	   JPAQuery query = RapportoLavoro.find("from RapportoLavoro ral where ral.tipoRapportoLavoro = :tipoRapportoLavoro and ral.dataInizio <= :dataRapporto and (ral.dataFine is null or ral.dataFine >= :dataRapporto)");
	   query.bind("tipoRapportoLavoro", tipoRapLav);
	   query.bind("dataRapporto", dataRapporto);
	   List<RapportoLavoro> list  =  query.fetch();
	   System.out.println("....SIZE:" + list.size());
	   return list;
   }
   
}

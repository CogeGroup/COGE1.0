package models;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
public class Costo extends GenericModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer idCosto;
	
	@Required
	@Min(0.001)
	public Float importoGiornaliero;
	
	@CheckWith(ImportoMensileCheck.class)
	@Min(0.001)
	public Float importoMensile;

	@Required
	@CheckWith(MyDataInizioCheck.class)
	@As("dd-MM-yyyy")
	public Date dataInizio;
	
	@CheckWith(MyDataFineCheck.class)
	@As("dd-MM-yyyy")
	public Date dataFine;

	@Required
	@ManyToOne
	public Risorsa risorsa;
	
	public Costo() {}

	public Costo(Float importoGiornaliero, Float importoMensile, Date dataInizio, Risorsa risorsa) {
		super();
		this.importoGiornaliero = importoGiornaliero;
		this.importoMensile = importoMensile;
		this.dataInizio = dataInizio;
		this.risorsa = risorsa;
	}
	
	public Costo(Date dataInizio, Date dataFine, Risorsa risorsa) {
		super();
		this.importoGiornaliero = 0.0f;
		this.importoMensile = 0.0f;
		this.dataInizio = dataInizio;
		this.dataFine = dataFine;
		this.risorsa = risorsa;
	}
	
	public Costo(Risorsa risorsa) {
		this.risorsa = risorsa;
	}
	
	static class MyDataInizioCheck extends Check {

		static final String message = "validation.costo.dataInizio";

		public boolean isSatisfied(Object object, Object value) {
			//se dataInizio è null salta la validazione
			Date dataInizio = (Date) value;
	    	if(dataInizio != null) {
				Costo costo = (Costo) object;
				Date dataMinima = null;
	    		//popolo la lista dei costi della risorsa
	    		List<Costo> listaCosti = Costo.find("byRisorsa", costo.risorsa).fetch();
	    		//caso aggiunta primo costo
	    		if(listaCosti == null || listaCosti.size() == 0) {
	    			dataMinima = MyUtility.subOneDay(costo.risorsa.dataIn);
	    		} else if (listaCosti.indexOf(costo) < 0) {
	    			//caso inserimento nuovi costi
	    			Costo ultimoCosto = listaCosti.get(listaCosti.size() - 1);
		    		dataMinima = ultimoCosto.dataFine == null ? ultimoCosto.dataInizio : ultimoCosto.dataFine;
	    		} else if (listaCosti.size() == 1) {
					//caso modifica primo costo
					dataMinima = MyUtility.subOneDay(costo.risorsa.dataIn);
	    		} else {
	    			//caso modifica ultimo costo
	    			dataMinima = listaCosti.get(listaCosti.size() - 2).dataFine;
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
		static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.costo.dataFine.lt.dataInizio";
		
		public boolean isSatisfied(Object object, Object value) {
			Costo costo = (Costo) object;
			//effettuo la validazione solo se dataInizio è valorizzata
			if(costo.dataInizio == null) {
				return true;
			}
			Date dataFine = (Date) value;
			if (dataFine != null && dataFine.compareTo(costo.dataInizio) < 0) {
				setMessage(DATAFINE_LESS_THAN_DATAINIZIO, MyUtility.dateToString(costo.dataInizio));
				return false;
			}
			RapportoLavoro ral = RapportoLavoro.findByRisorsaAndPeriodo(costo.risorsa, costo.dataInizio, dataFine);
			if(ral == null){
				setMessage("Errore dataFine");
				return false;
			}
			return true;
		}
	}

	
	static class ImportoMensileCheck extends Check {
		static final String MESSAGE = "validation.costo.importoMensile";
		
		public boolean isSatisfied(Object costo, Object value) {
			Costo c = (Costo) costo;
			//valido l'importo mensile solo se la dataInizio del costo è stata valorizzata
			if(c.dataInizio == null) {
				return true;
			}
			TipoRapportoLavoro trl = TipoRapportoLavoro.findByRisorsaAndPeriodo(c.risorsa, c.dataInizio, c.dataFine);
			if( trl != null && !trl.codice.equals("CCP") && (((Costo) costo).importoMensile == null 
					|| ((Costo) costo).importoMensile == 0) ){
				setMessage(MESSAGE);
				return false;
			}
			return true;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Costo ? ((Costo) other).idCosto == this.idCosto: false;
	}

	
		
	public static float totaleCosto(Costo costo, Risorsa risorsa, int mese, int anno) {
		if (costo.importoMensile != null)  {
			return costo.importoMensile;
		}
		List<RendicontoAttivita> rendicontoAttivitas = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa,(mese+1),anno).fetch();
		Float tot = 0f;
		for (RendicontoAttivita rendicontoAttivita : rendicontoAttivitas) {
			tot += rendicontoAttivita.oreLavorate;
		}
		return (costo.importoGiornaliero/8) * tot;
	}
	
	public static Costo extractCostoByMeseAndAnno(Risorsa risorsa, int mese, int anno) {
		Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		Date dataFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		JPAQuery query = Costo.find("from Costo c where c.risorsa = :risorsa and c.dataInizio <= :dataFine and (c.dataFine is null or c.dataFine >= :dataInizio)");
		query.bind("risorsa", risorsa);
		query.bind("dataInizio", dataInizio);
		query.bind("dataFine", dataFine);
		return query.first();
	}
	
	public static Costo extractByRisorsaAndPeriodo(Risorsa risorsa, Date dataInizio, Date dataFine, Date data) {
		if(dataFine == null){
			dataFine = data;
		}
		JPAQuery query = Costo.find("from Costo c where c.risorsa = :risorsa and (c.dataInizio <= :dataFine and (c.dataFine is null or c.dataFine >= :dataInizio))");
		query.bind("risorsa", risorsa);
		query.bind("dataInizio", dataInizio);
		if(dataFine == null){
			dataFine = MyUtility.MeseEdAnnoToDataFine(MyUtility.getMeseFromDate(dataInizio), MyUtility.getAnnoFromDate(dataInizio));
		}
		query.bind("dataFine", dataFine);
		List<Costo> result = query.fetch();
		return result.get(result.size() - 1);
	}
	
	// mio
	public static List<Costo> findAllByRisorsaAndPeriodo(Risorsa risorsa, Date dataInizio, Date dataFine) {
		JPAQuery query = Costo.find("from Costo c where c.risorsa = :risorsa and c.dataInizio <= :dataFine and (c.dataFine is null or c.dataFine >= :dataInizio)");
		query.bind("risorsa", risorsa);
		query.bind("dataInizio", dataInizio);
		query.bind("dataFine", dataFine);
		return query.fetch();
	}
	
	public static Float calcolaCostoTotale(Commessa commessa, Date dataFine){
		Float costoTotale = 0F;
		Date dataInizioPeriodo = commessa.dataInizioCommessa;
		Date dataFinePeriodo = new Date();
		if(commessa.dataFineCommessa != null && commessa.dataFineCommessa.before(dataFine)) {
			dataFinePeriodo = commessa.dataFineCommessa;
		} else {
			dataFinePeriodo = dataFine;
		}
		List<Risorsa> listaRisorse = Risorsa.findByCommessa(commessa);
		for(Risorsa r: listaRisorse){
			Float costoRisorsa = 0F;
			List<Map> periodo = MyUtility.calcolaMesi(dataInizioPeriodo, dataFinePeriodo);
			for(Map<String,Integer> mappa: periodo){
				Costo c = Costo.extractCostoByMeseAndAnno(r, mappa.get("mese") - 1, mappa.get("anno"));
				if(c != null){
					if(c.importoMensile != null){
						costoRisorsa = c.importoMensile;
					} else {
						int m = MyUtility.getMeseFromDate(c.dataInizio) + 1;
						int a = MyUtility.getAnnoFromDate(c.dataInizio);
						costoRisorsa = totaleCosto(c, r, m - 1, a);
					}
				}
				costoTotale += costoRisorsa;
			}
		}
		return costoTotale;
	}
	
}

//static class MyDataInizioCheck extends Check {
//
//	static final String mes = "validation.costo.sovrapposto";
//
//	public boolean isSatisfied(Object costo, Object value) {
//		if(!Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty()){
//			setMessage(mes);
//			return false;
//		}
//		return true;
//	}
//}
//
//static class MyDataFineCheck extends Check {
//	static final String DATE_SOVRAPPOSTE_MESSAGE = "validation.costo.sovrapposto";
//	static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.costo.dataFine.lt.dataInizio";
//
//	public boolean isSatisfied(Object costo, Object value) {
//		if(value !=null){
//			if(((Date)value).compareTo(((Costo)costo).dataInizio) <=0 ){
//				setMessage(DATAFINE_LESS_THAN_DATAINIZIO);
//				return false;
//			}
//			
//			if (!Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty()) {
//				setMessage(DATE_SOVRAPPOSTE_MESSAGE);
//				return false;
//			}
//		}
//		
//		return true;
//	}
//	}
//
//public static List<Costo> costiSovrapposti(Date valuableDate,Costo costo){
//
//	//StringBuffer query = new StringBuffer("from Costo c where (? between c.dataInizio and c.dataFine or (c.dataFine is null and c.dataInizio < ?)  or (c.dataInizio > ? and c.dataFine < ?) ) and c.risorsa = ? ");
//	StringBuffer query = new StringBuffer("from Costo c where (? between c.dataInizio and c.dataFine or (c.dataFine is null and c.dataInizio < ?)  or (c.dataInizio > ? and c.dataFine < ?) ) and c.risorsa = ? ");
//
//	if(costo.isPersistent()){
//		query.append(" and c != ? ");
//		return Costo
//		.find(query.toString(),
//				valuableDate,valuableDate,costo.dataInizio ,costo.dataFine , costo.risorsa,costo).fetch();
//	}else{
//		return Costo
//		.find(query.toString(),
//				valuableDate,valuableDate,costo.dataInizio ,costo.dataFine , costo.risorsa).fetch();
//	}
//
//	}
//
////verifica che la dataInizio inserita non sia sovrapposta (ovvero contenuta) agli altri costi della risorsa
//public static List<Costo> dataInizioSovrapposta(Costo costo) {
//	StringBuffer query = new StringBuffer("from Costo c where ? >= c.dataInizio and (c.dataFine is null or ? <= c.dataFine) and c.risorsa = ?");
//	if(costo.isPersistent()) {
//		query.append(" and c != ?");
//		return Costo.find(query.toString(), costo.dataInizio, costo.dataInizio, costo.risorsa, costo).fetch();
//	}
//	return Costo.find(query.toString(), costo.dataInizio, costo.dataInizio, costo.risorsa).fetch();
//}
//
//
////verifica che la dataFine inserita non sia sovrapposta (ovvero contenuta) agli altri costi della risorsa
////NB: viene invocato solo dopo aver verificato che dataInizio sia valida
//public static List<Costo> costoSovrapposto(Costo costo) {
//	StringBuffer query = new StringBuffer();
//	//se dataFine è null, mi interessa verificare che non vi siano costi successivi già inseriti
//	if (costo.dataFine == null) {
//		query.append("from Costo c where c.dataInizio > ? and c.risorsa = ?");
//		if(costo.isPersistent()) {
//			query.append(" and c != ?");
//			return Costo.find(query.toString(), costo.dataInizio, costo.risorsa, costo).fetch();
//		}
//		return Costo.find(query.toString(), costo.dataInizio, costo.risorsa).fetch();
//	} else {
//		query.append("from Costo c where ? >= c.dataInizio and (c.dataFine is null or ? <= c.dataFine) and c.risorsa = ?");
//		if(costo.isPersistent()) {
//			query.append(" and c != ?");
//			return Costo.find(query.toString(), costo.dataFine, costo.dataFine, costo.risorsa, costo).fetch();
//		}
//		return Costo.find(query.toString(), costo.dataFine, costo.dataFine, costo.risorsa).fetch();
//	}
//	
//}

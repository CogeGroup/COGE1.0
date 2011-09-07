package models;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

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
	public Float importo;
	
	public Float importoGiornaliero;
	
	public Float importoMensile;

	@CheckWith(MyCostoDateCheck.class)
	@As("dd-MM-yyyy")
	public Date dataInizio;
	
	@As("dd-MM-yyyy")
	public Date dataFine;

	@Required
	@ManyToOne
	public Risorsa risorsa;

	@Transient
	public int meseInizio;
	
	@Transient
	public int annoInizio;
	
	@Transient
	public int meseFine;
	
	@Transient
	public int annoFine;

	public Costo(Float importo, Date dataInizio, Risorsa risorsa) {
		super();
		this.importo = importo;
		this.dataInizio = dataInizio;
		this.risorsa = risorsa;
	}
	public Costo(Risorsa risorsa) {
		this.risorsa = risorsa;
		this.meseInizio = Calendar.getInstance().get(Calendar.MONTH);
		this.annoInizio = Calendar.getInstance().get(Calendar.YEAR);
		this.meseFine= -1;
		this.annoFine= -1;
	}
	
	
	
	static class MyCostoDateCheck extends Check {
		static final String DATE_SOVRAPPOSTE_MESSAGE = "validation.costo.sovrapposto";
		static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.costo.dataFine.lt.dataInizio";
		static final String MESE_ED_ANNO_NON_SELEZIONATE_CONTEMPORANEAMENTE = "validation.costo.meseFine.annoFine.wrongSelection";
		
		public boolean isSatisfied(Object costo, Object value) {
			Costo costo2 = (Costo) costo;
			costo2.dataInizio = MyUtility.MeseEdAnnoToDataInizio(costo2.meseInizio, costo2.annoInizio);
			//valido dataInizio (ovvero non sovrapposta a nessun costo della risorsa)
			if(!dataInizioSovrapposta(costo2).isEmpty()){
				setMessage(DATE_SOVRAPPOSTE_MESSAGE);
				return false;
			}
			//verifico che meseFine e annoFine siano entrambi contemporaneamente selezionati/deselezionati
			if (costo2.meseFine > -1 ^ costo2.annoFine > -1) {
				setMessage(MESE_ED_ANNO_NON_SELEZIONATE_CONTEMPORANEAMENTE);
				return false;
			}
			//se arrivo a questo punto meseFine ed annoFine saranno entrambe od uguali a -1 oppure entrambi valorizzati
			//indi faccio solo il controllo meseFine == -1 (inoltre dataInizio è valida)
			costo2.dataFine = costo2.meseFine == -1 ? null : MyUtility.MeseEdAnnoToDataFine(costo2.meseFine, costo2.annoFine);
			//controllo che dataInizio sia > di dataFine
			if(costo2.dataFine != null && costo2.dataFine.compareTo(costo2.dataInizio) <=0){
				setMessage(DATAFINE_LESS_THAN_DATAINIZIO);
				return false;
			}
			//controllo che il costo non sia sovrapposto a nessun altro
			if (!costoSovrapposto(costo2).isEmpty()) {
				setMessage(DATE_SOVRAPPOSTE_MESSAGE);
				return false;
			}
			return true;
		}
	}
	
	//verifica che la dataInizio inserita non sia sovrapposta (ovvero contenuta) agli altri costi della risorsa
	public static List<Costo> dataInizioSovrapposta(Costo costo) {
		StringBuffer query = new StringBuffer("from Costo c where ? >= c.dataInizio and (c.dataFine is null or ? <= c.dataFine) and c.risorsa = ?");
		if(costo.isPersistent()) {
			query.append(" and c != ?");
			return Costo.find(query.toString(), costo.dataInizio, costo.dataInizio, costo.risorsa, costo).fetch();
		}
		return Costo.find(query.toString(), costo.dataInizio, costo.dataInizio, costo.risorsa).fetch();
	}
	
	
	//verifica che la dataFine inserita non sia sovrapposta (ovvero contenuta) agli altri costi della risorsa
	//NB: viene invocato solo dopo aver verificato che dataInizio sia valida
	public static List<Costo> costoSovrapposto(Costo costo) {
		StringBuffer query = new StringBuffer();
		//se dataFine è null, mi interessa verificare che non vi siano costi successivi già inseriti
		if (costo.dataFine == null) {
			query.append("from Costo c where c.dataInizio > ? and c.risorsa = ?");
			if(costo.isPersistent()) {
				query.append(" and c != ?");
				return Costo.find(query.toString(), costo.dataInizio, costo.risorsa, costo).fetch();
			}
			return Costo.find(query.toString(), costo.dataInizio, costo.risorsa).fetch();
		} else {
			query.append("from Costo c where ? >= c.dataInizio and (c.dataFine is null or ? <= c.dataFine) and c.risorsa = ?");
			if(costo.isPersistent()) {
				query.append(" and c != ?");
				return Costo.find(query.toString(), costo.dataFine, costo.dataFine, costo.risorsa, costo).fetch();
			}
			return Costo.find(query.toString(), costo.dataFine, costo.dataFine, costo.risorsa).fetch();
		}
		
	}
	
	public static float totaleCosto(Costo costo, Risorsa risorsa, int mese, int anno) {
		List<RendicontoAttivita> rendicontoAttivitas = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa,mese,anno).fetch();
		Float tot = 0f;
		for (RendicontoAttivita rendicontoAttivita : rendicontoAttivitas) {
			tot += rendicontoAttivita.oreLavorate;
		}
		return (costo.importo/8) * tot;
	}
	
	public static Costo extractCostoByMeseAndAnno(Risorsa risorsa, int mese, int anno) {
		Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		Date dataFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		JPAQuery query = Costo.find("from Costo c where c.risorsa = :risorsa and c.dataInizio <= :dataInizio and c.dataFine >= :dataFine");
		query.bind("risorsa", risorsa);
		query.bind("dataInizio", dataInizio);
		query.bind("dataFine", dataFine);
		return query.first();
	}
	
}

//static class MyDataInizioCheck extends Check {
//
//static final String mes = "validation.costo.sovrapposto";
//
//public boolean isSatisfied(Object costo, Object value) {
//	if(!Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty()){
//		setMessage(mes);
//		return false;
//	}
//	return true;
//}
//}

//static class MyDataFineCheck extends Check {
//static final String DATE_SOVRAPPOSTE_MESSAGE = "validation.costo.sovrapposto";
//static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.costo.dataFine.lt.dataInizio";
//
//public boolean isSatisfied(Object costo, Object value) {
//	if(value !=null){
//		if(((Date)value).compareTo(((Costo)costo).dataInizio) <=0 ){
//			setMessage(DATAFINE_LESS_THAN_DATAINIZIO);
//			return false;
//		}
//		
//		if (!Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty()) {
//			setMessage(DATE_SOVRAPPOSTE_MESSAGE);
//			return false;
//		}
//	}
//	
//	return true;
//}
//}

//public static List<Costo> costiSovrapposti(Date valuableDate,Costo costo){
//
////StringBuffer query = new StringBuffer("from Costo c where (? between c.dataInizio and c.dataFine or (c.dataFine is null and c.dataInizio < ?)  or (c.dataInizio > ? and c.dataFine < ?) ) and c.risorsa = ? ");
//StringBuffer query = new StringBuffer("from Costo c where (? between c.dataInizio and c.dataFine or (c.dataFine is null and c.dataInizio < ?)  or (c.dataInizio > ? and c.dataFine < ?) ) and c.risorsa = ? ");
//
//if(costo.isPersistent()){
//	query.append(" and c != ? ");
//	return Costo
//	.find(query.toString(),
//			valuableDate,valuableDate,costo.dataInizio ,costo.dataFine , costo.risorsa,costo).fetch();
//}else{
//	return Costo
//	.find(query.toString(),
//			valuableDate,valuableDate,costo.dataInizio ,costo.dataFine , costo.risorsa).fetch();
//}
//
//}

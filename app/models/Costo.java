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
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import utility.MyUtility;

@javax.persistence.Entity
public class Costo extends GenericModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer idCosto;
	@Required
	public Float importo;
	
	@Transient
	public int meseInizio;
	
	@Transient
	public int annoInizio;
	
	@Transient
	public int meseFine;
	
	@Transient
	public int annoFine;


	@CheckWith(MyDataInizioCheck.class)
	@As("dd-MM-yyyy")
	public Date dataInizio;
	
	@CheckWith(MyDataFineCheck.class)
	@As("dd-MM-yyyy")
	public Date dataFine;

	@Required
	@ManyToOne
	public Risorsa risorsa;

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
	
//	static class MyDataInizioCheck extends Check {
//		
//		static final String mes = "validation.costo.sovrapposto";
//
//		public boolean isSatisfied(Object costo, Object value) {
//			if(!Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty()){
//				setMessage(mes);
//				return false;
//			}
//			return true;
//		}
//	}
	
	static class MyDataInizioCheck extends Check {
		
		static final String mes = "validation.costo.sovrapposto";

		public boolean isSatisfied(Object costo, Object value) {
			Costo costo2 = (Costo) costo;
			Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(costo2.meseInizio, costo2.annoInizio);
			if(!Costo.costiSovrapposti(dataInizio, costo2).isEmpty()){
				setMessage(mes);
				return false;
			}
			costo2.dataInizio = dataInizio;
			return true;
		}
	}
	
//	static class MyDataFineCheck extends Check {
//		static final String DATE_SOVRAPPOSTE_MESSAGE = "validation.costo.sovrapposto";
//		static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.costo.dataFine.lt.dataInizio";
//		
//		public boolean isSatisfied(Object costo, Object value) {
//			if(value !=null){
//				if(((Date)value).compareTo(((Costo)costo).dataInizio) <=0 ){
//					setMessage(DATAFINE_LESS_THAN_DATAINIZIO);
//					return false;
//				}
//				
//				if (!Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty()) {
//					setMessage(DATE_SOVRAPPOSTE_MESSAGE);
//					return false;
//				}
//			}
//			
//			return true;
//		}
//	}
	
	static class MyDataFineCheck extends Check {
		static final String DATE_SOVRAPPOSTE_MESSAGE = "validation.costo.sovrapposto";
		static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.costo.dataFine.lt.dataInizio";
		static final String MESE_ED_ANNO_NON_SELEZIONATE_CONTEMPORANEAMENTE = "validation.costo.meseFine.annoFine.wrongSelection";
		
		public boolean isSatisfied(Object costo, Object value) {
			Costo costo2 = (Costo) costo;
			if (costo2.meseFine > -1 ^ costo2.annoFine > -1) {
				setMessage(MESE_ED_ANNO_NON_SELEZIONATE_CONTEMPORANEAMENTE);
				return false;
			}
			//se arrivo a questo punto meseFine ed annoFine saranno entrambe od uguali a -1 oppure entrambi valorizzati
			//indi faccio solo il controllo meseFine == -1
			Date dataFine = costo2.meseFine == -1 ? null : MyUtility.MeseEdAnnoToDataFine(costo2.meseFine, costo2.annoFine);
			if(dataFine != null){
				if(dataFine.compareTo(costo2.dataInizio) <=0 ){
					setMessage(DATAFINE_LESS_THAN_DATAINIZIO);
					return false;
				}
				
				if (!Costo.costiSovrapposti(dataFine, costo2).isEmpty()) {
					setMessage(DATE_SOVRAPPOSTE_MESSAGE);
					return false;
				}
			}
			return true;
		}
	}
	
	public static List<Costo> costiSovrapposti(Date valuableDate,Costo costo){
		
		StringBuffer query = new StringBuffer("from Costo c where (? between c.dataInizio and c.dataFine or (c.dataFine is null and c.dataInizio < ?)  or (c.dataInizio > ? and c.dataFine < ?) ) and c.risorsa = ? ");
		
		if(costo.isPersistent()){
			query.append(" and c != ? ");
			return Costo
			.find(query.toString(),
					valuableDate,valuableDate,costo.dataInizio ,costo.dataFine , costo.risorsa,costo).fetch();
		}else{
			return Costo
			.find(query.toString(),
					valuableDate,valuableDate,costo.dataInizio ,costo.dataFine , costo.risorsa).fetch();
		}
		
	}
	
	public static float totaleCosto(Costo costo, Risorsa risorsa, int mese, int anno) {
		List<RendicontoAttivita> rendicontoAttivitas = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa,mese,anno).fetch();
		float tot = 0;
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

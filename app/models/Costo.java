package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.data.binding.As;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class Costo extends GenericModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer idCosto;
	@Required
	public Float importo;

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

	public Costo(Float importo, Date dataInizio, Risorsa risorsa) {
		super();
		this.importo = importo;
		this.dataInizio = dataInizio;
		this.risorsa = risorsa;
	}
	public Costo(Risorsa risorsa) {
		this.risorsa = risorsa;
	}

	static class MyDataInizioCheck extends Check {
		
		static final String mes = "validation.costo.sovrapposto";

		public boolean isSatisfied(Object costo, Object value) {
			if(!Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty()){
				setMessage(mes);
				return false;
			}
			return true;
		}
	}
	
	static class MyDataFineCheck extends Check {
		static final String DATE_SOVRAPPOSTE_MESSAGE = "validation.costo.sovrapposto";
		static final String DATAFINE_LESS_THAN_DATAINIZIO = "validation.costo.dataFine.lt.dataInizio";
		
		public boolean isSatisfied(Object costo, Object value) {
			if(value !=null){
				if(((Date)value).compareTo(((Costo)costo).dataInizio) <=0 ){
					setMessage(DATAFINE_LESS_THAN_DATAINIZIO);
					return false;
				}
				
				if (!Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty()) {
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
	
}

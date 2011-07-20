package models;

import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class Costo extends GenericModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer idCosto;

	public Float importo;

	@Required
	@CheckWith(MyDataInizioCheck.class)
	public Date dataInizio;
	
	@CheckWith(MyDataFineCheck.class)
	public Date dataFine;

	@ManyToOne
	public Risorsa risorsa;

	public Costo(Float importo, Date dataInizio, Risorsa risorsa) {
		super();
		this.importo = importo;
		this.dataInizio = dataInizio;
		this.risorsa = risorsa;
	}

	static class MyDataInizioCheck extends Check {

		public boolean isSatisfied(Object costo, Object value) {
			return Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty();
		}

	}
	public static List<Costo> costiSovrapposti(Date valuableDate,Costo costo){
		return Costo
		.find("from Costo c where (? between c.dataInizio and c.dataFine or (c.dataFine is null and c.dataInizio < ?) " +
				" or (c.dataInizio > ? and c.dataFine < ?) "+
				") and c.risorsa = ?",
				valuableDate,valuableDate,costo.dataInizio ,costo.dataFine , costo.risorsa).fetch();
	}
	static class MyDataFineCheck extends Check {

		public boolean isSatisfied(Object costo, Object value) {
			return Costo.costiSovrapposti((Date)value, (Costo)costo).isEmpty();
		}

	}
	
}

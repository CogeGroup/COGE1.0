package models;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;

@javax.persistence.Entity
public class TipoRapportoLavoro extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTipoRapportoLavoro;
	
	@Required
	@CheckWith(CodiceCheck.class)
	@MaxSize(255)
	public String codice;
	
	@Required
	@MaxSize(255)
	public String descrizione;
	
	public TipoRapportoLavoro() {}
	
	public TipoRapportoLavoro(String descrizione, String codice) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
	}
	
	static class CodiceCheck extends Check {
		static final String message = "validation.tipoRapportoLavoro.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object tipoRapportoLavoro, Object codice) {
			TipoRapportoLavoro app = TipoRapportoLavoro.find("byCodice", codice).first();
			if(app != null && ((TipoRapportoLavoro) tipoRapportoLavoro).idTipoRapportoLavoro != app.idTipoRapportoLavoro) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	public static TipoRapportoLavoro findByRisorsaAndPeriodo(Risorsa risorsa , Date dataInizio, Date dataFine) {
		JPAQuery query = TipoRapportoLavoro.find("select trl from TipoRapportoLavoro trl, RapportoLavoro ral " +
				"where ral.risorsa = :risorsa and (ral.dataInizio between :dataInizio and :dataFine) " +
		   		"and (ral.dataFine is null or (ral.dataFine between :dataInizio and :dataFine)) " +
		   		"and ral.tipoRapportoLavoro = trl");
		query.bind("risorsa", risorsa);
		query.bind("dataInizio", dataInizio);
		query.bind("dataFine", dataFine != null ? dataFine : dataInizio);
		return query.first();
	}

}

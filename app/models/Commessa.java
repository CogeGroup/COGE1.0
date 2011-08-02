package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.Risorsa.MatricolaCheck;

import org.hibernate.annotations.Type;


import play.data.binding.As;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.InFuture;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;

@javax.persistence.Entity
public class Commessa extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCommessa;
	
	@Required
	@CheckWith(CodiceCheck.class)
	public String codice;

	@Required
	public String descrizione;
	
	public boolean fatturabile;
	
/* Per il momento la data non è obligatoria, perchè nel caso in cui le commesse sono ferie, malattia, ecc 
 * non hanno una data di inizio.
 */
//	@Required
	@As("dd-MM-yyyy")
	@CheckWith(DataInizioCheck.class)
	public Date dataInizioCommessa;
	
	@As("dd-MM-yyyy")
	@CheckWith(DataFineCheck.class)
	public Date dataFineCommessa;
	
	@ManyToOne
	public Cliente cliente;
	
	@OneToMany(mappedBy="commessa",cascade=CascadeType.ALL)
	public List<Tariffa> tariffe;
	
	public Commessa(){}

	public Commessa(String descrizione, String codice, boolean fatturabile) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
		this.fatturabile = fatturabile;
	}
	
	static class CodiceCheck extends Check {
		static final String message = "validation.commessa.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object commessa, Object codice) {
			Commessa app = Commessa.find("byCodice", codice).first();
			if(app != null && ((Commessa) commessa).idCommessa != app.idCommessa) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	static class DataInizioCheck extends Check {
		static final String message = "validation.commessa.data_inizio_se_fatturabile";
		
		@Override
		public boolean isSatisfied(Object commessa, Object dataInizio) {
			if(((Commessa) commessa).fatturabile == true && (dataInizio == null || dataInizio.equals(""))) {
				setMessage(message);
				return false;
	    	}
			return true;
		}
	}
	
	static class DataFineCheck extends Check {
		static final String message = "validation.commessa.data_fine_after_dataInizio";
		
		@Override
		public boolean isSatisfied(Object commessa, Object dataFine) {
			if(((Commessa) commessa).idCommessa != null && 
					((Commessa) commessa).fatturabile == true && 
					dataFine != null && !((Date) dataFine).after(((Commessa) commessa).dataInizioCommessa)) {
				
				setMessage(message);
				return false;
	    	}
			return true;
		}
	}
	
	public static List<Commessa> listaCommesseAttive(){
		return Commessa.find("select cm from Commessa cm where cm.fatturabile = true and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
	}
	
	public float calcolaRicavo(int mese, int anno) {
		
		float importoTotale = 0.0f;
		JPAQuery query  = RendicontoAttivita.find("from RendicontoAttivita ra where ra.commessa = :commessa and ra.mese = "+mese+" and ra.anno = "+anno);
		query.bind("commessa",this);
		List<RendicontoAttivita> lista = query.fetch();

		for(RendicontoAttivita index:lista){
			
			Tariffa t = Tariffa.calcolaTariffaForRisorsaAndCommessa(mese, anno, index.risorsa,index.commessa);
			importoTotale += t.calcolaRicavoTariffa(index.oreLavorate);
		}
		
		return importoTotale;
	}
	
	// chiude tutte le commesse di un cliente e le relative tariffe
	public static void chiudiCommesseByCliente(Cliente cliente) {
		for (Commessa commessa : cliente.commesse) {
			if(commessa.dataInizioCommessa != null){
			   	if (commessa.dataFineCommessa != null) {
	    			if(commessa.dataInizioCommessa.after(new Date())){
	    				commessa.dataFineCommessa = commessa.dataInizioCommessa;
	    			}else{
	    				commessa.dataFineCommessa = new Date();
	    			}
				} else {
					if(commessa.dataInizioCommessa.after(new Date())){
		    			commessa.dataFineCommessa = commessa.dataInizioCommessa;
		    		}else{
		    			commessa.dataFineCommessa = new Date();
		    		}
				}
			   	commessa.save();
			}
		   	Tariffa.chiudiTariffeByCommessa(commessa);
		}
	}
	
}

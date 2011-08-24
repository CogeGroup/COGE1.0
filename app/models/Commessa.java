package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.binding.As;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import utility.MyUtility;

@javax.persistence.Entity
public class Commessa extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCommessa;
	
	@Required
	@MinSize(2)
	@CheckWith(CodiceCheck.class)
	public String codice;

	@Required
	public String descrizione;
	
	public boolean fatturabile;
	
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
	
	// Costruttori
	public Commessa(){
		this.cliente = new Cliente();
	}

	public Commessa(String descrizione, String codice, boolean fatturabile) {
		this.descrizione = descrizione;
		this.codice = codice;
		this.fatturabile = fatturabile;
	}
	
	// Validazione
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
	
	public static List<Commessa> listaCommesseFatturabiliAttive(){
		return Commessa.find("select cm from Commessa cm where cm.fatturabile is true and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
	}
	
	public static List<Commessa> listaCommesseNonFatturabiliAttive(){
		return Commessa.find("select cm from Commessa cm where cm.fatturabile is false and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
	}
	
	public float calcolaRicavo(int mese, int anno) {
		float importoTotale = 0.0f;
		
		JPAQuery query  = RendicontoAttivita.find("from RendicontoAttivita ra where ra.commessa = :commessa and ra.mese = "+mese+" and ra.anno = "+anno);
		query.bind("commessa",this);
		
		List<RendicontoAttivita> lista = query.fetch();
		for(RendicontoAttivita index:lista){
			Tariffa t = Tariffa.findByRisorsaAndCommessaAndData(mese, anno, index.risorsa,index.commessa);
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
	
	public static List<Commessa> findCommesseFatturabiliPerRisorsa(int mese,
			int anno, Risorsa risorsa) {
		List<Commessa> listaCommesse = new ArrayList<Commessa>();
		Date dataRapportoFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		Date dataRapportoInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		
		JPAQuery query = Tariffa.find("from Tariffa t where t.risorsa = :risorsa and t.commessa.fatturabile is true and t.dataInizio <= :dataRapportoFine and (t.dataFine is null or t.dataFine >= :dataRapportoInizio)");
		query.bind("dataRapportoFine", dataRapportoFine);
		query.bind("dataRapportoInizio", dataRapportoInizio);
		query.bind("risorsa",risorsa);
		
		List<Tariffa> listaTariffe = query.fetch();
		if (listaTariffe != null && !listaTariffe.isEmpty()){
		   for(Tariffa t:listaTariffe){
			   if(!(t.commessa instanceof CommessaACorpo)){
				   listaCommesse.add(t.commessa);
			   }
		   }
		}
		return listaCommesse;
	}
	
	public static List<CommessaACorpo> findCommesseACorpoPerRisorsa(int mese,
			int anno, Risorsa risorsa) {
		List<CommessaACorpo> listaCommesse = new ArrayList<CommessaACorpo>();
		Date dataRapportoFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		Date dataRapportoInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		
		JPAQuery query = Tariffa.find("from Tariffa t where t.risorsa = :risorsa and t.commessa.fatturabile is true and t.dataInizio <= :dataRapportoFine and (t.dataFine is null or t.dataFine >= :dataRapportoInizio)");
		query.bind("dataRapportoFine", dataRapportoFine);
		query.bind("dataRapportoInizio", dataRapportoInizio);
		query.bind("risorsa",risorsa);
		
		List<Tariffa> listaTariffe = query.fetch();
		if (listaTariffe != null && !listaTariffe.isEmpty()){
		   for(Tariffa t:listaTariffe){
			   if(t.commessa instanceof CommessaACorpo){
				   listaCommesse.add((CommessaACorpo) t.commessa);
			   }
		   }
		}
		return listaCommesse;
	}
}

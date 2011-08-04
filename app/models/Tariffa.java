package models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import play.data.binding.As;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.db.jpa.GenericModel;
import utility.MyUtility;

@javax.persistence.Entity
public class Tariffa extends GenericModel{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTariffa;
	
	@As("dd-MM-yyyy")
	@CheckWith(dataInizio.class)
	public Date dataInizio;
	
	@As("dd-MM-yyyy")
	@CheckWith(dataFine.class)
	public Date dataFine;

	@CheckWith(importo.class)
	public float importoGiornaliero;

	@ManyToOne
	public Risorsa risorsa;
	
	@ManyToOne
	public Commessa commessa;
	
	// Transient
	@Transient
	public int meseInizio;
	
	@Transient
	public int annoInizio;
	
	@Transient
	public int meseFine;
	
	@Transient
	public int annoFine;
	
	// Costruttori
	public Tariffa() {}

	public Tariffa(Date dataInizio, float importoGiornaliero, Risorsa risorsa,
			Commessa commessa) {
		this.dataInizio = dataInizio;
		this.importoGiornaliero = importoGiornaliero;
		this.risorsa = risorsa;
		this.commessa = commessa;
	}
	
	
	public Tariffa(Risorsa risorsa, int meseInizio, int annoInizio) {
		this.risorsa = risorsa;
		this.meseInizio = meseInizio;
		this.annoInizio = annoInizio;
		this.commessa = new Commessa();
	}



	static class dataInizio extends Check {
		static final String message = "validation.tariffa.dataInizio_before_dataInizoCommessa";
		static final String message2 = "validation.tariffa.dataInizio_after_dataFineCommessa";
		static final String message3 = "validation.tariffa.dataInizio_before_dataInizio_ultima_tariffa";
		
		@Override
		public boolean isSatisfied(Object tariffa, Object value) {
			Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(((Tariffa) tariffa).meseInizio, ((Tariffa) tariffa).annoInizio);
			Commessa commessa = ((Tariffa) tariffa).commessa;
			Risorsa risorsa = ((Tariffa) tariffa).risorsa;
			if(((Tariffa) tariffa).idTariffa == null){
				if(commessa.dataInizioCommessa != null){
					Date dataCommessa = commessa.dataInizioCommessa;
					int meseCommessa = MyUtility.getMeseFromDate(dataCommessa);
					int annoCommessa = MyUtility.getAnnoFromDate(dataCommessa);
					dataCommessa = MyUtility.MeseEdAnnoToDataInizio(meseCommessa, annoCommessa);
					if(dataInizio.before(dataCommessa)){
						setMessage(message, new SimpleDateFormat("MMM/yyyy").format(dataCommessa));
						return false;
					}
				}
				if(commessa.dataFineCommessa != null){
					Date dataCommessa = commessa.dataFineCommessa;
					int meseCommessa = MyUtility.getMeseFromDate(dataCommessa);
					int annoCommessa = MyUtility.getAnnoFromDate(dataCommessa);
					dataCommessa = MyUtility.MeseEdAnnoToDataFine(meseCommessa, annoCommessa);
					if(!dataInizio.before(dataCommessa)){
						setMessage(message2, new SimpleDateFormat("MMM/yyyy").format(dataCommessa));
				    	return false;
					}
				}
				List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa", commessa, risorsa).fetch();
				if(lista.size() > 0){
					Tariffa t = lista.get(lista.size()-1);
					if(t.dataFine == null){
						if(!dataInizio.after(t.dataInizio)){
					       	setMessage(message3, new SimpleDateFormat("MMM/yyyy").format(t.dataInizio));
					       	return false;
					    }
						Calendar c = Calendar.getInstance();
						c.setTime(dataInizio);
						c.add(Calendar.DAY_OF_MONTH, -1);
						Date data = c.getTime();
						t.dataFine=data;
						t.save();
					}else{
						if(dataInizio.before(t.dataFine)){
							setMessage(message3, new SimpleDateFormat("MMM/yyyy").format(t.dataFine));
							return false;
					    }
					}
				}
			}else{
				List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa",commessa, risorsa).fetch();
		        if(lista.size() > 0) {
		        	// la penultima tariffa poiché l'ultima tariffa è quella da modificare
		        	Tariffa t = lista.size() > 1 ? lista.get(lista.size()-2) : lista.get(0);
		        	if(lista.size() > 1){
		        		// se la dataInizio della tariffa è maggiore della dataFine della penultima tariffa
					    if(!dataInizio.after(t.dataFine)){
					       	setMessage(message3, new SimpleDateFormat("MMM/yyyy").format(t.dataFine));
					       	return false;
					    }
		        	}
		        }
			}
			return true;
		}
	}
	
	static class dataFine extends Check {
		static final String message = "validation.tariffa.dataFine_before_dataInizo";
		static final String message2 = "validation.tariffa.dataFine_after_dataFineCommessa";
		static final String message3 = "validation.tariffa.dataFine_wrong_selection";
		
		@Override
		public boolean isSatisfied(Object tariffa, Object value) {
			Tariffa tariffa2 = (Tariffa) tariffa;
			if(tariffa2.idTariffa == null){
				return true;
			}
			tariffa2.dataInizio = MyUtility.MeseEdAnnoToDataInizio(((Tariffa) tariffa).meseInizio, ((Tariffa) tariffa).annoInizio);
			if (tariffa2.meseFine == -1 ^ tariffa2.annoFine == -1) {
				setMessage(message3);
				return false;
			}
			if(tariffa2.meseFine > -1) {
				tariffa2.dataFine = MyUtility.MeseEdAnnoToDataFine(tariffa2.meseFine, tariffa2.annoFine);
				if(tariffa2.dataFine.before(tariffa2.dataInizio)){
					setMessage(message);
					return false;
				}
				if(tariffa2.commessa.dataFineCommessa != null && !tariffa2.dataFine.before(tariffa2.commessa.dataFineCommessa)){
					setMessage(message2, new SimpleDateFormat("MMM/yyyy").format(tariffa2.commessa.dataFineCommessa));
					return false;
				}
			}
			return true;
		}
	}
	
	static class importo extends Check {
		static final String message = "validation.tariffa.importo";
		
		@Override
		public boolean isSatisfied(Object tariffa, Object dataInizio) {
			if(!(((Tariffa)tariffa).commessa instanceof CommessaACorpo) && ((Tariffa) tariffa).importoGiornaliero <= 0){
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	
	public static Tariffa findByRisorsaAndCommessaAndData(int mese,int anno,Risorsa risorsa,Commessa commessa){
		Tariffa tariffa = null;
		try {
			Date dataInizioRapporto = MyUtility.MeseEdAnnoToDataInizio(mese+1, anno);
			Date dataFineRapporto = MyUtility.MeseEdAnnoToDataFine(mese+1, anno);
			JPAQuery query = Tariffa.find("from Tariffa t where t.risorsa = :risorsa and t.commessa = :commessa and t.dataInizio <= :dataFineRapporto and (t.dataFine is null or t.dataFine >= :dataInizioRapporto)");
			query.bind("commessa", commessa);
			query.bind("risorsa",risorsa);
			query.bind("dataInizioRapporto", dataInizioRapporto);
			query.bind("dataFineRapporto", dataFineRapporto);
			Object t = query.first();
			if (t != null){
				tariffa = (Tariffa)t;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tariffa;
	}
	
	public static List<Tariffa> findByRisorsaAndMeseAndAnno(int mese,int anno,Risorsa risorsa){
		Date dataInizioRapporto = MyUtility.MeseEdAnnoToDataInizio(mese+1, anno);
		Date dataFineRapporto = MyUtility.MeseEdAnnoToDataFine(mese+1, anno);
		JPAQuery query = Tariffa.find("from Tariffa t where t.risorsa = :risorsa and t.dataInizio <= :dataFineRapporto and (t.dataFine is null or t.dataFine >= :dataInizioRapporto)");
		query.bind("risorsa",risorsa);
		query.bind("dataInizioRapporto", dataInizioRapporto);
		query.bind("dataFineRapporto", dataFineRapporto);
		return query.fetch();
	}
	
	public  float calcolaRicavoTariffa(int oreLavorate){
		return calcolaTariffaOraria() * oreLavorate;
	}
	
	public float calcolaTariffaOraria(){
		return this.importoGiornaliero /8;
	}

	/* chiude tutte le tariffe associate a una commessa
     * se la data inizio è maggiore di oggi, data fine uguale alla data inizio
     * se la data fine è minore di oggi rimane invariate
     * se la data fine è maggiore di oggi, data fine uguale a oggi
     */
    public static void chiudiTariffeByCommessa(Commessa commessa) {
    	for (Tariffa tariffa : commessa.tariffe) {
    		if (tariffa.dataFine == null) {
    			if(tariffa.dataInizio.after(new Date())){
    				tariffa.dataFine = tariffa.dataInizio;
    			}else{
    				tariffa.dataFine = commessa.dataFineCommessa;
    			}
			} else {
				if(tariffa.dataFine.after(new Date())){
					if(tariffa.dataInizio.after(new Date())){
						tariffa.dataFine = tariffa.dataInizio;
					}else{
						tariffa.dataFine = commessa.dataFineCommessa;
					}
				}
			}
    		tariffa.save();
		}
    }
}

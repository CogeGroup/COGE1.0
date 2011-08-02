package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.h2.constant.SysProperties;

import models.Risorsa.DataOutCheck;
import net.sf.oval.constraint.NotEqual;

import play.data.binding.As;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.InFuture;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;
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
	
//	@Required(message="Importo giornaliero obligatorio")
//	@Min(message = "L'importo deve essere maggiore di 0.0",value = 0.1)
	@CheckWith(importo.class)
	public float importoGiornaliero;

	@ManyToOne
	public Risorsa risorsa;
	
	@ManyToOne
	public Commessa commessa;
	
	@Transient
	public Integer idCommessa;
	
	@Transient
	public Integer idRisorsa;
	
	@Transient
	public int meseInizio;
	
	@Transient
	public int annoInizio;
	
	@Transient
	public int meseFine;
	
	@Transient
	public int annoFine;
	
	public Tariffa() {}

	public Tariffa(Date dataInizio, float importoGiornaliero, Risorsa risorsa,
			Commessa commessa) {
		super();
		this.dataInizio = dataInizio;
		this.importoGiornaliero = importoGiornaliero;
		this.risorsa = risorsa;
		this.commessa = commessa;
	}
	
	static class dataInizio extends Check {
		static final String message = "validation.tariffa.dataInizio_before_dataInizoCommessa";
		static final String message2 = "validation.tariffa.dataInizio_after_dataFineCommessa";
		static final String message3 = "validation.tariffa.dataInizio_before_dataInizio_ultima_tariffa";
		
		@Override
		public boolean isSatisfied(Object tariffa, Object value) {
			Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(((Tariffa) tariffa).meseInizio, ((Tariffa) tariffa).annoInizio);
			if(((Tariffa) tariffa).idTariffa == null){
				Commessa commessa = Commessa.findById(((Tariffa) tariffa).idCommessa);
				if(commessa.dataInizioCommessa != null){
					Date dataCommessa = commessa.dataInizioCommessa;
					int meseCommessa = MyUtility.getMeseFromDate(dataCommessa);
					int annoCommessa = MyUtility.getAnnoFromDate(dataCommessa);
					dataCommessa = MyUtility.MeseEdAnnoToDataInizio(meseCommessa, annoCommessa);
					if(dataInizio.before(dataCommessa)){
						setMessage(message, new SimpleDateFormat("dd/MM/yyyy").format(dataCommessa));
						return false;
					}
				}
				if(commessa.dataFineCommessa != null){
					Date dataCommessa = commessa.dataFineCommessa;
					int meseCommessa = MyUtility.getMeseFromDate(dataCommessa);
					int annoCommessa = MyUtility.getAnnoFromDate(dataCommessa);
					dataCommessa = MyUtility.MeseEdAnnoToDataFine(meseCommessa, annoCommessa);
					if(!dataInizio.before(dataCommessa)){
						setMessage(message2, new SimpleDateFormat("dd/MM/yyyy").format(dataCommessa));
				    	return false;
					}
				}
				Risorsa risorsa = Risorsa.findById(((Tariffa) tariffa).idRisorsa);
				List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa", commessa, risorsa).fetch();
				if(lista.size() > 0){
					Tariffa t = lista.get(lista.size()-1);
					if(t.dataFine == null){
						if(!dataInizio.after(t.dataInizio)){
					       	setMessage(message3, new SimpleDateFormat("dd/MM/yyyy").format(t.dataInizio));
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
							setMessage(message3, new SimpleDateFormat("dd/MM/yyyy").format(t.dataFine));
							return false;
					    }
					}
				}
			}else{
				Commessa commessa = Commessa.findById(((Tariffa) tariffa).idCommessa);
				Risorsa risorsa = Risorsa.findById(((Tariffa) tariffa).idRisorsa);
				List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa",commessa, risorsa).fetch();
		        if(lista.size() > 0) {
		        	// la penultima tariffa poiché l'ultima tariffa è quella da modificare
		        	Tariffa t = lista.size() > 1 ? lista.get(lista.size()-2) : lista.get(0);
		        	if(lista.size() > 1){
		        		// se la dataInizio della tariffa è maggiore della dataFine della penultima tariffa
					    if(!dataInizio.after(t.dataFine)){
					       	setMessage(message3, new SimpleDateFormat("dd/MM/yyyy").format(t.dataFine));
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
		static final String message3 = "validation.tariffa.dataFine";
		
		@Override
		public boolean isSatisfied(Object tariffa, Object value) {
			if(((Tariffa) tariffa).idTariffa == null){
				return true;
			}
			Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(((Tariffa) tariffa).meseInizio, ((Tariffa) tariffa).annoInizio);
			if(((Tariffa) tariffa).meseFine != -1 && ((Tariffa) tariffa).annoFine != -1){
				Date dataFine = MyUtility.MeseEdAnnoToDataFine(((Tariffa) tariffa).meseFine, ((Tariffa) tariffa).annoFine);
				if(dataFine.before(dataInizio)){
					setMessage(message);
					return false;
				}
				Commessa commessa = Commessa.findById(((Tariffa) tariffa).idCommessa);
				if(commessa.dataFineCommessa != null && !dataFine.before(commessa.dataFineCommessa)){
					setMessage(message2, new SimpleDateFormat("dd/MM/yyyy").format(commessa.dataFineCommessa));
					return false;
				}
			}else{
				Commessa commessa = Commessa.findById(((Tariffa) tariffa).idCommessa);
				if(commessa.dataFineCommessa != null){
					setMessage(message3, new SimpleDateFormat("dd/MM/yyyy").format(commessa.dataFineCommessa));
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
			Commessa commessa = Commessa.findById(((Tariffa) tariffa).idCommessa);
			if(!(commessa instanceof CommessaACorpo) && ((Tariffa) tariffa).importoGiornaliero <= 0){
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	
	public static Tariffa calcolaTariffaForRisorsaAndCommessa(int mese,int anno,Risorsa risorsa,Commessa commessa){
		Tariffa tariffa = null;
		try {
			Date dataInizioRapporto = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
			Date dataFineRapporto = MyUtility.MeseEdAnnoToDataFine(mese, anno);
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
	
	public  float calcolaRicavoTariffa(int oreLavorate){
		return calcolaTariffaOraria() * oreLavorate;
	}
	
	public float calcolaTariffaOraria(){
		return this.importoGiornaliero /8;
	}

	public static List<Commessa> trovaCommessePerRisorsa(int mese,
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

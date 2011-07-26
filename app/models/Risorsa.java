package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import play.data.binding.As;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import utility.MyUtility;



@javax.persistence.Entity
public class Risorsa extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRisorsa;
	
	@Required
	@CheckWith(MatricolaCheck.class)
	@MaxSize(255)
	public String matricola;
	
	@Required
	@MaxSize(255)
	public String codice;
	
	@Required
	@MaxSize(255)
	public String nome;
	
	@Required
	@MaxSize(255)
	public String cognome;
	
	@Required
	@CheckWith(DataInCheck.class)
	@As("dd-MM-yyyy")
	public Date dataIn;
	
	@CheckWith(DataOutCheck.class)
	@As("dd-MM-yyyy")
	public Date dataOut;
	
	@OneToMany (mappedBy="risorsa", cascade=CascadeType.ALL)
	public List<RapportoLavoro> rapportiLavoro = new ArrayList<RapportoLavoro>();

	@OneToMany (mappedBy="risorsa", cascade=CascadeType.ALL)
	public List<Costo> listaCosti = new ArrayList<Costo>();

	@OneToMany (mappedBy="risorsa", cascade=CascadeType.ALL)
	public List<Tariffa> listaTariffe = new ArrayList<Tariffa>();

	@OneToMany (mappedBy="risorsa", cascade=CascadeType.ALL)
	public List<RapportoAttivita> rapportiAttivita = new ArrayList<RapportoAttivita>();

	//constructors
	public Risorsa() {
	}


	public Risorsa(String matricola, String codice, String nome,
			String cognome, Date dataIn) {
		super();
		this.matricola = matricola;
		this.codice = codice;
		this.nome = nome;
		this.cognome = cognome;
		this.dataIn = dataIn;
	}
	
	static class MatricolaCheck extends Check {
		static final String message = "validation.risorsa.matricola_esistente";
		
		@Override
		public boolean isSatisfied(Object risorsa, Object matricola) {
			Risorsa app = Risorsa.find("byMatricola", matricola).first();
			if(app != null && ((Risorsa) risorsa).idRisorsa != app.idRisorsa) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	static class DataInCheck extends Check {
		static final String message = "validation.risorsa.dataIn_maggiore_primo_rapporto_lavoro";
		
		@Override
		public boolean isSatisfied(Object risorsa, Object dataIn) {
			Risorsa risorsa2 = (Risorsa) risorsa;
			if(risorsa2.rapportiLavoro != null && risorsa2.rapportiLavoro.size() > 0 && ((Date) dataIn).after(risorsa2.rapportiLavoro.get(0).dataInizio)) {
				setMessage(message, MyUtility.dateToString(risorsa2.rapportiLavoro.get(0).dataInizio));
				return false;
			}
			return true;
		}
	}
	
	static class DataOutCheck extends Check {
		static final String message = "validation.risorsa.dataOut_minore_ultimo_rapporto_lavoro";
		
		@Override
		public boolean isSatisfied(Object risorsa, Object dataOut) {
			Risorsa risorsa2 = (Risorsa) risorsa;
			if(dataOut != null && !((Date) dataOut).after(risorsa2.rapportiLavoro.get(risorsa2.rapportiLavoro.size() - 1).dataInizio)) {
				setMessage(message, MyUtility.dateToString(risorsa2.rapportiLavoro.get(risorsa2.rapportiLavoro.size() - 1).dataInizio));
				return false;
			}
			return true;
		}
	}
	
	public void addRapportoLavoro(RapportoLavoro rl){
		
		rapportiLavoro.add(rl);
		rl.risorsa=this;
		
	}
	
	public float calcolaRicavo(String mese,String anno) throws ParseException{
		
		float importoTotale = 0f;
		RapportoAttivita rapportoAttivita = RapportoAttivita.find("byRisorsaAndMeseAndAnno",this,mese,anno).first();
		if (rapportoAttivita == null )
			return importoTotale;
		
		List<DettaglioRapportoAttivita> lista = rapportoAttivita.dettagliRapportoAttivita;
		if (lista == null || lista.size() == 0)
			return importoTotale;
		
		for (DettaglioRapportoAttivita dra : lista){
			Tariffa t = Tariffa.calcolaTariffaRisorsaCommessa(mese, anno, dra.rapportoAttivita.risorsa,dra.commessa);
			importoTotale += t.calcolaRicavoTariffa(dra.oreLavorate);
			}
		return importoTotale;
	}
	
	
	public static float calcolaRicavoPerTipoRapportoLavoro(String mese, String anno, TipoRapportoLavoro tiporapLav){
		List<RapportoLavoro> rapLavList = null;
		try {
			rapLavList = RapportoLavoro.findByTipoRapportoLavoroAndPeriodo(tiporapLav, mese, anno);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		float ret = 0f;
		
		
		if (rapLavList == null || rapLavList.size() == 0)
			return ret;
		
		for (RapportoLavoro ral : rapLavList){
			try {
				ret += ral.risorsa.calcolaRicavo(mese, anno);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
		}
		
		return ret;
	}



}

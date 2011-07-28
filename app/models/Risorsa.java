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
import javax.persistence.Transient;

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
import play.db.jpa.GenericModel.JPAQuery;
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
	
	@Transient
	public String codiceRapporto;
	
	@Transient
	public Integer ore;
	
	@Transient
	public float ricavo;
	
	@Transient
	public float costo;
	
	@Transient
	public String margine;
	
	@Transient
	public float importoTariffa;
	
	@Transient
	public String clienteProgetto;

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
		
		List<RendicontoAttivita> listaRendicontoAttivita = RendicontoAttivita.find("byRisorsaAndMeseAndAnno",this,mese,anno).fetch();
		if (listaRendicontoAttivita == null || listaRendicontoAttivita.size() == 0)
			return importoTotale;
		
		for (RendicontoAttivita ra : listaRendicontoAttivita){
			if(ra.commessa.fatturabile){
				Tariffa t = Tariffa.calcolaTariffaRisorsaCommessa(mese, anno, ra.risorsa,ra.commessa);
				importoTotale += t.calcolaRicavoTariffa(ra.oreLavorate);
			}else if(ra.commessa instanceof CommessaACorpo){
				System.out.println("commessa a corpo");
			}
		}
		return importoTotale;
	}
	
	public List<Risorsa> reportRisorse(String mese,String anno) throws ParseException{
		float ricavo = 0f;
		float costoTotale = 0f;
		List<Risorsa> lista = new ArrayList<Risorsa>(); 
		List<RendicontoAttivita> listaRendicontoAttivita = RendicontoAttivita.find("byRisorsaAndMeseAndAnno",this,Integer.parseInt(mese),Integer.parseInt(anno)).fetch();
		if (listaRendicontoAttivita == null || listaRendicontoAttivita.size() == 0)
			return lista;
		
		for (RendicontoAttivita ra : listaRendicontoAttivita){
			Tariffa t = Tariffa.calcolaTariffaRisorsaCommessa(mese, anno, ra.risorsa, ra.commessa);
			if(ra.commessa.fatturabile){
				ricavo = t.calcolaRicavoTariffa(ra.oreLavorate);
				JPAQuery query = Costo.find("from Costo c where c.risorsa=:risorsa and (c.dataInizio <= :dataFine or c.dataFine >= :dataInizio)");
				query.bind("risorsa", ra.risorsa);
				query.bind("dataInizio", new SimpleDateFormat("dd/MM/yyyy").parse("01/" + mese + "/" + anno));
				query.bind("dataFine", new SimpleDateFormat("dd/MM/yyyy").parse("31/" + mese + "/" + anno));
				Costo costo = query.first();
				System.out.println(ra.risorsa.nome + " " + costo.idCosto);
				if(costo != null)
					costoTotale = (costo.importo/8)*ra.oreLavorate;
				else
					costoTotale = 0;
				
				Risorsa RisorsaReport = new Risorsa();
				RisorsaReport.matricola = this.matricola;
				RisorsaReport.codice = this.codice;
				RisorsaReport.nome = this.nome;
				RisorsaReport.cognome = this.cognome;
				RisorsaReport.codiceRapporto = ra.risorsa.rapportiLavoro.get(rapportiLavoro.size()-1).tipoRapportoLavoro.codice;
				RisorsaReport.ore = ra.oreLavorate;
				RisorsaReport.ricavo = ricavo;
				RisorsaReport.costo = costoTotale;
				RisorsaReport.margine = ((ricavo-costoTotale)*100)/ricavo + "%";
				RisorsaReport.importoTariffa = t.importoGiornaliero;
				RisorsaReport.clienteProgetto = ra.commessa.cliente.codice + "-" + ra.commessa.codice;
				
				lista.add(RisorsaReport);
			}else if(ra.commessa instanceof CommessaACorpo){
				// commessa a corpo
				// in rendicontoAttivita non ci sono commesse a corpo
			}else{
				// se non è fatturabile
				Risorsa RisorsaReport = new Risorsa();
				RisorsaReport.matricola = this.matricola;
				RisorsaReport.codice = this.codice;
				RisorsaReport.nome = this.nome;
				RisorsaReport.cognome = this.cognome;
				RisorsaReport.codiceRapporto = ra.risorsa.rapportiLavoro.get(rapportiLavoro.size()-1).tipoRapportoLavoro.codice;
				RisorsaReport.ore = ra.oreLavorate;
				RisorsaReport.margine = "0.0%";
				RisorsaReport.importoTariffa = t.importoGiornaliero;
				RisorsaReport.clienteProgetto = ra.commessa.cliente.codice + "-" + ra.commessa.codice;

				lista.add(RisorsaReport);
			}
		}
		
		return lista;
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
	
	public Costo extractLastCosto(){
		if(listaCosti == null || listaCosti.isEmpty()){
			return null;
		}
		return listaCosti.get(listaCosti.size()-1);
	}



}

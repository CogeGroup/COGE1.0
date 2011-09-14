package models;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.h2.constant.SysProperties;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateMidnight;

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
	
	@OneToOne
	@CheckWith(TipoStatoRisorsaCheck.class)
	public TipoStatoRisorsa tipoStatoRisorsa;
	
	@ManyToOne
	public Gruppo gruppo;
	
	@Transient
	public float guadagno;
	
	//constructors
	public Risorsa() {
	}

	public Risorsa(String codice, String nome,
			String cognome, Date dataIn) {
		super();
		this.codice = codice;
		this.nome = nome;
		this.cognome = cognome;
		this.dataIn = dataIn;
	}
	
	static class DataInCheck extends Check {
		static final String message = "validation.risorsa.dataIn_maggiore_primo_rapporto_lavoro";
		
		@Override
		public boolean isSatisfied(Object risorsa, Object dataIn) {
			Risorsa risorsa2 = (Risorsa) risorsa;
			//effettuo la validazione solo se dataIn è valorizzata
			if(risorsa2.dataIn == null) {
				return true;
			}
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
			//effettuo la validazione solo se dataIn è valorizzata
			if(risorsa2.dataIn == null) {
				return true;
			}
			if(dataOut != null && !((Date) dataOut).after(risorsa2.rapportiLavoro.get(risorsa2.rapportiLavoro.size() - 1).dataInizio)) {
				setMessage(message, MyUtility.dateToString(risorsa2.rapportiLavoro.get(risorsa2.rapportiLavoro.size() - 1).dataInizio));
				return false;
			}
			return true;
		}
	}
	
	
	static class TipoStatoRisorsaCheck extends Check {
		static final String message = "validation.risorsa.tipoStatoRisorsa.stato_chiuso_non_valido";
		
		@Override
		public boolean isSatisfied(Object risorsa, Object tipoStatoRisorsa) {
			Risorsa risorsa2 = (Risorsa) risorsa;
			if (risorsa2.dataOut == null && ((TipoStatoRisorsa) tipoStatoRisorsa).codice.equals("CHIUSO")) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	public void addRapportoLavoro(RapportoLavoro rl){
		rapportiLavoro.add(rl);
		rl.risorsa=this;
	}
	
	public static List<Risorsa> findCoCoPro(String parametro, String ordinamento) {
		JPAQuery query  = Risorsa.find("SELECT r FROM Risorsa r, RapportoLavoro rl, TipoRapportoLavoro trl " +
				"where trl.codice = 'CCP' and trl = rl.tipoRapportoLavoro " +
				"and rl.risorsa = r " +
				"order by r." + parametro + " " + ordinamento);
		return query.fetch();
//		return  Risorsa.find("SELECT r FROM Risorsa r, RapportoLavoro rl, TipoRapportoLavoro trl " +
//				"where trl.codice = 'CCP' and trl = rl.tipoRapportoLavoro " +
//				"and rl.risorsa = r order by r.cognome, r.nome").fetch();
	}
	
	public static List<Risorsa> findDipendenti(String parametro, String ordinamento) {
		JPAQuery query  = Risorsa.find("SELECT r FROM Risorsa r, RapportoLavoro rl, TipoRapportoLavoro trl " +
				"where trl.codice <> 'CCP' and trl = rl.tipoRapportoLavoro " +
				"and rl.risorsa = r " +
				"order by r." + parametro + " " + ordinamento);
		return query.fetch();
//		return  Risorsa.find("SELECT r FROM Risorsa r, RapportoLavoro rl, TipoRapportoLavoro trl " +
//				"where trl.codice <> 'CCP' and trl = rl.tipoRapportoLavoro " +
//				"and rl.risorsa = r order by r.cognome, r.nome").fetch();
	}
	
	public static List<Risorsa> findByCommessa(Commessa commessa) {
		JPAQuery query  = Risorsa.find("SELECT distinct r FROM Tariffa t, Risorsa r " +
				"WHERE t.commessa = :commessa " +
				"AND (t.dataFine is null OR ( t.dataFine >= :data AND t.dataFine <= :data)) " +
				"AND t.risorsa = r");
		query.bind("commessa",commessa);
		query.bind("data",new Date());
		return query.fetch();
	}
	
	public Float calcolaRicavo(int mese,int anno){
		Float importoTotale = 0f;
		List<Tariffa> listaTariffa = Tariffa.findByRisorsaAndMeseAndAnno(mese,anno,this);
		if(listaTariffa == null || listaTariffa.size() == 0) {
			return null;
		}
		List<RendicontoAttivita> listaRendicontoAttivita = RendicontoAttivita.find("byRisorsaAndMeseAndAnno",this,mese+1,anno).fetch();
		for (Tariffa tariffa : listaTariffa) {
			if(tariffa.commessa instanceof CommessaACorpo) {
				importoTotale += ((CommessaACorpo) tariffa.commessa).importo;
			}
		}
		for (RendicontoAttivita ra : listaRendicontoAttivita){
			if(ra.commessa.calcoloRicavi){
				Tariffa t = Tariffa.findByRisorsaAndCommessaAndData(mese, anno, ra.risorsa,ra.commessa);
				importoTotale += t.calcolaRicavoTariffa(ra.oreLavorate);
			}
		}
		return importoTotale == 0 ? null : importoTotale;
	}
	
	public static float calcolaRicavoPerTipoRapportoLavoro(int mese, int anno, TipoRapportoLavoro tiporapLav){
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public Costo extractLastCosto(){
		if(listaCosti == null || listaCosti.isEmpty())
			return null;
		return listaCosti.get(listaCosti.size()-1);
	}
	
	public static List<Risorsa> listAnomalieRicavi(int mese, int anno) {
		List<Risorsa> listaAnomalie = new ArrayList<Risorsa>();
		List<Risorsa> listaRisorse = Risorsa.findAll();
    	for (Risorsa risorsa : listaRisorse) {
			Float ricavo = risorsa.calcolaRicavo(mese, anno);
			Costo costo = Costo.extractCostoByMeseAndAnno(risorsa,mese,anno);
			if(ricavo != null && costo != null){
				float guadagno = ricavo - costo.totaleCosto(costo,risorsa,mese,anno);
				if(guadagno < 0){
					risorsa.guadagno = guadagno;
					listaAnomalie.add(risorsa);
				}
			}
		}
    	return listaAnomalie;
	}
	
//	public static ArrayList<HashMap> statisticaRisorsa(int mese,int anno){
//		DateMidnight dataInizio = new DateMidnight().withDayOfMonth(1).withMonthOfYear(mese).withYear(anno);
//		DateMidnight dataFine = new DateMidnight().withMonthOfYear(mese).withYear(anno).dayOfMonth().withMaximumValue();
//		String queryString = "SELECT r.idRisorsa as idRisorsa, " +
//				"r.codice as codice, r.cognome as cognome, sum(if(com.fatturabile=true,ra.oreLavorate,0)) as ore_lavorate,"+
//				" sum(costo(c.importo,ra.oreLavorate)) as costo_totale, sum(ricavo(t.importoGiornaliero,ra.oreLavorate)) as ricavo_totale,"+
//				" sum(margine(costo(c.importo,ra.oreLavorate), ricavo(t.importoGiornaliero,ra.oreLavorate))) as margine_totale, t.importoGiornaliero as importo_tariffa "+
//				" FROM `risorsa` r, `costo` c, `tariffa` t, `commessa` com, `rendicontoattivita` ra,`rapportolavoro` rl"+
//				" WHERE r.idRisorsa = c.risorsa_idRisorsa and c.dataInizio <= "
//				+dataInizio+
//				" and (c.dataFine is null or c.dataFine >= "
//				+dataFine+
//				") and r.idRisorsa = t.risorsa_idRisorsa and t.commessa_idCommessa = com.idCommessa"+
//				" and t.dataInizio <= " 
//				+dataInizio+
//				"and (t.dataFine is null or t.dataFine >= " 
//				+dataFine+
//				") and r.idRisorsa = rl.risorsa_idRisorsa and rl.dataInizio <= "
//				+dataInizio+
//				" and (rl.dataFine is null or rl.dataFine >= "
//				+dataFine+
//				") and r.idRisorsa = ra.risorsa_idRisorsa and ra.commessa_idCommessa = com.idCommessa"+
//				"and ra.mese= "
//				+mese+ 
//				"and ra.anno= "
//				+anno+
//				" group by r.idRisorsa";
//		 Session session = (Session)JPA.em().getDelegate();
//		 List<Object[]> resultList = session.createSQLQuery(queryString).list();
//		 ArrayList<HashMap> listaMapResult = new ArrayList<HashMap>();
//		 for (Object[] objects : resultList) {
//			 HashMap map = new HashMap();
//			 map.put("codice", (String) objects[1]);
//			 map.put("cognome", (String) objects[2]);
//			 map.put("ore_lavorate", (Integer) objects[3]);
//			 map.put("costo_totale", (Double) objects[4]);
//			 map.put("ricavo_totale", (String) objects[5]);
//			 map.put("margine_totale", (String) objects[6]);
//			 map.put("importo_tariffa", (String) objects[7]);
//			 listaMapResult.add(map); 
//		 }
//		 
//		 return listaMapResult;
//	}
	
	
	public static ArrayList<HashMap> statisticaRisorsa(int mese,int anno){
		DateMidnight dataInizio = new DateMidnight().withDayOfMonth(1).withMonthOfYear(mese).withYear(anno);
		DateMidnight dataFine = new DateMidnight().withMonthOfYear(mese).withYear(anno).dayOfMonth().withMaximumValue();
		String queryString = "SELECT r.codice as codice,  concat(r.cognome,' ',r.nome)  as cognome, trl.codice as rappLavoro, sum(if(com.fatturabile=true,ra.oreLavorate,0)) as ore_lavorate, "+
				"sum(costo(c.importo,ra.oreLavorate)) as costo_totale, sum(ricavo(t.importoGiornaliero,ra.oreLavorate)) as ricavo_totale, "+
				"sum(margine(costo(c.importo,ra.oreLavorate), ricavo(t.importoGiornaliero,ra.oreLavorate))) as margine_totale "+
				"FROM `Risorsa` r, `Costo` c, `Tariffa` t, `Commessa` com, `RendicontoAttivita` ra,`RapportoLavoro` rl,`TipoRapportoLavoro` trl "+
				"WHERE r.idRisorsa = c.risorsa_idRisorsa and c.dataInizio <= '"
				+dataInizio.toDate()+
				"' and (c.dataFine is null or c.dataFine >= '"
				+dataFine.toDate()+
				"' ) and r.idRisorsa = t.risorsa_idRisorsa and t.commessa_idCommessa = com.idCommessa and t.dataInizio <= '"
				+dataInizio.toDate()+
				"' and (t.dataFine is null or t.dataFine >= '"
				+dataFine.toDate()+
				"' ) and r.idRisorsa = rl.risorsa_idRisorsa and rl.dataInizio <= '"
				+dataInizio.toDate()+
				"' and (rl.dataFine is null or rl.dataFine >= '"
				+dataFine.toDate()+
				"' ) and r.idRisorsa = ra.risorsa_idRisorsa and ra.commessa_idCommessa = com.idCommessa and rl.dataInizio <= '"
				+dataInizio.toDate()+
				"' and (rl.dataFine is null or rl.dataFine >= '"
				+dataFine.toDate()+
				"' )and rl.tipoRapportoLavoro_idTipoRapportoLavoro = trl.idTipoRapportoLavoro "+
				"and r.idRisorsa = rl.risorsa_idRisorsa "+
				"and ra.mese= "
				+mese+
				" and ra.anno= "
				+anno+
				 " group by r.idRisorsa";
		 Session session = (Session)JPA.em().getDelegate();
		 List<Object[]> resultList = session.createSQLQuery(queryString).list();
		 ArrayList<HashMap> listaMapResult = new ArrayList<HashMap>();
		 for (Object[] objects : resultList) {
			 HashMap map = new HashMap();
			 map.put("codice", (String) objects[0]);
			 map.put("cognome", (String) objects[1]);
			 map.put("rappLavoro", (String) objects[2]);
			 map.put("ore_lavorate", (BigDecimal) objects[3]);
			 map.put("costo_totale", (Double) objects[4]);
			 map.put("ricavo_totale", (Double) objects[5]);
			 map.put("margine_totale", (Double) objects[6]);
			 listaMapResult.add(map); 
		 }
		 return listaMapResult;
	}

}

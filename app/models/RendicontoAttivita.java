package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.Session;

import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import sun.text.normalizer.Utility;
import utility.MyUtility;

@javax.persistence.Entity
public class RendicontoAttivita extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRendicontoAttivita;
	
	@Required
	public float oreLavorate;
	
	public int mese;
	
	public int anno;
	
	@ManyToOne
	public Risorsa risorsa;
	
	@ManyToOne
	public Commessa commessa;

	public RendicontoAttivita() {}
	
	public RendicontoAttivita(float oreLavorate, int mese, int anno,
			Risorsa risorsa, Commessa commessa) {
		this.oreLavorate = oreLavorate;
		this.mese = mese;
		this.anno = anno;
		this.risorsa = risorsa;
		this.commessa = commessa;
	}

	public static List<RendicontoAttivita> findByExample(Integer idRisorsa,int mese,int anno) {
		String query = "SELECT ra " +
			"FROM RendicontoAttivita ra WHERE 1=1 ";
		if(idRisorsa != null && idRisorsa > 0){
			Risorsa risorsa = Risorsa.findById(idRisorsa);
			query += "AND ra.risorsa=" + risorsa; 
		}
		if(mese != -1 && anno != -1){
			query += " AND ra.mese=" + mese + " AND anno=" + anno;
		}
		query += "GROUP BY ra.risorsa, ra.mese, ra.anno " +
			"ORDER BY ra.risorsa ASC, ra.anno ASC, ra.mese ASC";
		
		List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find(query).fetch();
		return listaRapportini;
	}
	
	public static List<RendicontoAttivita> findAndOrder() {
		String query = "SELECT ra " +
				"FROM RendicontoAttivita ra " +
				"GROUP BY ra.risorsa, ra.mese, ra.anno " +
				"ORDER BY ra.risorsa ASC, ra.anno ASC, ra.mese ASC";
		List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find(query).fetch();
		return listaRapportini;
	}
	
	public static List<Risorsa> listRapportiniMancanti(int mese, int anno) {
		Date dataFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		JPAQuery query  = Risorsa.find("from Risorsa r where r.dataIn <= '" + MyUtility.dateToString(dataFine, "yyyy-MM-dd") + "' " +
				"and (r.dataOut is null or r.dataOut >= '" + MyUtility.dateToString(dataInizio, "yyyy-MM-dd") + "') " +
				"and r not in (select r from Risorsa r, RendicontoAttivita ra where ra.mese = " + (mese+1) + " and ra.anno = " + anno + " and ra.risorsa = r)");
    	return query.fetch();
	}

	public static List<Risorsa> listRapportiniIncompleti(int mese, int anno) {
		List<Risorsa> listaAnomalie = new ArrayList<Risorsa>();
		List<Risorsa> listaRisorse = Risorsa.findAll();
		for (Risorsa risorsa : listaRisorse) {
			List<RendicontoAttivita> listaRendicontoAttivitas = listaRendicontoAttivitaFatturabili(risorsa,mese + 1,anno);
			List<Commessa> listaCommesse  = Commessa.findCommesseFatturabiliPerRisorsa(mese, anno, risorsa);
			if(listaRendicontoAttivitas.size()<listaCommesse.size()){
				listaAnomalie.add(risorsa);
			}
		}
		return listaAnomalie;
	}
	
	private static List<RendicontoAttivita> listaRendicontoAttivitaFatturabili(Risorsa risorsa, int mese, int anno){
		String queryString = "SELECT ra.* " +
		"FROM RendicontoAttivita ra " +
		"inner join Commessa c on ra.commessa_idCommessa = c.idCommessa " +
		"WHERE ra.risorsa_idRisorsa = " + risorsa.idRisorsa +
		" AND ra.mese = " + mese + " AND ra.anno = " + anno +
		" AND c.calcoloRicavi = true";
		
		Session session = (Session)JPA.em().getDelegate();
		List<RendicontoAttivita> listaRendicontoAttivitas = session.createSQLQuery(queryString).list();
		
		return listaRendicontoAttivitas;
	}
	
	public static ArrayList<HashMap> statisticheCommesseNonFatturabili(String mese,String anno){
		String queryString ="SELECT r.idRisorsa,c.idCommessa,r.codice,r.cognome,c.codice,c.descrizione,sum(ra.oreLavorate)"+
							" from RendicontoAttivita ra                                                                                 "+
							" inner join Commessa c on ra.commessa_idCommessa=c.idCommessa                                               "+
							" inner join Risorsa r on ra.risorsa_idRisorsa=r.idRisorsa                                                   "+
							" inner join RapportoLavoro rl on rl.risorsa_idRisorsa=r.idRisorsa											 "+	
							" where c.calcoloRicavi=false and c.calcoloCosti=false                                                                             	 "+
							" and ra.mese= "
							+mese+
							" and ra.anno= "
							+anno+
							" and rl.tipoRapportoLavoro_idTipoRapportoLavoro != 5 														 "+
							" group by r.idRisorsa,c.idCommessa,r.codice,concat(r.cognome,' ',r.nome),c.codice,c.descrizione "+
							" order by r.cognome,r.nome";
		 Session session = (Session)JPA.em().getDelegate();
		 List<Object[]> resultList = session.createSQLQuery(queryString).list();
		 ArrayList<HashMap> listaMapResult = new ArrayList<HashMap>();
		 Double count = 0.0;
		 Integer personaCorrente = null;
		 HashMap map = null;
		 for (Object[] objects : resultList) {
			 if(personaCorrente != (Integer) objects[0]){
				 count = 0.0;
				 personaCorrente = (Integer) objects[0];
				 map = new HashMap();
				 map.put("idRisorsa", (Integer) objects[0]);
				 map.put("codiceRisorsa", (String) objects[2]);
				 map.put("risorsa", (String) objects[3]);
				 listaMapResult.add(map); 
			 }
			 map.put(""+objects[1], objects[6]);
			 count  += ((Double)objects[6]);
			 map.put("totaleGiorni", MyUtility.calcolaGiorni(count));
		 }
		 return listaMapResult;
	}
	
	public static ArrayList<HashMap> statisticheCommesseNonFatturabiliAnno(String anno){
		String queryString ="SELECT r.idRisorsa,c.idCommessa,r.codice,r.cognome,c.codice,c.descrizione,sum(ra.oreLavorate)"+
							" from RendicontoAttivita ra                                                                                 "+
							" inner join Commessa c on ra.commessa_idCommessa=c.idCommessa                                               "+
							" inner join Risorsa r on ra.risorsa_idRisorsa=r.idRisorsa                                                   "+
							" inner join RapportoLavoro rl on rl.risorsa_idRisorsa=r.idRisorsa											 "+	
							" where c.calcoloRicavi=false and c.calcoloCosti=false                                                                               	 "+
							" and ra.anno= "
							+anno+
							" and rl.tipoRapportoLavoro_idTipoRapportoLavoro != 5 														 "+
							" group by r.idRisorsa,c.idCommessa,r.codice,concat(r.cognome,' ',r.nome),c.codice,c.descrizione "+
							" order by r.cognome,r.nome";
		 Session session = (Session)JPA.em().getDelegate();
		 List<Object[]> resultList = session.createSQLQuery(queryString).list();
		 ArrayList<HashMap> listaMapResult = new ArrayList<HashMap>();
		 Double count = 0.0;
		 Integer personaCorrente = null;
		 HashMap map = null;
		 for (Object[] objects : resultList) {
			 if(personaCorrente != (Integer) objects[0]){
				 count = 0.0;
				 personaCorrente = (Integer) objects[0];
				 map = new HashMap();
				 map.put("idRisorsa", (Integer) objects[0]);
				 map.put("codiceRisorsa", (String) objects[2]);
				 map.put("risorsa", (String) objects[3]);
				 listaMapResult.add(map); 
			 }
			 map.put(""+objects[1], objects[6]);
			 count  += ((Double)objects[6]);
			 map.put("totaleGiorni", MyUtility.calcolaGiorni(count));
		 }
		 return listaMapResult;
	}
	
	public static ArrayList<HashMap> statisticheCommesseNonFatturabiliCollaboratori(String mese,String anno){
		String queryString ="SELECT r.idRisorsa,c.idCommessa,r.codice,r.cognome,c.codice,c.descrizione,sum(ra.oreLavorate)"+
							" from RendicontoAttivita ra                                                                                 "+
							" inner join Commessa c on ra.commessa_idCommessa=c.idCommessa                                               "+
							" inner join Risorsa r on ra.risorsa_idRisorsa=r.idRisorsa                                                   "+
							" inner join RapportoLavoro rl on rl.risorsa_idRisorsa=r.idRisorsa											 "+	
							" where c.calcoloRicavi=false                                                                               	 "+
							" and ra.mese= "
							+mese+
							" and ra.anno= "
							+anno+
							" and rl.tipoRapportoLavoro_idTipoRapportoLavoro = 5 														 "+
							" and c.flagCoCoPro = true 														 "+
							" group by r.idRisorsa,c.idCommessa,r.codice,concat(r.cognome,' ',r.nome),c.codice,c.descrizione "+
							" order by r.cognome,r.nome";
		 Session session = (Session)JPA.em().getDelegate();
		 List<Object[]> resultList = session.createSQLQuery(queryString).list();
		 ArrayList<HashMap> listaMapResult = new ArrayList<HashMap>();
		 Double count = 0.0;
		 Integer personaCorrente = null;
		 HashMap map = null;
		 for (Object[] objects : resultList) {
			 if(personaCorrente != (Integer) objects[0]){
				 count = 0.0;
				 personaCorrente = (Integer) objects[0];
				 map = new HashMap();
				 map.put("idRisorsa", (Integer) objects[0]);
				 map.put("codiceRisorsa", (String) objects[2]);
				 map.put("risorsa", (String) objects[3]);
				 listaMapResult.add(map); 
			 }
			 map.put(""+objects[1], objects[6]);
			 count  += ((Double)objects[6]);
			 map.put("totaleGiorni", MyUtility.calcolaGiorni(count));
		 }
		 return listaMapResult;
	}
	
	public static ArrayList<HashMap> statisticheCommesseNonFatturabiliCollaboratoriAnno(String anno){
		String queryString ="SELECT r.idRisorsa,c.idCommessa,r.codice,r.cognome,c.codice,c.descrizione,sum(ra.oreLavorate)"+
							" from RendicontoAttivita ra                                                                                 "+
							" inner join Commessa c on ra.commessa_idCommessa=c.idCommessa                                               "+
							" inner join Risorsa r on ra.risorsa_idRisorsa=r.idRisorsa                                                   "+
							" inner join RapportoLavoro rl on rl.risorsa_idRisorsa=r.idRisorsa											 "+	
							" where c.calcoloRicavi=false                                                                                 	 "+
							" and ra.anno= "
							+anno+
							" and rl.tipoRapportoLavoro_idTipoRapportoLavoro = 5 														 "+
							" and c.flagCoCoPro = true 														 							 "+
							" group by r.idRisorsa,c.idCommessa,r.codice,concat(r.cognome,' ',r.nome),c.codice,c.descrizione "+
							" order by r.cognome,r.nome";
		 Session session = (Session)JPA.em().getDelegate();
		 List<Object[]> resultList = session.createSQLQuery(queryString).list();
		 ArrayList<HashMap> listaMapResult = new ArrayList<HashMap>();
		 Double count = 0.0;
		 Integer personaCorrente = null;
		 HashMap map = null;
		 for (Object[] objects : resultList) {
			 if(personaCorrente != (Integer) objects[0]){
				 count = 0.0;
				 personaCorrente = (Integer) objects[0];
				 map = new HashMap();
				 map.put("idRisorsa", (Integer) objects[0]);
				 map.put("codiceRisorsa", (String) objects[2]);
				 map.put("risorsa", (String) objects[3]);
				 listaMapResult.add(map); 
			 }
			 map.put(""+objects[1], objects[6]);
			 count  += ((Double)objects[6]);
			 map.put("totaleGiorni", MyUtility.calcolaGiorni(count));
		 }
		 return listaMapResult;
	}
	
	public static ArrayList<HashMap> statisticheDettaglioAssenzaRetribuitaCollaboratori(String anno){
		String queryString ="SELECT r.idRisorsa,c.idCommessa,r.codice,r.cognome,c.codice,c.descrizione,sum(ra.oreLavorate)   "+
							" from RendicontoAttivita ra                                                                                 "+
							" inner join Commessa c on ra.commessa_idCommessa=c.idCommessa                                               "+
							" inner join Risorsa r on ra.risorsa_idRisorsa=r.idRisorsa                                                   "+
							" inner join RapportoLavoro rl on rl.risorsa_idRisorsa=r.idRisorsa											 "+	
							" where c.calcoloRicavi=false and c.calcoloCosti=true                                                                                	 "+
							" and ra.anno= "
							+anno+
							" and rl.tipoRapportoLavoro_idTipoRapportoLavoro = 5 														 "+
							" and c.flagCoCoPro = true 														 							 "+
							" and c.idCommessa = 91 														 							 "+
							" group by r.idRisorsa,c.idCommessa,r.codice,concat(r.cognome,' ',r.nome),c.codice,c.descrizione "+
							" order by r.cognome,r.nome";
		 Session session = (Session)JPA.em().getDelegate();
		 List<Object[]> resultList = session.createSQLQuery(queryString).list();
		 ArrayList<HashMap> listaMapResult = new ArrayList<HashMap>();
		 Double count = 0.0;
		 Integer personaCorrente = null;
		 HashMap map = null;
		 for (Object[] objects : resultList) {
			 if(personaCorrente != (Integer) objects[0]){
				 count = 0.0;
				 personaCorrente = (Integer) objects[0];
				 map = new HashMap();
				 map.put("idRisorsa", (Integer) objects[0]);
				 map.put("codiceRisorsa", (String) objects[2]);
				 map.put("risorsa", (String) objects[3]);
				 listaMapResult.add(map); 
			 }
			 map.put(""+objects[1], objects[6]);
			 count  += ((Double)objects[6]);
			 map.put("totaleGiorni", MyUtility.calcolaGiorni(count));
		 }
		 return listaMapResult;
	}
	
}

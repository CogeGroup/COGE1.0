package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.GenericModel;
import utility.MyUtility;

@javax.persistence.Entity
public class RendicontoAttivita extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRendicontoAttivita;
	
	@Required
	public Float oreLavorate;
	
	public int mese;
	
	public int anno;
	
	@ManyToOne
	public Risorsa risorsa;
	
	@ManyToOne
	public Commessa commessa;

	public RendicontoAttivita() {}
	
	public RendicontoAttivita(Float oreLavorate, int mese, int anno,
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
    	JPAQuery query  = Risorsa.find("from Risorsa r where r not in (select r from Risorsa r, RendicontoAttivita ra where ra.mese = " + (mese+1) + " and ra.anno = " + anno + " and ra.risorsa = r)");
    	return query.fetch();
	}

	public static List<Risorsa> listRapportiniIncompleti(int mese, int anno) {
		List<Risorsa> listaAnomalie = new ArrayList<Risorsa>();
		if(MyUtility.MeseEdAnnoToDataFine(mese, anno).after(new Date())){
			return listaAnomalie;
		}
		List<Risorsa> listaRisorse = Risorsa.findAll();
		for (Risorsa risorsa : listaRisorse) {
			List<RendicontoAttivita> listaRendicontoAttivitas = RendicontoAttivita.find("byRisorsaAndMeseAndAnno",risorsa,mese,anno).fetch();
			List<Commessa> listaCommesse  = Commessa.findCommesseFatturabiliPerRisorsa(mese, anno, risorsa);
			if(listaRendicontoAttivitas.size()<listaCommesse.size()){
				listaAnomalie.add(risorsa);
			}
		}
		return listaAnomalie;
	}
}

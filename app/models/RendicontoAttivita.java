package models;

import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.hql.classic.GroupByParser;

import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;

@javax.persistence.Entity
public class RendicontoAttivita extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRendicontoAttivita;
	
	@Required
	public Integer oreLavorate;
	
	
	public int mese;
	
	public int anno;
	
	@ManyToOne
	public Risorsa risorsa;
	
	@ManyToOne
	public Commessa commessa;

	public RendicontoAttivita() {}
	
	public RendicontoAttivita(int oreLavorate, int mese, int anno,
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

}

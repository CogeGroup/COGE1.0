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

import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;

@javax.persistence.Entity
public class RendicontoAttivita extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRendicontoAttivita;
	
	@Required(message="Inserire il corretto valore")
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
	

	public static List<RendicontoAttivita> findByExample(Integer idRisorsa,String data) {
		Session session = (Session)JPA.em().getDelegate();
		Criteria cri = session.createCriteria(RendicontoAttivita.class);
		if(idRisorsa != null && idRisorsa > 0){
			cri.add(Restrictions.eq("risorsa", Risorsa.findById(idRisorsa)));
		}
		if(data != null && !data.equals("")){
			String[] meseAnno = data.split("-");
			cri.add(Restrictions.eq("mese", Integer.parseInt(meseAnno[0].trim())));
			cri.add(Restrictions.eq("anno", Integer.parseInt(meseAnno[1].trim())));
		}
		cri.addOrder(Order.asc("anno"));
		cri.addOrder(Order.asc("mese"));
		List<RendicontoAttivita> listaRapportini = cri.list();
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

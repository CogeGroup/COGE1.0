package models;

import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class RendicontoAttivita extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRendicontoAttivita;
	
	public int oreLavorate;
	
	
	public String mese;
	
	public String anno;
	
	@ManyToOne
	public Risorsa risorsa;
	
	@ManyToOne
	public Commessa commessa;

	public RendicontoAttivita(int oreLavorate, String mese, String anno,
			Risorsa risorsa, Commessa commessa) {
		this.oreLavorate = oreLavorate;
		this.mese = mese;
		this.anno = anno;
		this.risorsa = risorsa;
		this.commessa = commessa;
	}

}

package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class RapportoAttivita extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRapportoAttivita;
	
	public String mese;

	public String anno;
	
	public boolean approvato;
	
	@ManyToOne
	public Risorsa risorsa;
	
	@OneToMany(mappedBy="rapportoAttivita",cascade=CascadeType.ALL)
	public List <DettaglioRapportoAttivita> dettagliRapportoAttivita = new ArrayList<DettaglioRapportoAttivita>();

	public RapportoAttivita(String mese,
			String anno, boolean approvato, Risorsa risorsa) {
		super();
		this.mese = mese;
		this.anno = anno;
		this.approvato = approvato;
		this.risorsa = risorsa;
	}
	
	public void addDettRappAtt(DettaglioRapportoAttivita dt){
		dettagliRapportoAttivita.add(dt);
		dt.rapportoAttivita = this;
	}
	
	
	
}

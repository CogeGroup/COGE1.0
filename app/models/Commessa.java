package models;

import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;


import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;

@javax.persistence.Entity
public class Commessa extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCommessa;
	
	public String descrizione;
	
	public String codice;
	
	public boolean fatturabile;
	
	@ManyToOne
	public Cliente cliente;

	public Commessa(String descrizione, String codice, boolean fatturabile) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
		this.fatturabile = fatturabile;
	}
	
	public Commessa(){
		
		
	}
	
	
	public Date dataInizioCommessa;

	
	public float calcolaRicavo(String mese, String anno) {
		
		float importoTotale = 0.0f;
		JPAQuery query  = DettaglioRapportoAttivita.find("from DettaglioRapportoAttivita dra where dra.commessa=:commessa and dra.rapportoAttivita.mese=:rapMese and dra.rapportoAttivita.anno=:rapAnno");
		query.bind("commessa",this);
		query.bind("rapMese",mese);
		query.bind("rapAnno",anno);
		List<DettaglioRapportoAttivita> lista = query.fetch();
		
		for(DettaglioRapportoAttivita index:lista){
			
			Tariffa t = Tariffa.calcolaTariffaRisorsaCommessa(mese, anno, index.rapportoAttivita.risorsa,index.commessa);
			importoTotale += t.calcolaRicavoTariffa(index.oreLavorate);
		}
		
		
		return importoTotale;
	}
	
	
	
	

	
}

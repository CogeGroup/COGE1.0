package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;

@javax.persistence.Entity
public class Cliente extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCliente;
	
	public String codice;
	
	public String nominativo;
	
	@OneToMany(mappedBy="cliente", cascade=CascadeType.ALL)
	public List<Commessa> commesse = new ArrayList<Commessa>();

	public Cliente(String codice, String nominativo) {
		super();
		this.codice = codice;
		this.nominativo = nominativo;
	}

	public float calcolaRicavo(String mese, String anno) {
		float importoTotale = 0.0f;
		JPAQuery query  = DettaglioRapportoAttivita.find("from DettaglioRapportoAttivita dra where dra.commessa.cliente=:cliente and dra.rapportoAttivita.mese=:rapMese and dra.rapportoAttivita.anno=:rapAnno");
		query.bind("cliente",this);
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

package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;


import play.data.binding.As;
import play.data.validation.InFuture;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;

@javax.persistence.Entity
public class Commessa extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCommessa;
	
	@Required(message="Codice obligatorio")
	public String codice;

	@Required(message="Descrizione obligatoria")
	public String descrizione;
	
	public boolean fatturabile;
	
/* Per il momento la data non è obligatoria, perchè nel caso in cui le commesse sono ferie, malattia, ecc 
 * non hanno una data di inizio.
 */
//	@Required(message="Data obligatoria")
	@As("dd-MM-yyyy")
	public Date dataInizioCommessa;
	
	public boolean attivo;
	
	@ManyToOne
	public Cliente cliente;
	
	@OneToMany(mappedBy="commessa",cascade=CascadeType.ALL)
	public List<Tariffa> tariffe;
	
	public Commessa(){}

	public Commessa(String descrizione, String codice, boolean fatturabile) {
		super();
		this.descrizione = descrizione;
		this.codice = codice;
		this.fatturabile = fatturabile;
	}
	
	public float calcolaRicavo(String mese, String anno) {
		
		float importoTotale = 0.0f;
//		JPAQuery query  = DettaglioRapportoAttivita.find("from DettaglioRapportoAttivita dra where dra.commessa=:commessa and dra.rapportoAttivita.mese=:rapMese and dra.rapportoAttivita.anno=:rapAnno");
		JPAQuery query  = RendicontoAttivita.find("from RendicontoAttivita ra where ra.commessa=:commessa and ra.mese=:rapMese and ra.anno=:rapAnno");
		query.bind("commessa",this);
		query.bind("rapMese",mese);
		query.bind("rapAnno",anno);
//		List<DettaglioRapportoAttivita> lista = query.fetch();
		List<RendicontoAttivita> lista = query.fetch();
		
//		for(DettaglioRapportoAttivita index:lista){
//			
//			Tariffa t = Tariffa.calcolaTariffaRisorsaCommessa(mese, anno, index.rapportoAttivita.risorsa,index.commessa);
//			importoTotale += t.calcolaRicavoTariffa(index.oreLavorate);
//		}
		for(RendicontoAttivita index:lista){
			
			Tariffa t = Tariffa.calcolaTariffaRisorsaCommessa(mese, anno, index.risorsa,index.commessa);
			importoTotale += t.calcolaRicavoTariffa(index.oreLavorate);
		}
		
		return importoTotale;
	}
	
	// chiude tutte le commesse di un cliente e le relative tariffe
	public static void chiudiCommesseByCliente(Cliente cliente) {
		for (Commessa commessa : cliente.commesse) {
		   	commessa.attivo = false;
		   	Tariffa.chiudiTariffeByCommessa(commessa);
		   	commessa.save();
		}
	}
	
}

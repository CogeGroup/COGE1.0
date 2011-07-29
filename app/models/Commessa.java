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
	
	@As("dd-MM-yyyy")
	public Date dataFineCommessa;
	
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
		Integer meseInt = Integer.parseInt(mese);
		Integer annoInt = Integer.parseInt(anno);
		JPAQuery query  = RendicontoAttivita.find("from RendicontoAttivita ra where ra.commessa = :commessa and ra.mese = "+meseInt+" and ra.anno = "+annoInt);
		query.bind("commessa",this);
		List<RendicontoAttivita> lista = query.fetch();

		for(RendicontoAttivita index:lista){
			
			Tariffa t = Tariffa.calcolaTariffaRisorsaCommessa(mese, anno, index.risorsa,index.commessa);
			importoTotale += t.calcolaRicavoTariffa(index.oreLavorate);
		}
		
		return importoTotale;
	}
	
	// chiude tutte le commesse di un cliente e le relative tariffe
	public static void chiudiCommesseByCliente(Cliente cliente) {
		for (Commessa commessa : cliente.commesse) {
			if(commessa.dataInizioCommessa != null){
			   	if (commessa.dataFineCommessa != null) {
	    			if(commessa.dataInizioCommessa.after(new Date())){
	    				commessa.dataFineCommessa = commessa.dataInizioCommessa;
	    			}else{
	    				commessa.dataFineCommessa = new Date();
	    			}
				} else {
					if(commessa.dataInizioCommessa.after(new Date())){
		    			commessa.dataFineCommessa = commessa.dataInizioCommessa;
		    		}else{
		    			commessa.dataFineCommessa = new Date();
		    		}
				}
			   	commessa.save();
			}
		   	Tariffa.chiudiTariffeByCommessa(commessa);
		}
	}
	
}

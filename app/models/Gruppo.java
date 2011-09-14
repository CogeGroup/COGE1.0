package models;

import play.*;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.*;

@Entity
public class Gruppo extends GenericModel {
    
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idGruppo;
	
	@Required
	@MinSize(2)
	@CheckWith(CodiceCheck.class)
	public String codice;
	
	@Required
	public String descrizione;
	
	@OneToMany (mappedBy="gruppo")
	public List<Risorsa> risorse;
	
	@ManyToMany
	public List<Commessa> commesse;
	
	public Gruppo() {}

	public Gruppo(String codice, String descrizione) {
		super();
		this.codice = codice;
		this.descrizione = descrizione;
	}
	
	// Validazione
	static class CodiceCheck extends Check {
		static final String message = "validation.gruppo.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object gruppo, Object codice) {
			Gruppo g = Gruppo.find("byCodice", codice).first();
			if(g != null && ((Gruppo) gruppo).idGruppo != g.idGruppo) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	public static List<Gruppo> findByCommessa(Commessa commessa) {
		List<Gruppo> list = Gruppo.findAll();
		List<Gruppo> listaGruppi = new ArrayList<Gruppo>();
		for (Gruppo gruppo : list) {
			if (gruppo.commesse.contains(commessa)){
				listaGruppi.add(gruppo);
			}
		}
		return listaGruppi;
	}
	
}

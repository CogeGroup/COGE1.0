package models;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class Utente extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idUtente;
	
	@Required
	@CheckWith(UsernameCheck.class)
	@MaxSize(255)
	public String username;
	
	@Required
	@MinSize(8)
	@MaxSize(255)
	public String password;
	
	@Required
	public boolean abilitato;
	
	@ManyToMany 
	public List<Ruolo> ruoli = new ArrayList<Ruolo>();
	 
    
	@OneToOne
	public Risorsa risorsa;
	
	

  //constructors
	public Utente(String username, String password, boolean abilitato) {
		super();
		this.username = username;
		this.password = password;
		this.abilitato = abilitato;
	}
	
	public Utente() {}
	
	public void addRuolo(Ruolo ruolo){
		
		ruoli.add(ruolo);
		ruolo.addUtente(this);
		
	}
	
	public boolean isUserInRole(String descriRole){
		for (Ruolo r: ruoli){
			
			if (r.descrizione.equals(descriRole))
				return true;
			
		}
		
		return false;
	}
	
	
	public boolean isAuthorized(String path){
		for (Ruolo r: ruoli){
			for (models.Job j : r.jobs){
				String ctrlName[] = path.split("/");
				String pathAuthorized[] = j.descrizione.split("/");
				if (pathAuthorized[1].equals("*") && ctrlName[0].equals(pathAuthorized[0])){
					return true;
				}
				if (j.descrizione.equals(path))
					return true;
				
			}
		
			
		}
		
		return false;
	}
	
	static class UsernameCheck extends Check {
		static final String message = "validation.utente.username_esistente";
		
		@Override
		public boolean isSatisfied(Object utente, Object username) {
			Utente app = Utente.find("byUsername", username).first();
			if(app != null && ((Utente) utente).idUtente!= app.idUtente) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}

}

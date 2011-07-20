package models;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.Model;

@javax.persistence.Entity
public class Utente extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idUtente;
	
	@Required(message="username obbligatorio")
	@MaxSize(255)
	public String username;
	
	@Required(message="password obbligatoria")
	@MinSize(8)
	@MaxSize(255)
	public String password;
	
	@Required(message="scegliere abilitato")
	public boolean abilitato;
	
	@ManyToMany (cascade=CascadeType.ALL)
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
	
	public void addRuolo(Ruolo ruolo){
		
		ruoli.add(ruolo);
		ruolo.addUtente(this);
		
	}
	
	


	 
 
	

}

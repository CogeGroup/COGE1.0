package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;

@javax.persistence.Entity
public class Ruolo extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)

    public Integer idRuolo;
	
	@Required
	@MaxSize(255)
	public String descrizione;
	
	
	
	 @ManyToMany(mappedBy="ruoli")
	 List<Utente> utenti = new ArrayList<Utente>();
	 
	
	 @ManyToMany(mappedBy="ruoli")
	 public List<Job> jobs = new ArrayList<Job>();

	//constructors
	public Ruolo(String descrizione) {
		super();
		this.descrizione = descrizione;
	
	}

	public void addUtente(Utente utente){
		
		utenti.add(utente);
		
	}
	

	public void addJob(models.Job job){
		
		jobs.add(job);
		
		
	}

public void remove() {
	
     for (Job j: this.jobs){
    	 j.ruoli.remove(this);
    	 j.save();	 
     }
     for (Utente u:this.utenti){
    	 u.ruoli.remove(this);
    	 u.save();
     }
    
     this.delete();
     
	
	
}


	
	
	
	
	

}

package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import play.db.jpa.GenericModel;

@javax.persistence.Entity
public class Job extends GenericModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer idJob;
	
	
	public Job(String descrizione){
		super();
		this.descrizione = descrizione;
		
	}
	
	public Job(){
		super();
		
	}
	
	
	public String descrizione;
	
	 @ManyToMany
	 public List<Ruolo> ruoli = new ArrayList<Ruolo>();

}

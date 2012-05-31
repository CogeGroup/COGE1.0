package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.Required;
import play.db.jpa.GenericModel;

@Entity
public class GiornateLavorative extends GenericModel {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idGiornateLavorative;
	
	@Required
	public int mese;
	
	@Required
	public int anno;
	
	@Required
	public int giorni;
	
	public GiornateLavorative(){
	}

	public GiornateLavorative(int mese, int anno, int giorni) {
		this.mese = mese;
		this.anno = anno;
		this.giorni = giorni;
	}
    
}

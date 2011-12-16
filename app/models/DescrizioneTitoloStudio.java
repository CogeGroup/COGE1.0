package models;

import play.*;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class DescrizioneTitoloStudio extends GenericModel {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idDescrizioneTitoloStudio;
	
	@Required
	public String descrizione;
	
	@ManyToOne
	public TipoTitoloStudio tipoTitoloStudio;
	
	public DescrizioneTitoloStudio() {
		// TODO Auto-generated constructor stub
	}

	public DescrizioneTitoloStudio(String descrizione,
			TipoTitoloStudio tipoTitoloStudio) {
		this.descrizione = descrizione;
		this.tipoTitoloStudio = tipoTitoloStudio;
	}
    
}

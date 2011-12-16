package models;

import play.*;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class TipoTitoloStudio extends GenericModel {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idTipoTitoloStudio;
	
	@Required
	public String descrizione;
	
	@OneToMany (mappedBy="tipoTitoloStudio", cascade=CascadeType.ALL)
	List<DescrizioneTitoloStudio> listaDescrizioneTitoloStudio;
	
	public TipoTitoloStudio() {
		// TODO Auto-generated constructor stub
	}

	public TipoTitoloStudio(String descrizione) {
		this.descrizione = descrizione;
		this.listaDescrizioneTitoloStudio = new ArrayList<DescrizioneTitoloStudio>();
	}
	
	
}

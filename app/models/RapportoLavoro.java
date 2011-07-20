package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;

@javax.persistence.Entity
public class RapportoLavoro extends GenericModel{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idRapportoLavoro;
	
	public Date dataInizio;
	
	public Date dataFine;
	
	@ManyToOne
	TipoRapportoLavoro tipoRapportoLavoro;
	
	
	@ManyToOne
	public Risorsa risorsa;


	public RapportoLavoro(Date dataInizio,
			TipoRapportoLavoro tipoRapportoLavoro, Risorsa risorsa) {
		super();
		this.dataInizio = dataInizio;
		this.tipoRapportoLavoro = tipoRapportoLavoro;
		this.risorsa = risorsa;
	}


   public static List<RapportoLavoro> findByTipoRapportoLavoroAndPeriodo(TipoRapportoLavoro tipoRapLav , String mese, String anno) throws ParseException{
	   Date dataRapporto = new SimpleDateFormat("dd/MM/yyyy").parse("01/" + mese + "/" + anno);
	   JPAQuery query = RapportoLavoro.find("from RapportoLavoro ral where ral.tipoRapportoLavoro = :tipoRapportoLavoro and ral.dataInizio <= :dataRapporto and (ral.dataFine is null or ral.dataFine >= :dataRapporto)");
	   query.bind("tipoRapportoLavoro", tipoRapLav);
	   query.bind("dataRapporto", dataRapporto);
	   List<RapportoLavoro> list  =  query.fetch();
	   System.out.println("....SIZE:" + list.size());
	   return list;
   }
	
	
	
	
}

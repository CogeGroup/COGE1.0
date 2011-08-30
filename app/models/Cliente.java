package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import models.Commessa.CodiceCheck;

import org.hibernate.Session;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPA;

@javax.persistence.Entity
public class Cliente extends GenericModel{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCliente;
	
	@Required
	@MinSize(2)
	@CheckWith(CodiceCheck.class)
	public String codice;
	
	@Required
	public String nominativo;
	
	public boolean attivo;
	
	@OneToMany(mappedBy="cliente", cascade=CascadeType.ALL)
	public List<Commessa> commesse = new ArrayList<Commessa>();
	
	// Costruttori
	public Cliente() {}
	
	public Cliente(String codice, String nominativo, boolean attivo) {
		this.codice = codice;
		this.nominativo = nominativo;
		this.attivo = attivo;
	}
	
	// Validazione
	static class CodiceCheck extends Check {
		static final String message = "validation.cliente.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object cliente, Object codice) {
			Cliente app = Cliente.find("byCodice", codice).first();
			if(app != null && ((Cliente) cliente).idCliente != app.idCliente) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	public static List<Cliente> findAllAttivo(){
		return Cliente.find("select cl from Cliente cl where cl.attivo is true order by codice asc").fetch();
	}
	
	public float calcolaRicavo(int mese, int anno) {
		float importoTotale = 0.0f;
		JPAQuery query  = DettaglioRapportoAttivita.find("from DettaglioRapportoAttivita dra where dra.commessa.cliente=:cliente and dra.rapportoAttivita.mese=:rapMese and dra.rapportoAttivita.anno=:rapAnno");
		query.bind("cliente",this);
		query.bind("rapMese",mese);
		query.bind("rapAnno",anno);
		
		List<DettaglioRapportoAttivita> lista = query.fetch();
		for(DettaglioRapportoAttivita index:lista){
			Tariffa t = Tariffa.findByRisorsaAndCommessaAndData(mese, anno, index.rapportoAttivita.risorsa,index.commessa);
			importoTotale += t.calcolaRicavoTariffa(index.oreLavorate);
		}
		return importoTotale;
	}
	
	//test query statistiche clienti
	public static ArrayList<HashMap> statisticheClienti(int anno){
		String queryString = "select cl.nominativo, "+
			"sum(ricavo(ra.oreLavorate,t.importoGiornaliero)) as ricavo_Totale, "+
			"sum(costo(ra.oreLavorate,costo.importo)) as costo_Totale, "+
			"sum(if(ra.mese <= 6, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_primo_semestre, "+
			"sum(if(ra.mese > 6, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_secondo_semestre, "+
			"sum(if(ra.mese <= 6, costo(ra.oreLavorate,costo.importo), 0)) as costo_primo_semestre, "+
			"sum(if(ra.mese > 6, costo(ra.oreLavorate,costo.importo), 0)) as costo_secondo_semestre, "+
			"sum(if(ra.mese = 1, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_gennaio, "+
			"sum(if(ra.mese = 2, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_febbraio, "+
			"sum(if(ra.mese = 3, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_marzo, "+
			"sum(if(ra.mese = 4, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_aprile, "+
			"sum(if(ra.mese = 5, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_maggio, "+
			"sum(if(ra.mese = 6, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_giugno, "+
			"sum(if(ra.mese = 7, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_luglio, "+
			"sum(if(ra.mese = 8, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_agosto, "+
			"sum(if(ra.mese = 9, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_settembre, "+
			"sum(if(ra.mese = 10, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_ottobre, "+
			"sum(if(ra.mese = 11, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_novembre, "+
			"sum(if(ra.mese = 12, ricavo(ra.oreLavorate,t.importoGiornaliero), 0)) as ricavo_dicembre, "+
			"sum(if(ra.mese = 1, costo(ra.oreLavorate,costo.importo), 0)) as costo_gennaio, "+
			"sum(if(ra.mese = 2, costo(ra.oreLavorate,costo.importo), 0)) as costo_febbraio, "+
			"sum(if(ra.mese = 3, costo(ra.oreLavorate,costo.importo), 0)) as costo_marzo, "+
			"sum(if(ra.mese = 4, costo(ra.oreLavorate,costo.importo), 0)) as costo_aprile, "+
			"sum(if(ra.mese = 5, costo(ra.oreLavorate,costo.importo), 0)) as costo_maggio, "+
			"sum(if(ra.mese = 6, costo(ra.oreLavorate,costo.importo), 0)) as costo_giugno, "+
			"sum(if(ra.mese = 7, costo(ra.oreLavorate,costo.importo), 0)) as costo_luglio, "+
			"sum(if(ra.mese = 8, costo(ra.oreLavorate,costo.importo), 0)) as costo_agosto, "+
			"sum(if(ra.mese = 9, costo(ra.oreLavorate,costo.importo), 0)) as costo_settembre, "+
			"sum(if(ra.mese = 10, costo(ra.oreLavorate,costo.importo), 0)) as costo_ottobre, "+
			"sum(if(ra.mese = 11, costo(ra.oreLavorate,costo.importo), 0)) as costo_novembre, "+
			"sum(if(ra.mese = 12, costo(ra.oreLavorate,costo.importo), 0)) as costo_dicembre "+
			"FROM RendicontoAttivita ra "+
			"inner join Commessa com on ra.commessa_idCommessa=com.idCommessa "+
			"inner join Risorsa ris on ra.risorsa_idRisorsa=ris.idRisorsa "+
			"inner join Cliente cl on  com.cliente_idCliente=cl.idCliente "+
			"inner join Tariffa t on  (t.commessa_idCommessa = ra.commessa_idCommessa and t.risorsa_idRisorsa = ra.risorsa_idRisorsa)" +
			"inner join Costo costo on   costo.risorsa_idRisorsa = ris.idRisorsa "+
			"where com.DTYPE = 'Commessa' and com.fatturabile=1 and ra.anno="+anno+
			" and concat(ra.anno, lpad(ra.mese, 2, 0)) >= date_format(costo.dataInizio, '%Y%m')" +
			" and (concat(ra.anno, lpad(ra.mese, 2, 0)) <= date_format(costo.dataFine, '%Y%m') or costo.dataFine is null)" +
			" and concat(ra.anno, lpad(ra.mese, 2, 0)) >= date_format(t.dataInizio, '%Y%m')" +
			" and (concat(ra.anno, lpad(ra.mese, 2, 0)) <= date_format(t.dataFine, '%Y%m') or t.dataFine is null)" +
			" group by  cl.nominativo";
	
			Session session = (Session)JPA.em().getDelegate();
			List<Object[]> resultList = session.createSQLQuery(queryString).list();
			ArrayList<HashMap> listaMapResult = new ArrayList<HashMap>();
			
			for (Object[] objects : resultList) {
				HashMap map = new HashMap();
				map.put("nominativo", (String) objects[0]);
				map.put("ricavo_Totale", (Double) objects[1]);
				map.put("costo_Totale", (Double) objects[2]);
				map.put("ricavo_primo_semestre", (Double) objects[3]);
				map.put("ricavo_secondo_semestre", (Double) objects[4]);
				map.put("costo_primo_semestre", (Double) objects[5]);
				map.put("costo_secondo_semestre", (Double) objects[6]);
				map.put("ricavo_gennaio", (Double) objects[7]);
				map.put("ricavo_febbraio", (Double) objects[8]);
				map.put("ricavo_marzo", (Double) objects[9]);
				map.put("ricavo_aprile", (Double) objects[10]);
				map.put("ricavo_maggio", (Double) objects[11]);
				map.put("ricavo_giugno", (Double) objects[12]);
				map.put("ricavo_luglio", (Double) objects[13]);
				map.put("ricavo_agosto", (Double) objects[14]);
				map.put("ricavo_settembre", (Double) objects[15]);
				map.put("ricavo_ottobre", (Double) objects[16]);
				map.put("ricavo_novembre", (Double) objects[17]);
				map.put("ricavo_dicembre", (Double) objects[18]); 
				map.put("costo_gennaio", (Double) objects[19]);
				map.put("costo_febbraio", (Double) objects[20]);
				map.put("costo_marzo", (Double) objects[21]);
				map.put("costo_aprile", (Double) objects[22]);
			 	map.put("costo_maggio", (Double) objects[23]);
			 	map.put("costo_giugno", (Double) objects[24]);
			 	map.put("costo_luglio", (Double) objects[25]);
			 	map.put("costo_agosto", (Double) objects[26]);
			 	map.put("costo_settembre", (Double) objects[27]);
			 	map.put("costo_ottobre", (Double) objects[28]);
			 	map.put("costo_novembre", (Double) objects[29]);
			 	map.put("costo_dicembre", (Double) objects[30]);
			 	
			 	listaMapResult.add(map); 
			}
		return listaMapResult;
	}
}
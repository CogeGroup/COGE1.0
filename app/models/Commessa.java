package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;

import play.data.binding.As;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import utility.MyUtility;

@javax.persistence.Entity
public class Commessa extends GenericModel implements Comparable<Commessa> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer idCommessa;
	
	@Required
	@MinSize(2)
	@CheckWith(CodiceCheck.class)
	public String codice;

	@Required
	public String descrizione;
	
	public boolean calcoloCosti;
	
	@CheckWith(CalcoloRicavi.class)
	public boolean calcoloRicavi;
	
	@As("dd-MM-yyyy")
	public Date dataInizioCommessa;
	
	@As("dd-MM-yyyy")
	@CheckWith(DataFineCheck.class)
	public Date dataFineCommessa;
	
	@ManyToOne
	public TipoCommessa tipoCommessa;
	
	@ManyToOne
	public Cliente cliente;
	
	@OneToMany(mappedBy="commessa",cascade=CascadeType.ALL)
	public List<Tariffa> tariffe;
	
	// Costruttori
	public Commessa(){
		this.cliente = new Cliente();
	}

	public Commessa(String descrizione, String codice, boolean calcoloCosti, boolean calcoloRicavi) {
		this.descrizione = descrizione;
		this.codice = codice;
		this.calcoloCosti = calcoloCosti;
		this.calcoloRicavi = calcoloRicavi;
	}
	
	// Validazione
	static class CodiceCheck extends Check {
		static final String message = "validation.commessa.codice_esistente";
		
		@Override
		public boolean isSatisfied(Object commessa, Object codice) {
			Commessa app = Commessa.find("byCodice", codice).first();
			if(app != null && ((Commessa) commessa).idCommessa != app.idCommessa) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	static class CalcoloRicavi extends Check {
		static final String message = "validation.commessa.calcoloRicavi";
		
		@Override
		public boolean isSatisfied(Object commessa, Object calcoloRicavi) {
			if(((Commessa) commessa).calcoloRicavi == true && ((Commessa) commessa).calcoloCosti == false) {
				setMessage(message);
				return false;
			}
			return true;
		}
	}
	
	static class DataFineCheck extends Check {
		static final String MESSAGE_DATA_INIZIO_NULL = "validation.commessa.dataInizio_null";
		static final String MESSAGE_DF_AFTER_DI = "validation.commessa.data_fine_after_dataInizio";
		
		@Override
		public boolean isSatisfied(Object commessa, Object dataFine) {
			
			if(((Commessa) commessa).idCommessa != null && 
					((Commessa) commessa).calcoloRicavi == true && 
					dataFine != null && ((Commessa) commessa).dataInizioCommessa == null) {
					setMessage(MESSAGE_DATA_INIZIO_NULL);
					return false;
		    	}
			else if(((Commessa) commessa).idCommessa != null && 
				((Commessa) commessa).calcoloRicavi == true && 
				dataFine != null && !((Date) dataFine).after(((Commessa) commessa).dataInizioCommessa)) {
				setMessage(MESSAGE_DF_AFTER_DI);
				return false;
	    	}
			return true;
		}
	}
	
	public static List<Commessa> findCommesseFatturabili(){
		return Commessa.find("SELECT com FROM Commessa com WHERE com.calcoloRicavi is true ORDER BY com.codice asc").fetch();
	}
	
	public static List<Commessa> findCommesseNonFatturabili(){
		return Commessa.find("SELECT com FROM Commessa com WHERE com.calcoloRicavi is false and com.calcoloCosti is false ORDER BY com.codice asc").fetch();
	}
	
	public static List<Commessa> findCommesseFatturabiliAttive(){
		return Commessa.find("select cm from Commessa cm where cm.calcoloRicavi is true and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
	}
	
	public static List<Commessa> findCommesseNonFatturabiliAttive(){
		return Commessa.find("select cm from Commessa cm where com.calcoloRicavi is false and com.calcoloCosti is false and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
	}
	
	public static void commessaToCommessaACorpo(Integer id, float importo) {
		Session session = (Session)JPA.em().getDelegate();
		session.createSQLQuery("UPDATE Commessa SET DTYPE = 'CommessaACorpo', importo = " + importo + " WHERE idCommessa = " + id).executeUpdate();
		return;
	}
	
	public static void commessaACorpoToCommessa(Integer id) {
		Session session = (Session)JPA.em().getDelegate();
		session.createSQLQuery("UPDATE Commessa SET DTYPE = 'Commessa', importo = 0 WHERE idCommessa = " + id).executeUpdate();
		return;
	}
	
	public float calcolaRicavo(int mese, int anno) {
		float importoTotale = 0.0f;
		
		JPAQuery query  = RendicontoAttivita.find("from RendicontoAttivita ra where ra.commessa = :commessa and ra.mese = "+mese+" and ra.anno = "+anno);
		query.bind("commessa",this);
		
		List<RendicontoAttivita> lista = query.fetch();
		for(RendicontoAttivita index:lista){
			Tariffa t = Tariffa.findByRisorsaAndCommessaAndData(mese, anno, index.risorsa,index.commessa);
			importoTotale += t.calcolaRicavoTariffa(index.oreLavorate);
		}
		return importoTotale;
	}
	
	// chiude tutte le commesse di un cliente e le relative tariffe
	public static void chiudiCommesseByCliente(Cliente cliente) {
		for (Commessa commessa : cliente.commesse) {
			if(commessa.dataInizioCommessa != null){
			   	if (commessa.dataFineCommessa != null) {
	    			if(commessa.dataInizioCommessa.after(new Date())){
	    				commessa.dataFineCommessa = commessa.dataInizioCommessa;
	    			}else{
	    				commessa.dataFineCommessa = new Date();
	    			}
				} else {
					if(commessa.dataInizioCommessa.after(new Date())){
		    			commessa.dataFineCommessa = commessa.dataInizioCommessa;
		    		}else{
		    			commessa.dataFineCommessa = new Date();
		    		}
				}
			   	commessa.save();
			}
		   	Tariffa.chiudiTariffeByCommessa(commessa);
		}
	}
	
	public static List<Commessa> findCommesseFatturabiliPerRisorsa(int mese,
			int anno, Risorsa risorsa) {
		List<Commessa> listaCommesse = new ArrayList<Commessa>();
		Date dataFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		JPAQuery query = Tariffa.find("from Tariffa t where t.risorsa = :risorsa and t.commessa.calcoloRicavi is true and t.dataInizio <= :dataFine and (t.dataFine is null or t.dataFine >= :dataInizio)");
		query.bind("dataFine", dataFine);
		query.bind("dataInizio", dataInizio);
		query.bind("risorsa",risorsa);
		
		List<Tariffa> listaTariffe = query.fetch();
		if (listaTariffe != null && !listaTariffe.isEmpty()){
		   for(Tariffa t:listaTariffe){
			   List<RapportoLavoro> listaRapportiLavoro = RapportoLavoro.findByRisorsaAndMeseAndAnno(risorsa, mese, anno);
			   for (@SuppressWarnings("unused") RapportoLavoro rapportoLavoro : listaRapportiLavoro) {
				   listaCommesse.add(t.commessa);
			   }
		   }
		}
		return listaCommesse;
	}
	
	public static List<CommessaACorpo> findCommesseACorpo() {
		List<CommessaACorpo> listaCommesseACorpo = new ArrayList<CommessaACorpo>();
		List<Commessa> listaCommesse = Commessa.findAll();
		if (listaCommesse != null && !listaCommesse.isEmpty()){
		   for(Commessa c: listaCommesse){
			   if(c instanceof CommessaACorpo){
				   listaCommesseACorpo.add((CommessaACorpo) c);
			   }
		   }
		}
		 return listaCommesseACorpo;
	}
	
	public static List<CommessaACorpo> findCommesseACorpoPerRisorsa(int mese,
			int anno, Risorsa risorsa) {
		List<CommessaACorpo> listaCommesse = new ArrayList<CommessaACorpo>();
		Date dataRapportoFine = MyUtility.MeseEdAnnoToDataFine(mese, anno);
		Date dataRapportoInizio = MyUtility.MeseEdAnnoToDataInizio(mese, anno);
		
		JPAQuery query = Tariffa.find("from Tariffa t where t.risorsa = :risorsa and t.commessa.calcoloRicavi is true and t.dataInizio <= :dataRapportoFine and (t.dataFine is null or t.dataFine >= :dataRapportoInizio)");
		query.bind("dataRapportoFine", dataRapportoFine);
		query.bind("dataRapportoInizio", dataRapportoInizio);
		query.bind("risorsa",risorsa);
		
		List<Tariffa> listaTariffe = query.fetch();
		if (listaTariffe != null && !listaTariffe.isEmpty()){
		   for(Tariffa t:listaTariffe){
			   if(t.commessa instanceof CommessaACorpo){
				   listaCommesse.add((CommessaACorpo) t.commessa);
			   }
		   }
		}
		return listaCommesse;
	}

	@Override
	public int compareTo(Commessa commessa) {
		if(commessa!=null)
			return descrizione.compareToIgnoreCase(commessa.descrizione);
		return 0;
	}
	
	public static List<Map> prepareReportCommesseClienti(Integer anno) {
		List<Map> resultSet = new ArrayList<Map>();
		List<Commessa> listaComesse = Commessa.findCommesseFatturabili();
		for(Commessa c : listaComesse) {
			Map result = new HashMap();
			List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byAnnoAndCommessa", anno, c).fetch();
			String staff = "";
			float[] tariffaTot = new float[12];
			float[] costoTot = new float[12];
			result.put("nominativo", c.cliente.nominativo);
			result.put("descrizione", c.descrizione);
			result.put("codice", c.codice);
			result.put("tipo", c.tipoCommessa.idTipoCommessa);
			for(RendicontoAttivita ra : listaRapportini) {
				staff += ra.risorsa.cognome + ",";
				Tariffa t = new Tariffa();
				switch (ra.mese) {
					case 1:
						t = Tariffa.findByRisorsaAndCommessaAndData(0, anno, ra.risorsa, c);
						tariffaTot[0] += t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
						// TODO importoMensile ?
						costoTot[0] += (ra.costo.importoGiornaliero * ra.oreLavorate) / 8;
						break;
					case 2:
						t = Tariffa.findByRisorsaAndCommessaAndData(1, anno, ra.risorsa, c);
						tariffaTot[1] += t != null ? t.importoGiornaliero: 0;
						costoTot[1] = costoTot[1] + ra.costo.importoGiornaliero;				
						break;
					case 3:
						t = Tariffa.findByRisorsaAndCommessaAndData(2, anno, ra.risorsa, c);
						tariffaTot[2] += t != null ? t.importoGiornaliero: 0;
						costoTot[2] = costoTot[2] + ra.costo.importoGiornaliero;
						break;
					case 4:
						t = Tariffa.findByRisorsaAndCommessaAndData(3, anno, ra.risorsa, c);
						tariffaTot[3] += t != null ? t.importoGiornaliero: 0;
						costoTot[3] = costoTot[3] + ra.costo.importoGiornaliero;
						break;
					case 5:
						t = Tariffa.findByRisorsaAndCommessaAndData(4, anno, ra.risorsa, c);
						tariffaTot[4] += t != null ? t.importoGiornaliero: 0;
						costoTot[4] = costoTot[4] + ra.costo.importoGiornaliero;
						break;
					case 6:
						t = Tariffa.findByRisorsaAndCommessaAndData(5, anno, ra.risorsa, c);
						tariffaTot[5] += t != null ? t.importoGiornaliero: 0;
						costoTot[5] = costoTot[5] + ra.costo.importoGiornaliero;
						break;
					case 7:
						t = Tariffa.findByRisorsaAndCommessaAndData(6, anno, ra.risorsa, c);
						tariffaTot[6] += t != null ? t.importoGiornaliero: 0;
						costoTot[6] = costoTot[6] + ra.costo.importoGiornaliero;
						break;
					case 8:
						t = Tariffa.findByRisorsaAndCommessaAndData(7, anno, ra.risorsa, c);
						tariffaTot[7] += t != null ? t.importoGiornaliero: 0;
						costoTot[7] = costoTot[7] + ra.costo.importoGiornaliero;
						break;
					case 9:
						t = Tariffa.findByRisorsaAndCommessaAndData(8, anno, ra.risorsa, c);
						tariffaTot[8] += t != null ? t.importoGiornaliero: 0;
						costoTot[8] = costoTot[8] + ra.costo.importoGiornaliero;
						break;
					case 10:
						t = Tariffa.findByRisorsaAndCommessaAndData(9, anno, ra.risorsa, c);
						tariffaTot[9] += t != null ? t.importoGiornaliero: 0;
						costoTot[9] = costoTot[9] + ra.costo.importoGiornaliero;
						break;
					case 11:
						t = Tariffa.findByRisorsaAndCommessaAndData(10, anno, ra.risorsa, c);
						tariffaTot[10] += t != null ? t.importoGiornaliero: 0;
						costoTot[10] = costoTot[10] + ra.costo.importoGiornaliero;
						break;
					case 12:
						t = Tariffa.findByRisorsaAndCommessaAndData(11, anno, ra.risorsa, c);
						tariffaTot[11] += t != null ? t.importoGiornaliero: 0;
						costoTot[11] = costoTot[11] + ra.costo.importoGiornaliero;
						break;
				}
			}
			for(int i=0;i<12;i++){
				if(tariffaTot[i] != 0 && costoTot[i] != 0) {
					if(c instanceof CommessaACorpo) {
						result.put("ricavo_" + MyUtility.getStringMese(i+1), ((CommessaACorpo) c).importo);
						CostoCommessa cc = CostoCommessa.find("byCommessaAndData", c, MyUtility.MeseEdAnnoToDataInizio(i, anno)).first();
						float costoTotale = costoTot[i];
						if(cc!=null){
							costoTotale = costoTotale + cc.importo;
						}
						result.put("costo_" + MyUtility.getStringMese(i+1), costoTotale);
					} else {
						result.put("ricavo_" + MyUtility.getStringMese(i+1), tariffaTot[i]);
						result.put("costo_" + MyUtility.getStringMese(i+1), costoTot[i]);
					}
				}
			}
			if(!staff.equals("")){
				result.put("staff",  MyUtility.cleanStaff(staff));
				resultSet.add(result);
			}
		}
		return MyUtility.orderResultSet(resultSet, "nominativo");
	}
	
	
}

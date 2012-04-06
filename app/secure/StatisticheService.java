package secure;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import models.Cliente;
import models.Commessa;
import models.CommessaACorpo;
import models.Costo;
import models.CostoCommessa;
import models.RapportoLavoro;
import models.RendicontoAttivita;
import models.Risorsa;
import models.Tariffa;
import models.TipoCommessa;
import models.TipoRapportoLavoro;
import utility.MyUtility;

public class StatisticheService {
	

	public static List<Map> prepareReportPortafoglioOrdini(Integer anno) {
		List<Map> resultSet = new ArrayList<Map>();
		List<TipoCommessa> listaTipicommesse = TipoCommessa.findAll();
		for(TipoCommessa tc : listaTipicommesse) {
			Map result = new HashMap();
			float[] tariffaTot = new float[12];
			float[] costoTot = new float[12];
			List<Commessa> listaCommesse = Commessa.find("byTipoCommessa",tc).fetch();
			for(Commessa com : listaCommesse) {
				if(com.calcoloCosti == true && com.calcoloRicavi == true) {
					List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byAnnoAndCommessa", anno, com).fetch();
					for(RendicontoAttivita ra : listaRapportini) {
						Tariffa t = new Tariffa();
						switch (ra.mese) {
							case 1:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(0, anno, ra.risorsa, com);
									tariffaTot[0] += t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
								}
								costoTot[0] += (ra.costo.importoGiornaliero * ra.oreLavorate) / 8;
								break;
							case 2:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(1, anno, ra.risorsa, com);
									tariffaTot[1] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[1] = costoTot[1] + ra.costo.importoGiornaliero;				
								break;
							case 3:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(2, anno, ra.risorsa, com);
									tariffaTot[2] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[2] = costoTot[2] + ra.costo.importoGiornaliero;
								break;
							case 4:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(3, anno, ra.risorsa, com);
									tariffaTot[3] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[3] = costoTot[3] + ra.costo.importoGiornaliero;
								break;
							case 5:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(4, anno, ra.risorsa, com);
									tariffaTot[4] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[4] = costoTot[4] + ra.costo.importoGiornaliero;
								break;
							case 6:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(5, anno, ra.risorsa, com);
									tariffaTot[5] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[5] = costoTot[5] + ra.costo.importoGiornaliero;
								break;
							case 7:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(6, anno, ra.risorsa, com);
									tariffaTot[6] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[6] = costoTot[6] + ra.costo.importoGiornaliero;
								break;
							case 8:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(7, anno, ra.risorsa, com);
									tariffaTot[7] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[7] = costoTot[7] + ra.costo.importoGiornaliero;
								break;
							case 9:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(8, anno, ra.risorsa, com);
									tariffaTot[8] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[8] = costoTot[8] + ra.costo.importoGiornaliero;
								break;
							case 10:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(9, anno, ra.risorsa, com);
									tariffaTot[9] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[9] = costoTot[9] + ra.costo.importoGiornaliero;
								break;
							case 11:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(10, anno, ra.risorsa, com);
									tariffaTot[10] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[10] = costoTot[10] + ra.costo.importoGiornaliero;
								break;
							case 12:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(11, anno, ra.risorsa, com);
									tariffaTot[11] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[11] = costoTot[11] + ra.costo.importoGiornaliero;
								break;
						}
					}
				}
				
				if(com instanceof CommessaACorpo) {
					if(RendicontoAttivita.find("byAnnoAndCommessa", anno, com).fetch().size() > 0){
						for(int i=0;i<12;i++){
							if(tariffaTot[i] != 0 || costoTot[i] != 0) {
								tariffaTot[i] += ((CommessaACorpo) com).importo;
								CostoCommessa cc = CostoCommessa.find("byCommessaAndData", com, MyUtility.MeseEdAnnoToDataInizio(i, anno)).first();
								float costoTotale = costoTot[i];
								if(cc!=null){
									costoTotale += cc.importo;
								}
								costoTot[i] = costoTotale;
							}
						}
					}
				}
			}
			float costoTotale = 0f;
			float ricavoTotale = 0f;
			float margine = 0f;
			result.put("descrizione", tc.descrizione);
			result.put("tipo", tc.idTipoCommessa);
			for(int i=0;i<12;i++){
				result.put("ricavo_" + MyUtility.getStringMese(i+1), tariffaTot[i]);
				result.put("costo_" + MyUtility.getStringMese(i+1), costoTot[i]);
				ricavoTotale += tariffaTot[i];
				costoTotale += costoTot[i];
			}
			result.put("ricavo_Totale", ricavoTotale);
			result.put("costo_Totale", costoTotale);
			margine = ricavoTotale - costoTotale;
			result.put("margine_val", margine);
			result.put("margine_perc", margine == 0 ? 0 : margine / ricavoTotale);
			if(ricavoTotale > 0 && costoTotale > 0 ) 
				resultSet.add(result);
		}
		return MyUtility.orderResultSet(resultSet, "descrizione");
	}
	
	public static List<Map> prepareReportClienti(Integer anno) {
		List<Map> resultSet = new ArrayList<Map>();
		List<Cliente> listaclienti = Cliente.findAll();
		listaclienti.remove(Cliente.find("byCodice","ST").first());
		for(Cliente c : listaclienti) {
			Map result = new HashMap();
			List<Commessa> listaComesse = c.commesse;
			float[] tariffaTot = new float[12];
			float[] costoTot = new float[12];
			float ricavoPrimoSemestre = 0f;
			float ricavoSecondoSemestre = 0f;
			float costoPrimoSemestre = 0f;
			float costoSecondoSemestre = 0f;
			for(Commessa com : listaComesse) {
				if(com.calcoloCosti == true && com.calcoloRicavi == true) {
					List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byAnnoAndCommessa", anno, com).fetch();
					for(RendicontoAttivita ra : listaRapportini) {
						Tariffa t = new Tariffa();
						switch (ra.mese) {
							case 1:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(0, anno, ra.risorsa, com);
									tariffaTot[0] += t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
								}
								costoTot[0] += (ra.costo.importoGiornaliero * ra.oreLavorate) / 8;
								break;
							case 2:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(1, anno, ra.risorsa, com);
									tariffaTot[1] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[1] = costoTot[1] + ra.costo.importoGiornaliero;				
								break;
							case 3:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(2, anno, ra.risorsa, com);
									tariffaTot[2] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[2] = costoTot[2] + ra.costo.importoGiornaliero;
								break;
							case 4:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(3, anno, ra.risorsa, com);
									tariffaTot[3] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[3] = costoTot[3] + ra.costo.importoGiornaliero;
								break;
							case 5:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(4, anno, ra.risorsa, com);
									tariffaTot[4] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[4] = costoTot[4] + ra.costo.importoGiornaliero;
								break;
							case 6:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(5, anno, ra.risorsa, com);
									tariffaTot[5] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[5] = costoTot[5] + ra.costo.importoGiornaliero;
								break;
							case 7:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(6, anno, ra.risorsa, com);
									tariffaTot[6] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[6] = costoTot[6] + ra.costo.importoGiornaliero;
								break;
							case 8:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(7, anno, ra.risorsa, com);
									tariffaTot[7] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[7] = costoTot[7] + ra.costo.importoGiornaliero;
								break;
							case 9:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(8, anno, ra.risorsa, com);
									tariffaTot[8] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[8] = costoTot[8] + ra.costo.importoGiornaliero;
								break;
							case 10:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(9, anno, ra.risorsa, com);
									tariffaTot[9] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[9] = costoTot[9] + ra.costo.importoGiornaliero;
								break;
							case 11:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(10, anno, ra.risorsa, com);
									tariffaTot[10] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[10] = costoTot[10] + ra.costo.importoGiornaliero;
								break;
							case 12:
								if(!(com instanceof CommessaACorpo)) {
									t = Tariffa.findByRisorsaAndCommessaAndData(11, anno, ra.risorsa, com);
									tariffaTot[11] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[11] = costoTot[11] + ra.costo.importoGiornaliero;
								break;
						}
					}
				}
				if(com instanceof CommessaACorpo) {
					if(RendicontoAttivita.find("byAnnoAndCommessa", anno, com).fetch().size() > 0){
						for(int i=0;i<12;i++){
							if(tariffaTot[i] != 0 || costoTot[i] != 0) {
								tariffaTot[i] += ((CommessaACorpo) com).importo;
								CostoCommessa cc = CostoCommessa.find("byCommessaAndData", com, MyUtility.MeseEdAnnoToDataInizio(i, anno)).first();
								if(cc!=null){
									costoTot[i] += cc.importo;
								}
							}
						}
					}
				}
			}
			result.put("codice", c.codice);
			result.put("nominativo", c.nominativo);
			for(int i=0;i<12;i++){
				result.put("ricavo_" + MyUtility.getStringMese(i+1), tariffaTot[i]);
				result.put("costo_" + MyUtility.getStringMese(i+1), costoTot[i]);
				if(i<6){
					ricavoPrimoSemestre += tariffaTot[i];
					costoPrimoSemestre += costoTot[i];
				} else {
					ricavoSecondoSemestre += tariffaTot[i];
					costoSecondoSemestre += costoTot[i];
				}
			}
			result.put("ricavo_primo_semestre", ricavoPrimoSemestre);
			result.put("ricavo_secondo_semestre", ricavoSecondoSemestre);
			result.put("costo_primo_semestre", costoPrimoSemestre);
			result.put("costo_secondo_semestre", costoSecondoSemestre);
			result.put("ricavo_Totale", ricavoPrimoSemestre + ricavoSecondoSemestre);
			result.put("costo_Totale", costoPrimoSemestre + costoSecondoSemestre);
			if((ricavoPrimoSemestre + ricavoSecondoSemestre) > 0 && (costoPrimoSemestre + costoSecondoSemestre) > 0)
				resultSet.add(result);
		}
		return MyUtility.orderResultSet(resultSet, "codice");
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
			result.put("tipo", c.tipoCommessa != null ? c.tipoCommessa.idTipoCommessa : null);
			for(RendicontoAttivita ra : listaRapportini) {
				staff += ra.risorsa.cognome + ",";
				Tariffa t = new Tariffa();
				switch (ra.mese) {
					case 1:
						t = Tariffa.findByRisorsaAndCommessaAndData(0, anno, ra.risorsa, c);
						tariffaTot[0] += t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
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
	
	public static List<Map> prepareReportCommesseNonFatturabili(Integer anno) {
		List<Map> resultSet = new ArrayList<Map>();
		List<Commessa> listaComesse = Commessa.findCommesseNonFatturabili();
		for(Commessa c : listaComesse) {
			Map result = new HashMap();
			List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byAnnoAndCommessa", anno, c).fetch();
			float[] oreTot = new float[12];
			result.put("descrizione", c.descrizione);
			result.put("codice", c.codice);
			for(RendicontoAttivita ra : listaRapportini) {
				if(!(ra.rapportoLavoro.tipoRapportoLavoro.codice.equals("CCP"))){
					switch (ra.mese) {
						case 1:
								oreTot[0] += ra.oreLavorate;
							break;
						case 2:
								oreTot[1] += ra.oreLavorate;
							break;
						case 3:
								oreTot[2] += ra.oreLavorate;
							break;
						case 4:
								oreTot[3] += ra.oreLavorate;
							break;
						case 5:
								oreTot[4] += ra.oreLavorate;
							break;
						case 6:
								oreTot[5] += ra.oreLavorate;
							break;
						case 7:
								oreTot[6] += ra.oreLavorate;
							break;
						case 8:
								oreTot[7] += ra.oreLavorate;
							break;
						case 9:
								oreTot[8] += ra.oreLavorate;
							break;
						case 10:
								oreTot[9] += ra.oreLavorate;
							break;
						case 11:
								oreTot[10] += ra.oreLavorate;
							break;
						case 12:
								oreTot[11] += ra.oreLavorate;
							break;
					}
				}
			}
			float totPrimoSempestre = 0f;
			float totsecondoSempestre = 0f;
			for(int i=0;i<12;i++){
				if(oreTot[i] > 0) {
					result.put("totale_" + MyUtility.getStringMese(i+1), oreTot[i] / 8);
				}
				if(i<6){
					totPrimoSempestre += oreTot[i] / 8;
				} else {
					totsecondoSempestre += oreTot[i] / 8;
				}
			}
			float tot = totPrimoSempestre + totsecondoSempestre;
			result.put("ore_totali_primo_semestre", totPrimoSempestre);
			result.put("ore_totali_secondo_semestre", totsecondoSempestre);
			result.put("ore_totali", tot);
			if(tot > 0)
				resultSet.add(result);
		}
		return MyUtility.orderResultSet(resultSet, "codice");
	}
	
	public static List<Map> prepareReportCommesseNonFatturabiliCollaboratori(Integer anno) {
		List<Map> resultSet = new ArrayList<Map>();
		List<Commessa> listaComesse = Commessa.findCommesseNonFatturabili();
		for(Commessa c : listaComesse) {
			Map result = new HashMap();
			List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byAnnoAndCommessa", anno, c).fetch();
			float[] oreTot = new float[12];
			result.put("descrizione", c.descrizione);
			result.put("codice", c.codice);
			for(RendicontoAttivita ra : listaRapportini) {
				if(ra.rapportoLavoro.tipoRapportoLavoro.codice.equals("CCP")){
					switch (ra.mese) {
						case 1:
								oreTot[0] += ra.oreLavorate;
							break;
						case 2:
								oreTot[1] += ra.oreLavorate;
							break;
						case 3:
								oreTot[2] += ra.oreLavorate;
							break;
						case 4:
								oreTot[3] += ra.oreLavorate;
							break;
						case 5:
								oreTot[4] += ra.oreLavorate;
							break;
						case 6:
								oreTot[5] += ra.oreLavorate;
							break;
						case 7:
								oreTot[6] += ra.oreLavorate;
							break;
						case 8:
								oreTot[7] += ra.oreLavorate;
							break;
						case 9:
								oreTot[8] += ra.oreLavorate;
							break;
						case 10:
								oreTot[9] += ra.oreLavorate;
							break;
						case 11:
								oreTot[10] += ra.oreLavorate;
							break;
						case 12:
								oreTot[11] += ra.oreLavorate;
							break;
					}
				}
			}
			float totPrimoSempestre = 0f;
			float totsecondoSempestre = 0f;
			for(int i=0;i<12;i++){
				if(oreTot[i] > 0) {
					result.put("totale_" + MyUtility.getStringMese(i+1), oreTot[i] / 8);
				}
				if(i<6){
					totPrimoSempestre += oreTot[i] / 8;
				} else {
					totsecondoSempestre += oreTot[i] / 8;
				}
			}
			float tot = totPrimoSempestre + totsecondoSempestre;
			result.put("ore_totali_primo_semestre", totPrimoSempestre);
			result.put("ore_totali_secondo_semestre", totsecondoSempestre);
			result.put("ore_totali", tot);
			System.out.println(c.codice);
			System.out.println(tot);
			if(tot > 0)
				resultSet.add(result);
		}
		return MyUtility.orderResultSet(resultSet, "codice");
	}
	
	public static List<Map> prepareReportCommesseACorpo(Integer anno, Integer mese) {
		List<Map> resultSet = new ArrayList<Map>();
		List<CommessaACorpo> listaComesse = Commessa.findCommesseACorpo();
		List<Map> listarisorse = new ArrayList<Map>();
		for(CommessaACorpo c : listaComesse) {
			Map result = new HashMap();
			List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byCommessaAndAnnoAndMese", c, anno, mese).fetch();
			float tariffaTot = 0f;
			float costoTot = 0f;
			result.put("id", c.idCommessa);
			result.put("cliente", c.cliente.nominativo);
			result.put("nome", c.descrizione);
			result.put("valore", c.importo);
			listarisorse = new ArrayList<Map>();
			for(RendicontoAttivita ra : listaRapportini) {
				Tariffa t = Tariffa.findByRisorsaAndCommessaAndData(mese, anno, ra.risorsa, c);
				float ricavo = t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
				float costo = (ra.costo.importoGiornaliero * ra.oreLavorate) / 8;
				costoTot += costo; 
				tariffaTot += ricavo;
				
				Map risorsa = new HashMap();
				risorsa.put("nome", ra.risorsa.cognome + " " + ra.risorsa.nome);
				risorsa.put("ricavo", ricavo);
				risorsa.put("costo", costo);
				listarisorse.add(risorsa);
			}
			float costoAggiuntivo = CostoCommessa.caloloCostiAggiuntivi(c,MyUtility.MeseEdAnnoToDataFine(mese - 1, anno));
			if(costoAggiuntivo > 0){
				costoTot += costoAggiuntivo;
			}
			result.put("risorse", listarisorse);
			result.put("COSTO", costoTot);
			result.put("RICAVO", tariffaTot);
			if(costoTot > 0 && tariffaTot > 0)
				resultSet.add(result);
		}
		return MyUtility.orderResultSet(resultSet, "nome");
	}
	
	public static List<Map> prepareReportRisorse(Integer anno, Integer mese) {
		List<Map> resultSet = new ArrayList<Map>();
		List<Risorsa> listaRisorse = Risorsa.findAll();
		for(Risorsa r : listaRisorse) {
			Map result = new HashMap();
			List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byRisorsaAndAnnoAndMese", r, anno, mese).fetch();
			float oreLavorate = 0f;
			float costo = 0f;
			String cliComm = "";
			float ricavoTot = 0f;
			float costoTot = 0f;
			result.put("codice", r.codice);
			result.put("cognome", r.cognome + " " + r.nome);
			result.put("stato_risorsa", r.tipoStatoRisorsa.codice);
			int m = mese - 1;
			List<RapportoLavoro> rl = RapportoLavoro.findByRisorsaAndMeseAndAnno(r, m, anno);
			for(RendicontoAttivita ra : listaRapportini) {
				oreLavorate += ra.oreLavorate;
				cliComm += ra.commessa.cliente.codice + "-" + ra.commessa.codice + " ";
				Tariffa t = Tariffa.findByRisorsaAndCommessaAndData(m, anno, ra.risorsa, ra.commessa);
				ricavoTot += t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
				costo = ra.costo.importoGiornaliero;
				if(ra.costo.importoMensile != null) {
					costoTot = ra.costo.importoMensile;
				} else {
					costoTot += (ra.costo.importoGiornaliero * ra.oreLavorate) / 8;
				}
			}
			result.put("ore_lavorate", oreLavorate);
			result.put("cliente_commessa", cliComm);
			if(!r.gruppo.codice.equals("STAFF")){
				result.put("costo_giornaliero", costo);
				result.put("ricavo_totale", ricavoTot);
				if(r.tipoStatoRisorsa.codice.equals("SOSP")){
					costoTot = 0;
				}
				if(ricavoTot != 0){
					Double mt = (double) (((ricavoTot - costoTot) / ricavoTot)*100);
					DecimalFormat df = new DecimalFormat("#,##", new DecimalFormatSymbols(Locale.ITALIAN));
					result.put("margine_totale",Float.parseFloat(df.format(mt)));
				} else if(ricavoTot == 0 && costoTot != 0){
					result.put("margine_totale", new Integer(-1));
				} else if(ricavoTot == 0 && costoTot == 0){
					result.put("margine_totale", 0);
				}
				result.put("costo_totale", costoTot);
			}
			if(listaRapportini.size() > 0) {
				result.put("rappLavoro",rl.get(0).tipoRapportoLavoro.codice);
				resultSet.add(result);
			}
		}
		return MyUtility.orderResultSet(resultSet, "cognome");
	}
	
	public static List<Map> prepareReportTipoLavoro(Integer anno, Integer mese) {
		List<Map> resultSet = new ArrayList<Map>();
		List<TipoRapportoLavoro> listaTipoRapportolavoro = TipoRapportoLavoro.findAll();
		Integer meseCorretto = mese - 1;
		for(TipoRapportoLavoro trl : listaTipoRapportolavoro) {
			Map result = new HashMap();
			float oreLavorate = 0f;
			float ricavoTot = 0f;
			float costoTot = 0f;
			Integer numeroRisorse = RapportoLavoro.countRisorse(trl);
			result.put("numRisorse", numeroRisorse);
			result.put("codice", trl.codice);
			result.put("descrizione", trl.descrizione);
			// new
			List<Risorsa> listaRisorse = RapportoLavoro.findRisorseByTipoRapportoLavoroAndMeseAndAnno(trl, meseCorretto, anno);
			float oreLavorateAppo = 0;
			for(Risorsa r : listaRisorse) {
				oreLavorateAppo = 0;
				List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byRisorsaAndAnnoAndMese", r, anno, mese).fetch();
				for(RendicontoAttivita ra : listaRapportini) {
					if(ra.rapportoLavoro.tipoRapportoLavoro.idTipoRapportoLavoro == trl.idTipoRapportoLavoro) {
						if(!ra.risorsa.gruppo.codice.equals("STAFF")){
							oreLavorateAppo += ra.oreLavorate;
							Tariffa t = Tariffa.findByRisorsaAndCommessaAndData(meseCorretto, anno, ra.risorsa, ra.commessa);
							ricavoTot += t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
						}
					}
				}
				if(!r.gruppo.codice.equals("STAFF")){
					if(!r.tipoStatoRisorsa.codice.equals("SOSP")){
						Costo c = Costo.extractCostoByMeseAndAnno(r, meseCorretto, anno);
						if(c.importoMensile != null) {
							costoTot += c.importoMensile;
						} else {
							costoTot += (c.importoGiornaliero * oreLavorateAppo) / 8;
						}
					}
				}
				oreLavorate += oreLavorateAppo;
			}
			
//			List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byAnnoAndMese", anno, mese).fetch();
//			for(RendicontoAttivita ra : listaRapportini) {
//				List<RapportoLavoro> rl = RapportoLavoro.findByRisorsaAndMeseAndAnno(ra.risorsa, mese-1, anno);
//				if(rl.get(0).tipoRapportoLavoro == trl){
//					if(!ra.risorsa.gruppo.codice.equals("STAFF")){
//						oreLavorate += ra.oreLavorate;
//						if(!ra.risorsa.tipoStatoRisorsa.codice.equals("SOSP")){
//							if(ra.costo.importoMensile != null) {
//								costoTot += ra.costo.importoMensile;
//							} else {
//								costoTot += (ra.costo.importoGiornaliero * ra.oreLavorate) / 8;
//							}
//						}
//						if(ra.rapportoLavoro.tipoRapportoLavoro.codice.equals("ADG")){
//							if(ra.costo.importoMensile != null) {
//								System.out.println(ra.costo.importoMensile);
//							} else {
//								System.out.println((ra.costo.importoGiornaliero * ra.oreLavorate) / 8);
//							}
//						}
//						Tariffa t = Tariffa.findByRisorsaAndCommessaAndData(mese-1, anno, ra.risorsa, ra.commessa);
//						ricavoTot += t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
//					}
//				}
//			}
			result.put("costoTotale", costoTot);
			result.put("ricavoTotale", ricavoTot);
			if(ricavoTot != 0){
				result.put("margineTotale", (ricavoTot - costoTot) / ricavoTot);
			} else {
				result.put("margineTotale", new Integer(-1));
			}
			result.put("oreLavorate", oreLavorate);
			if(oreLavorate > 0)
				resultSet.add(result);
		}
		return MyUtility.orderResultSet(resultSet, "descrizione");
	}
	
	public static List<Map> prepareReportRisorseTotali() {
		List<Map> resultSet = new ArrayList<Map>();
		List<Risorsa> listaRisorse = Risorsa.findAll();
		for(Risorsa r : listaRisorse) {
			Map result = new HashMap();
			result.put("CODICE", r.codice);
			result.put("RISORSA", r.cognome + " " + r.nome);
			result.put("DATAIN", r.dataIn);
			result.put("DATAOUT", r.dataOut);
			result.put("STATO", r.tipoStatoRisorsa.codice);
			List<RapportoLavoro> rl = RapportoLavoro.find("byRisorsa", r).fetch();
			if(rl.size() > 0)
				result.put("RAPPLAVORO", rl.get(rl.size() - 1));
				resultSet.add(result);
		}
		return MyUtility.orderResultSet(resultSet, "RISORSA");
	}
	
	public static List<Map> prepareReportSubreportRisorseTotali() {
		List<Map> resultSet = new ArrayList<Map>();
		List<TipoRapportoLavoro> listaTipoRapportolavoro = TipoRapportoLavoro.findAll();
		for(TipoRapportoLavoro trl : listaTipoRapportolavoro) {
			Map result = new HashMap();
			Integer numeroRisorse = RapportoLavoro.countRisorse(trl);
			result.put("N_RISORSE", numeroRisorse);
			result.put("CODICE", trl.codice);
			result.put("DESCRIZIONE", trl.descrizione);
			resultSet.add(result);
		}
		return MyUtility.orderResultSet(resultSet, "CODICE");
	}
}

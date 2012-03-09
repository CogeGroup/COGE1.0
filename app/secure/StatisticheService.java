package secure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Cliente;
import models.Commessa;
import models.CommessaACorpo;
import models.CostoCommessa;
import models.RendicontoAttivita;
import models.Tariffa;
import models.TipoCommessa;
import utility.MyUtility;

public class StatisticheService {
	
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
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(0, anno, ra.risorsa, com);
									tariffaTot[0] += t != null ? ((t.importoGiornaliero * ra.oreLavorate) / 8): 0;
								}
								// TODO importoMensile ?
								costoTot[0] += (ra.costo.importoGiornaliero * ra.oreLavorate) / 8;
								break;
							case 2:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(1, anno, ra.risorsa, com);
									tariffaTot[1] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[1] = costoTot[1] + ra.costo.importoGiornaliero;				
								break;
							case 3:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(2, anno, ra.risorsa, com);
									tariffaTot[2] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[2] = costoTot[2] + ra.costo.importoGiornaliero;
								break;
							case 4:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(3, anno, ra.risorsa, com);
									tariffaTot[3] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[3] = costoTot[3] + ra.costo.importoGiornaliero;
								break;
							case 5:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(4, anno, ra.risorsa, com);
									tariffaTot[4] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[4] = costoTot[4] + ra.costo.importoGiornaliero;
								break;
							case 6:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(5, anno, ra.risorsa, com);
									tariffaTot[5] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[5] = costoTot[5] + ra.costo.importoGiornaliero;
								break;
							case 7:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(6, anno, ra.risorsa, com);
									tariffaTot[6] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[6] = costoTot[6] + ra.costo.importoGiornaliero;
								break;
							case 8:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(7, anno, ra.risorsa, com);
									tariffaTot[7] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[7] = costoTot[7] + ra.costo.importoGiornaliero;
								break;
							case 9:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(8, anno, ra.risorsa, com);
									tariffaTot[8] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[8] = costoTot[8] + ra.costo.importoGiornaliero;
								break;
							case 10:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(9, anno, ra.risorsa, com);
									tariffaTot[9] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[9] = costoTot[9] + ra.costo.importoGiornaliero;
								break;
							case 11:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(10, anno, ra.risorsa, com);
									tariffaTot[10] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[10] = costoTot[10] + ra.costo.importoGiornaliero;
								break;
							case 12:
								if(com instanceof CommessaACorpo) {
									tariffaTot[0] += 0;
								} else {
									t = Tariffa.findByRisorsaAndCommessaAndData(11, anno, ra.risorsa, com);
									tariffaTot[11] += t != null ? t.importoGiornaliero: 0;
								}
								costoTot[11] = costoTot[11] + ra.costo.importoGiornaliero;
								break;
						}
					}
				}
				for(int i=0;i<12;i++){
					if(tariffaTot[i] != 0 || costoTot[i] != 0) {
						if(com instanceof CommessaACorpo) {
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
								// TODO importoMensile ?
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
				if(c.codice.equals("MININT"))
					System.out.println("MININT " + com.codice+ ": " + tariffaTot[0]);
			}
			if(c.codice.equals("MININT"))
				System.out.println("MININT " + tariffaTot[0]);
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
	
	// conviene fare un metodo di smistamento? si provare per dipendente poi cambiare in ccp se funziona metodo di smistamento obbligatorio :-)
	// TODO giorni statistiche commesse non fatturabili DIP
	// TODO giorni statistiche commesse non fatturabili CCP

}

package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Commessa;
import models.Costo;
import models.RapportoLavoro;
import models.RendicontoAttivita;
import models.Risorsa;
import models.TipoRapportoLavoro;
import play.data.validation.Required;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.DomainWrapper;
import utility.MyUtility;

@With(SecureCOGE.class)
public class RendicontoAttivitaController extends Controller {

	public static void index() {
		 render();
    }

// Ricerca rapportino
	public static void search(){
		List<Integer> listaAnni = MyUtility.createListaAnni();
    	int mese = (Calendar.getInstance().get(Calendar.MONTH)) - 1;
        int anno = Calendar.getInstance().get(Calendar.YEAR);
		render(listaAnni, mese, anno);
	}
	
	public static void result(Integer idRisorsa, int mese, int anno){
		List<RendicontoAttivita> listaRapportini = new ArrayList<RendicontoAttivita>();
		mese++;
		if(idRisorsa != null){
			Risorsa risorsa = Risorsa.findById(idRisorsa);
			listaRapportini = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa,mese,anno).fetch();
			ValuePaginator paginator = new ValuePaginator(listaRapportini);
			paginator.setPageSize(10);
			render("RendicontoAttivitaController/dettaglio.html", paginator, risorsa, mese, anno);
		}else{
			listaRapportini = RendicontoAttivita.findByExample(idRisorsa,mese,anno);
			ValuePaginator paginator = new ValuePaginator(listaRapportini);
			paginator.setPageSize(10);
			render("RendicontoAttivitaController/list.html", paginator,mese,anno);
		}
		search();
	}
	
	public static void dettaglio(Integer idRisorsa, int mese, int anno){
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byRisorsaAndMeseAndAnno",risorsa,mese,anno).fetch();
		ValuePaginator paginator = new ValuePaginator(listaRapportini);
		paginator.setPageSize(10);
		render(paginator, risorsa, mese, anno);
	}
	
	public static void backList(int mese, int anno){
		Integer idRisorsa = null;
		List<RendicontoAttivita> listaRapportini = new ArrayList<RendicontoAttivita>();
			listaRapportini = RendicontoAttivita.findByExample(idRisorsa,mese,anno);
			ValuePaginator paginator = new ValuePaginator(listaRapportini);
			paginator.setPageSize(10);
			render("RendicontoAttivitaController/list.html", paginator,mese,anno);
	}
	
// Inserimento nuovo rapportino
//mese - 1 per inserire il rapportino del mese precedente
	public static void chooseRisorsa() {
		List<Integer> listaAnni = MyUtility.createListaAnni();
    	int mese = (Calendar.getInstance().get(Calendar.MONTH)) - 1;
        int anno = Calendar.getInstance().get(Calendar.YEAR);
		render(listaAnni, mese, anno);
    }
	
	public static void createRendicontoAttivita(@Required(message="Inserire una risorsa") Integer idRisorsa, int mese, int anno) {
		if(validation.hasErrors()){
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("RendicontoAttivitaController/chooseRisorsa.html", listaAnni, mese, anno);
		}
		if(new Date().before(MyUtility.MeseEdAnnoToDataFine(mese, anno))){
			validation.addError("data", "Data selezionata non valida");
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("RendicontoAttivitaController/chooseRisorsa.html", listaAnni, mese, anno);
		}
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		if(risorsa == null){
			validation.addError("risorsa", "Risorsa non trovata");
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("RendicontoAttivitaController/chooseRisorsa.html", listaAnni, mese, anno);
		}
		if(risorsa.dataOut != null && risorsa.dataOut.before(MyUtility.MeseEdAnnoToDataInizio(mese, anno))){
			validation.addError("risorsa.dataOut", "La risorsa è in uscita dal: " + MyUtility.dateToString(risorsa.dataOut, "dd-MM-yyyy"));
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("RendicontoAttivitaController/chooseRisorsa.html", listaAnni, mese, anno);
		}
		RendicontoAttivita ra = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa,mese+1,anno).first();
		if(ra != null){
			validation.addError("meseAnno", "il rapportino per il mese " + (mese+1) + "-" + anno +" della risorsa " + risorsa.cognome + " già esistente");
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("RendicontoAttivitaController/chooseRisorsa.html", listaAnni, mese, anno);
		}
		
		List<RendicontoAttivita> listaRendicontoAttivita = RendicontoAttivita.findCommesseFatturabiliPerRisorsa(mese, anno, risorsa);
		List<Commessa> listaCommesseNonFatturabili = risorsa.gruppo.commesse;
		render(idRisorsa,listaRendicontoAttivita,listaCommesseNonFatturabili,mese,anno,risorsa);
	}
	
	// Modifica o aggiunge rendicontoAttivita nel rapportino
	public static void aggiungiAttivita(Integer idRisorsa, int mese, int anno){
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		List<RendicontoAttivita> listaRendicontoAttivita = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa,mese,anno).fetch();

		// lista commesse fatturabile meno quelle gia salvate
		List<RendicontoAttivita> listaCommesse = RendicontoAttivita.findCommesseFatturabiliPerRisorsa(mese-1, anno, risorsa);
		listaRendicontoAttivita = commesseFatturabiliNonSalvate(listaRendicontoAttivita, listaCommesse);
		
		// lista commesse non fatturabile meno quelle gia salvate
		List<RendicontoAttivita> attivitaSalvate = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa, mese, anno).fetch();
		List<Commessa> listaCommesseNonFatturabili = commesseNonFatturabiliNonSalvate(attivitaSalvate, risorsa.gruppo.commesse);
		mese--;
		render(idRisorsa,listaRendicontoAttivita,listaCommesseNonFatturabili,mese,anno,risorsa);
	}
	
	// rimuove tutte le commesse fatturabili gia inserite nel rendicontoAttivita
	private static List<RendicontoAttivita> commesseFatturabiliNonSalvate(List<RendicontoAttivita> listaAttivitaSalvate, List<RendicontoAttivita> listaAttivita){
		List<RendicontoAttivita> commesseDaTogliere = new ArrayList<RendicontoAttivita>();
		for (RendicontoAttivita ra : listaAttivita) {
			for (RendicontoAttivita raSalvato : listaAttivitaSalvate) {
				if(ra.commessa.idCommessa == raSalvato.commessa.idCommessa && ra.rapportoLavoro.idRapportoLavoro == raSalvato.rapportoLavoro.idRapportoLavoro){
					commesseDaTogliere.add(ra);
				}
			}
		}
		listaAttivita.removeAll(commesseDaTogliere);
		listaAttivitaSalvate.addAll(listaAttivita);
		return listaAttivitaSalvate;
	}
	
	// rimuove tutte le commesse non fatturabili gia inserite nel rendicontoAttivita
	private static List<Commessa> commesseNonFatturabiliNonSalvate(List<RendicontoAttivita> attivitaSalvate, List<Commessa> listaCommesseNonFatturabili){
		List<Commessa> commesseDaTogliere = new ArrayList<Commessa>();
		for (Commessa commessa : listaCommesseNonFatturabili) {
			for (RendicontoAttivita ra : attivitaSalvate) {
				if(ra.commessa.idCommessa == commessa.idCommessa){
					commesseDaTogliere.add(commessa);
				}
			}
		}
		listaCommesseNonFatturabili.removeAll(commesseDaTogliere);
		return listaCommesseNonFatturabili;
	}

	// Salva il rapportino aggiungendo nuovi rendicontoAttivita
	public static void saveRendicontoAttivita(int mese, int anno, Integer idRisorsa){
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		List<RendicontoAttivita> listaRendicontoAttivita = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa, mese+1, anno).fetch();
		for (String key : params.all().keySet()) {
			if(key.contains("id_")){
				String oreLavorateString = params.get(key);
				String[] s = key.split("_");
				Integer idCommessa = Integer.parseInt(s[1]);
				Commessa commessa = Commessa.findById(idCommessa);
				if(!oreLavorateString.equals("")){
					float oreLavorate = 0;
					try {
						oreLavorate = Float.parseFloat(oreLavorateString);
						RendicontoAttivita rendicontoAttivita = new RendicontoAttivita(oreLavorate, mese+1, anno, risorsa, commessa);
						if(s.length != 2){
							TipoRapportoLavoro trl = TipoRapportoLavoro.find("byCodice",s[2]).first();
							rendicontoAttivita.rapportoLavoro = RapportoLavoro.findByRisorsaAndMeseAndAnnoAndTipoRapportoLavoro(risorsa, trl, mese, anno);
						}else{
							RapportoLavoro ral = RapportoLavoro.findByRisorsaAndPeriodo(risorsa, MyUtility.MeseEdAnnoToDataFine(mese, anno), MyUtility.MeseEdAnnoToDataInizio(mese, anno));
							rendicontoAttivita.rapportoLavoro = ral;
						}
						rendicontoAttivita.costo = Costo.extractByRisorsaAndPeriodo(risorsa, rendicontoAttivita.rapportoLavoro.dataInizio, rendicontoAttivita.rapportoLavoro.dataFine);
						if(oreLavorate > 0){
							for (RendicontoAttivita ra : listaRendicontoAttivita) {
								if(ra.commessa.idCommessa == rendicontoAttivita.commessa.idCommessa && ra.rapportoLavoro != null 
										&& ra.rapportoLavoro.idRapportoLavoro == rendicontoAttivita.rapportoLavoro.idRapportoLavoro){
									rendicontoAttivita = ra;
									rendicontoAttivita.oreLavorate = oreLavorate;
									rendicontoAttivita.save();
								}
							}
							rendicontoAttivita.save();
						}else{
							for (RendicontoAttivita ra : listaRendicontoAttivita) {
								if(ra.commessa.idCommessa == rendicontoAttivita.commessa.idCommessa
										&& ra.rapportoLavoro.idRapportoLavoro == rendicontoAttivita.rapportoLavoro.idRapportoLavoro){
									ra.delete();
								}
							}
						}
					} catch (IllegalArgumentException e) {
						if(listaRendicontoAttivita.size()==0){
							listaRendicontoAttivita = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa, mese+1, anno).fetch();
							for (RendicontoAttivita ra : listaRendicontoAttivita) {
								ra.delete();
							}
						}
						validation.addError("oreLavorate", "inserire correttamente le ore totali");
						List<RendicontoAttivita> listaCommesse = RendicontoAttivita.findCommesseFatturabiliPerRisorsa(mese, anno, risorsa);
						List<Commessa> listaCommesseNonFatturabili = risorsa.gruppo.commesse;
						render("rendicontoattivitacontroller/createRendicontoAttivita.html",
								idRisorsa,listaCommesse,listaCommesseNonFatturabili,mese,anno,risorsa);
					}
				}else{
					for (RendicontoAttivita ra : listaRendicontoAttivita) {
						if(ra.commessa.idCommessa == commessa.idCommessa){
							ra.delete();
						}
					}
				}
			}
		}
		flash.success("Attivita aggiunta con successo");
		dettaglio(idRisorsa, mese+1,anno);
	}
	
// Cancella rendicontoAttivita
	public static void delete(Integer id){
		RendicontoAttivita rendicontoAttivita = RendicontoAttivita.findById(id);
		rendicontoAttivita.delete();
		flash.success("Rapportino cancellato con successo");
		dettaglio(rendicontoAttivita.risorsa.idRisorsa, rendicontoAttivita.mese, rendicontoAttivita.anno);
	}

// Rapportini incompleti
	public static void rapportiniIncompleti() {
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int mese = (Calendar.getInstance().get(Calendar.MONTH)) - 1;
        int anno = Calendar.getInstance().get(Calendar.YEAR);
        render(mese, listaAnni, anno);
    }
    
    public static void listRapportiniIncompleti(int mese, int anno) {
		if(!MyUtility.MeseEdAnnoToDataFine(mese, anno).before(new Date())){
			validation.addError("data", "Data selezionata non valida");
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("rendicontoattivitacontroller/rapportiniIncompleti.html",listaAnni,mese,anno);
		}
    	List<Risorsa> listaAnomalie = RendicontoAttivita.listRapportiniIncompleti(mese, anno);
    	ValuePaginator paginator = new ValuePaginator(listaAnomalie);
		paginator.setPageSize(10);
		render(paginator, mese, anno);
    }
	
// Auotocomplete delle risorse
	public static void autocompleteRisorsaRapportoAttivita(String term) {
		List<Risorsa> listaRisorse = Risorsa.find("codice like ? or cognome like ?","%"+term+"%","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Risorsa ris:listaRisorse){
			listaResult.add(new DomainWrapper(ris.idRisorsa, ris.codice + " - " + ris.cognome));
		}
		renderJSON(listaResult);
    }
}

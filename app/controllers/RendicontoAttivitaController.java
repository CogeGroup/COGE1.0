package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Commessa;
import models.RendicontoAttivita;
import models.Risorsa;
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
    	int mese = Calendar.getInstance().get(Calendar.MONTH);
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
	public static void chooseRisorsa() {
		List<Integer> listaAnni = MyUtility.createListaAnni();
    	int mese = Calendar.getInstance().get(Calendar.MONTH);
        int anno = Calendar.getInstance().get(Calendar.YEAR);
		render(listaAnni, mese, anno);
    }
	
	public static void createRendicontoAttivita(@Required(message="Inserire una risorsa") Integer idRisorsa, int mese, int anno) {
		if(validation.hasErrors()){
			flash.error("");
			validation.keep();
			chooseRisorsa();
		}
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		mese++;
		
		RendicontoAttivita ra = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa,mese,anno).first();
		if(ra != null){
			mese--;
			validation.addError("meseAnno", "il rapportino per il mese " + mese + "-" + anno +" della risorsa " + risorsa.cognome + " già esistente");
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("RendicontoAttivitaController/chooserisorsa.html", listaAnni, mese, anno);
		}
		if(risorsa == null){
			flash.error("Risorsa non trovata");
			validation.keep();
			chooseRisorsa();
		}
		List<RendicontoAttivita> listaRendicontoAttivita = new ArrayList<RendicontoAttivita>();
		List<Commessa> listaCommesse  = Commessa.findCommesseFatturabiliPerRisorsa(mese, anno, risorsa);
		List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
		render(idRisorsa,listaCommesse,listaCommesseNonFatturabili,mese,anno,listaRendicontoAttivita,risorsa);
	}
	
// Modifica o aggiunge rendicontoAttivita nel rapportino
	public static void aggiungiAttivita(Integer idRisorsa, int mese, int anno){
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		List<RendicontoAttivita> listaRendicontoAttivita = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa,mese,anno).fetch();
		
		// lista Commesse fatturabili piu le commesse non fatturabili gia salvate
		List<Commessa> listaCommesse  = Commessa.findCommesseFatturabiliPerRisorsa(mese, anno, risorsa);
		listaCommesse.addAll(listaCommesseNonFattSalvate(listaRendicontoAttivita));
		
		// lista commesse non fatturabile meno quelle gia salvate
		List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
		List<RendicontoAttivita> attivitaSalvate = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa, mese, anno).fetch();
		listaCommesseNonFatturabili = commesseNonFatturabiliNonSalvate(attivitaSalvate, listaCommesseNonFatturabili);
		
		render(idRisorsa,listaCommesse,listaCommesseNonFatturabili,mese,anno,listaRendicontoAttivita);
	}

	private static List<Commessa> listaCommesseNonFattSalvate(List<RendicontoAttivita> listaRendicontoAttivita) {
		List<Commessa> daAggiungere = new ArrayList<Commessa>();
		for (RendicontoAttivita rendicontoAttivita : listaRendicontoAttivita) {
			if(rendicontoAttivita.commessa.fatturabile == false){
				daAggiungere.add(rendicontoAttivita.commessa);
			}
		}
		return daAggiungere;
	}
	
// Salva il rapportino aggiungendo nuovi rendicontoAttivita
	public static void saveRendicontoAttivita(int mese, int anno, Integer idRisorsa){
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		List<RendicontoAttivita> listaRendicontoAttivita = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa, mese, anno).fetch();
		for (String key : params.all().keySet()) {
			if(key.contains("id_")){
				String oreLavorateString = params.get(key);
				Integer idCommessa = Integer.parseInt(key.substring(3));
				Commessa commessa = Commessa.findById(idCommessa);
				if(!oreLavorateString.equals("")){
					float oreLavorate = 0;
					try {
						oreLavorate = Float.parseFloat(oreLavorateString);
						RendicontoAttivita rendicontoAttivita = new RendicontoAttivita(oreLavorate, mese, anno, risorsa, commessa);
						if(oreLavorate > 0){
							for (RendicontoAttivita ra : listaRendicontoAttivita) {
								if(ra.commessa.idCommessa == rendicontoAttivita.commessa.idCommessa){
									rendicontoAttivita = ra;
									rendicontoAttivita.oreLavorate = oreLavorate;
									rendicontoAttivita.save();
								}
							}
							rendicontoAttivita.save();
						}else{
							for (RendicontoAttivita ra : listaRendicontoAttivita) {
								if(ra.commessa.idCommessa == rendicontoAttivita.commessa.idCommessa){
									ra.delete();
								}
							}
						}
					} catch (IllegalArgumentException e) {
						validation.addError("oreLavorate", "inserire correttamente le ore totali");
						List<Commessa> listaCommesse  = Commessa.findCommesseFatturabiliPerRisorsa(mese, anno, risorsa);
						List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
						render("rendicontoattivitacontroller/createRendicontoAttivita.html",
								idRisorsa,listaCommesse,listaCommesseNonFatturabili,mese,anno,listaRendicontoAttivita);
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
		dettaglio(idRisorsa, mese,anno);
	}
	
	// rimuove tutte le commesse non fatturabili gia inserite nel rapportino
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
    	int mese = Calendar.getInstance().get(Calendar.MONTH);
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
		List<Risorsa> listaRisorse = Risorsa.find("dataOut is null and matricola like ? or cognome like ?","%"+term+"%","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Risorsa r:listaRisorse){
			listaResult.add(new DomainWrapper(r.idRisorsa, r.matricola +" "+r.cognome));
		}
		renderJSON(listaResult);
    }
}

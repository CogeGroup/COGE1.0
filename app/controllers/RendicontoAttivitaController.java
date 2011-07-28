package controllers;

import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Scope.Params;
import secure.SecureCOGE;
import utility.DomainWrapper;
import utility.MyUtility;

import java.util.*;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import models.*;

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
			paginator.setPageSize(5);
			render("RendicontoAttivitaController/dettaglio.html", paginator, risorsa, mese, anno);
		}else{
			listaRapportini = RendicontoAttivita.findByExample(idRisorsa,mese,anno);
			ValuePaginator paginator = new ValuePaginator(listaRapportini);
			paginator.setPageSize(5);
			render("RendicontoAttivitaController/list.html", paginator,mese,anno);
		}
		search();
	}
	
	public static void dettaglio(Integer idRisorsa, int mese, int anno){
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byRisorsaAndMeseAndAnno",risorsa,mese,anno).fetch();
		ValuePaginator paginator = new ValuePaginator(listaRapportini);
		paginator.setPageSize(5);
		render(paginator, risorsa, mese, anno);
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
		RendicontoAttivita ra = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa, mese, anno).first();
		if(ra != null){
			validation.addError("meseAnno", "il rapportino per il mese " + mese++ + "-" + anno +" della risorsa " + risorsa.cognome + " già esistente");
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("RendicontoAttivitaController/chooserisorsa.html", listaAnni, mese, anno);
		}
		if(risorsa == null){
			flash.error("Risorsa non trovata");
			validation.keep();
			chooseRisorsa();
		}
		
		mese++;
		List<Commessa> listaCommesse  = Tariffa.trovaCommessePerRisorsa(mese, anno, risorsa);
		List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
		render(idRisorsa,listaCommesse,listaCommesseNonFatturabili,mese,anno);
	}
	
	public static void saveRendicontoAttivita(int mese, int anno, Integer idRisorsa) {
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		try {
			for (String key : params.all().keySet()) {
				if(key.contains("id_")){
					Integer oreLavorate = Integer.parseInt(params.get(key));
					if(oreLavorate < 0){
						throw new IllegalArgumentException();
					}
					Integer idCommessa = Integer.parseInt(key.substring(3));
					new RendicontoAttivita(oreLavorate, anno, mese, risorsa, (Commessa) Commessa.findById(idCommessa)).save();
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			validation.addError("oreLavorate", "OreLavorate deve essere maggiore o uguale a 0");
			List<Commessa> listaCommesse  = Tariffa.trovaCommessePerRisorsa(mese, anno, risorsa);
			List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
			render("rendicontoattivitacontroller/createRendicontoAttivita.html",idRisorsa,listaCommesse,listaCommesseNonFatturabili,mese,anno);
		}
		
		flash.success("Rapportino aggiunto con successo");
		dettaglio(idRisorsa, mese, anno);
	}
	
// Aggiungi Attivita
	public static void aggiungiAttivita(Integer idRisorsa, int mese, int anno){
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		List<RendicontoAttivita> attivitaSalvate = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa, mese, anno).fetch();
		List<Commessa> listaCommesse  = Tariffa.trovaCommessePerRisorsa(mese, anno, risorsa);
		List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
		
		System.out.println("size: " + listaCommesse.size());
		if (listaCommesse != null && listaCommesse.size() > 0) {
			for (Commessa commessa : listaCommesse) {
				System.out.println("idCommessa: " + commessa.idCommessa);
				for (RendicontoAttivita ra : attivitaSalvate) {
					if(ra.commessa.idCommessa == commessa.idCommessa)
						listaCommesse.remove(commessa);
				}
				if(listaCommesse.size() == 0)
					break;
			}
		}
		if (listaCommesseNonFatturabili != null && listaCommesseNonFatturabili.size() > 0) {
			for (Commessa commessa : listaCommesseNonFatturabili) {
				for (RendicontoAttivita ra : attivitaSalvate) {
					if(ra.commessa.idCommessa == commessa.idCommessa)
						listaCommesseNonFatturabili.remove(commessa);
				}
				if(listaCommesseNonFatturabili.size() == 0)
					break;
			}
		}
		if(listaCommesseNonFatturabili.size() == 0 && listaCommesse.size() == 0){
			flash.success("Nessuna attivita da aggiungere");
			dettaglio(idRisorsa, mese, anno);
		}
		render(idRisorsa,listaCommesse,listaCommesseNonFatturabili,mese,anno);
	}
	
// modifica rapportino
	public static void edit(Integer id){
		RendicontoAttivita rendicontoAttivita = RendicontoAttivita.findById(id);
		render(rendicontoAttivita);
	}
	
	public static void update(@Valid RendicontoAttivita rendicontoAttivita){
		//validazione lettere e no numeri
		if(rendicontoAttivita.oreLavorate == null || rendicontoAttivita.equals("")){
			validation.addError("oreLavorate", "Ore lavorate oblicatorio");
		}
		if(validation.hasErrors()) {
	        render("RendicontoAttivitaController/edit.html", rendicontoAttivita);
	    }
		rendicontoAttivita.save();
    	flash.success("Rapportino modificato con successo");
    	dettaglio(rendicontoAttivita.risorsa.idRisorsa, rendicontoAttivita.mese, rendicontoAttivita.anno);
	}

// Cancella rapportin
	public static void delete(Integer id){
		RendicontoAttivita rendicontoAttivita = RendicontoAttivita.findById(id);
		rendicontoAttivita.delete();
		flash.success("Rapportino cancellato con successo");
		index();
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

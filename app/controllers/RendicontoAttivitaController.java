package controllers;

import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.Scope.Params;
import utility.DomainWrapper;

import java.util.*;
import java.util.Map.Entry;

import models.*;

public class RendicontoAttivitaController extends Controller {

    
	
	public static void index() {
		 render();
    }
	
	public static void chooseRisorsa() {
		
		 render();
   }
	
	
	
	
	
	public static void createRendicontoAttivita(@Required(message="Inserire una risorsa") String idRisorsa, String data) {
		
		if(validation.hasErrors()){
			flash.error("");
			validation.keep();
			chooseRisorsa();
	  }
		Risorsa risorsa = Risorsa.findById(Integer.parseInt(idRisorsa));
		if(risorsa == null){
			flash.error("Risorsa non trovata");
			validation.keep();
			chooseRisorsa();
		}
		String[] meseAnno = data.split("-");
		List<Commessa> listaCommesse  = Tariffa.trovaCommessePerRisorsa(meseAnno[0].trim(), meseAnno[1].trim(), risorsa);
		List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
		render(idRisorsa,listaCommesse,listaCommesseNonFatturabili,meseAnno);
   }
	
	public static void saveRendicontoAttivita(String mese, String anno, Integer idRisorsa) {
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		try {
			for (String key : params.all().keySet()) {
				if(key.contains("id_")){
					Integer oreLavorate = Integer.parseInt(params.get(key));
					if(oreLavorate < 0){
						throw new IllegalArgumentException();
					}
					Integer idCommessa = Integer.parseInt(key.substring(3));
					new RendicontoAttivita(oreLavorate, mese, anno, risorsa, (Commessa) Commessa.findById(idCommessa)).save();
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			validation.addError("oreLavorate", "OreLavorate deve essere maggiore uguale a 0");
			String[] meseAnno = new String[] {mese,anno};
			List<Commessa> listaCommesse  = Tariffa.trovaCommessePerRisorsa(meseAnno[0].trim(), meseAnno[1].trim(), risorsa);
			List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
			render("rendicontoattivitacontroller/createRendicontoAttivita.html",idRisorsa,listaCommesse,listaCommesseNonFatturabili,meseAnno);
		}
		
		flash.success("RendicontoAttivita aggiunto con successo");
		render("LogInController/index.html");
   }
	
	
	public static void autocompleteRisorsaRapportoAttivita(String term) {
		List<Risorsa> listaRisorse = Risorsa.find("matricola like ? or cognome like ?","%"+term+"%","%"+term+"%").fetch();
		  List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		  for(Risorsa r:listaRisorse){
			  listaResult.add(new DomainWrapper(r.idRisorsa, r.matricola +" "+r.cognome));
		  }
		renderJSON(listaResult);
    }
	
	
	
	
	
	
	
	
	

}

package controllers;

import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import utility.DomainWrapper;

import java.util.*;

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
		 render(risorsa,listaCommesse,listaCommesseNonFatturabili);
   }
	
	public static void saveRendicontoAttivita() {
		
		
		
   }
	
	
	public static void autocompleteRisorsaRapportoAttivita(String term) {
		List<Risorsa> listaRisorse = Risorsa.find("dataOut is null and matricola like ? or cognome like ?","%"+term+"%","%"+term+"%").fetch();
		  List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		  for(Risorsa r:listaRisorse){
			  listaResult.add(new DomainWrapper(r.idRisorsa, r.matricola +" "+r.cognome));
		  }
		renderJSON(listaResult);
    }
	
	
	
	
	
	
	
	
	

}

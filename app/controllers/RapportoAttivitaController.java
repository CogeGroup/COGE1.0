package controllers;

import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import utility.DomainWrapper;

import java.util.*;

import models.*;

public class RapportoAttivitaController extends Controller {

    
	
	public static void index() {
		 render();
    }
	
	public static void chooseRisorsa() {
		 render();
   }
	
	
	
	
	
	public static void createRapportoAttivita(@Required(message="Inserire una risorsa") String idRisorsa) {
		
		if(validation.hasErrors()){
			flash.error("");
			validation.keep();
			chooseRisorsa();
	  }
		Risorsa r = Risorsa.findById(Integer.parseInt(idRisorsa));
		if(r == null){
			flash.error("Risorsa non trovata");
			validation.keep();
			chooseRisorsa();
		}
		
	
		 List<Commessa> listaCommesse  = Commessa.findAll();
		 render(r,listaCommesse);
   }
	
	public static void saveRapportoAttivita(String data) {
		System.out.println(data); 
		
		
		
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

package controllers;

import play.*;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
import utility.ConvertToJson;
import utility.DomainWrapper;

import java.util.*;

import models.*;
public class AccountController extends Controller {
	
	 @Catch(Exception.class)
	    public static void logException(Throwable throwable) {
	        //logger e redirect
	    }

    public static void index() {
        render();
    }
    
    
	  public static void listUtenti() {
		  //lista degli utenti in render
		  List<Utente> listaUtenti = Utente.all().fetch();
		   ValuePaginator<Utente> paginator = new ValuePaginator(listaUtenti);
		   render(paginator);
	    }
	  
	  public static void createUtente() {
		  
		 List<Ruolo> listaRuoli = Ruolo.findAll();
		 List<Risorsa> listaRisorse = Risorsa.all().fetch();
		 render(listaRuoli,listaRisorse);
	    }
	  
	  public static void saveUtente(@Valid Utente utente,@Required(message="Inserire una risorsa") String idRisorsa,@Required(message="Inserire un ruolo")String ruolo) {
		      
		  if(validation.hasErrors()){
					flash.error("");
					 List<Ruolo> listaRuoli = Ruolo.findAll();
					 List<Risorsa> listaRisorse = Risorsa.all().fetch();
					render("AccountController/createUtente.html",listaRuoli,listaRisorse,utente);
			  }
			  
			  //Gestione Ruoli
			  String [] listaR = ruolo.split(",");
			   //rimuovo gli eventuali valori doppi
			   Set<Object> uniquesetRuolo = new HashSet<Object>(Arrays.asList(listaR));
				  Object [] uniqueRuolo = uniquesetRuolo.toArray();
				  List<Ruolo> listaRuoliUtente = new ArrayList<Ruolo>();
				 
					  for(int i = 0;i<uniqueRuolo.length;i++){
						  Ruolo r = Ruolo.findById(Integer.parseInt(uniqueRuolo[i].toString()));
						  listaRuoliUtente.add(r);
					  }
					  if(listaRuoliUtente !=null && listaRuoliUtente.size()>0){
						
						  //find della risorsa by matricola e controllo se non esiste nessun utente associato alla risorsa
						  Risorsa r = Risorsa.findById(Integer.parseInt(idRisorsa));
						  if(r!=null){
							  //ho trovato la risorsa da associare al nuovo utente controllo che nessun utente è associato
							  Utente u = Utente.find("byRisorsa", r).first();
							  if(u == null){
								  //save dell'utente
								  utente.risorsa=r;
								  utente.ruoli = listaRuoliUtente;
								  utente.save();
								  flash.success("Utente %s salvato con successo",utente.username);
								  //lista degli utenti in render
								  List<Utente> listaUtenti = Utente.all().fetch();
								   ValuePaginator<Utente> paginator = new ValuePaginator(listaUtenti);
							     render("AccountController/listUtenti.html",paginator);
								  
							  }else{
								  flash.error("risorsa assegnata ad altro utente");
								  	List<Ruolo> listaRuoli = Ruolo.findAll();
									 List<Risorsa> listaRisorse = Risorsa.all().fetch();
									render("AccountController/createUtente.html",listaRuoli,listaRisorse,utente);
							  }
						  }else{
							  flash.error("risorsa non trovata");
							  List<Ruolo> listaRuoli = Ruolo.findAll();
						      List<Risorsa> listaRisorse = Risorsa.all().fetch();
						     render("AccountController/createUtente.html",listaRuoli,listaRisorse,utente);
						  }					  
					
				  }
				  
	    }
	  
	  public static void showUtente(Integer idU) {
		  Utente utente = Utente.findById(idU);
		  String mylist = ConvertToJson.convert(utente.ruoli, "idRuolo", "descrizione"); 
		  render(mylist,utente);
		  
	    }
	  
	  public static void updateUtente(@Valid Utente utente,@Required(message="Inserire una risorsa") String idRisorsa,@Required(message="Inserire un ruolo")String ruolo) {
		  if(validation.hasErrors()){
				flash.error("");
				 utente = Utente.findById(utente.idUtente);
				  String mylist = ConvertToJson.convert(utente.ruoli, "idRuolo", "descrizione"); 
				render("AccountController/showUtente.html",mylist,utente);
		  }
		  //Gestione Ruoli
		  String [] listaR = ruolo.split(",");
		   //rimuovo gli eventuali valori doppi
		   Set<Object> uniquesetRuolo = new HashSet<Object>(Arrays.asList(listaR));
			  Object [] uniqueRuolo = uniquesetRuolo.toArray();
			  List<Ruolo> listaRuoliUtente = new ArrayList<Ruolo>();
			     
				  for(int i = 0;i<uniqueRuolo.length;i++){
					  Ruolo r = Ruolo.findById(Integer.parseInt(uniqueRuolo[i].toString()));
					  listaRuoliUtente.add(r);
				  }
				  if(listaRuoliUtente !=null && listaRuoliUtente.size()>0){
					  //find della risorsa by matricola e controllo se non esiste nessun utente oltre al associato alla risorsa
					  Risorsa r = Risorsa.findById(Integer.parseInt(idRisorsa));
					  
					  
					  if(r!=null){
						  //ho trovato la risorsa da associare al nuovo utente controllo che nessun utente è associato
						  Utente u = Utente.find("byRisorsa", r).first();
						  if(u==null || u.idUtente == utente.idUtente ){
							  //update dell'utente
							  utente.risorsa=r;
							  utente.ruoli = listaRuoliUtente;
							  utente.save();
							  flash.success("Utente %s modificato con successo",utente.username);
							  //lista degli utenti in render
							  List<Utente> listaUtenti = Utente.all().fetch();
							   ValuePaginator<Utente> paginator = new ValuePaginator(listaUtenti);
						     render("AccountController/listUtenti.html",paginator);
							  
						  }else{
							  flash.error("risorsa assegnata ad altro utente");
							  utente = Utente.findById(utente.idUtente);
							  String mylist = ConvertToJson.convert(utente.ruoli, "idRuolo", "descrizione"); 
							  render("AccountController/showUtente.html",mylist,utente);
						  }
					  }else{
						  flash.error("risorsa non trovata");
						  utente = Utente.findById(utente.idUtente);
						  String mylist = ConvertToJson.convert(utente.ruoli, "idRuolo", "descrizione"); 
						  render("AccountController/showUtente.html",mylist,utente);
					  }		
				  }
		  
	    }
	  
	  public static void deleteUtente(Integer idU) {
		   Utente u = Utente.findById(idU);
		   Utente uDel = u.delete();
		   if(uDel !=null){
		    	 flash.success("Utente eliminato con successo");
		    	 List<Utente> listaUtenti = Utente.all().fetch();
			     ValuePaginator<Utente> paginator = new ValuePaginator(listaUtenti);
			     render("AccountController/listUtenti.html",paginator);
		    }else{
		    	 flash.error("Non è stato possibile eliminare l'utente");
		    	 List<Utente> listaUtenti = Utente.all().fetch();
			     ValuePaginator<Utente> paginator = new ValuePaginator(listaUtenti);
			     render("AccountController/listUtenti.html",paginator);
		    }
		  
	    }

	  
	  
	  public static void autocompleteRisorsa(String term) {
		  List<Risorsa> listaRisorse = Risorsa.find("matricola like ? or cognome like ?","%"+term+"%","%"+term+"%").fetch();
		  List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		  for(Risorsa r:listaRisorse){
			  listaResult.add(new DomainWrapper(r.idRisorsa, r.matricola +" "+r.cognome));
		  }
		renderJSON(listaResult);
	  }
	  
	  public static void autocompleteRuolo(String term) {
		  List<Ruolo> listaRuoli = Ruolo.find("descrizione like ?", "%"+term+"%").fetch();
		  List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		  for(Ruolo r:listaRuoli){
			  listaResult.add(new DomainWrapper(r.idRuolo, r.descrizione));
		  }
		   renderJSON(listaResult);
		 
	    }

}

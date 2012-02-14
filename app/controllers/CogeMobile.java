package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Cliente;
import models.Commessa;
import models.Risorsa;
import models.Utente;
import play.db.jpa.GenericModel.JPAQuery;
import play.mvc.*;
import utility.DomainWrapper;

public class CogeMobile extends Controller {

    public static void index() {
    	render();
    }
    
    public static void auth(String username, String psw) {
    	String risposta = "";
    	System.out.println("username: " + username);
    	System.out.println("psw: " + psw);
    	Utente u = Utente.find("byUsernameAndPassword",username,psw).first();
    	if(u!=null)
    	risposta = u.username;
    	renderText(risposta);
    }
    
    public static void getRisorse() {
    	List<Risorsa> listaRisorse = Risorsa.findAll();
    	List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
    	for(Risorsa ris:listaRisorse){
			listaResult.add(new DomainWrapper(ris.idRisorsa, ris.codice + " - " + ris.cognome));
		}
		renderJSON(listaResult);
    	
    	
    }
    
    public static void getCommesse() {
    	List<Commessa> listaCommesse = Commessa.findAll();
    	List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
    	for(Commessa com:listaCommesse){
			listaResult.add(new DomainWrapper(com.idCommessa, com.codice + " - " + com.descrizione));
		}
		renderJSON(listaResult);
    	
    	
    }
    
    public static void getClienti() {
    	List<Cliente> listaClienti = Cliente.findAll();
    	List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
    	for(Cliente cli:listaClienti){
			listaResult.add(new DomainWrapper(cli.idCliente, cli.codice + " - " + cli.nominativo));
		}
		renderJSON(listaResult);
    	
    	
    } 
    
    public static void getSingleCliente(Integer idC) {
    	Cliente c = Cliente.findById(idC);
		renderXml(c);
    	
    	
    } 

}

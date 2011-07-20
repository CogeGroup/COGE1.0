package controllers;

import play.*;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;

import java.util.*;

import models.*;

public class CommesseController extends Controller {

    public static void index() {
        list();
    }
    
    public static void list() {
    	List<Commessa> listaCommesse = Commessa.find("order by codice asc").fetch();
		ValuePaginator paginator = new ValuePaginator(listaCommesse);
//		paginator.setPageSize(5);
		render(paginator);
    }
    
    public static void create() {
    	Commessa commessa = new Commessa();
    	List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
        render(commessa, listaClienti);
    }
    
    public static void save(@Valid Commessa commessa, @Required(message="Selezionare un cliente") Integer idCliente, 
    		String aCorpo, CommessaACorpo commessaACorpo) {
    	
    	if(Commessa.find("byCodice", commessa.codice).first() != null){
    		validation.addError("commessa.codice", "Codice gia esistente");
    	}
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
	        render("CommesseController/create.html", commessa, listaClienti);
	    }
    	
    	if(aCorpo.equals("si")){
    		if(commessaACorpo.importo == 0) {
    			validation.addError("commessaACorpo.importo", "Importo obligatorio");
    			List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
    			render("CommesseController/create.html", commessa, listaClienti);
    		}
    		commessaACorpo.codice = commessa.codice.toUpperCase();
    		commessaACorpo.descrizione = commessa.descrizione;
    		commessaACorpo.dataInizioCommessa = commessa.dataInizioCommessa;
    		commessaACorpo.fatturabile = true;
    		commessaACorpo.cliente = Cliente.findById(idCliente);
    		commessaACorpo.create();
    	}else{
    		commessa.codice = commessa.codice.toUpperCase();
        	commessa.cliente = Cliente.findById(idCliente);
        	commessa.create();
    	}
    	
    	flash.success("%s aggiunta con successo", commessa.codice);
    	list();
    }
    
    public static void edit(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	if(commessa instanceof CommessaACorpo){
    		editACorpo(id);
    	}
    	List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
        render(commessa, listaClienti);
    }
    
    public static void update(@Valid Commessa commessa, @Required(message="Selezionare un cliente") Integer idCliente) {
    	if(Commessa.find("byCodice", commessa.codice).first() != null && Commessa.find("byCodice", commessa.codice).first() != commessa){
    		validation.addError("commessa.codice", "Codice gia esistente");
    	}
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
	        render("CommesseController/edit.html", commessa, listaClienti);
	    }
    	commessa.codice = commessa.codice.toUpperCase();
    	commessa.cliente = Cliente.findById(idCliente);
    	commessa.save();
    	flash.success("%s modificata con successo", commessa.codice);
        list();
    }
    
    public static void editACorpo(Integer id) {
    	CommessaACorpo commessaACorpo = CommessaACorpo.findById(id);
    	List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
        render(commessaACorpo, listaClienti);
    }
    
    public static void updateACorpo(@Valid CommessaACorpo commessaACorpo, @Required(message="Selezionare un cliente") Integer idCliente) {
    	if(CommessaACorpo.find("byCodice", commessaACorpo.codice).first() != null && Commessa.find("byCodice", commessaACorpo.codice).first() != commessaACorpo){
    		validation.addError("commessa.codice", "Codice gia esistente");
    		System.out.println("1");
    	}
    	if(validation.hasErrors()) {
    		System.out.println("2");
    		List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
	        render("CommesseController/edit.html", commessaACorpo, listaClienti);
	    }
    	System.out.println("3");
    	commessaACorpo.codice = commessaACorpo.codice.toUpperCase();
    	commessaACorpo.cliente = Cliente.findById(idCliente);
    	commessaACorpo.save();
    	flash.success("%s modificata con successo", commessaACorpo.codice);
        list();
    }
    
    public static void show(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	if(commessa instanceof CommessaACorpo){
    		showACorpo(id);
    	}
        render(commessa);
    }
    
    public static void showACorpo(Integer id) {
    	CommessaACorpo commessaACorpo = CommessaACorpo.findById(id);
        render(commessaACorpo);
    }
    
    public static void delete(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	commessa.delete();
    	flash.success("%s cancellata con successo", commessa.codice);
    	list();
    }

}

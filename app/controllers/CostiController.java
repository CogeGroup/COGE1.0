package controllers;

import play.*;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
import secure.SecureCOGE;

import java.util.*;

import models.*;

@With(SecureCOGE.class)
public class CostiController extends Controller {

    public static void index(Integer id) {
        list(id);
    }
    
    public static void list(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<Costo> listaCosti = Costo.find("byRisorsa", risorsa).fetch();
    	ValuePaginator paginator = new ValuePaginator(listaCosti);
    	paginator.setPageSize(5);
        render(paginator, risorsa);
    }
    
    public static void create(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	Costo costo = new Costo(risorsa);
    	render(costo);
	}
    public static void save(@Valid Costo costo){
    	
    	// Validazione del form
    	if(validation.hasErrors()) {
        	render("CostiController/create.html",costo);
    	}
    	
    	// Salvataggio tariffa
        costo.save();
        flash.success("Costo aggiunto con successo");
    	list(costo.risorsa.idRisorsa);
    	
    }
    public static void edit(Integer idCosto){
    	Costo costo = Costo.findById(idCosto);
    	render(costo);
    }
    public static void update(@Valid Costo costo){
    	// Validazione del form
    	if(validation.hasErrors()) {
        	render("CostiController/edit.html",costo);
    	}
    	costo.save();
    	flash.success("Costo aggiornato con successo");
    	list(costo.risorsa.idRisorsa);
    }
    public static void delete(Integer idCosto,Integer idRisorsa){
    	Costo costo = Costo.findById(idCosto);
    	if(costo == null){
    		flash.error("Costo non trovato");
    		list(idRisorsa);
    	}
    	costo.delete();
    	flash.success("Costo rimosso con successo");
    	list(idRisorsa);
    }
    

}

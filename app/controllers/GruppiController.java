package controllers;

import java.util.List;

import models.Gruppo;
import models.Risorsa;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;

public class GruppiController extends Controller {

    public static void list() {
    	ValuePaginator paginator = new ValuePaginator(Gruppo.findAll());
		paginator.setPageSize(10);
		render(paginator);
    }
    
    public static void create() {
    	Gruppo gruppo = new Gruppo();
    	render(gruppo);
    }
    
    public static void save(@Valid Gruppo gruppo) {
    	if(validation.hasErrors()) {
	        render("GruppiController/create.html", gruppo);
	    }
    	gruppo.codice = gruppo.codice.toUpperCase();
    	gruppo.save();
    	flash.success("%s aggiunto con successo", gruppo.descrizione);
    	list();
    }
    
    public static void edit(Integer id) {
    	Gruppo gruppo = Gruppo.findById(id);
    	render(gruppo);
    }
    
    public static void update(@Valid Gruppo gruppo) {
    	if(validation.hasErrors()) {
	        render("GruppiController/edit.html", gruppo);
	    }
    	gruppo.codice = gruppo.codice.toUpperCase();
    	gruppo.save();
    	flash.success("%s modificato con successo", gruppo.descrizione);
    	list();
    }
    
    public static void show(Integer id) {
    	Gruppo gruppo = Gruppo.findById(id);
    	
    	ValuePaginator listaRisorse = new ValuePaginator(Risorsa.find("byGruppo", gruppo).fetch());
    	listaRisorse.setPageSize(10);
    	
    	ValuePaginator listaCommesse = new ValuePaginator(gruppo.commesse);
    	listaCommesse.setPageSize(10);
    	
    	render(gruppo, listaRisorse, listaCommesse);
    }
    
    public static void delete(Integer id) {
    	Gruppo gruppo = Gruppo.findById(id);
    	if(gruppo.commesse != null && gruppo.commesse.size() > 0){
    		flash.error("Il gruppo ha delle commesse associate");
    		list();
    	}
    	if(gruppo.risorse != null && gruppo.risorse.size() > 0){
			flash.error("Il gruppo ha delle risorse associate");
			list();
		}
    	gruppo.delete();
    	flash.success("%s cancellato con successo", gruppo.descrizione);
    	list();
    }

}

package controllers;

import java.util.List;

import javax.persistence.PersistenceException;

import models.GiornateLavorative;
import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.MyUtility;

@With(SecureCOGE.class)
public class GiornateLavorativeController extends Controller {

    public static void index() {
    	list();
    }
    
    public static void list() {
    	List<GiornateLavorative> lista = GiornateLavorative.findAll();
        render(lista);
    }
    
    public static void create() {
    	GiornateLavorative giornateLavorative = new GiornateLavorative();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
        render(giornateLavorative, listaAnni);
    }
    
    public static void save(@Valid GiornateLavorative giornateLavorative) {
    	if(validation.hasErrors()) {
	        render("CertificazioniController/create.html", giornateLavorative);
	    }
    	giornateLavorative.save();
    	flash.success("Giorni Lavorativi %s aggiunti con successo", ""+giornateLavorative.mese + "/" + giornateLavorative.anno);
    	list();
    }
    
    public static void edit(Integer idGiornateLavorative) {
    	GiornateLavorative giornateLavorative = GiornateLavorative.findById(idGiornateLavorative);
    	List<Integer> listaAnni = MyUtility.createListaAnni();
        render(giornateLavorative, listaAnni);
    }
    
    public static void update(@Valid GiornateLavorative giornateLavorative) {
    	if(validation.hasErrors()) {
	        render("CertificazioniController/edit.html", giornateLavorative);
	    }
    	giornateLavorative.save();
    	flash.success("Giorni Lavorativi %s modificati con successo", ""+giornateLavorative.mese + "/" + giornateLavorative.anno);
    	list();
    }
    
    public static void delete(Integer idGiornateLavorative) {
    	GiornateLavorative giornateLavorative = GiornateLavorative.findById(idGiornateLavorative);
    	try {
    		giornateLavorative.delete();
    		flash.success("Giorni Lavorativi %s eliminati con successo", ""+giornateLavorative.mese + "/" + giornateLavorative.anno);
		} catch (Exception e) {
			flash.error("Impossibile eliminare i Giorni Lavorativi %s", ""+giornateLavorative.mese + "/" + giornateLavorative.anno);
		} finally {
			list();
		}
    }

}

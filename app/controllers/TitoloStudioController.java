package controllers;

import javax.persistence.PersistenceException;

import models.TitoloStudio;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;

@With(SecureCOGE.class)
public class TitoloStudioController extends Controller {

	public static void list() {
    	ValuePaginator paginator = new ValuePaginator(TitoloStudio.findAll());
    	paginator.setPageSize(10);
        render(paginator);
    }
    
    public static void show(Integer idTitoloStudio) {
    	TitoloStudio certificazione = TitoloStudio.findById(idTitoloStudio);
        render(certificazione);
    }
    
    public static void create() {
    	TitoloStudio titoloStudio = new TitoloStudio();
        render(titoloStudio);
    }
    
    public static void save(@Valid TitoloStudio titoloStudio) {
    	if(validation.hasErrors()) {
	        render("TitoloStudioController/create.html", titoloStudio);
	    }
    	titoloStudio.codice = titoloStudio.codice.toUpperCase();
    	titoloStudio.save();
    	flash.success("Titolo di Studio %s aggiunto con successo", titoloStudio.descrizione);
    	list();
    }
    
    public static void edit(Integer idTitoloStudio) {
    	TitoloStudio titoloStudio = TitoloStudio.findById(idTitoloStudio);
        render(titoloStudio);
    }
    
    public static void update(@Valid TitoloStudio titoloStudio) {
    	if(validation.hasErrors()) {
	        render("TitoloStudioController/edit.html", titoloStudio);
	    }
    	titoloStudio.codice = titoloStudio.codice.toUpperCase();
    	titoloStudio.save();
    	flash.success("Titolo di Studio %s modificato con successo", titoloStudio.descrizione);
    	list();
    }
    
    public static void delete(Integer idTitoloStudio) {
    	TitoloStudio titoloStudio = TitoloStudio.findById(idTitoloStudio);
    	try {
    		titoloStudio.delete();
    		flash.success("Titolo di Studio %s eliminata con successo", titoloStudio.descrizione);
		} catch (PersistenceException e) {
			flash.error("Impossibile eliminare il Titolo di Studio %s ha delle Risorse associate", titoloStudio.descrizione);
		} finally {
			list();
		}
    }

}

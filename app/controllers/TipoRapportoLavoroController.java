package controllers;

import play.*;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
import secure.SecureCOGE;
import utility.DomainWrapper;

import java.util.*;

import javax.persistence.PersistenceException;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.exception.ConstraintViolationException;

import models.*;

@With(SecureCOGE.class)
public class TipoRapportoLavoroController extends Controller {

    public static void index() {
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by codice").fetch();
    	ValuePaginator paginator = new ValuePaginator(listaTipoRapportoLavoro);
    	paginator.setPageSize(10);
        render(paginator);
    }
    
    public static void search(Integer idTipoRapportoLavoro) {
    	if(idTipoRapportoLavoro == null || idTipoRapportoLavoro.equals("")){
    		index();
    	}
    	TipoRapportoLavoro tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	List<TipoRapportoLavoro> listaTipiRapportoLavoro = new ArrayList<TipoRapportoLavoro>();
    	listaTipiRapportoLavoro.add(tipoRapportoLavoro);
		ValuePaginator paginator = new ValuePaginator(listaTipiRapportoLavoro);
		paginator.setPageSize(10);
		render("TipoRapportoLavoroController/index.html",paginator);
    }
    
    public static void create() {
    	TipoRapportoLavoro tipoRapportoLavoro = new TipoRapportoLavoro();
    	render(tipoRapportoLavoro);
    }
    
    public static void save(@Valid TipoRapportoLavoro tipoRapportoLavoro) {
    	if (validation.hasErrors()) {
			renderTemplate("TipoRapportoLavoroController/create.html", tipoRapportoLavoro);
		}
    	tipoRapportoLavoro.save();
		flash.success("Tipo Rapporto Lavoro %s inserito con successo", tipoRapportoLavoro.codice);
		index();
    }

    public static void edit(Integer idTipoRapportoLavoro) {
    	TipoRapportoLavoro tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	render(tipoRapportoLavoro);
    }
    
    public static void update(@Valid TipoRapportoLavoro tipoRapportoLavoro) {
    	if (validation.hasErrors()) {
			renderTemplate("TipoRapportoLavoroController/edit.html", tipoRapportoLavoro);
		}
    	tipoRapportoLavoro.save();
		flash.success("Tipo Rapporto Lavoro %s modificato con successo", tipoRapportoLavoro.codice);
		index();
    }

    public static void delete(Integer idTipoRapportoLavoro) {
    	TipoRapportoLavoro tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	try {
    		tipoRapportoLavoro.delete();
    		flash.success("Tipo Rapporto Lavoro %s eliminato con successo", tipoRapportoLavoro.codice);
		} catch (PersistenceException e) {
			flash.error("Impossibile eliminare il Tipo Rapporto Lavoro %s - Rapporti di lavoro associati", tipoRapportoLavoro.codice);
		} finally {
			index();
		}
    }
    
    public static void show(Integer idTipoRapportoLavoro) {
    	TipoRapportoLavoro tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	render(tipoRapportoLavoro);
    	
    }
    
 // Auotocomplete dei tipo rapporto lavoro
	public static void autocompleteTipoRapportoLavoro(String term) {
		List<TipoRapportoLavoro> listaCommesse = TipoRapportoLavoro.find("codice like ? or descrizione like ?","%"+term+"%","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(TipoRapportoLavoro tpl:listaCommesse){
			listaResult.add(new DomainWrapper(tpl.idTipoRapportoLavoro, tpl.codice +" - "+ tpl.descrizione));
		}
		renderJSON(listaResult);
    }
    
}

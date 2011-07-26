package controllers;

import play.*;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;

import java.util.*;

import javax.persistence.PersistenceException;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.exception.ConstraintViolationException;

import models.*;

public class TipoRapportoLavoroController extends Controller {

    public static void index() {
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by codice").fetch();
        render(listaTipoRapportoLavoro);
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
}

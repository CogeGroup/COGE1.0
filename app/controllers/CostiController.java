package controllers;

import java.util.List;

import models.Costo;
import models.Risorsa;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.MyUtility;

@With(SecureCOGE.class)
public class CostiController extends Controller {

    public static void index(Integer id) {
        list(id);
    }
    
    public static void list(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<Costo> listaCosti = Costo.find("byRisorsa", risorsa).fetch();
    	ValuePaginator paginator = new ValuePaginator(listaCosti);
    	paginator.setPageSize(10);
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
    		render("CostiController/create.html", costo);
    	}
    	//se esistono già dei costi associati alla risorsa
    	//procedo alla chiusura dell'ultimo (solo se ha dataFine null) 
    	List<Costo> listaCosti = Costo.find("byRisorsa", costo.risorsa).fetch();
    	if(listaCosti != null && listaCosti.size() > 0) {
    		Costo ultimoCosto = listaCosti.get(listaCosti.size() - 1);
        	if (ultimoCosto.dataFine == null) {
				ultimoCosto.dataFine = MyUtility.subOneDay(costo.dataInizio);
				ultimoCosto.save();
			}
    	}
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
    		render("CostiController/edit.html", costo);
    	}
    	costo.save();
    	flash.success("Costo aggiornato con successo");
    	list(costo.risorsa.idRisorsa);
    }
    public static void delete(Integer idCosto){
    	Costo costo = Costo.findById(idCosto);
    	costo.delete();
    	flash.success("Costo rimosso con successo");
    	list(costo.risorsa.idRisorsa);
    }
}
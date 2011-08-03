package controllers;

import play.*;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
import secure.SecureCOGE;
import utility.MyUtility;

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
    	Comparator<Costo> comparator = new Comparator<Costo>() {
			
			@Override
			public int compare(Costo o1, Costo o2) {
				return o1.dataInizio.compareTo(o2.dataInizio);
			}
		};
		Collections.sort(listaCosti, comparator);
    	ValuePaginator paginator = new ValuePaginator(listaCosti);
    	paginator.setPageSize(5);
        render(paginator, risorsa);
    }
    
    public static void create(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	Costo costo = new Costo(risorsa);
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	render(costo, listaAnni);
	}
    public static void save(@Valid Costo costo){
    	// Validazione del form
    	if(validation.hasErrors()) {
    		List<Integer> listaAnni = MyUtility.createListaAnni();
        	render("CostiController/create.html",costo, listaAnni);
    	}
    	costo.dataInizio = MyUtility.MeseEdAnnoToDataInizio(costo.meseInizio, costo.annoInizio);
    	costo.dataFine = costo.meseFine == -1 ? null : MyUtility.MeseEdAnnoToDataFine(costo.meseFine, costo.annoFine);
    	// Salvataggio tariffa
        costo.save();
        flash.success("Costo aggiunto con successo");
    	list(costo.risorsa.idRisorsa);
    	
    }
    public static void edit(Integer idCosto){
    	Costo costo = Costo.findById(idCosto);
    	costo.meseInizio = MyUtility.getMeseFromDate(costo.dataInizio);
    	costo.annoInizio = MyUtility.getAnnoFromDate(costo.dataInizio);
    	costo.meseFine = costo.dataFine == null ? -1 : MyUtility.getMeseFromDate(costo.dataFine);
    	costo.annoFine = costo.dataFine == null ? -1 : MyUtility.getAnnoFromDate(costo.dataFine);
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	render(costo, listaAnni);
	}
    public static void update(@Valid Costo costo){
    	// Validazione del form
    	if(validation.hasErrors()) {
    		List<Integer> listaAnni = MyUtility.createListaAnni();
        	render("CostiController/edit.html",costo, listaAnni);
    	}
    	costo.dataInizio = MyUtility.MeseEdAnnoToDataInizio(costo.meseInizio, costo.annoInizio);
    	costo.dataFine = costo.meseFine == -1 ? null : MyUtility.MeseEdAnnoToDataFine(costo.meseFine, costo.annoFine);
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

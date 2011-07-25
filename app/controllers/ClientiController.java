package controllers;

import play.*;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
import java.util.*;
import models.*;

public class ClientiController extends Controller {

    public static void index() {
        list();
    }
    
    public static void list() {
    	List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
		ValuePaginator paginator = new ValuePaginator(listaClienti);
		paginator.setPageSize(5);
		render(paginator);
    }
    
    public static void create() {
    	Cliente cliente = new Cliente();
        render(cliente);
    }
    
    public static void save(@Valid Cliente cliente) {
    	if(Cliente.find("byCodice", cliente.codice).first() != null){
    		validation.addError("cliente.codice", "Codice gia esistente");
    	}
    	if(validation.hasErrors()) {
	        render("ClientiController/create.html", cliente);
	    }
    	cliente.codice = cliente.codice.toUpperCase();
    	cliente.attivo = true;
    	cliente.save();
    	flash.success("%s aggiunto con successo", cliente.nominativo);
    	list();
    }
    
    public static void edit(Integer id) {
    	Cliente cliente = Cliente.findById(id);
        render(cliente);
    }
    
    public static void update(@Valid Cliente cliente) {
    	if(Cliente.find("byCodice", cliente.codice).first() != null && Cliente.find("byCodice", cliente.codice).first() != cliente){
    		validation.addError("cliente.codice", "Codice gia esistente");
    	}
    	if(validation.hasErrors()) {
	        render("ClientiController/edit.html", cliente);
	    }
    	cliente.codice = cliente.codice.toUpperCase();
    	System.out.println(cliente.attivo);
    	cliente.save();
    	flash.success("%s modificato con successo", cliente.nominativo);
        list();
    }
    
    public static void show(Integer id) {
    	Cliente cliente = Cliente.findById(id);
        render(cliente);
    }
    
    public static void delete(Integer id) {
    	Cliente cliente = Cliente.findById(id);
    	cliente.attivo = false;
    	cliente.save();
    	chiudiCommesseByCliente(cliente);
    	flash.success("%s cancellato con successo", cliente.nominativo);
    	list();
    }
    
    private static void chiudiCommesseByCliente(Cliente cliente) {
    	for (Commessa commessa : cliente.commesse) {
    		if(commessa.dataFineCommessa == null){
				commessa.dataFineCommessa = new Date();
	    		commessa.save();
	    		chiudiTariffeByCommessa(commessa);
    		}
    		// se c'è una commessa che comincia dopo oggi?
    		if(commessa.dataInizioCommessa == null || commessa.dataInizioCommessa.after(new Date())){
    			commessa.delete();
    		}
		}
    }
    
    private static void chiudiTariffeByCommessa(Commessa commessa) {
    	for (Tariffa tariffa : commessa.tariffe) {
    		if(tariffa.dataFine == null) {
	    		tariffa.dataFine = new Date();
	    		tariffa.save();
    		}
//    		// se c'è una tariffa che comincia dopo oggi?
    		if(tariffa.dataInizio.after(new Date())){
				tariffa.delete();
			}
		}
    }

}

package controllers;

import play.*;
import play.data.validation.Min;
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
		paginator.setPageSize(5);
		render(paginator);
    }
    
    public static void create() {
    	Commessa commessa = new Commessa();
    	List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
    	String aCorpo = "no";
    	float importo = 0;
        render(commessa, listaClienti, aCorpo, importo);
    }
    
    public static void save(@Valid Commessa commessa, @Required(message="Selezionare un cliente") Integer idCliente, 
    		String aCorpo, float importo) {
    	
    	if(Commessa.find("byCodice", commessa.codice).first() != null){
    		validation.addError("commessa.codice", "Codice gia esistente");
    	}
    	if(commessa.fatturabile == true && (commessa.dataInizioCommessa == null || commessa.dataInizioCommessa.equals(""))) {
    		validation.addError("commessa.dataInizioCommessa", "Una commessa fatturabile deve avere una data di inizio");
    	}
    	if(validation.hasErrors()) {
    		commessa.cliente = (Cliente) (idCliente == null ? null : Cliente.findById(idCliente));
    		List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
	        render("CommesseController/create.html", commessa, listaClienti, aCorpo, importo);
	    }
    	
    	if(aCorpo != null && aCorpo.equals("si")){
    		if(commessa.dataInizioCommessa == null || commessa.dataInizioCommessa.equals("")) {
    			validation.addError("commessaACorpo.dataInizioCommessa", "Data obligatoria");
    		}
    		if(importo > 0.1) {
    			validation.addError("commessaACorpo.importo", "L'importo deve essere maggiore di 0.1");
    		}
    		if(validation.hasErrors()) {
        		commessa.cliente = (Cliente) (idCliente == null ? null : Cliente.findById(idCliente));
        		List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
    	        render("CommesseController/create.html", commessa, listaClienti, aCorpo, importo);
    	    }
    		CommessaACorpo commessaACorpo = new CommessaACorpo();
    		commessaACorpo.codice = commessa.codice.toUpperCase();
    		commessaACorpo.descrizione = commessa.descrizione;
    		commessaACorpo.dataInizioCommessa = commessa.dataInizioCommessa;
    		commessaACorpo.fatturabile = true;
    		commessaACorpo.cliente = Cliente.findById(idCliente);
    		commessaACorpo.importo = importo;
    		commessaACorpo.save();
    	}else{
    		commessa.codice = commessa.codice.toUpperCase();
        	commessa.cliente = Cliente.findById(idCliente);
        	commessa.save();
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
    		validation.addError("commessaACorpo.codice", "Codice gia esistente");
    	}
    	if(commessaACorpo.dataInizioCommessa == null || commessaACorpo.dataInizioCommessa.equals("")){
    		validation.addError("commessaACorpo.dataInizioCommessa", "Data obligatoria");
    	}
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
	        render("CommesseController/editACorpo.html", commessaACorpo, listaClienti);
	    }
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

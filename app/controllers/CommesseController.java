package controllers;

import play.*;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
import secure.SecureCOGE;

import java.util.*;

import models.*;

@With(SecureCOGE.class)
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
    	List<Cliente> listaClienti = Cliente.findAllAttivo();
    	String aCorpo = "no";
    	float importo = 0;
        render(commessa, listaClienti, aCorpo, importo);
    }
    
    public static void save(@Valid Commessa commessa, Integer idCliente, 
    		String aCorpo, float importo) {
    	commessa.cliente = Cliente.findById(idCliente);
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.findAllAttivo();
	        render("CommesseController/create.html", commessa, listaClienti, aCorpo, importo);
	    }
    	
    	if(aCorpo != null && aCorpo.equals("si")){
    		if(importo < 0.1) {
        		validation.addError("importo", "L'importo deve essere maggiore di 0");
        	}
    		if(commessa.dataInizioCommessa == null || commessa.dataInizioCommessa.equals("")) {
    			validation.addError("commessaACorpo.dataInizioCommessa", "Data obligatoria");
    		}
    		if(validation.hasErrors()) {
        		List<Cliente> listaClienti = Cliente.findAllAttivo();
    	        render("CommesseController/create.html", commessa, listaClienti, aCorpo, importo);
    	    }
    		CommessaACorpo commessaACorpo = new CommessaACorpo();
    		commessaACorpo.codice = commessa.codice.toUpperCase();
    		commessaACorpo.descrizione = commessa.descrizione;
    		commessaACorpo.dataInizioCommessa = commessa.dataInizioCommessa;
    		commessaACorpo.fatturabile = true;
    		commessaACorpo.cliente = commessa.cliente;
    		commessaACorpo.importo = importo;
    		commessaACorpo.save();
    	}else{
    		commessa.codice = commessa.codice.toUpperCase();
    		commessa.dataInizioCommessa = commessa.fatturabile == false ? null : commessa.dataInizioCommessa;
        	commessa.save();
    	}
    	
    	flash.success("%s aggiunta con successo", commessa.codice);
    	list();
    }
    
    public static void edit(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	List<Cliente> listaClienti = Cliente.findAllAttivo();
        render(commessa, listaClienti);
    }
    
    public static void update(@Valid Commessa commessa, Integer idCliente) {
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.findAllAttivo();
	        render("CommesseController/edit.html", commessa, listaClienti);
	    }
    	commessa.codice = commessa.codice.toUpperCase();
    	commessa.cliente = Cliente.findById(idCliente);
    	commessa.dataInizioCommessa = commessa.fatturabile ? commessa.dataInizioCommessa : null;
    	commessa.dataFineCommessa = commessa.fatturabile ? commessa.dataFineCommessa : null;
    	if(commessa.dataFineCommessa != null){
    		Tariffa.chiudiTariffeByCommessa(commessa);
    	}
    	commessa.save();
    	flash.success("%s modificata con successo", commessa.codice);
        list();
    }
    
    public static void show(Integer id) {
    	Commessa commessa = Commessa.findById(id);
        render(commessa);
    }
    
    public static void delete(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	if(commessa.dataFineCommessa != null){
    		flash.success("Commessa gia chiusa");
    		list();
    	}
    	
    	if(commessa.dataInizioCommessa.after(new Date()))
    		commessa.dataFineCommessa = commessa.dataInizioCommessa;
    	else
    		commessa.dataFineCommessa = new Date();
    	
    	Tariffa.chiudiTariffeByCommessa(commessa);
    	commessa.save();
    	flash.success("%s cancellata con successo", commessa.codice);
    	list();
    }
    
}

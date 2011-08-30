package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Cliente;
import models.Commessa;
import models.Risorsa;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.DomainWrapper;

@With(SecureCOGE.class)
public class ClientiController extends Controller {
	
    public static void index() {
        list();
    }
    
    public static void list() {
    	List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
		ValuePaginator paginator = new ValuePaginator(listaClienti);
		paginator.setPageSize(10);
		render(paginator);
    }
    
    public static void search(Integer idCliente) {
    	if(idCliente == null || idCliente.equals("")){
    		list();
    	}
    	Cliente cliente = Cliente.findById(idCliente);
    	List<Cliente> listaClienti = new ArrayList<Cliente>();
    	listaClienti.add(cliente);
		ValuePaginator paginator = new ValuePaginator(listaClienti);
		paginator.setPageSize(10);
		render("ClientiController/list.html",paginator);
    }
    
    public static void create() {
    	Cliente cliente = new Cliente();
        render(cliente);
    }
    
    public static void save(@Valid Cliente cliente) {
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
    	if(validation.hasErrors()) {
	        render("ClientiController/edit.html", cliente);
	    }
    	if(cliente.attivo == false){
    		Commessa.chiudiCommesseByCliente(cliente);
    	}
    	cliente.codice = cliente.codice.toUpperCase();
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
    	Commessa.chiudiCommesseByCliente(cliente);
    	cliente.save();
    	flash.success("%s cancellato con successo", cliente.nominativo);
    	list();
    }
    
 // Auotocomplete dei clienti
	public static void autocompleteCliente(String term) {
		List<Cliente> listaClienti = Cliente.find("codice like ? or nominativo like ?","%"+term+"%","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Cliente cl:listaClienti){
			listaResult.add(new DomainWrapper(cl.idCliente, cl.codice +" - "+ cl.nominativo));
		}
		renderJSON(listaResult);
    }
}
package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Cliente;
import models.Commessa;
import models.CommessaACorpo;
import models.Risorsa;
import models.Tariffa;
import models.TipoCommessa;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.DomainWrapper;

@With(SecureCOGE.class)
public class CommesseController extends Controller {

    public static void index() {
        list();
    }
    
    public static void list() {
		ValuePaginator paginator = new ValuePaginator(Commessa.find("ORDER BY fatturabile desc").fetch());
		paginator.setPageSize(10);
		render(paginator);
    }
    
    public static void listFatturabili() {
    	ValuePaginator paginator = new ValuePaginator(Commessa.findCommesseFatturabili());
		paginator.setPageSize(10);
		render("CommesseController/list.html",paginator);
    }
    
    public static void listNonFatturabili() {
    	ValuePaginator paginator = new ValuePaginator(Commessa.findCommesseNonFatturabili());
		paginator.setPageSize(10);
		render("CommesseController/list.html",paginator);
    }
    
    public static void listACorpo() {
		ValuePaginator paginator = new ValuePaginator(CommessaACorpo.findCommesseACorpo());
		paginator.setPageSize(10);
		render("CommesseController/list.html",paginator);
    }
    
    public static void search(Integer idCommessa) {
    	if(idCommessa == null || idCommessa.equals("")){
    		list();
    	}
    	Commessa commessa = Commessa.findById(idCommessa);
    	List<Commessa> listaCommesse = new ArrayList<Commessa>();
    	listaCommesse.add(commessa);
		ValuePaginator paginator = new ValuePaginator(listaCommesse);
		paginator.setPageSize(10);
		render("CommesseController/list.html",paginator);
    }
    
    public static void create() {
    	Commessa commessa = new Commessa();
    	List<Cliente> listaClienti = Cliente.findAllAttivo();
    	List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
    	String aCorpo = "no";
    	float importo = 0;
        render(commessa, listaClienti, listaTipiCommessa, aCorpo, importo);
    }
    
    public static void save(@Valid Commessa commessa, String aCorpo, float importo) {
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.findAllAttivo();
    		List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
	        render("CommesseController/create.html", commessa, listaClienti, listaTipiCommessa, aCorpo, importo);
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
        		List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
    	        render("CommesseController/create.html", commessa, listaClienti, listaTipiCommessa, aCorpo, importo);
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
    	List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
        render(commessa, listaClienti, listaTipiCommessa);
    }
    
    public static void update(@Valid Commessa commessa) {
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.findAllAttivo();
    		List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
	        render("CommesseController/edit.html", commessa, listaClienti,listaTipiCommessa);
	    }
    	commessa.codice = commessa.codice.toUpperCase();
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
    	List<Risorsa> listaRisorse = commessa.fatturabile == true ? Risorsa.findByCommessa(commessa) : null;
        render(commessa,listaRisorse);
    }
    
    public static void delete(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	if(commessa.fatturabile == false){
    		flash.success("Non si puo' chiudere una commessa non fatturabile");
    		list();
    	}
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
    
    // Auotocomplete dei commessa
	public static void autocompleteCommessa(String term) {
		List<Commessa> listaCommesse = Commessa.find("descrizione like ?","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Commessa com:listaCommesse){
			listaResult.add(new DomainWrapper(com.idCommessa, com.descrizione));
		}
		renderJSON(listaResult);
    }
	
	public static void showCommessaModalBox(Integer id) {
		Commessa commessa = Commessa.findById(id);
        render(commessa);
	}
}

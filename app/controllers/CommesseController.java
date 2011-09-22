package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Cliente;
import models.Commessa;
import models.CommessaACorpo;
import models.Gruppo;
import models.Risorsa;
import models.Tariffa;
import models.TipoCommessa;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.ConvertToJson;
import utility.DomainWrapper;

@With(SecureCOGE.class)
public class CommesseController extends Controller {

    public static void index() {
        list();
    }
    
    public static void list() {
		ValuePaginator paginator = new ValuePaginator(Commessa.find("ORDER BY calcoloRicavi desc").fetch());
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
    
    public static void show(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	List<Gruppo> listaGruppi = Gruppo.findByCommessa(commessa);
    	List<Risorsa> listaRisorse = commessa.calcoloRicavi == true ? Risorsa.findByCommessa(commessa) : null;
    	render(commessa, listaRisorse, listaGruppi);
    }
    
    public static void create() {
    	Commessa commessa = new Commessa();
    	List<Cliente> listaClienti = Cliente.findAllAttivo();
    	List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
    	String aCorpo = "no";
    	float importo = 0;
    	List<Gruppo> lista = new ArrayList<Gruppo>();
    	String listaGruppi = ConvertToJson.convert(lista, "idGruppo", "descrizione"); 
        render(commessa, listaClienti, listaTipiCommessa, aCorpo, importo, listaGruppi);
    }
    
    public static void save(@Valid Commessa commessa, String aCorpo, float importo, String gruppo) {
    	List<Gruppo> lista = new ArrayList<Gruppo>();
    	if(!gruppo.equals("")) {
	    	//Lista Gruppi
			String [] listaG = gruppo.split(",");
			//rimuovo gli eventuali valori doppi
			Set<Object> uniquesetGruppo = new HashSet<Object>(Arrays.asList(listaG));
			Object [] uniqueGruppo = uniquesetGruppo.toArray();
			for(int i = 0;i<uniqueGruppo.length;i++){
				lista.add((Gruppo) Gruppo.findById(Integer.parseInt(uniqueGruppo[i].toString())));
			}
    	}
    	if(commessa.calcoloRicavi == false && (gruppo == null || gruppo.equals(""))){
    		validation.addError("gruppo", "Inserire un gruppo");
    	}
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.findAllAttivo();
    		List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
    		String listaGruppi = ConvertToJson.convert(lista, "idGruppo", "descrizione"); 
	        render("CommesseController/create.html", commessa, listaClienti, listaTipiCommessa, aCorpo, importo, listaGruppi);
	    }
    	if(aCorpo != null && aCorpo.equals("si")){
    		if(importo < 0.1) {
        		validation.addError("importo", "L'importo deve essere maggiore di 0");
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
    		commessaACorpo.calcoloCosti = true;
    		commessaACorpo.calcoloRicavi = true;
    		commessaACorpo.cliente = commessa.cliente;
    		commessaACorpo.importo = importo;
    		commessaACorpo.save();
    	}else{
    		commessa.codice = commessa.codice.toUpperCase();
    		commessa.dataInizioCommessa = 
    			commessa.calcoloCosti == false && commessa.calcoloRicavi == false ? null : commessa.dataInizioCommessa;
    		commessa.save();
    		if (commessa.calcoloRicavi == false){
    			for (Gruppo g : lista) {
    				g.commesse.add(commessa);
    				g.save();
				}
    		}
    	}
    	flash.success("%s aggiunta con successo", commessa.codice);
    	list();
    }
    
    public static void edit(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	List<Cliente> listaClienti = Cliente.findAllAttivo();
    	List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
    	float importo = 0;
    	if(commessa instanceof CommessaACorpo) {
    		importo = ((CommessaACorpo)commessa).importo;
    	}
    	List<Gruppo> lista = Gruppo.findByCommessa(commessa);
    	String listaGruppi = ConvertToJson.convert(lista, "idGruppo", "descrizione"); 
        render(commessa, listaClienti, listaTipiCommessa, importo, listaGruppi);
    }
    
    public static void update(@Valid Commessa commessa, boolean calcoloRicavi, boolean calcoloCosti, 
    		String aCorpo, float importo, String gruppo) {
    	
    	List<Gruppo> lista = new ArrayList<Gruppo>();
    	if(!gruppo.equals("")) {
			//Lista Gruppi
			String [] listaG = gruppo.split(",");
			//rimuovo gli eventuali valori doppi
			Set<Object> uniquesetGruppo = new HashSet<Object>(Arrays.asList(listaG));
			Object [] uniqueGruppo = uniquesetGruppo.toArray();
			for(int i = 0;i<uniqueGruppo.length;i++){
				Gruppo g = Gruppo.findById(Integer.parseInt(uniqueGruppo[i].toString()));
				lista.add(g);
			}
    	}
		// Validazione
    	if(validation.hasErrors()) {
    		List<Cliente> listaClienti = Cliente.findAllAttivo();
    		List<TipoCommessa> listaTipiCommessa = TipoCommessa.findAll();
    		String listaGruppi = ConvertToJson.convert(lista, "idGruppo", "descrizione"); 
	        render("CommesseController/edit.html", commessa, listaClienti,listaTipiCommessa,listaGruppi);
	    }
    	commessa.codice = commessa.codice.toUpperCase();
    	commessa.dataInizioCommessa = commessa.calcoloRicavi == true ? commessa.dataInizioCommessa : null;
    	commessa.dataFineCommessa = commessa.calcoloRicavi == true ? commessa.dataFineCommessa : null;
    	if(commessa.dataFineCommessa != null){
    		Tariffa.chiudiTariffeByCommessa(commessa);
    	}
    	if(aCorpo != null && aCorpo.equals("si")) {
    		Commessa.commessaToCommessaACorpo(commessa.idCommessa, importo);
    	}else if(aCorpo != null && aCorpo.equals("no")){
    		Commessa.commessaACorpoToCommessa(commessa.idCommessa);
    	}
    	commessa.save();
    	if (commessa.calcoloRicavi == false){
	    	// Salvataggio modifiche gruppi
	    	for (Gruppo g : Gruppo.findByCommessa(commessa)) {
				g.commesse.remove(commessa);
				g.save();
			}
	    	for (Gruppo g : lista) {
				g.commesse.add(commessa);
				g.save();
			}
    	}
    	flash.success("%s modificata con successo", commessa.codice);
        list();
    }
    
    public static void delete(Integer id) {
    	Commessa commessa = Commessa.findById(id);
    	if(commessa.calcoloRicavi == false && commessa.calcoloCosti == false){
    		flash.success("Non si puo' chiudere una commessa non fatturabile");
    		list();
    	}
    	if(commessa.dataInizioCommessa == null ){
    		flash.success("La commessa non ha una data inizio");
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
    	flash.success("%s chiusa con successo", commessa.codice);
    	list();
    }
    
    // Auotocomplete dei commessa
	public static void autocompleteCommessa(String term) {
		List<Commessa> listaCommesse = Commessa.find("codice like ? or descrizione like ?","%"+term+"%","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Commessa com:listaCommesse){
			listaResult.add(new DomainWrapper(com.idCommessa, com.codice + " - " + com.descrizione));
		}
		renderJSON(listaResult);
    }
	
	public static void showCommessaModalBox(Integer id) {
		Commessa commessa = Commessa.findById(id);
        render(commessa);
	}
	
	public static void autocompleteGruppo(String term) {
		List<Gruppo> listaGruppi = Gruppo.find("descrizione like ?", "%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Gruppo g:listaGruppi){
			listaResult.add(new DomainWrapper(g.idGruppo, g.descrizione));
		}
		renderJSON(listaResult);
	}
}

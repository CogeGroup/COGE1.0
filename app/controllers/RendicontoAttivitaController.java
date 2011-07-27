package controllers;

import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Scope.Params;
import secure.SecureCOGE;
import utility.DomainWrapper;

import java.util.*;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import models.*;

@With(SecureCOGE.class)
public class RendicontoAttivitaController extends Controller {

	public static void index() {
		 render();
    }
	
	public static void chooseRisorsa() {
		 render();
    }
	
	public static void search(){
		render();
	}
	
	public static void result(Integer idRisorsa, String data){
		List<RendicontoAttivita> listaRapportini = RendicontoAttivita.findByExample(idRisorsa,data);
		ValuePaginator paginator = new ValuePaginator(listaRapportini);
		paginator.setPageSize(5);
		render("RendicontoAttivitaController/show.html", paginator);
	}

	
	public static void list(){
		List<RendicontoAttivita> listaRapportini = RendicontoAttivita.findAndOrder();
		ValuePaginator paginator = new ValuePaginator(listaRapportini);
		paginator.setPageSize(5);
		render(paginator);
	}

	
	public static void show(Integer idRisorsa, int mese, int anno){
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		List<RendicontoAttivita> listaRapportini = RendicontoAttivita.find("byRisorsaAndMeseAndAnno",risorsa,mese,anno).fetch();
		ValuePaginator paginator = new ValuePaginator(listaRapportini);
		paginator.setPageSize(5);
		render(paginator);
	}
	
	public static void edit(Integer id){
		RendicontoAttivita rendicontoAttivita = RendicontoAttivita.findById(id);
		render(rendicontoAttivita);
	}
	
	public static void update(@Valid RendicontoAttivita rendicontoAttivita){
		//validazione lettere e no numeri
		if(rendicontoAttivita.oreLavorate == null || rendicontoAttivita.equals("")){
			validation.addError("oreLavorate", "Ore lavorate oblicatorio");
		}
		if(validation.hasErrors()) {
	        render("RendicontoAttivitaController/edit.html", rendicontoAttivita);
	    }
		rendicontoAttivita.save();
    	flash.success("Rapportino modificato con successo");
        index();
	}

	public static void createRendicontoAttivita(@Required(message="Inserire una risorsa") String idRisorsa, @Required(message="Inserire la data") String data) {
		
		if(validation.hasErrors()){
			flash.error("");
			validation.keep();
			chooseRisorsa();
		}
		Risorsa risorsa = Risorsa.findById(Integer.parseInt(idRisorsa));
		String[] meseAnno = data.split("-");
		RendicontoAttivita ra = RendicontoAttivita.find("byRisorsaAndMeseAndAnno", risorsa, Integer.parseInt(meseAnno[0].trim()), Integer.parseInt(meseAnno[1].trim())).first();
		if(ra != null){
			validation.addError("meseAnno", "il rapportino per il mese " + meseAnno[0].trim() + "-" + meseAnno[1].trim() +" della risorsa " + risorsa.cognome + " già esistente");
			render("RendicontoAttivitaController/chooserisorsa.html");
		}
		if(risorsa == null){
			flash.error("Risorsa non trovata");
			validation.keep();
			chooseRisorsa();
		}
		
		List<Commessa> listaCommesse  = Tariffa.trovaCommessePerRisorsa(meseAnno[0].trim(), meseAnno[1].trim(), risorsa);
		List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
		render(idRisorsa,listaCommesse,listaCommesseNonFatturabili,meseAnno);
	}
	
	public static void saveRendicontoAttivita(String mese, String anno, Integer idRisorsa) {
		Risorsa risorsa = Risorsa.findById(idRisorsa);
		try {
			for (String key : params.all().keySet()) {
				if(key.contains("id_")){
					Integer oreLavorate = Integer.parseInt(params.get(key));
					if(oreLavorate < 0){
						throw new IllegalArgumentException();
					}
					Integer idCommessa = Integer.parseInt(key.substring(3));
					new RendicontoAttivita(oreLavorate, Integer.parseInt(mese), Integer.parseInt(anno), risorsa, (Commessa) Commessa.findById(idCommessa)).save();
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			validation.addError("oreLavorate", "OreLavorate deve essere maggiore o uguale a 0");
			String[] meseAnno = new String[] {mese,anno};
			List<Commessa> listaCommesse  = Tariffa.trovaCommessePerRisorsa(meseAnno[0].trim(), meseAnno[1].trim(), risorsa);
			List<Commessa> listaCommesseNonFatturabili  = Commessa.find("byFatturabile", false).fetch();
			render("rendicontoattivitacontroller/createRendicontoAttivita.html",idRisorsa,listaCommesse,listaCommesseNonFatturabili,meseAnno);
		}
		
		flash.success("Rapportino aggiunto con successo");
		index();
	}
	
	public static void delete(Integer id){
		RendicontoAttivita rendicontoAttivita = RendicontoAttivita.findById(id);
		rendicontoAttivita.delete();
		flash.success("Rapportino cancellato con successo");
		index();
	}
	
	public static void autocompleteRisorsaRapportoAttivita(String term) {
		List<Risorsa> listaRisorse = Risorsa.find("dataOut is null and matricola like ? or cognome like ?","%"+term+"%","%"+term+"%").fetch();
		  List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		  for(Risorsa r:listaRisorse){
			  listaResult.add(new DomainWrapper(r.idRisorsa, r.matricola +" "+r.cognome));
		  }
		renderJSON(listaResult);
    }

}

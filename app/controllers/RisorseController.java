package controllers;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import models.Costo;
import models.RapportoLavoro;
import models.Risorsa;
import models.Tariffa;
import models.TipoRapportoLavoro;
import models.Utente;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.MyUtility;

@With(SecureCOGE.class)
public class RisorseController extends Controller {


	public static void index() {
        render();
    }
    

    public static void list() {
    	ValuePaginator listaRisorse = new ValuePaginator(Risorsa.find("order by matricola").fetch());
    	listaRisorse.setPageSize(5);
		render(listaRisorse); 
    }
    
    public static void show(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
        render(risorsa);
    }
    
    public static void create() {
    	Risorsa risorsa = new Risorsa();
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
        render(risorsa, listaTipoRapportoLavoro);
    }
    
    public static void save(@Valid Risorsa risorsa, Integer idTipoRapportoLavoro) {
    	validation.min(idTipoRapportoLavoro, 1).message("selezionare un tipo rapporto lavoro");
    	if(validation.hasErrors()) {
        	List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
        	renderTemplate("RisorseController/create.html", risorsa, idTipoRapportoLavoro, listaTipoRapportoLavoro);
        }
	 	//crea e popola il primo rapporto lavoro con data inizio uguale alla data in della risorsa
	 	//aggiunge il primo rapporto lavoro alla lista rapportiLavoro della risorsa e salva il tutto
        RapportoLavoro primoRapportoLavoro = new RapportoLavoro(risorsa.dataIn, (TipoRapportoLavoro) TipoRapportoLavoro.findById(idTipoRapportoLavoro), risorsa);
        risorsa.addRapportoLavoro(primoRapportoLavoro);
        risorsa.save();
		flash.success("risorsa %s inserita con successo", risorsa.matricola);
		list();
	}
    
    public static void edit(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	render(risorsa);
    }
    
    public static void update(@Valid Risorsa risorsa) {
    	if(validation.hasErrors()) {
        	renderTemplate("RisorseController/edit.html", risorsa);
        }
    	//nel caso in cui viene settata data out della risorsa
    	//procedo alla disabilitazione della risorsa e delle info relative
        if(risorsa.dataOut != null) {
        	disabilitaRisorsa(risorsa, risorsa.dataOut);
        }
    	//procede alla modifica
        risorsa.save();
		flash.success("risorsa %s modificata con successo", risorsa.matricola);
		list();
	}
    
    public static void delete(Integer idRisorsa) {
    	Date dataChiusura = new Date();
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	disabilitaRisorsa(risorsa, dataChiusura);
    	
    	//procede alla cancellazione logica
        if(risorsa.save() != null) {
			 flash.success("risorsa %s disabilitata con successo", risorsa.matricola);
		 } else {
			flash.error("si sono verificati dei problemi nel disabilitare la risorsa %s", risorsa.matricola);
		}		 
		list();
    }
    
    private static void disabilitaRisorsa(Risorsa risorsa, Date dataChiusura) {
    	//in questo metodo setto le dateFinali di tutte le info associate alla risorsa:
    	//- se sono null le setto a dataChiusura
    	//- se sono < di dataChiusura non le modifico
    	//- se sono > di dataChiusura le setto a dataInizio
    	
    	//rapporto di lavoro
    	RapportoLavoro ultimoRapportoLavoro = risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1);
    	if (ultimoRapportoLavoro.dataFine == null) {
			ultimoRapportoLavoro.dataFine = ultimoRapportoLavoro.dataInizio.before(dataChiusura) ? dataChiusura : ultimoRapportoLavoro.dataInizio;
		} else if (ultimoRapportoLavoro.dataFine.after(dataChiusura)) {
			ultimoRapportoLavoro.dataFine = ultimoRapportoLavoro.dataInizio.before(dataChiusura) ? dataChiusura : ultimoRapportoLavoro.dataInizio;
		}
    	
    	//costo
    	if (risorsa.listaCosti != null && risorsa.listaCosti.size() > 0) {
    		Comparator<Costo> orderByDataInizioDesc = new Comparator<Costo>() {	
    			@Override
    			public int compare(Costo o1, Costo o2) {
    				return o1.dataInizio.before(o2.dataInizio) ? 1 : o1.dataInizio.equals(o2.dataInizio) ? 0 : -1;
    			}
    		};
    		Collections.sort(risorsa.listaCosti, orderByDataInizioDesc);
    		Costo ultimoCosto = risorsa.listaCosti.get(0);
    		if (ultimoCosto.dataFine == null) {
    			ultimoCosto.dataFine = ultimoCosto.dataInizio.before(dataChiusura) ? dataChiusura : ultimoCosto.dataInizio;
    		} else if (ultimoCosto.dataFine.after(dataChiusura)) {
    			ultimoCosto.dataFine = ultimoCosto.dataInizio.before(dataChiusura) ? dataChiusura : ultimoCosto.dataInizio;
    		}	
		}
    	
    	//tariffe
    	if (risorsa.listaTariffe != null) {
			for (Tariffa tariffa : risorsa.listaTariffe) {
				if (tariffa.dataFine == null) {
					tariffa.dataFine = tariffa.dataInizio.before(dataChiusura) ? dataChiusura : tariffa.dataInizio;
	    		} else if (tariffa.dataFine.after(dataChiusura)) {
	    			tariffa.dataFine = tariffa.dataInizio.before(dataChiusura) ? dataChiusura : tariffa.dataInizio;
	    		}
			}
		}
    	
    	//utente
    	Utente utente = Utente.find("byRisorsa", risorsa).first();
    	if(utente != null) {
    		utente.abilitato = false;
    		utente.save();
    	}
    	
    	//risorsa
    	if (risorsa.dataOut == null) {
    		risorsa.dataOut = risorsa.dataIn.before(dataChiusura) ? dataChiusura : risorsa.dataIn;
		} else if (risorsa.dataOut.after(dataChiusura)) {
			risorsa.dataOut = risorsa.dataIn.before(dataChiusura) ? dataChiusura : risorsa.dataIn;
		}
    }
    
}

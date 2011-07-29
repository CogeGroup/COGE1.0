package controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Cliente;
import models.Commessa;
import models.Risorsa;
import models.Tariffa;
import models.TipoRapportoLavoro;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.MyUtility;

@With(SecureCOGE.class)
public class TariffeController extends Controller {

    public static void index(Integer id) {
        list(id);
    }
    
    public static void list(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<Tariffa> listaTariffe = Tariffa.find("byRisorsa", risorsa).fetch();
    	ValuePaginator paginator = new ValuePaginator(listaTariffe);
    	paginator.setPageSize(5);
        render(paginator, risorsa);
    }
    
    public static void create(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.fatturabile = true and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int meseInizio = Calendar.getInstance().get(Calendar.MONTH);
        int annoInizio = Calendar.getInstance().get(Calendar.YEAR);
        render(risorsa.idRisorsa, listaCommesse, meseInizio, listaAnni, annoInizio);
    }
    
/*
 * La data di inizio tariffa per una commessa non puo essere inferiore alla data fine dell'ultima tariffa della stessa commessa,
 * se l'ultima tariffa non ha data fine, la data inizio non può essere inferiore alla data inizio dell'ultima tariffa piu 2 giorni.
 */
    public static void save(@Valid Tariffa tariffa, Integer idRisorsa, @Required(message="Selezionare una commessa") Integer idCommessa, int meseInizio, int annoInizio) {
    	// Validazione del form
    	if(validation.hasErrors()) {
    		List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.fatturabile = true and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
    		List<Integer> listaAnni = MyUtility.createListaAnni();
        	render("TariffeController/create.html", idRisorsa, tariffa, listaCommesse, meseInizio, listaAnni, annoInizio);
    	}
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(meseInizio, annoInizio);
    	
        Commessa commessa = Commessa.findById(idCommessa);
        tariffa.commessa = commessa;
        Risorsa risorsa = Risorsa.findById(idRisorsa);
        tariffa.risorsa = risorsa;
        tariffa.dataInizio = dataInizio;
        
        if(!validateForSave(tariffa, idRisorsa, meseInizio, annoInizio, dataInizio, commessa, risorsa)){
        	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.fatturabile = true and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
    		List<Integer> listaAnni = MyUtility.createListaAnni();
        	render("TariffeController/create.html", idRisorsa, tariffa, listaCommesse, meseInizio, listaAnni, annoInizio);
        }
        
        // Salvataggio tariffa
        tariffa.save();
        flash.success("Tariffa aggiunta con successo");
    	list(idRisorsa);
    }

	private static boolean validateForSave(Tariffa tariffa, Integer idRisorsa,
			int meseInizio, int annoInizio, Date dataInizio, Commessa commessa,
			Risorsa risorsa) {
		
		if(tariffa.commessa.fatturabile == false) {
        	tariffa.importoGiornaliero = 0;
        }else{
        	if(tariffa.importoGiornaliero <= 0) {
        		validation.addError("tariffa.importoGiornaliero", "Importo obligatorio");
        		return false;
        	}
        }
		
		// Validazione data inizio
		// se la data inizio della tariffa è minore della data inizio della commessa
		if(commessa.dataInizioCommessa != null){
			Date dataCommessa = commessa.dataInizioCommessa;
			int meseCommessa = MyUtility.getMeseFromDate(dataCommessa);
			int annoCommessa = MyUtility.getAnnoFromDate(dataCommessa);
			dataCommessa = MyUtility.MeseEdAnnoToDataInizio(meseCommessa, annoCommessa);
			if(tariffa.dataInizio.before(dataCommessa)){
		    	validation.addError("tariffa.dataInizio", "La data inizio per la commessa "
		    			+ commessa.codice + " non puo' essere inferione al: " + new SimpleDateFormat("dd/MM/yyyy").format(dataCommessa));
		    	return false;
			}
		}
		if(commessa.dataFineCommessa != null){
			Date dataCommessa = commessa.dataFineCommessa;
			int meseCommessa = MyUtility.getMeseFromDate(dataCommessa);
			int annoCommessa = MyUtility.getAnnoFromDate(dataCommessa);
			dataCommessa = MyUtility.MeseEdAnnoToDataFine(meseCommessa, annoCommessa);
			if(!tariffa.dataInizio.before(dataCommessa)){
		    	validation.addError("tariffa.dataInizio", "La data inizio per la commessa "
		    			+ commessa.codice + " non puo' essere inferione al: " + new SimpleDateFormat("dd/MM/yyyy").format(dataCommessa));
		    	return false;
			}
		}
		
		// Lista tariffe con dataFine a null
		List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsaAndDataFineIsNull", commessa, risorsa).fetch();
		if(lista.size() > 0 ) {
			// Prendo l'ultima tariffa con la stessa commessa
			Tariffa t = lista.get(lista.size()-1);
			Calendar c = Calendar.getInstance();
			c.setTime(dataInizio);
			c.add(Calendar.DAY_OF_MONTH, -1);
			Date data = c.getTime();
			if(!tariffa.dataInizio.after(t.dataInizio)){
		       	validation.addError("tariffa.dataInizio", "La data inizio per la commessa "
		       			+ commessa.codice + " non puo essere inferione al o uguale: " + new SimpleDateFormat("dd/MM/yyyy").format(t.dataInizio));
		       	return false;
		    }
			t.dataFine = data;
			t.save();
		}else{
			// ultima tariffa gia chiusa
		    lista = Tariffa.find("byCommessaAndRisorsaAndDataFineIsNotNull",commessa, risorsa).fetch();
		    if(lista.size() > 0) {
		    	// Prendo l'ultima tariffa con la stessa commessa
		    	Tariffa t = lista.get(lista.size()-1);
		    	// controlla se la data inizio della nuova tariffa è maggiore della data fine dell'ultima tariffa
			    if(tariffa.dataInizio.before(t.dataFine)){
			       	validation.addError("tariffa.dataInizio", "La data inizio per la commessa "
			       			+ commessa.codice + " non puo essere inferione o uguale al: " + new SimpleDateFormat("dd/MM/yyyy").format(t.dataFine));
			       	return false;
			    }
		    }
		}
		return true;
	}

    public static void edit(Integer idTariffa) {
    	Tariffa tariffa = Tariffa.findById(idTariffa);
    	// Controlla se la tariffa da modificare è l'ultima
    	List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa",tariffa.commessa, tariffa.risorsa).fetch();
        if(tariffa.dataFine != null){
        	// Controlla se la tariffa da modificare è l'ultima
	        for (Tariffa t : lista) {
				if(t.dataInizio.after(tariffa.dataFine)){
		        	validation.addError("dataInizio", "La commessa: " + tariffa.commessa.codice + " ha gia altre tariffe");
		        	// torna alla pagina list.html con il messaggio di errore
		        	Risorsa risorsa = Risorsa.findById(tariffa.risorsa.idRisorsa);
		        	List<Tariffa> listaTariffe = Tariffa.find("byRisorsa", risorsa).fetch();
		        	ValuePaginator paginator = new ValuePaginator(listaTariffe);
		        	paginator.setPageSize(5);
		        	Integer idUltimaTariffa = listaTariffe.get(listaTariffe.size()-1).idTariffa;
		        	render("TariffeController/list.html", tariffa.risorsa.idRisorsa, paginator, risorsa, idUltimaTariffa);
				}
			}
        }
        List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
    	int meseInizio = MyUtility.getMeseFromDate(tariffa.dataInizio);
        int annoInizio = MyUtility.getAnnoFromDate(tariffa.dataInizio);
        int meseFine = tariffa.dataFine == null ? -1 : MyUtility.getMeseFromDate(tariffa.dataFine);
        int annoFine = tariffa.dataFine == null ? -1 : MyUtility.getAnnoFromDate(tariffa.dataFine);
    	List<Integer> listaAnni = MyUtility.createListaAnni();
        render(tariffa, listaCommesse, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
    }
    
    public static void update(@Valid Tariffa tariffa, Integer idCommessa, int meseInizio, int annoInizio, int meseFine, int annoFine) {
    	// Validazione del form
    	if(validation.hasErrors()) {
    		List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.fatturabile = true and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
        	List<Integer> listaAnni = MyUtility.createListaAnni();
            render("TariffeController/edit.html", tariffa, listaCommesse, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
    	}
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(meseInizio, annoInizio);
    	
    	Commessa commessa = Commessa.findById(idCommessa);
        tariffa.commessa = commessa;
        tariffa.dataInizio = dataInizio;
        
        if(!valitateForUpdate(tariffa, meseInizio, annoInizio, meseFine, annoFine, dataInizio, commessa)){
        	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.fatturabile = true and cm.dataFineCommessa is null or cm.dataFineCommessa >= ? order by codice asc", new Date()).fetch();
        	List<Integer> listaAnni = MyUtility.createListaAnni();
            render("TariffeController/edit.html", tariffa, listaCommesse, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
        }
        
        // salvataggio modifiche di tariffa
    	tariffa.save();
        flash.success("Tariffa modificata con successo");
    	list(tariffa.risorsa.idRisorsa);
    }

	private static boolean valitateForUpdate(Tariffa tariffa, int meseInizio,
			int annoInizio, int meseFine, int annoFine, Date dataInizio,
			Commessa commessa) {
		
		if(tariffa.commessa.fatturabile == false) {
        	tariffa.importoGiornaliero = 0;
        }else{
        	if(tariffa.importoGiornaliero <= 0) {
        		validation.addError("tariffa.importoGiornaliero", "Importo obligatorio");
        		return false;
        	}
        }
		
		// controllo se ci sono tariffe della stessa commessa che hanno una data inizio maggiore della data fine della tariffa da modificare
		List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa",commessa, tariffa.risorsa).fetch();
        if(lista.size() > 0) {
        	// prendo la penultima tariffa poiché l'ultima tariffa è quella da modificare
        	Tariffa t = lista.size() > 1 ? lista.get(lista.size()-2) : lista.get(0);
        	if(lista.size() > 1){
			    if(!tariffa.dataInizio.after(t.dataFine)){
			       	validation.addError("dataInizio", "La data inizio non puo' essere minore o uguale " +
			       			"al: " + new SimpleDateFormat("dd/MM/yyyy").format(t.dataFine));
			       	return false;
			    }
        	}
        }
		
		if(meseFine != -1 && annoFine != -1){
			Date dataFine = MyUtility.MeseEdAnnoToDataFine(meseFine, annoFine);
			if(dataFine.before(dataInizio)){
				validation.addError("tariffa.dataFine", "La data fine deve essere maggiore della data inizio");
				return false;
			}
			if(dataFine.after(commessa.dataFineCommessa)){
				validation.addError("tariffa.dataFine", "La data fine deve essere maggiore di: "+ MyUtility.dateToString(commessa.dataFineCommessa));
				return false;
			}
			tariffa.dataFine = dataFine;
		}else if(meseFine == -1 && annoFine == -1){
			tariffa.dataFine = null;
		}else{
			validation.addError("tariffa.dataFine", "Inserire correttamente la data fine");
			return false;
		}
		return true;
	}
    
    public static void show(Integer idTariffa) {
    	Tariffa tariffa = Tariffa.findById(idTariffa);
        render(tariffa);
    }
    
    public static void delete(Integer idTariffa) {
    	Tariffa tariffa = Tariffa.findById(idTariffa);
    	Integer idRisorsa = tariffa.risorsa.idRisorsa;
    	
    	if(tariffa.dataFine != null){
    		 flash.success("Tariffa gia chiusa");
    		list(idRisorsa);
    	}
    	
    	// Controlla se la tariffa da modificare è l'ultima
    	List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa",tariffa.commessa, tariffa.risorsa).fetch();
        if(tariffa.dataFine != null){
        	// Controlla se la tariffa da modificare è l'ultima
	        for (Tariffa t : lista) {
				if(t.dataInizio.after(tariffa.dataFine)){
		        	validation.addError("dataInizio", "La commessa: " + tariffa.commessa.codice + " ha gia altre tariffe");
		        	// torna alla pagina list.html con il messaggio di errore
		        	Risorsa risorsa = Risorsa.findById(tariffa.risorsa.idRisorsa);
		        	List<Tariffa> listaTariffe = Tariffa.find("byRisorsa", risorsa).fetch();
		        	ValuePaginator paginator = new ValuePaginator(listaTariffe);
		        	paginator.setPageSize(5);
		        	Integer idUltimaTariffa = listaTariffe.get(listaTariffe.size()-1).idTariffa;
		        	render("TariffeController/list.html", tariffa.risorsa.idRisorsa, paginator, risorsa, idUltimaTariffa);
				}
			}
        }
    	
    	if(tariffa.dataInizio.after(new Date())){
    		tariffa.dataFine = tariffa.dataInizio;
    	}else{
        	int meseFine = MyUtility.getMeseFromDate(new Date());
        	int annoFine = MyUtility.getAnnoFromDate(new Date());
    		tariffa.dataFine = MyUtility.MeseEdAnnoToDataFine(meseFine, annoFine);
    	}
    	
    	tariffa.save();
        list(idRisorsa);
    }
}
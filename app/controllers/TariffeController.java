package controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Cliente;
import models.Commessa;
import models.Risorsa;
import models.Tariffa;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;

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
    	Tariffa tariffa = new Tariffa();
    	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
        render(risorsa.idRisorsa, tariffa, listaCommesse);
    }
    
/*
 * La data di inizio tariffa per una commessa non puo essere inferiore alla data fine dell'ultima tariffa della stessa commessa,
 * se l'ultima tariffa non ha data fine, la data inizio non può essere inferiore alla data inizio dell'ultima tariffa piu 2 giorni.
 */
    public static void save(@Valid Tariffa tariffa, Integer idRisorsa, @Required(message="Selezionare una commessa") Integer idCommessa) {
    	// Validazione del form
    	if(validation.hasErrors()) {
    		List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
	        render("TariffeController/create.html", idRisorsa, tariffa, listaCommesse);
    	}
        Commessa commessa = Commessa.findById(idCommessa);
        tariffa.commessa = commessa;
        Risorsa risorsa = Risorsa.findById(idRisorsa);
        tariffa.risorsa = risorsa;
        
        // Validazione importo
        if(tariffa.commessa.fatturabile == false) {
        	tariffa.importoGiornaliero = 0;
        }else{
        	if(tariffa.importoGiornaliero < 0) {
        		validation.addError("tariffa.importoGiornaliero", "Importo obligatorio");
        	}
        }
        
        // Validazione data inizio
        // se la data inizio della tariffa è minore della data inizio della commessa
        if(commessa.dataInizioCommessa != null && tariffa.dataInizio.before(commessa.dataInizioCommessa)){
        	validation.addError("tariffa.dataInizio", "La data inizio per la commessa "
        			+ commessa.codice + " non puo' essere inferione al: " + new SimpleDateFormat("dd/MM/yyyy").format(commessa.dataInizioCommessa));
        	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
	        render("TariffeController/create.html", idRisorsa, tariffa, listaCommesse);
        }
        // dataFine a null
        List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsaAndDataFineIsNull", commessa, risorsa).fetch();
        if(lista.size() > 0 ) {
        	// Prendo l'ultima tariffa con la stessa commessa
        	Tariffa t = lista.get(lista.size()-1);
        	Calendar c = Calendar.getInstance();
        	c.setTime(t.dataInizio);
        	c.add(Calendar.DAY_OF_MONTH, 1);
        	Date data = c.getTime();
        	// Controlla se la data inizio della tariffa nuova è maggiore di almeno 1 giorno della data inizio dell'ultima tareffa
        	if(!tariffa.dataInizio.after(data)){
        		List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
            	validation.addError("tariffa.dataInizio", "La data inizio per la commessa "
		        			+ commessa.codice + " non puo' essere inferione o uguale al: " + new SimpleDateFormat("dd/MM/yyyy").format(data));
    	        render("TariffeController/create.html", idRisorsa, tariffa, listaCommesse);
        	}
        	c.setTime(tariffa.dataInizio);
        	c.add(Calendar.DAY_OF_MONTH, -1);
        	t.dataFine = c.getTime();
        	t.save();
        }else{
        	// data fine != null
		    lista = Tariffa.find("byCommessaAndRisorsaAndDataFineIsNotNull",commessa, risorsa).fetch();
		    if(lista.size() > 0) {
		    	Tariffa t = lista.get(lista.size()-1);
		    	// controlla se la data inizio della nuova tariffa è maggiore della data fine dell'ultima tariffa
			    if(tariffa.dataInizio.before(t.dataFine)){
			    	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
			       	validation.addError("tariffa.dataInizio", "La data inizio per la commessa "
			       			+ commessa.codice + " non puo essere inferione al: " + new SimpleDateFormat("dd/MM/yyyy").format(t.dataFine));
				    render("TariffeController/create.html", idRisorsa, tariffa, listaCommesse);
			    }
		    }
        }
        
        // Salvataggio tariffa
        tariffa.save();
        flash.success("Tariffa aggiunta con successo");
    	list(idRisorsa);
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
        List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
        render(tariffa, listaCommesse);
    }
    
    public static void update(@Valid Tariffa tariffa, Integer idCommessa) {
    	// Validazione del form
    	if(validation.hasErrors()) {
    		List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
	        render("TariffeController/edit.html", tariffa, listaCommesse);
    	}
    	Commessa commessa = Commessa.findById(idCommessa);
        tariffa.commessa = commessa;
        // Validazione date
        // se la data inizio della tariffa è minore della data inizio della commessa
        if(commessa.dataInizioCommessa != null && tariffa.dataInizio.before(commessa.dataInizioCommessa)){
        	validation.addError("tariffa.dataInizio", "La data inizio per la commessa "
        			+ commessa.codice + " non puo' essere inferione al: " + new SimpleDateFormat("dd/MM/yyyy").format(commessa.dataInizioCommessa));
        	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
	        render("TariffeController/edit.html", tariffa, listaCommesse);
        }
        List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa",commessa, tariffa.risorsa).fetch();
        // controllo se ci sono tariffe della stessa commessa che hanno una data inizio maggiore della data fine della tariffa da modificare
        if(lista.size() > 0) {
        	// prendo la penultima tariffa poiché l'ultima tariffa è quella da modificare
        	Tariffa t = lista.size() > 1 ? lista.get(lista.size()-2) : lista.get(0);
        	if(lista.size() > 1){
        		System.out.println(t.idTariffa + " " + tariffa.idTariffa);
			    if(!tariffa.dataInizio.after(t.dataFine)){
			    	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
			       	validation.addError("dataInizio", "La data inizio non puo' essere minore o uguale " +
			       			"al: " + new SimpleDateFormat("dd/MM/yyyy").format(t.dataFine));
			       	render("TariffeController/edit.html", tariffa, listaCommesse);
			    }
        	}
	        if(tariffa.dataFine != null){
		        // data fine deve essere minimo dopo 1 giorno dalla data inizio
		        Calendar c = Calendar.getInstance();
		    	c.setTime(tariffa.dataInizio);
		    	c.add(Calendar.DAY_OF_MONTH, 1);
		    	Date dataInizio = c.getTime();
		        if(tariffa.dataFine.before(dataInizio)){
		        	List<Commessa> listaCommesse = Cliente.find("select cm from Commessa cm where cm.attivo = ? order by codice asc", true).fetch();
		        	validation.addError("date", "La data fine non puo essere minore o uguale della data inizio");
		        	render("TariffeController/edit.html", tariffa, listaCommesse);
		        }
	        }
        }
        // salvataggio modifiche di tariffa
    	tariffa.save();
        flash.success("Tariffa modificata con successo");
    	list(tariffa.risorsa.idRisorsa);
    }
    
    public static void show(Integer idTariffa) {
    	Tariffa tariffa = Tariffa.findById(idTariffa);
        render(tariffa);
    }
    
    public static void delete(Integer idTariffa) {
    	Tariffa tariffa = Tariffa.findById(idTariffa);
    	Integer idRisorsa = tariffa.risorsa.idRisorsa;
    	tariffa.dataFine = new Date();
    	tariffa.save();
        list(idRisorsa);
    }
}
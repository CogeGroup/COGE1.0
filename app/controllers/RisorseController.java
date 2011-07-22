package controllers;

import play.*;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.PersistenceException;

import net.sf.oval.constraint.Min;

import models.*;
import utility.*;

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
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
        render(listaTipoRapportoLavoro);
    }
    
    public static void save(@Valid Risorsa risorsa, Integer idTipoRapportoLavoro) {
    	//se ci sono campi non validi in risorsa passa avanti
    	if(validation.hasErrors()) {
    		;
        }
    	//verifica se rapporto lavoro è stato selezionato
    	else if(idTipoRapportoLavoro == 0){
	 		validation.addError("idTipoRapportoLavoro", "rapporto lavorativo obbligatorio");
	 	}
    	//verifica se esista già una risorsa con la mtricola inserita
    	else if(Risorsa.find("byMatricola", risorsa.matricola).first() != null) {
        	validation.addError("matricola", "matricola esistente");
        }        
    	//se ci sono errori nella validazione ritorna alla maschera di inserimento altrimentiu prosegui
	 	if(validation.hasErrors()) {
        	List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
        	renderTemplate("RisorseController/create.html", risorsa, idTipoRapportoLavoro, listaTipoRapportoLavoro);
        }
	 	//crea e popola il primo rapporto lavoro con data inizio uguale alla data in della risorsa
	 	//aggiunge il primo rapporto lavoro alla lista rapportiLavoro della risorsa e salva il tutto
        RapportoLavoro primoRapportoLavoro = new RapportoLavoro(risorsa.dataIn, (TipoRapportoLavoro) TipoRapportoLavoro.findById(idTipoRapportoLavoro), risorsa);
        risorsa.addRapportoLavoro(primoRapportoLavoro);
        if(risorsa.save() != null) {
			 flash.success("risorsa %s inserita con successo", risorsa.matricola);
		 } else {
			flash.error("impossibile inserire la risorsa %s", risorsa.matricola);
		}		 
		list();
	}
    
    public static void edit(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	//mi porto dietro la matricola originale per il controllo di univocità
    	String matricolaOriginale = risorsa.matricola;
        render(risorsa, matricolaOriginale);
    }
    
    public static void update(@Valid Risorsa risorsa, String matricolaOriginale) {
    	//primo controllo validazione su risorsa
    	if(validation.hasErrors()) {
        	;
        }
    	//verifica matricola unique (se é diversa da quella vecchia della matricola vedi se esiste già sul db)
    	else if(!risorsa.matricola.equals(matricolaOriginale) && Risorsa.find("byMatricola", risorsa.matricola).first() != null) {
        	validation.addError("matricola", "matricola esistente");
        }
    	//verifico che la data in della risorsa non sia > dell'inizio del primo rapporto di lavoro
    	else if (risorsa.dataIn.after(risorsa.rapportiLavoro.get(0).dataInizio)) {
        	validation.addError("dataIn", "%s deve essere <= di %s (inizio primo rapporto di lavoro)", MyUtility.dateToString(risorsa.rapportiLavoro.get(0).dataInizio));
        }
    	//verifico che data out di risorsa (se valorizzata) sia > dell'inizio dell'ultimo rapporto di lavoro
    	else if (risorsa.dataOut != null && !risorsa.dataOut.after(risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1).dataInizio)) {
    		validation.addError("dataOut", "%s deve essere > di %s (inizio ultimo rapporto di lavoro)", MyUtility.dateToString(risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1).dataInizio));
        }
    	//finiti i controlli di validazione, se c sono errori torna al form di modifica
    	if(validation.hasErrors()) {
        	renderTemplate("RisorseController/edit.html", risorsa, matricolaOriginale);
        }
    	//nel caso in cui viene settata data out della risorsa
    	//aggiorna la data fine dell'ultimo rapporto lavoro se è null, oppure > di data out
        RapportoLavoro ultimoRapportoLavoro = risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1);
        if (risorsa.dataOut != null && (ultimoRapportoLavoro.dataFine == null || risorsa.dataOut.before(ultimoRapportoLavoro.dataFine))) {
			ultimoRapportoLavoro.dataFine = risorsa.dataOut;
		}
        //procede alla modifica
        if(risorsa.save() != null) {
			 flash.success("risorsa %s modificata con successo", risorsa.matricola);
		 } else {
			flash.error("impossibile modificare la risorsa %s", risorsa.matricola);
		}		 
		list();
	}
    
    public static void delete(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	Utente utente = Utente.find("byRisorsa", risorsa).first();
    	if(utente != null) {
         utente.delete();	
    	}
    	risorsa.delete();
    	list();
    }
    
    //-----------------------------RISORSA - RAPPORTO LAVORO
    
    public static void listRapportoLavoro(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	render(risorsa);
    }
    
    public static void createRapportoLavoro(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int meseInizio = Calendar.getInstance().get(Calendar.MONTH);
        int annoInizio = Calendar.getInstance().get(Calendar.YEAR);
        //passo alla maschera per inserire il rapporto lavoro
        //la risorsa su cui stiamo operando, la lista di tipo rapporto lavoro, la lista degli anni e di dafault mese ed anno correnti
        render(risorsa, listaTipoRapportoLavoro, meseInizio, listaAnni, annoInizio);
    }
    
    public static void saveRapportoLavoro(Integer idRisorsa, Integer idTipoRapportoLavoro, int meseInizio, int annoInizio) {
    	//PRESUPPOSTO a questo punto la risorsa avrà già almeno un rapporto di lavoro!
    	//preparo per i controlli: dataInizio del rapporto a partire da mese ed anno selezionato, l'ultimo rapporto di lavoro della risorsa
    	//e la dataMinima per l'inizio del rapporto lavoro(che sarà la dataFine dell'ultimo rapporto lavoro, oppure la dataInizio dello stesso de dataFine è null)
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(meseInizio, annoInizio);
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	RapportoLavoro ultimoRapportoLavoro = risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1);
    	Date dataMinima = ultimoRapportoLavoro.dataFine == null ? ultimoRapportoLavoro.dataInizio : ultimoRapportoLavoro.dataFine;
    	//verifico la selezione di un tipo rapporto lavoro
    	if(idTipoRapportoLavoro == 0){
	 		validation.addError("idTipoRapportoLavoro", "rapporto lavorativo obbligatorio");
    	}
    	//verifico che la data inizio sia > di dataMinima
    	else if (!dataInizio.after(dataMinima)) {
    		validation.addError("dataInizio", "%s deve essere > di %s", MyUtility.dateToString(dataMinima).substring(3));
		}
    	//se ci sono errori di validazione ritorna alla maschera di inserimento
    	if (validation.hasErrors()) {
    		List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
	 		List<Integer> listaAnni = MyUtility.createListaAnni();
	    	renderTemplate("RisorseController/createRapportoLavoro.html", risorsa, listaTipoRapportoLavoro, idTipoRapportoLavoro, meseInizio, listaAnni, annoInizio);
        }
    	//se la validazione va a buon fine
    	//procedo alla chiusura dell'ultimo rapporto di lavoro (solo se ha dataFine null) 
    	if (ultimoRapportoLavoro.dataFine == null) {
			ultimoRapportoLavoro.dataFine = MyUtility.subOneDay(dataInizio);
		}
    	//infine creo il nuovo rapporto lavoro, lo aggiungo alla risorsa e salvo
    	RapportoLavoro nuovoRapportoLavoro = new RapportoLavoro(dataInizio, (TipoRapportoLavoro) TipoRapportoLavoro.findById(idTipoRapportoLavoro), (Risorsa) Risorsa.findById(idRisorsa));
    	risorsa.rapportiLavoro.add(nuovoRapportoLavoro);
    	if(risorsa.save() != null) {
			 flash.success("rapporto lavoro aggiunto con successo");
		 } else {
			flash.error("impossibile aggiungere il nuovo rapporto lavoro");
		}		 
		listRapportoLavoro(idRisorsa);
    }
    
    public static void editRapportoLavoro(Integer idRapportoLavoro) {
    	//preparo le informazioni associate alla maschera di modifica del rapporto lavoro
    	RapportoLavoro rapportoLavoro = RapportoLavoro.findById(idRapportoLavoro);
    	Risorsa risorsa = rapportoLavoro.risorsa;
    	int idTipoRapportoLavoro = rapportoLavoro.tipoRapportoLavoro.idTipoRapportoLavoro;
    	int meseInizio = MyUtility.getMeseFromDate(rapportoLavoro.dataInizio);
        int annoInizio = MyUtility.getAnnoFromDate(rapportoLavoro.dataInizio);
        int meseFine = rapportoLavoro.dataFine == null ? -1 : MyUtility.getMeseFromDate(rapportoLavoro.dataFine);
        int annoFine = rapportoLavoro.dataFine == null ? -1 : MyUtility.getAnnoFromDate(rapportoLavoro.dataFine);
        List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	render(idRapportoLavoro, risorsa, listaTipoRapportoLavoro, idTipoRapportoLavoro, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
    }
    
    public static void updateRapportoLavoro(Integer idRapportoLavoro, Integer idTipoRapportoLavoro, int meseInizio, int annoInizio, int meseFine, int annoFine) {
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(meseInizio, annoInizio);
    	RapportoLavoro rapportoLavoroAttuale = RapportoLavoro.findById(idRapportoLavoro);
    	Risorsa risorsa = rapportoLavoroAttuale.risorsa;
    	//index contiene la posizione del rapporto di lavoro sotto modifca nella lista dei rapporti lavoro della risorsa
    	//index = 0 significa che sto provando a modificare il primo rapporto di lavoro --> effettua i controlli sulle date rispetto a quelle della risorsa
    	//index > 0 per i controlli tieni conto del rapporto di lavoro precedente
    	//index < 0 caso non previsto, la risorsa deve avere almeno un rapporto di lavoro
    	int index = risorsa.rapportiLavoro.indexOf(rapportoLavoroAttuale);
    	Date dataMinima = index == 0 ? MyUtility.subOneMonth(risorsa.dataIn) : risorsa.rapportiLavoro.get(index - 1).dataFine;
    	//verifico tipo rapporto lavoro selezionato
    	if(idTipoRapportoLavoro == 0){
	 		validation.addError("idTipoRapportoLavoro", "rapporto lavorativo obbligatorio");
    	}
    	//verifico che dataInizio del rapporto lavoro sia > della dataMinima
    	else if (!dataInizio.after(dataMinima)) {
    		validation.addError("dataInizio", "%s deve essere maggiore di %s", MyUtility.dateToString(dataMinima).substring(3));
		}
    	//verifico che mese ed anno fine siano entrambi contemporaneamente selezionati o meno
    	else if (meseFine > -1 ^ annoFine > -1) {
    		validation.addError("dataFine", "dataFine non valida selezionare/deselezionare contemporaneamente mese ed anno");
		} 
    	//se selezionata e valida dataFine verifico che sia > di dataInizio
    	else if (meseFine > -1 && annoFine > -1 && !MyUtility.MeseEdAnnoToDataFine(meseFine, annoFine).after(dataInizio)) {
    		validation.addError("dataFine", "%s deve essere >= di %s", MyUtility.dateToString(dataInizio).substring(3));
		}
    	//se ci sono errori di validazione ritorna alla maschera di modifica
    	if (validation.hasErrors()) {
    		List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
	 		List<Integer> listaAnni = MyUtility.createListaAnni();
	    	renderTemplate("RisorseController/editRapportoLavoro.html", idRapportoLavoro, risorsa, listaTipoRapportoLavoro, idTipoRapportoLavoro, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
        }
    	//se va tutto bene procediamo alla modifica del rapporto lavoro
    	rapportoLavoroAttuale.tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	//nel caso in cui si tratti del primo rapporto di lavoro e la data inizio e la data inizio del rapporto è < di quella di in della risorsa
    	//dataInizio rapporto diventa uguale a dataIn risorsa
    	rapportoLavoroAttuale.dataInizio = (index == 0 && dataInizio.before(risorsa.dataIn)) ? risorsa.dataIn : dataInizio;
    	//valorizza o meno dataFine
    	rapportoLavoroAttuale.dataFine = meseFine == -1 ? null : MyUtility.MeseEdAnnoToDataFine(meseFine, annoFine);
    	if(rapportoLavoroAttuale.save() != null) {
			 flash.success("rapporto lavoro modificato con successo");
		 } else {
			flash.error("impossibile modificare il nuovo rapporto lavoro");
		}		 
		listRapportoLavoro(risorsa.idRisorsa);
    }
    
    public static void deleteRapportoLavoro(Integer idRapportoLavoro) {
    	RapportoLavoro rapportoLavoro = RapportoLavoro.findById(idRapportoLavoro);
    	if (rapportoLavoro.delete() != null) {
			flash.success("Rapporto Lavoro %s eliminato con successo", rapportoLavoro.idRapportoLavoro);
		} else {
			flash.error("Impossibile eliminare il Rapporto Lavoro %s", rapportoLavoro.idRapportoLavoro);
		}
    	listRapportoLavoro(rapportoLavoro.risorsa.idRisorsa);
    }

}

package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.RapportoLavoro;
import models.Risorsa;
import models.TipoRapportoLavoro;
import play.mvc.Controller;
import utility.MyUtility;

public class RapportoLavoroController extends Controller {
	public static void list(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	render(risorsa);
    }
    
    public static void create(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int meseInizio = Calendar.getInstance().get(Calendar.MONTH);
        int annoInizio = Calendar.getInstance().get(Calendar.YEAR);
        //passo alla maschera per inserire il rapporto lavoro
        //la risorsa su cui stiamo operando, la lista di tipo rapporto lavoro, la lista degli anni e di dafault mese ed anno correnti
        render(risorsa, listaTipoRapportoLavoro, meseInizio, listaAnni, annoInizio);
    }
    
    public static void save(Integer idRisorsa, Integer idTipoRapportoLavoro, int meseInizio, int annoInizio) {
    	//PRESUPPOSTO a questo punto la risorsa avrà già almeno un rapporto di lavoro!
    	//preparo per i controlli: dataInizio del rapporto a partire da mese ed anno selezionato, l'ultimo rapporto di lavoro della risorsa
    	//e la dataMinima per l'inizio del rapporto lavoro(che sarà la dataFine dell'ultimo rapporto lavoro, oppure la dataInizio dello stesso de dataFine è null)
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(meseInizio, annoInizio);
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	RapportoLavoro ultimoRapportoLavoro = risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1);
    	Date dataMinima = ultimoRapportoLavoro.dataFine == null ? ultimoRapportoLavoro.dataInizio : ultimoRapportoLavoro.dataFine;
    	
    	validation.min(idTipoRapportoLavoro, 1).message("selezionare un tipo rapporto lavoro");
    	//verifico che la data inizio sia > di dataMinima
    	if (!dataInizio.after(dataMinima)) {
    		validation.addError("dataInizio", "%s deve essere > di %s", MyUtility.dateToString(dataMinima).substring(3));
		}
    	//se ci sono errori di validazione ritorna alla maschera di inserimento
    	if (validation.hasErrors()) {
    		List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
	 		List<Integer> listaAnni = MyUtility.createListaAnni();
	    	renderTemplate("RapportoLavoroController/create.html", risorsa, listaTipoRapportoLavoro, idTipoRapportoLavoro, meseInizio, listaAnni, annoInizio);
        }
    	//se la validazione va a buon fine
    	//procedo alla chiusura dell'ultimo rapporto di lavoro (solo se ha dataFine null) 
    	if (ultimoRapportoLavoro.dataFine == null) {
			ultimoRapportoLavoro.dataFine = MyUtility.subOneDay(dataInizio);
		}
    	//infine creo il nuovo rapporto lavoro, lo aggiungo alla risorsa e salvo
    	RapportoLavoro nuovoRapportoLavoro = new RapportoLavoro(dataInizio, (TipoRapportoLavoro) TipoRapportoLavoro.findById(idTipoRapportoLavoro), (Risorsa) Risorsa.findById(idRisorsa));
    	risorsa.rapportiLavoro.add(nuovoRapportoLavoro);
    	risorsa.save();
		flash.success("rapporto lavoro aggiunto con successo");
		list(idRisorsa);
    }
    
    public static void edit(Integer idRapportoLavoro) {
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
    
    public static void update(Integer idRapportoLavoro, Integer idTipoRapportoLavoro, int meseInizio, int annoInizio, int meseFine, int annoFine) {
    	//PREMESSA si può modificare solo l'ultimo rapporto lavoro della risorsa
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
    	validation.min(idTipoRapportoLavoro, 1).message("selezionare un tipo rapporto lavoro");
    	//verifico che dataInizio del rapporto lavoro sia > della dataMinima
    	if (!dataInizio.after(dataMinima)) {
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
	    	renderTemplate("RapportoLavoroController/edit.html", idRapportoLavoro, risorsa, listaTipoRapportoLavoro, idTipoRapportoLavoro, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
        }
    	//se va tutto bene procediamo alla modifica del rapporto lavoro
    	rapportoLavoroAttuale.tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	//nel caso in cui si tratti del primo rapporto di lavoro e la data inizio e la data inizio del rapporto è < di quella di in della risorsa
    	//dataInizio rapporto diventa uguale a dataIn risorsa
    	rapportoLavoroAttuale.dataInizio = (index == 0 && dataInizio.before(risorsa.dataIn)) ? risorsa.dataIn : dataInizio;
    	//valorizza o meno dataFine
    	rapportoLavoroAttuale.dataFine = meseFine == -1 ? null : MyUtility.MeseEdAnnoToDataFine(meseFine, annoFine);
    	rapportoLavoroAttuale.save();
		flash.success("rapporto lavoro modificato con successo");
		list(risorsa.idRisorsa);
    }
    
    public static void delete(Integer idRapportoLavoro) {
    	//PREMESSA si può cancellare solo l'ultimo rapporto lavoro della risorsa e solo se non è l'unico!
    	RapportoLavoro rapportoLavoro = RapportoLavoro.findById(idRapportoLavoro);
    	rapportoLavoro.delete();
		flash.success("Rapporto Lavoro eliminato con successo");
		list(rapportoLavoro.risorsa.idRisorsa);
    }
}

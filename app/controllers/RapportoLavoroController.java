package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.RapportoLavoro;
import models.Risorsa;
import models.TipoRapportoLavoro;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.MyUtility;

@With(SecureCOGE.class)
public class RapportoLavoroController extends Controller {
	
	public static void list(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	ValuePaginator paginator = new ValuePaginator(risorsa.rapportiLavoro);
    	paginator.setPageSize(10);
        render(paginator, risorsa);
    }
    
    public static void create(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	RapportoLavoro rapportoLavoro =  new RapportoLavoro(risorsa);
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	Integer idTipoRapportoLavoro = 0; 
    	Integer idCoCoPro = ((TipoRapportoLavoro)TipoRapportoLavoro.find("byCodice", "CCP").first()).idTipoRapportoLavoro;
    	render(listaTipoRapportoLavoro, idTipoRapportoLavoro, listaAnni, rapportoLavoro,idCoCoPro);
    }
    
    //PREMESSA a questo punto la risorsa avrà già almeno un rapporto di lavoro!
	public static void save(Integer idTipoRapportoLavoro, @Valid RapportoLavoro rapportoLavoro) {
    	validation.min(idTipoRapportoLavoro, 1).message("selezionare un tipo rapporto lavoro");
    	//se ci sono errori di validazione ritorna alla maschera di inserimento
    	if (validation.hasErrors()) {
    		List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
	 		List<Integer> listaAnni = MyUtility.createListaAnni();
	 		Integer idCoCoPro = ((TipoRapportoLavoro)TipoRapportoLavoro.find("byCodice", "CCP").first()).idTipoRapportoLavoro;
	 		renderTemplate("RapportoLavoroController/create.html", listaTipoRapportoLavoro, idTipoRapportoLavoro, listaAnni, rapportoLavoro, idCoCoPro);
        }
    	//se la validazione è andata a buon fine setta dataInizio e tipoRapportoLavoro
    	rapportoLavoro.dataInizio = MyUtility.MeseEdAnnoToDataInizio(rapportoLavoro.meseInizio, rapportoLavoro.annoInizio);
    	rapportoLavoro.tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	if(!rapportoLavoro.tipoRapportoLavoro.codice.equals("CCP")){
    		rapportoLavoro.giorniAssenzeRetribuite = 0;
    	}
    	//procedo alla chiusura dell'ultimo rapporto di lavoro (solo se ha dataFine null) 
    	RapportoLavoro ultimoRapportoLavoro = rapportoLavoro.risorsa.rapportiLavoro.get(rapportoLavoro.risorsa.rapportiLavoro.size() - 1);
    	if (ultimoRapportoLavoro.dataFine == null) {
			ultimoRapportoLavoro.dataFine = MyUtility.subOneDay(rapportoLavoro.dataInizio);
		}
    	//infine creo il nuovo rapporto lavoro, lo aggiungo alla risorsa e salvo
    	rapportoLavoro.risorsa.rapportiLavoro.add(rapportoLavoro);
    	rapportoLavoro.risorsa.save();
		flash.success("rapporto lavoro aggiunto con successo");
		list(rapportoLavoro.risorsa.idRisorsa);
    }
    
    public static void edit(Integer idRapportoLavoro) {
    	RapportoLavoro rapportoLavoro = RapportoLavoro.findById(idRapportoLavoro);
    	rapportoLavoro.meseInizio = MyUtility.getMeseFromDate(rapportoLavoro.dataInizio);
    	rapportoLavoro.annoInizio = MyUtility.getAnnoFromDate(rapportoLavoro.dataInizio);
    	rapportoLavoro.meseFine = rapportoLavoro.dataFine == null ? -1 : MyUtility.getMeseFromDate(rapportoLavoro.dataFine);
    	rapportoLavoro.annoFine = rapportoLavoro.dataFine == null ? -1 : MyUtility.getAnnoFromDate(rapportoLavoro.dataFine);
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	Integer idTipoRapportoLavoro = rapportoLavoro.tipoRapportoLavoro.idTipoRapportoLavoro;
    	Integer idCoCoPro = ((TipoRapportoLavoro)TipoRapportoLavoro.find("byCodice", "CCP").first()).idTipoRapportoLavoro;
    	render(listaTipoRapportoLavoro, idTipoRapportoLavoro, listaAnni, rapportoLavoro,idCoCoPro);
    }
    
    //PREMESSA si può modificare solo l'ultimo rapporto lavoro della risorsa
	public static void update(Integer idTipoRapportoLavoro, @Valid RapportoLavoro rapportoLavoro) {
    	validation.min(idTipoRapportoLavoro, 1).message("selezionare un tipo rapporto lavoro");
    	if (validation.hasErrors()) {
    		List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
	 		List<Integer> listaAnni = MyUtility.createListaAnni();
	 		Integer idCoCoPro = ((TipoRapportoLavoro)TipoRapportoLavoro.find("byCodice", "CCP").first()).idTipoRapportoLavoro;
	    	renderTemplate("RapportoLavoroController/edit.html", listaTipoRapportoLavoro, idTipoRapportoLavoro, listaAnni, rapportoLavoro, idCoCoPro);
        }
    	//se va tutto bene procediamo alla modifica del rapporto lavoro
    	rapportoLavoro.tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	if(!rapportoLavoro.tipoRapportoLavoro.codice.equals("CCP")){
    		rapportoLavoro.giorniAssenzeRetribuite = 0;
    	}
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(rapportoLavoro.meseInizio, rapportoLavoro.annoInizio);
    	//nel caso in cui si tratti del primo rapporto di lavoro e la data inizio del rapporto è < di quella di in della risorsa
    	//dataInizio rapporto diventa uguale a dataIn risorsa
    	int index = rapportoLavoro.risorsa.rapportiLavoro.indexOf(rapportoLavoro);
    	rapportoLavoro.dataInizio = (index == 0 && dataInizio.before(rapportoLavoro.risorsa.dataIn)) ? rapportoLavoro.risorsa.dataIn : dataInizio;
    	//valorizza o meno dataFine
    	rapportoLavoro.dataFine = rapportoLavoro.meseFine == -1 ? null : MyUtility.MeseEdAnnoToDataFine(rapportoLavoro.meseFine, rapportoLavoro.annoFine);
    	rapportoLavoro.save();
		flash.success("rapporto lavoro modificato con successo");
		list(rapportoLavoro.risorsa.idRisorsa);
    }
    
	//PREMESSA si può cancellare solo l'ultimo rapporto lavoro della risorsa e solo se non è l'unico!
	public static void delete(Integer idRapportoLavoro) {
    	RapportoLavoro rapportoLavoro = RapportoLavoro.findById(idRapportoLavoro);
    	rapportoLavoro.delete();
		flash.success("Rapporto Lavoro eliminato con successo");
		list(rapportoLavoro.risorsa.idRisorsa);
    }
}

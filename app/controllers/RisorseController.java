package controllers;

import play.*;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;

import java.util.*;

import javax.persistence.PersistenceException;

import net.sf.oval.constraint.Min;

import models.*;

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
    	if(idTipoRapportoLavoro == 0){
	 		validation.addError("idTipoRapportoLavoro", "rapporto lavorativo obbligatorio");
	 	}
	 	if(validation.hasErrors()) {
        	List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
        	renderTemplate("RisorseController/create.html", risorsa, idTipoRapportoLavoro, listaTipoRapportoLavoro);
        }
        if(Risorsa.find("byMatricola", risorsa.matricola).first() != null) {
        	flash.error("matricola esistente");
        	List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
        	renderTemplate("RisorseController/create.html", risorsa, idTipoRapportoLavoro, listaTipoRapportoLavoro);
        }
        
        Calendar app = Calendar.getInstance();
        app.setTime(risorsa.dataIn);
        app.set(Calendar.DAY_OF_MONTH, 1);
        RapportoLavoro primoRapportoLavoro = new RapportoLavoro(app.getTime(), (TipoRapportoLavoro) TipoRapportoLavoro.findById(idTipoRapportoLavoro), risorsa);
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
    	String matricolaOriginale = risorsa.matricola;
        render(risorsa, matricolaOriginale);
    }
    
    public static void update(@Valid Risorsa risorsa, String matricolaOriginale) {
    	if(validation.hasErrors()) {
        	renderTemplate("RisorseController/edit.html", risorsa, matricolaOriginale);
        }
        if(!risorsa.matricola.equals(matricolaOriginale) && Risorsa.find("byMatricola", risorsa.matricola).first() != null) {
        	flash.error("matricola esistente");
        	renderTemplate("RisorseController/edit.html", risorsa, matricolaOriginale);
        } else if (risorsa.dataOut != null && !risorsa.dataOut.after(risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1).dataInizio)) {
        	flash.error("data out non valida");
        	renderTemplate("RisorseController/edit.html", risorsa, matricolaOriginale);
        }
        RapportoLavoro ultimoRapportoLavoro = risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1);
        if (risorsa.dataOut != null && (ultimoRapportoLavoro.dataFine == null || risorsa.dataOut.before(ultimoRapportoLavoro.dataFine))) {
			ultimoRapportoLavoro.dataFine = risorsa.dataOut;
		}
        if(risorsa.save() != null) {
			 flash.success("risorsa %s modificata con successo", risorsa.matricola);
		 } else {
			flash.error("impossibile modificare la risorsa %s", risorsa.matricola);
		}		 
		list();
	}
    
    public static void delete(Integer idRisorsa) {
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
    	List<Integer> listaAnni = createListaAnni();
    	int meseInizio = Calendar.getInstance().get(Calendar.MONTH);
        int annoInizio = Calendar.getInstance().get(Calendar.YEAR);
        render(risorsa, listaTipoRapportoLavoro, meseInizio, listaAnni, annoInizio);
    }
    public static void saveRapportoLavoro(Integer idRisorsa, Integer idTipoRapportoLavoro, int meseInizio, int annoInizio) {
    	Date dataInizio = MeseEdAnnoToDataInizio(meseInizio, annoInizio);
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	RapportoLavoro ultimoRapportoLavoro = risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1);
    	Date dataMinima = ultimoRapportoLavoro.dataFine == null ? ultimoRapportoLavoro.dataInizio : ultimoRapportoLavoro.dataFine;
    	if(idTipoRapportoLavoro == 0){
	 		validation.addError("idTipoRapportoLavoro", "rapporto lavorativo obbligatorio");
    	} else if (!dataInizio.after(dataMinima)) {
    		validation.addError("dataInizio", "data inizio non valida");
		}
    	if (validation.hasErrors()) {
    		List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
	 		List<Integer> listaAnni = createListaAnni();
	    	renderTemplate("RisorseController/createRapportoLavoro.html", risorsa, listaTipoRapportoLavoro, idTipoRapportoLavoro, meseInizio, listaAnni, annoInizio);
        }
    	if (ultimoRapportoLavoro.dataFine == null) {
			ultimoRapportoLavoro.dataFine = subOneDay(dataInizio);
		}
    	RapportoLavoro nuovoRapportoLavoro = new RapportoLavoro(MeseEdAnnoToDataInizio(meseInizio, annoInizio), (TipoRapportoLavoro) TipoRapportoLavoro.findById(idTipoRapportoLavoro), (Risorsa) Risorsa.findById(idRisorsa));
    	risorsa.rapportiLavoro.add(nuovoRapportoLavoro);
    	risorsa.save();
    	listRapportoLavoro(idRisorsa);
    }
    
    public static void editRapportoLavoro(Integer idRapportoLavoro) {
    	RapportoLavoro rapportoLavoro = RapportoLavoro.findById(idRapportoLavoro);
    	Risorsa risorsa = rapportoLavoro.risorsa;
    	int idTipoRapportoLavoro = rapportoLavoro.tipoRapportoLavoro.idTipoRapportoLavoro;
    	int meseInizio = getMeseFromDate(rapportoLavoro.dataInizio);
        int annoInizio = getAnnoFromDate(rapportoLavoro.dataInizio);
        int meseFine = rapportoLavoro.dataFine == null ? -1 : getMeseFromDate(rapportoLavoro.dataFine);
        int annoFine = rapportoLavoro.dataFine == null ? -1 : getAnnoFromDate(rapportoLavoro.dataFine);
        List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
    	List<Integer> listaAnni = createListaAnni();
    	render(idRapportoLavoro, risorsa, listaTipoRapportoLavoro, idTipoRapportoLavoro, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
    }
    
    public static void updateRapportoLavoro(Integer idRapportoLavoro, Integer idTipoRapportoLavoro, int meseInizio, int annoInizio, int meseFine, int annoFine) {
    	Date dataInizio = MeseEdAnnoToDataInizio(meseInizio, annoInizio);
    	RapportoLavoro rapportoLavoroAttuale = RapportoLavoro.findById(idRapportoLavoro);
    	Risorsa risorsa = rapportoLavoroAttuale.risorsa;
    	int index = risorsa.rapportiLavoro.indexOf(rapportoLavoroAttuale);
    	Date dataMinima = index == 0 ? subOneMonth(risorsa.dataIn) : risorsa.rapportiLavoro.get(index - 1).dataFine;
    	if(idTipoRapportoLavoro == 0){
	 		validation.addError("idTipoRapportoLavoro", "rapporto lavorativo obbligatorio");
    	} else if (!dataInizio.after(dataMinima)) {
    		validation.addError("dataInizio", "data inizio non valida");
		} else if (meseFine > -1 ^ annoFine > -1) {
    		validation.addError("dataFine", "data fine non valida");
		} else if (meseFine > -1 && annoFine > -1 && !MeseEdAnnoToDataFine(meseFine, annoFine).after(dataInizio)) {
    		validation.addError("dataFine", "data fine non valida");
		} 
    	if (validation.hasErrors()) {
    		List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
	 		List<Integer> listaAnni = createListaAnni();
	    	renderTemplate("RisorseController/editRapportoLavoro.html", idRapportoLavoro, risorsa, listaTipoRapportoLavoro, idTipoRapportoLavoro, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
        }
    	rapportoLavoroAttuale.tipoRapportoLavoro = TipoRapportoLavoro.findById(idTipoRapportoLavoro);
    	rapportoLavoroAttuale.dataInizio = (index == 0 && dataInizio.before(risorsa.dataIn)) ? risorsa.dataIn : dataInizio;
    	rapportoLavoroAttuale.dataFine = meseFine == -1 ? null : MeseEdAnnoToDataFine(meseFine, annoFine);
    	rapportoLavoroAttuale.save();
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
    
    //------------------------ UTILITY RAPPORTO LAVORO
    
    private static List<Integer> createListaAnni() {
    	List<Integer> listaAnni = new ArrayList<Integer>();
    	Calendar calendar = Calendar.getInstance();
    	int annoCorrente = calendar.get(Calendar.YEAR);
    	for(int i = annoCorrente - 20; i <= annoCorrente + 20; i++) {
    		listaAnni.add(i);
    	}
    	return listaAnni;
    }
    
    private static Date MeseEdAnnoToDataInizio(int mese, int anno) {
    	return new GregorianCalendar(anno, mese, 1).getTime();
    }
    
    private static Date MeseEdAnnoToDataFine(int mese, int anno) {
    	Calendar calendar = new GregorianCalendar(anno, mese, 1);
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    	return calendar.getTime();
    }
    
    private static int getMeseFromDate(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	return calendar.get(Calendar.MONTH);
    }
    
    private static int getAnnoFromDate(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	return calendar.get(Calendar.YEAR);
    }
    
    private static Date subOneDay(Date original) {
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(original);
		 calendar.add(Calendar.DAY_OF_YEAR, -1);
		 return calendar.getTime();
	 }
    
    private static Date subOneMonth(Date original) {
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(original);
		 calendar.add(Calendar.MONTH, -1);
		 return calendar.getTime();
	 }

}

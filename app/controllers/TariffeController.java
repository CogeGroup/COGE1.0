package controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Cliente;
import models.Commessa;
import models.CommessaACorpo;
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
    	List<Commessa> listaCommesse = Commessa.listaCommesseAttive();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int meseInizio = Calendar.getInstance().get(Calendar.MONTH);
        int annoInizio = Calendar.getInstance().get(Calendar.YEAR);
        render(risorsa.idRisorsa, listaCommesse, meseInizio, listaAnni, annoInizio);
    }
    
    public static void save(@Valid Tariffa tariffa) {
    	int meseInizio = tariffa.meseInizio;
    	int annoInizio = tariffa.annoInizio;
    	Integer idCommessa = tariffa.idCommessa;
    	Integer idRisorsa = tariffa.idRisorsa;
    	
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(meseInizio, annoInizio);
        Commessa commessa = idCommessa != null ? (Commessa) Commessa.findById(idCommessa) : new Commessa();
        tariffa.commessa = commessa;
        tariffa.dataInizio = dataInizio;
        Risorsa risorsa = Risorsa.findById(idRisorsa);
        tariffa.risorsa = risorsa;
        
        if(validation.hasErrors()){
        	List<Commessa> listaCommesse = Commessa.listaCommesseAttive();
    		List<Integer> listaAnni = MyUtility.createListaAnni();
        	render("TariffeController/create.html", idRisorsa, tariffa, listaCommesse, meseInizio, listaAnni, annoInizio);
        }
        
        // Se la commessa è a corpo l'importo della tariffa seve essere 0
        tariffa.importoGiornaliero = commessa instanceof CommessaACorpo ? 0 : tariffa.importoGiornaliero;
        // Salvataggio tariffa
        tariffa.save();
        flash.success("Tariffa aggiunta con successo");
    	list(idRisorsa);
    }
    
    public static void edit(Integer idTariffa) {
    	Tariffa tariffa = Tariffa.findById(idTariffa);
    	// Controlla se la tariffa da modificare è l'ultima
    	tariffaIsLast(tariffa);
    	
        List<Commessa> listaCommesse = Commessa.listaCommesseAttive();
    	int meseInizio = MyUtility.getMeseFromDate(tariffa.dataInizio);
        int annoInizio = MyUtility.getAnnoFromDate(tariffa.dataInizio);
        int meseFine = tariffa.dataFine == null ? -1 : MyUtility.getMeseFromDate(tariffa.dataFine);
        int annoFine = tariffa.dataFine == null ? -1 : MyUtility.getAnnoFromDate(tariffa.dataFine);
    	List<Integer> listaAnni = MyUtility.createListaAnni();
        render(tariffa, listaCommesse, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
    }

    public static void update(@Valid Tariffa tariffa) {
    	int meseInizio = tariffa.meseInizio;
    	int annoInizio = tariffa.annoInizio;
    	int meseFine = tariffa.meseFine;
    	int annoFine = tariffa.annoFine;
    	Integer idCommessa = tariffa.idCommessa;
    	
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(meseInizio, annoInizio);    	
    	Commessa commessa = Commessa.findById(idCommessa);
        tariffa.commessa = commessa;
        tariffa.dataInizio = dataInizio;
        
        if(validation.hasErrors()){
        	List<Commessa> listaCommesse = Commessa.listaCommesseAttive();
    		List<Integer> listaAnni = MyUtility.createListaAnni();
    		render("TariffeController/edit.html", tariffa, listaCommesse, meseInizio, listaAnni, annoInizio, meseFine, annoFine);
        }
        if(meseFine != -1 && annoFine != -1){
	        Date dataFine = MyUtility.MeseEdAnnoToDataFine(meseFine, annoFine);
	        if(commessa.dataFineCommessa != null && commessa.dataFineCommessa.before(dataFine)){
	        	 tariffa.dataFine = commessa.dataFineCommessa;
	        }else{
	        	 tariffa.dataFine = dataFine;
	        }
        }else{
        	tariffa.dataFine = null;
        }
        
        // Se la commessa è a corpo l'importo della tariffa seve essere 0
        tariffa.importoGiornaliero = commessa instanceof CommessaACorpo ? 0 : tariffa.importoGiornaliero;
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
    	// Controlla se la tariffa da modificare è l'ultima
    	tariffaIsLast(tariffa);
    	
    	// Controlla se è gia chiusa
    	if(tariffa.dataFine != null){
    		flash.success("Tariffa gia chiusa");
    		list(tariffa.risorsa.idRisorsa);
    	}
    	
    	if(tariffa.dataInizio.after(new Date())){
    		tariffa.dataFine = tariffa.dataInizio;
    	}else{
        	int meseFine = MyUtility.getMeseFromDate(new Date());
        	int annoFine = MyUtility.getAnnoFromDate(new Date());
        	Date dataFine = MyUtility.MeseEdAnnoToDataFine(meseFine, annoFine);
        	if(tariffa.commessa.dataFineCommessa != null && dataFine.after(tariffa.commessa.dataFineCommessa)){
        		tariffa.dataFine = tariffa.commessa.dataFineCommessa;
        	}else{
        		tariffa.dataFine = dataFine;
        	}
    	}
    	
    	tariffa.save();
        list(tariffa.risorsa.idRisorsa);
    }
    
	private static void tariffaIsLast(Tariffa tariffa) {
		List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa",tariffa.commessa, tariffa.risorsa).fetch();
        if(tariffa.dataFine != null){
        	// Controlla se la tariffa da modificare è l'ultima
	        for (Tariffa t : lista) {
				if(t.dataInizio.after(tariffa.dataFine)){
					flash.success("La commessa: " + tariffa.commessa.codice + " ha gia altre tariffe");
					list(tariffa.risorsa.idRisorsa);
				}
			}
        }
	}
    
}
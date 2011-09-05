package controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Commessa;
import models.CommessaACorpo;
import models.Risorsa;
import models.Tariffa;
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
    	paginator.setPageSize(10);
        render(paginator, risorsa);
    }
    
    public static void create(Integer idRisorsa) {
    	Tariffa tariffa = new Tariffa((Risorsa)Risorsa.findById(idRisorsa), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR));
    	List<Commessa> listaCommesse = Commessa.findCommesseFatturabiliAttive();
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	render(tariffa, listaCommesse, listaAnni);
    }
    
    public static void save(@Valid Tariffa tariffa) {
    	if(validation.hasErrors()){
        	List<Commessa> listaCommesse = Commessa.findCommesseFatturabiliAttive();
    		List<Integer> listaAnni = MyUtility.createListaAnni();
        	render("TariffeController/create.html", tariffa, listaCommesse, listaAnni);
        }
    	
    	Date dataInizio = MyUtility.MeseEdAnnoToDataInizio(tariffa.meseInizio, tariffa.annoInizio);
    	tariffa.dataInizio = dataInizio;
    	System.out.println(dataInizio);
    	System.out.println(tariffa.dataInizio);
    	List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa", tariffa.commessa, tariffa.risorsa).fetch();
    	if(lista.size() > 0){
			Tariffa t = lista.get(lista.size()-1);
			if(t.dataFine == null && dataInizio.after(t.dataInizio)){
				Calendar c = Calendar.getInstance();
				c.setTime(tariffa.dataInizio);
				c.add(Calendar.DAY_OF_MONTH, -1);
				Date data = c.getTime();
				t.dataFine=data;
				t.save();
			}
		}
    	
    	tariffa.dataInizio = dataInizio;
        tariffa.save();
        flash.success("Tariffa aggiunta con successo");
    	list(tariffa.risorsa.idRisorsa);
    }
    
    public static void edit(Integer idTariffa) {
    	Tariffa tariffa = Tariffa.findById(idTariffa);
    	// Controlla se la tariffa da modificare è l'ultima
    	if(!tariffaIsLast(tariffa)){
    		list(tariffa.risorsa.idRisorsa);
    	}
    	
        List<Commessa> listaCommesse = Commessa.findCommesseFatturabiliAttive();
        tariffa.meseInizio = MyUtility.getMeseFromDate(tariffa.dataInizio);
        tariffa.annoInizio = MyUtility.getAnnoFromDate(tariffa.dataInizio);
        tariffa.meseFine = tariffa.dataFine == null ? -1 : MyUtility.getMeseFromDate(tariffa.dataFine);
        tariffa.annoFine = tariffa.dataFine == null ? -1 : MyUtility.getAnnoFromDate(tariffa.dataFine);
    	List<Integer> listaAnni = MyUtility.createListaAnni();
        render(tariffa, listaCommesse, listaAnni);
    }

    public static void update(@Valid Tariffa tariffa) {
    	if(validation.hasErrors()){
        	List<Commessa> listaCommesse = Commessa.findCommesseFatturabiliAttive();
    		List<Integer> listaAnni = MyUtility.createListaAnni();
    		render("TariffeController/edit.html", tariffa, listaCommesse, listaAnni);
        }
        
    	if(tariffa.meseFine != -1 && tariffa.annoFine != -1){
	        Date dataFine = MyUtility.MeseEdAnnoToDataFine(tariffa.meseFine, tariffa.annoFine);
	        if(tariffa.commessa.dataFineCommessa != null && tariffa.commessa.dataFineCommessa.before(dataFine)){
	        	 tariffa.dataFine = tariffa.commessa.dataFineCommessa;
	        }else{
	        	 tariffa.dataFine = dataFine;
	        }
        }else{
        	tariffa.dataFine = null;
        }
        tariffa.dataInizio = MyUtility.MeseEdAnnoToDataInizio(tariffa.meseInizio, tariffa.annoInizio);    	
        tariffa.save();
        flash.success("Tariffa modificata con successo");
    	list(tariffa.risorsa.idRisorsa);
    }

    public static void delete(Integer idTariffa) {
    	Tariffa tariffa = Tariffa.findById(idTariffa);
    	// Controlla se la tariffa da modificare è l'ultima
    	if(!tariffaIsLast(tariffa)){
    		list(tariffa.risorsa.idRisorsa);
    	}
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
    
	private static boolean tariffaIsLast(Tariffa tariffa) {
		List<Tariffa> lista = Tariffa.find("byCommessaAndRisorsa",tariffa.commessa, tariffa.risorsa).fetch();
        if(tariffa.dataFine != null){
        	// Controlla se la tariffa da modificare è l'ultima
	        for (Tariffa t : lista) {
				if(t.dataInizio.after(tariffa.dataFine)){
					flash.success("La commessa: " + tariffa.commessa.codice + " ha gia altre tariffe");
					return false;
				}
			}
        }
        return true;
	}
	
}
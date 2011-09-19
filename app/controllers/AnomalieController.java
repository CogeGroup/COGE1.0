package controllers;

import java.util.Calendar;
import java.util.List;

import models.RendicontoAttivita;
import models.Risorsa;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.MyUtility;

@With(SecureCOGE.class)
public class AnomalieController extends Controller {

    public static void index() {
        render();
    }
    
    public static void rapportiniMancanti() {
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int mese = Calendar.getInstance().get(Calendar.MONTH)-1;
        int anno = Calendar.getInstance().get(Calendar.YEAR);
        render(mese, listaAnni, anno);
    }

    public static void listRapportiniMancanti(int mese, int anno) {
    	List<Risorsa> listaAnomalie = RendicontoAttivita.listRapportiniMancanti(mese, anno);
    	ValuePaginator paginator = new ValuePaginator(listaAnomalie);
		paginator.setPageSize(10);
		render(paginator, mese, anno);
    }
    
    public static void anomalieRicavi() {
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int mese = Calendar.getInstance().get(Calendar.MONTH)-1;
        int anno = Calendar.getInstance().get(Calendar.YEAR);
        render(mese, listaAnni, anno);
    }
    
    public static void listAnomalieRicavi(int mese, int anno) {
    	List<Risorsa> listaAnomalie = Risorsa.listAnomalieRicavi(mese, anno);
    	ValuePaginator paginator = new ValuePaginator(listaAnomalie);
		paginator.setPageSize(10);
		render(paginator, mese, anno);
    }
}

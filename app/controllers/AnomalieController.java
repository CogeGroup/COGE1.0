package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Costo;
import models.DettaglioRapportoAttivita;
import models.RendicontoAttivita;
import models.Risorsa;

import play.db.jpa.GenericModel.JPAQuery;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
import utility.MyUtility;

public class AnomalieController extends Controller {

    public static void index() {
        render();
    }
    
    public static void rapportiniMancanti() {
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int mese = Calendar.getInstance().get(Calendar.MONTH);
        int anno = Calendar.getInstance().get(Calendar.YEAR);
        render(mese, listaAnni, anno);
    }

    public static void listRapportiniMancanti(int mese, int anno) {
    	mese++;
    	List<Risorsa> listaAnomalie = RendicontoAttivita.listRapportiniMancanti(mese, anno);
    	ValuePaginator paginator = new ValuePaginator(listaAnomalie);
		paginator.setPageSize(5);
		mese--;
		render(paginator, mese, anno);
    }
    
    public static void anomalieRicavi() {
    	List<Integer> listaAnni = MyUtility.createListaAnni();
    	int mese = Calendar.getInstance().get(Calendar.MONTH);
        int anno = Calendar.getInstance().get(Calendar.YEAR);
        render(mese, listaAnni, anno);
    }
    
    public static void listAnomalieRicavi(int mese, int anno) {
    	mese++;
    	List<Risorsa> listaAnomalie = Risorsa.listAnomalieRicavi(mese, anno);
    	ValuePaginator paginator = new ValuePaginator(listaAnomalie);
		paginator.setPageSize(5);
		mese--;
		render(paginator, mese, anno);
    }
}

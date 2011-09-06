package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Costo;
import models.DettaglioRapportoAttivita;
import models.RendicontoAttivita;
import models.Risorsa;

import play.data.validation.Validation;
import play.db.jpa.GenericModel.JPAQuery;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
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
    	if(!MyUtility.MeseEdAnnoToDataFine(mese, anno).before(new Date())){
			Validation.addError("data", "Data selezionata non valida");
			List<Integer> listaAnni = MyUtility.createListaAnni();
			render("anomaliecontroller/rapportiniMancanti.html",listaAnni,mese,anno);
		}
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

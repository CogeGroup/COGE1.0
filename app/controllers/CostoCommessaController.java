package controllers;

import java.util.List;

import models.Commessa;
import models.CommessaACorpo;
import models.CostoCommessa;
import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.MyUtility;

@With(SecureCOGE.class)
public class CostoCommessaController extends Controller  {
	
	public static void create(Integer idCommessa) {
		CommessaACorpo commessa = Commessa.findById(idCommessa);
    	CostoCommessa costo = new CostoCommessa(commessa);
    	render(costo);
	}
    public static void save(@Valid CostoCommessa costo){
    	// Validazione del form
    	if(validation.hasErrors()) {
    		render("CostoCommessaController/create.html", costo);
    	}
    	costo.data = MyUtility.getPrimoDelMese(costo.data);
    	//se esiste già un costo associato alla commessa per lo stesso mese
    	List<CostoCommessa> listaCosti = CostoCommessa.find("byCommessaAndData", costo.commessa,costo.data).fetch();
    	if(listaCosti == null || listaCosti.size() == 0) {
    		costo.save();
            flash.success("Costo aggiunto con successo");
            CommesseController.show(costo.commessa.idCommessa);
    	} else {
    		flash.error("Costo già presente per il: " + MyUtility.dateToString(costo.data, "MM-yyyy"));
    		create(costo.commessa.idCommessa);
    	}
    }
    public static void delete(Integer idCostoCommessa){
    	CostoCommessa costo = CostoCommessa.findById(idCostoCommessa);
    	Integer idCommessa = costo.commessa.idCommessa;
    	costo.delete();
        flash.success("Costo rimosso con successo");
        CommesseController.show(idCommessa);
    }

}

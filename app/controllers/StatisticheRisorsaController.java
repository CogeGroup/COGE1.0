package controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import models.Risorsa;
import play.mvc.*;

public class StatisticheRisorsaController extends Controller {

    public static void index() throws Exception {
    	List<Risorsa> risorse = Risorsa.findAll();
    	List<Risorsa> reports = new ArrayList<Risorsa>();
    	for (Risorsa risorsa : risorse) {
    		reports.addAll(risorsa.reportRisorse("07", "2010"));
		}
    	System.out.println("matr | cod | nome | rapp | ore | ricavo | costo | margine | tariffa | cliente/commessa");
    	for (Risorsa report : reports) {
			System.out.println(report.matricola + " | " + report.codice + " | " + 
					report.cognome+ " " +report.nome + " | " + report.codiceRapporto + " | " + report.ore + " | " + report.ricavo + " | " + 
					report.costo + " | " + report.margine + " | " +report.importoTariffa + " | " + report.clienteProgetto);
		}
    	render("RisorseController/index.html");
    }
    
}

package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import models.Commessa;
import models.Ruolo;
import models.Utente;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import play.db.DB;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;

@With(SecureCOGE.class)
public class LogInController extends Controller {

    public static void index() {
    	
        render();
    }
    
    
	
    public static void authenticate() {
        render();
    }
    
    
    public static void stat1(){
    	
//      JasperPrint jasperPrint=null;
    	
    	ArrayList<Map> list = new ArrayList<Map>();
    	Map m1 = new HashMap();
    	m1.put("numeroRisorse", 2);
    	m1.put("tipoRapporto", "dipendente");
    	m1.put("codice", "CCP");
    	m1.put("ore", 30.0f);
    	m1.put("ricavo", 3000.0f);
    	m1.put("costo", 2000.0f);
    	m1.put("margine", 50.0f);
    	
    	Map m2 = new HashMap();
    	m2.put("numeroRisorse", 2);
    	m2.put("tipoRapporto", "dipendente");
    	m2.put("codice", "CCP");
    	m2.put("ore", 30.0f);
    	m2.put("ricavo", 3000.0f);
    	m2.put("costo", 2000.0f);
    	m2.put("margine", 50.0f);
    	
    	list.add(m1);
    	list.add(m2);
    	
        Map reportParams = new HashMap();
        reportParams.put("MESE", "7");
        reportParams.put("ANNO", "2011");
       try {
		Date dataRapporto = new SimpleDateFormat("dd/MM/yyyy").parse("01/" + "07" + "/" + "2011");
	} catch (ParseException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
        reportParams.put("cognome", "b");
        reportParams.put("costoUnitario", "c");
        
         try{
              
               
             // JRBeanCollectionDataSource datasource = new JRBeanCollectionDataSource(list);                       
              JasperPrint jrprint =  JasperFillManager.fillReport("c://WSCoge//COGE1.0//reports//report2.jasper", reportParams,DB.getConnection()); 
              response.setHeader("Content-disposition", "attachment;filename=report.pdf");
              JasperExportManager.exportReportToPdfStream(jrprint,response.out);
                
             
         }catch (Exception e) {
                 e.printStackTrace();
               }

         
    	
    	
    	
    }
}

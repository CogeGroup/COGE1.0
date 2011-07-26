package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Commessa;
import models.Utente;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import play.mvc.Controller;
import play.mvc.With;

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
    	
    	ArrayList<Commessa> list = new ArrayList<Commessa>();
    	list.add(new Commessa("d1","c1",true));
    	list.add(new Commessa("d2","c2",true));
    	list.add(new Commessa("d3","c3",true));
    	list.add(new Commessa("d4","c4",true));
    	list.add(new Commessa("d5","c5",true));
    	
        Map reportParams = new HashMap();
        reportParams.put("nome", "a");
        reportParams.put("cognome", "b");
        reportParams.put("costoUnitario", "c");
     
         try{
              
               
              JRBeanCollectionDataSource datasource = new JRBeanCollectionDataSource(list);                       
              JasperPrint jrprint =  JasperFillManager.fillReport("c://WSCoge//COGE1.0//reports//statisticaPerRisorsa.jasper", reportParams,datasource); 
              response.setHeader("Content-disposition", "attachment;filename=report.pdf");
              JasperExportManager.exportReportToPdfStream(jrprint,response.out);
                
             
         }catch (Exception e) {
                 e.printStackTrace();
               }

         
    	
    	
    	
    }
}

package controllers;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.tools.reflect.Sample;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import models.Cliente;
import models.RendicontoAttivita;
import models.TipoRapportoLavoro;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import play.db.DB;
import play.mvc.Controller;
import play.mvc.With;
import play.vfs.VirtualFile;
import secure.SecureCOGE;

public class StatisticheController extends Controller {
	
	
	public static void index() {
		 render();
   }
	

	public static void tipoRapportoLavoro() {

		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
	  System.out.println(listaAnni.size());
		 render(listaAnni);
   }
	
	
	public static void showRapportoLavoro(Integer mese,Integer anno){

		DateMidnight dataInizio = new DateMidnight().withDayOfMonth(1).withMonthOfYear(mese).withYear(anno);
		DateMidnight dataFine = new DateMidnight().withMonthOfYear(mese).withYear(anno).dayOfMonth().withMaximumValue();
		Map reportParams = new HashMap();
		 reportParams.put("MESE", mese);
		 reportParams.put("ANNO", anno);
		 reportParams.put("DATA_INIZIO", dataInizio.toDate());
		 reportParams.put("DATA_FINE", dataFine.toDate());
		 JasperPrint jrprint;
		 
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/report2.jasper");
			jrprint = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams,DB.getConnection());
			 response.setHeader("Content-disposition", "attachment;filename=report.pdf");
	          JasperExportManager.exportReportToPdfStream(jrprint,response.out);
		} catch (JRException e) {
			e.printStackTrace();
		} 
	       	
   }
	
	
	
	
	public static void showTest(){

		
		Map reportParams = new HashMap();
		 reportParams.put("xx", "aa");
		 JasperPrint jrprint;
		 
		 ArrayList<HashMap> list = new ArrayList<HashMap>();
		 list = Cliente.statisticheClienti(2010);
//		 HashMap m1 = new HashMap();
//		 m1.put("nome", "nome1");
//		 m1.put("cognome", "cognome1");
//		 
//		 HashMap m2 = new HashMap();
//		 m2.put("nome", "nome2");
//		 m2.put("cognome", "cognome2");
//		 
//		 list.add(m1);
//		 list.add(m2);
//		 
	
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/reportTestClienti.jasper");
			jrprint = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams,new JRBeanCollectionDataSource(list) );
			 response.setHeader("Content-disposition", "attachment;filename=report.pdf");
	          JasperExportManager.exportReportToPdfStream(jrprint,response.out);
		} catch (JRException e) {
			e.printStackTrace();
		} 
	       	
   }
	
	
	
	public static void clienti() {

		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		 render(listaAnni);
   }
	public static void showClienti(Integer anno){

		Map reportParams = new HashMap();
		 reportParams.put("ANNO", anno);
		 JasperPrint jrprint;
		String dateStr =  new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_clienti.jrxml");
			 JasperReport jasperReport =  JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,DB.getConnection());
			 response.setHeader("Content-disposition", "attachment;filename=report_"+dateStr+".pdf");
	          JasperExportManager.exportReportToPdfStream(jrprint,response.out);
		} catch (JRException e) {
			e.printStackTrace();
		} 
	       	
   }

}

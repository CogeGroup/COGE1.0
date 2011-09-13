package controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import models.Commessa;
import models.Risorsa;
import models.Ruolo;
import models.Utente;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.ConvertToJson;
import utility.DomainWrapper;
import utility.MyUtility;
@With(SecureCOGE.class)
public class AccountController extends Controller {
	
	@Catch(Exception.class)
	public static void logException(Throwable throwable) {
		//logger e redirect
	}

	public static void index() {
    	render();
    }
    
	public static void listUtenti() {
		//lista degli utenti in render
		List<Utente> listaUtenti = Utente.all().fetch();
		ValuePaginator<Utente> paginator = new ValuePaginator(listaUtenti);
		render(paginator);
	}
	
	public static void searchUtente(Integer idUtente) {
    	if(idUtente == null || idUtente.equals("")){
    		listUtenti();
    	}
    	Utente commessa = Utente.findById(idUtente);
    	List<Utente> listaUtenti = new ArrayList<Utente>();
    	listaUtenti.add(commessa);
		ValuePaginator paginator = new ValuePaginator(listaUtenti);
		paginator.setPageSize(10);
		render("AccountController/listUtenti.html",paginator);
    }
	  
	public static void createUtente() {
		List<Ruolo> listaRuoli = Ruolo.findAll();
		List<Risorsa> listaRisorse = Risorsa.find("dataOut is null").fetch();
		Utente utente = new Utente();
		render(listaRuoli,listaRisorse, utente);
	}
	  
	public static void saveUtente(@Valid Utente utente, @Required(message="Inserire un ruolo")String ruolo, @Required(message="Inserire una risorsa") Integer idRisorsa) {
		if(validation.hasErrors()){
			List<Ruolo> listaRuoli = Ruolo.findAll();
			List<Risorsa> listaRisorse = Risorsa.all().fetch();
			render("AccountController/createUtente.html",listaRuoli,listaRisorse,utente);
		}
		//Gestione Ruoli
		String [] listaR = ruolo.split(",");
		//rimuovo gli eventuali valori doppi
		Set<Object> uniquesetRuolo = new HashSet<Object>(Arrays.asList(listaR));
		Object [] uniqueRuolo = uniquesetRuolo.toArray();
		List<Ruolo> listaRuoliUtente = new ArrayList<Ruolo>();
		for(int i = 0;i<uniqueRuolo.length;i++){
			Ruolo r = Ruolo.findById(Integer.parseInt(uniqueRuolo[i].toString()));
			listaRuoliUtente.add(r);
		}		  
		if(listaRuoliUtente !=null && listaRuoliUtente.size()>0){  
			//find della risorsa by id e controllo se non esiste nessun utente associato alla risorsa
			Risorsa r = Risorsa.findById(idRisorsa);
			if(r!=null){
				//ho trovato la risorsa da associare al nuovo utente controllo che nessun utente è associato
				Utente u = Utente.find("byRisorsa", r).first();
				if(u == null){
					//save dell'utente
					utente.risorsa=r;
					utente.ruoli = listaRuoliUtente;
					utente.abilitato = true;
					utente.save();
					flash.success("Utente %s salvato con successo",utente.username);
					//lista degli utenti in render
					listUtenti();
				}else{
					validation.addError("idRisorsa", "risorsa assegnata ad altro utente");
					// flash.error("risorsa assegnata ad altro utente");
					List<Ruolo> listaRuoli = Ruolo.findAll();
					List<Risorsa> listaRisorse = Risorsa.all().fetch();
					render("AccountController/createUtente.html",listaRuoli,listaRisorse,utente);
				}
			}else{
				validation.addError("idRisorsa", "risorsa non trovata");
				// flash.error("risorsa non trovata");
				List<Ruolo> listaRuoli = Ruolo.findAll();
				List<Risorsa> listaRisorse = Risorsa.all().fetch();
				render("AccountController/createUtente.html",listaRuoli,listaRisorse,utente);
			}					  
		}
	}
	  
	public static void showUtente(Integer idU) {
		Utente utente = Utente.findById(idU);
		String mylist = ConvertToJson.convert(utente.ruoli, "idRuolo", "descrizione"); 
		render(mylist,utente);
	}
	  
	public static void updateUtente(@Valid Utente utente, @Required(message="Inserire un ruolo")String ruolo, @Required(message="Inserire una risorsa") Integer idRisorsa) {
		if(validation.hasErrors()){
			utente = Utente.findById(utente.idUtente);
			String mylist = ConvertToJson.convert(utente.ruoli, "idRuolo", "descrizione"); 
			render("AccountController/showUtente.html",mylist,utente);
		}
		//Gestione Ruoli
		String [] listaR = ruolo.split(",");
		//rimuovo gli eventuali valori doppi
		Set<Object> uniquesetRuolo = new HashSet<Object>(Arrays.asList(listaR));
		Object [] uniqueRuolo = uniquesetRuolo.toArray();
		List<Ruolo> listaRuoliUtente = new ArrayList<Ruolo>();
		for(int i = 0;i<uniqueRuolo.length;i++){
			Ruolo r = Ruolo.findById(Integer.parseInt(uniqueRuolo[i].toString()));
			listaRuoliUtente.add(r);
		}
		if(listaRuoliUtente !=null && listaRuoliUtente.size()>0){
			//find della risorsa by id e controllo se non esiste nessun utente oltre al associato alla risorsa
			Risorsa r = Risorsa.findById(idRisorsa);
			if(r!=null){
				//ho trovato la risorsa da associare al nuovo utente controllo che nessun utente è associato
				Utente u = Utente.find("byRisorsa", r).first();
				if(u==null || u.idUtente == utente.idUtente ){
					//update dell'utente
					utente.risorsa=r;
					utente.ruoli = listaRuoliUtente;
					utente.save();
					flash.success("Utente %s modificato con successo",utente.username);
					//lista degli utenti in render
					listUtenti();	  
				}else{
					validation.addError("idRisorsa", "risorsa assegnata ad altro utente");
					// flash.error("risorsa assegnata ad altro utente");
					utente = Utente.findById(utente.idUtente);
					String mylist = ConvertToJson.convert(utente.ruoli, "idRuolo", "descrizione"); 
					render("AccountController/showUtente.html",mylist,utente);
				}
			}else{
				validation.addError("idRisorsa", "risorsa non trovata");
				// flash.error("risorsa non trovata");
				utente = Utente.findById(utente.idUtente);
				String mylist = ConvertToJson.convert(utente.ruoli, "idRuolo", "descrizione"); 
				render("AccountController/showUtente.html",mylist,utente);
			}		
		}
	}
	  
	public static void deleteUtente(Integer idU) {
		Utente u = Utente.findById(idU);
		if(session.get("username").equals(u.username)){
			flash.error("Non è stato possibile eliminare l'utente in sessione");
			listUtenti();
		}
		Utente uDel = u.delete();
		if(uDel !=null){
			flash.success("Utente eliminato con successo");	 
		}else{
			flash.error("Non è stato possibile eliminare l'utente");
		}
		listUtenti();
	}
	  
	public static void autocompleteRisorsa(String term) {
		List<Risorsa> listaRisorse = Risorsa.find("codice like ? or cognome like ?","%"+term+"%","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Risorsa ris:listaRisorse){
			listaResult.add(new DomainWrapper(ris.idRisorsa, ris.codice + " - " + ris.cognome));
		}
		renderJSON(listaResult);
    }
	  
	public static void autocompleteRuolo(String term) {
		List<Ruolo> listaRuoli = Ruolo.find("descrizione like ?", "%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Ruolo r:listaRuoli){
			listaResult.add(new DomainWrapper(r.idRuolo, r.descrizione));
		}
		renderJSON(listaResult);
	}
	
	public static void autocompleteUtente(String term) {
		List<Utente> listaUtenti = Utente.find("username like ?","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Utente u:listaUtenti){
			listaResult.add(new DomainWrapper(u.idUtente, u.username));
		}
		renderJSON(listaResult);
	}
	  
	public static void export() {
		String dateStr =  new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		response.setHeader("Content-disposition", "attachment;filename=ListaUtenti"+dateStr+".xls");
	    response.setContentTypeIfNotSet("application/msexcel");
	    OutputStream out;
		try {
			out = response.out;
			//Creo un Workbook e poi un foglio di lavoro
			HSSFWorkbook wb  = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("Utenti");
			HSSFCellStyle style = wb.createCellStyle();
			style.setTopBorderColor(IndexedColors.BLACK.getIndex());
			style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			style.setRightBorderColor(IndexedColors.BLACK.getIndex());
			style.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
			style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			HSSFRow row = sheet.createRow((short)0);
		    Cell c1 = row.createCell(0);
		    c1.setCellStyle(style);
		    c1.setCellValue("USERNAME");
		    Cell c3 = row.createCell(1);
		    c3.setCellStyle(style);
		    c3.setCellValue("NOMINATIVO");
		    Cell c4 = row.createCell(2);
		    c4.setCellStyle(style);
		    c4.setCellValue("E-MAIL");
		    Cell c5 = row.createCell(3);
		    c5.setCellStyle(style);
		    c5.setCellValue("PASSWORD");
		    Cell c6 = row.createCell(4);
		    c6.setCellStyle(style);
		    c6.setCellValue("ATTIVO");
		    short i = 1;
		    List<Utente> listaUtenti = Utente.find("order by username").fetch();
		    for(Utente utente : listaUtenti){
		    	row = sheet.createRow(i);
			    row.createCell(0).setCellValue(utente.username);
			    row.createCell(1).setCellValue(utente.risorsa.nome + " " + utente.risorsa.cognome);
			    row.createCell(2).setCellValue(utente.email);
			    row.createCell(3).setCellValue(utente.password);
			    row.createCell(4).setCellValue(utente.abilitato == true ? "SI" : "NO");
			    i++;
		    }
			sheet.autoSizeColumn(0);
		    sheet.autoSizeColumn(1);
		    sheet.autoSizeColumn(2);
		    sheet.autoSizeColumn(3);
		    sheet.autoSizeColumn(4);
		    out.flush();
		    wb.write(out);  
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	  
}
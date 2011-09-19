package controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;


import models.Commessa;
import models.Costo;
import models.Gruppo;
import models.RapportoLavoro;
import models.Risorsa;
import models.Tariffa;
import models.TipoRapportoLavoro;
import models.TipoStatoRisorsa;
import models.Utente;
import play.data.validation.Min;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;
import utility.DomainWrapper;
import utility.MyUtility;

@With(SecureCOGE.class)
public class RisorseController extends Controller {

	public static void index() {
        render();
    }
	
    public static void list() {
    	ValuePaginator listaRisorse = new ValuePaginator(Risorsa.find("order by cognome, nome").fetch());
    	listaRisorse.setPageSize(10);
		render(listaRisorse); 
    }
    
    public static void listCoCoPro() {
    	ValuePaginator listaRisorse = new ValuePaginator(Risorsa.findCoCoPro("cognome","asc"));
    	listaRisorse.setPageSize(10);
    	String filtro = "CCP";
    	String lastParametro = "cognome";
    	String ordinamento = "desc";
		render("RisorseController/list.html",listaRisorse, filtro, lastParametro, ordinamento); 
    }
    
    public static void listDipendenti() {
    	ValuePaginator listaRisorse = new ValuePaginator(Risorsa.findDipendenti("cognome","asc"));
    	listaRisorse.setPageSize(10);
    	String filtro = "DIP";
    	String lastParametro = "cognome";
    	String ordinamento = "desc";
		render("RisorseController/list.html",listaRisorse, filtro, lastParametro, ordinamento); 
    }
    
    public static void order(String parametro, String ordinamento, String lastParametro, String filtro) {
		if(ordinamento == null || (lastParametro != null && !lastParametro.equals(parametro))){
			ordinamento = "asc";
		}
		ValuePaginator listaRisorse = null;
		if(filtro.equals("DIP")){
			listaRisorse = new ValuePaginator(Risorsa.findDipendenti(parametro,ordinamento));
		}else{
			listaRisorse = new ValuePaginator(Risorsa.findCoCoPro(parametro,ordinamento));
		}
    	if(ordinamento.equals("desc")){
    		ordinamento = "asc";
    	}else{
    		ordinamento = "desc";
    	}
    	lastParametro = parametro;
    	listaRisorse.setPageSize(10);
		render("RisorseController/list.html",listaRisorse,ordinamento,lastParametro,filtro); 
    }
    
    public static void search(Integer idRisorsa) {
    	if(idRisorsa == null || idRisorsa.equals("")){
    		list();
    	}
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<Risorsa> lista = new ArrayList<Risorsa>();
    	lista.add(risorsa);
		ValuePaginator listaRisorse = new ValuePaginator(lista);
		listaRisorse.setPageSize(10);
		render("RisorseController/list.html",listaRisorse);
    }
    
    public static void show(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<Tariffa> listaTariffe = Tariffa.find("byRisorsa", risorsa).fetch();
    	List<Costo> listaCosti = Costo.find("byRisorsa", risorsa).fetch();
        render(risorsa,listaTariffe,listaCosti);
    }
    
    public static void create() {
    	Risorsa risorsa = new Risorsa();
    	List<TipoRapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
    	List<TipoStatoRisorsa> listaTipoStatoRisorsa = TipoStatoRisorsa.find("byCodiceNotEqual", "CHIUSO").fetch();
    	Integer idCoCoPro = ((TipoRapportoLavoro)TipoRapportoLavoro.find("byCodice", "CCP").first()).idTipoRapportoLavoro;
    	List<Gruppo> listaGruppi = Gruppo.findAll();
        render(risorsa, listaTipoRapportoLavoro, listaTipoStatoRisorsa, idCoCoPro, listaGruppi);
    }
    
    public static void save(@Valid Risorsa risorsa, Integer idTipoRapportoLavoro, @Min(0) int giorniAssenzeRetribuite) {
    	validation.min(idTipoRapportoLavoro, 1).message("selezionare un tipo rapporto lavoro");
    	if(validation.hasErrors()) {
        	List<RapportoLavoro> listaTipoRapportoLavoro = TipoRapportoLavoro.find("order by descrizione").fetch();
        	List<TipoStatoRisorsa> listaTipoStatoRisorsa = TipoStatoRisorsa.find("byCodiceNotEqual", "CHIUSO").fetch();
        	Integer idCoCoPro = ((TipoRapportoLavoro)TipoRapportoLavoro.find("byCodice", "CCP").first()).idTipoRapportoLavoro;
            renderTemplate("RisorseController/create.html", risorsa, idTipoRapportoLavoro, listaTipoRapportoLavoro, listaTipoStatoRisorsa, idCoCoPro, giorniAssenzeRetribuite);
        }
	 	//crea e popola il primo rapporto lavoro con data inizio uguale alla data in della risorsa
	 	//aggiunge il primo rapporto lavoro alla lista rapportiLavoro della risorsa e salva il tutto
        RapportoLavoro primoRapportoLavoro = new RapportoLavoro(risorsa.dataIn, (TipoRapportoLavoro) TipoRapportoLavoro.findById(idTipoRapportoLavoro), risorsa);
        primoRapportoLavoro.giorniAssenzeRetribuite = giorniAssenzeRetribuite;
        risorsa.addRapportoLavoro(primoRapportoLavoro);
        risorsa.save();
		flash.success("risorsa inserita con successo");
		list();
	}
    
    public static void edit(Integer idRisorsa) {
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	List<TipoStatoRisorsa> listaTipoStatoRisorsa = TipoStatoRisorsa.findAll();
    	List<Gruppo> listaGruppi = Gruppo.findAll();
    	render(risorsa, listaTipoStatoRisorsa, listaGruppi);
    }
    
    public static void update(@Valid Risorsa risorsa) {
    	if(validation.hasErrors()) {
    		List<TipoStatoRisorsa> listaTipoStatoRisorsa = TipoStatoRisorsa.findAll();
    		List<Gruppo> listaGruppi = Gruppo.findAll();
        	renderTemplate("RisorseController/edit.html", risorsa, listaTipoStatoRisorsa, listaGruppi);
        }
    	//nel caso in cui viene settata data out della risorsa
    	//procedo alla disabilitazione della risorsa e delle info relative
        if(risorsa.dataOut != null) {
        	disabilitaRisorsa(risorsa, risorsa.dataOut);
        }
    	//procede alla modifica
        risorsa.save();
		flash.success("risorsa modificata con successo");
		list();
	}
    
    public static void delete(Integer idRisorsa) {
    	Date dataChiusura = new Date();
    	Risorsa risorsa = Risorsa.findById(idRisorsa);
    	disabilitaRisorsa(risorsa, dataChiusura);
    	
    	//procede alla cancellazione logica
        if(risorsa.save() != null) {
			 flash.success("risorsa disabilitata con successo");
		 } else {
			flash.error("si sono verificati dei problemi nel disabilitare la risorsa");
		}		 
		list();
    }
    
    private static void disabilitaRisorsa(Risorsa risorsa, Date dataChiusura) {
    	//in questo metodo setto le dateFinali di tutte le info associate alla risorsa:
    	//- se sono null le setto a dataChiusura
    	//- se sono < di dataChiusura non le modifico
    	//- se sono > di dataChiusura le setto a dataInizio
    	
    	//rapporto di lavoro
    	RapportoLavoro ultimoRapportoLavoro = risorsa.rapportiLavoro.get(risorsa.rapportiLavoro.size() - 1);
    	if (ultimoRapportoLavoro.dataFine == null) {
			ultimoRapportoLavoro.dataFine = ultimoRapportoLavoro.dataInizio.before(dataChiusura) ? dataChiusura : ultimoRapportoLavoro.dataInizio;
		} else if (ultimoRapportoLavoro.dataFine.after(dataChiusura)) {
			ultimoRapportoLavoro.dataFine = ultimoRapportoLavoro.dataInizio.before(dataChiusura) ? dataChiusura : ultimoRapportoLavoro.dataInizio;
		}
    	
    	//costo
    	if (risorsa.listaCosti != null && risorsa.listaCosti.size() > 0) {
    		Comparator<Costo> orderByDataInizioDesc = new Comparator<Costo>() {	
    			@Override
    			public int compare(Costo o1, Costo o2) {
    				return o1.dataInizio.before(o2.dataInizio) ? 1 : o1.dataInizio.equals(o2.dataInizio) ? 0 : -1;
    			}
    		};
    		Collections.sort(risorsa.listaCosti, orderByDataInizioDesc);
    		Costo ultimoCosto = risorsa.listaCosti.get(0);
    		if (ultimoCosto.dataFine == null) {
    			ultimoCosto.dataFine = ultimoCosto.dataInizio.before(dataChiusura) ? dataChiusura : ultimoCosto.dataInizio;
    		} else if (ultimoCosto.dataFine.after(dataChiusura)) {
    			ultimoCosto.dataFine = ultimoCosto.dataInizio.before(dataChiusura) ? dataChiusura : ultimoCosto.dataInizio;
    		}	
		}
    	
    	//tariffe
    	if (risorsa.listaTariffe != null) {
			for (Tariffa tariffa : risorsa.listaTariffe) {
				if (tariffa.dataFine == null) {
					tariffa.dataFine = tariffa.dataInizio.before(dataChiusura) ? dataChiusura : tariffa.dataInizio;
	    		} else if (tariffa.dataFine.after(dataChiusura)) {
	    			tariffa.dataFine = tariffa.dataInizio.before(dataChiusura) ? dataChiusura : tariffa.dataInizio;
	    		}
			}
		}
    	
    	//utente
    	Utente utente = Utente.find("byRisorsa", risorsa).first();
    	if(utente != null) {
    		utente.abilitato = false;
    		utente.save();
    	}
    	
    	//risorsa
    	if (risorsa.dataOut == null) {
    		risorsa.dataOut = risorsa.dataIn.before(dataChiusura) ? dataChiusura : risorsa.dataIn;
		} else if (risorsa.dataOut.after(dataChiusura)) {
			risorsa.dataOut = risorsa.dataIn.before(dataChiusura) ? dataChiusura : risorsa.dataIn;
		}
    	risorsa.tipoStatoRisorsa = TipoStatoRisorsa.find("byCodice", "CHIUSO").first();
    }
    
    public static void export() {
    	String dateStr =  new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		response.setHeader("Content-disposition", "attachment;filename=ListaRisorse_"+dateStr+".xls");
        response.setContentTypeIfNotSet("application/msexcel");
        OutputStream out;
		try {
			out = response.out;
			//Creo un Workbook e poi un foglio di lavoro
			HSSFWorkbook wb  = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("Risorse");
			HSSFCellStyle style = wb.createCellStyle();
			style.setTopBorderColor(IndexedColors.BLACK.getIndex());
			style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			style.setRightBorderColor(IndexedColors.BLACK.getIndex());
			style.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
			style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			HSSFRow row = sheet.createRow((short)0);
	        Cell c2 = row.createCell(0);
	        c2.setCellStyle(style);
	        c2.setCellValue("CODICE");
	        Cell c3 = row.createCell(1);
	        c3.setCellStyle(style);
	        c3.setCellValue("NOME");
	        Cell c4 = row.createCell(2);
	        c4.setCellStyle(style);
	        c4.setCellValue("COGNOME");
	        Cell c5 = row.createCell(3);
	        c5.setCellStyle(style);
	        c5.setCellValue("DATA IN");
	        Cell c6 = row.createCell(4);
	        c6.setCellStyle(style);
	        c6.setCellValue("DATA OUT");
	        Cell c7 = row.createCell(5);
	        c7.setCellStyle(style);
	        c7.setCellValue("STATO");
	        short i = 1;
	        List<Risorsa> listaRisorse = Risorsa.find("order by cognome").fetch();
	        for(Risorsa risorsa : listaRisorse){
		        row = sheet.createRow(i);
		        row.createCell(0).setCellValue(risorsa.codice);
		        row.createCell(1).setCellValue(risorsa.nome);
		        row.createCell(2).setCellValue(risorsa.cognome);
		        row.createCell(3).setCellValue(MyUtility.dateToString(risorsa.dataIn));
		        row.createCell(4).setCellValue(risorsa.dataOut == null ? null : MyUtility.dateToString(risorsa.dataOut));
		        row.createCell(5).setCellValue(risorsa.tipoStatoRisorsa.descrizione);
		        i++;
		    }
	        sheet.autoSizeColumn(0);
	        sheet.autoSizeColumn(1);
	        sheet.autoSizeColumn(2);
	        sheet.autoSizeColumn(3);
	        sheet.autoSizeColumn(4);
	        sheet.autoSizeColumn(5);
	        out.flush();
	        wb.write(out);  
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
 // Auotocomplete dei risorsa
	public static void autocompleteRisorsa(String term) {
		List<Risorsa> listaRisorse = Risorsa.find("codice like ? or cognome like ?","%"+term+"%","%"+term+"%").fetch();
		List<DomainWrapper> listaResult = new ArrayList<DomainWrapper>();
		for(Risorsa ris:listaRisorse){
			listaResult.add(new DomainWrapper(ris.idRisorsa, ris.codice + " - " + ris.cognome));
		}
		renderJSON(listaResult);
    }
    
}
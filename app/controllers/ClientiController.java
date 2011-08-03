package controllers;

import play.*;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;
import secure.SecureCOGE;
import utility.MyUtility;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import models.*;

@With(SecureCOGE.class)
public class ClientiController extends Controller {
	
    public static void index() {
        list();
    }
    
    public static void list() {
    	List<Cliente> listaClienti = Cliente.find("order by codice asc").fetch();
		ValuePaginator paginator = new ValuePaginator(listaClienti);
		paginator.setPageSize(5);
		render(paginator);
    }
    
    public static void create() {
    	Cliente cliente = new Cliente();
        render(cliente);
    }
    
    public static void save(@Valid Cliente cliente) {
    	if(validation.hasErrors()) {
	        render("ClientiController/create.html", cliente);
	    }
    	cliente.codice = cliente.codice.toUpperCase();
    	cliente.attivo = true;
    	cliente.save();
    	flash.success("%s aggiunto con successo", cliente.nominativo);
    	list();
    }
    
    public static void edit(Integer id) {
    	Cliente cliente = Cliente.findById(id);
        render(cliente);
    }
    
    public static void update(@Valid Cliente cliente) {
    	if(validation.hasErrors()) {
	        render("ClientiController/edit.html", cliente);
	    }
    	if(cliente.attivo == false){
    		Commessa.chiudiCommesseByCliente(cliente);
    	}
    	cliente.codice = cliente.codice.toUpperCase();
    	cliente.save();
    	flash.success("%s modificato con successo", cliente.nominativo);
        list();
    }
    
    public static void show(Integer id) {
    	Cliente cliente = Cliente.findById(id);
        render(cliente);
    }
    
    public static void delete(Integer id) {
    	Cliente cliente = Cliente.findById(id);
    	cliente.attivo = false;
    	Commessa.chiudiCommesseByCliente(cliente);
    	cliente.save();
    	flash.success("%s cancellato con successo", cliente.nominativo);
    	list();
    }
    
}
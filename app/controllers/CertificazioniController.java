package controllers;

import java.util.List;

import javax.persistence.PersistenceException;

import models.Certificazione;
import play.data.validation.Valid;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import secure.SecureCOGE;

@With(SecureCOGE.class)
public class CertificazioniController extends Controller {

    public static void list() {
    	List<Certificazione> lista = Certificazione.findAll();
        render(lista);
    }
    
    public static void show(Integer idCertificazione) {
    	Certificazione certificazione = Certificazione.findById(idCertificazione);
        render(certificazione);
    }
    
    public static void create() {
    	Certificazione certificazione = new Certificazione();
        render(certificazione);
    }
    
    public static void save(@Valid Certificazione certificazione) {
    	if(validation.hasErrors()) {
	        render("CertificazioniController/create.html", certificazione);
	    }
    	certificazione.codice = certificazione.codice.toUpperCase();
    	certificazione.save();
    	flash.success("Certificazione %s aggiunta con successo", certificazione.descrizione);
    	list();
    }
    
    public static void edit(Integer idCertificazione) {
    	Certificazione certificazione = Certificazione.findById(idCertificazione);
        render(certificazione);
    }
    
    public static void update(@Valid Certificazione certificazione) {
    	if(validation.hasErrors()) {
	        render("CertificazioniController/edit.html", certificazione);
	    }
    	certificazione.codice = certificazione.codice.toUpperCase();
    	certificazione.save();
    	flash.success("Certificazione %s modificata con successo", certificazione.descrizione);
    	list();
    }
    
    public static void delete(Integer idCertificazione) {
    	Certificazione certificazione = Certificazione.findById(idCertificazione);
    	try {
    		certificazione.delete();
    		flash.success("Certificazione %s eliminata con successo", certificazione.descrizione);
		} catch (PersistenceException e) {
			flash.error("Impossibile eliminare la Certificazione %s ha delle Risorse associate", certificazione.descrizione);
		} finally {
			list();
		}
    }

}

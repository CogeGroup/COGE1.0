package controllers;

import models.Utente;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Mail;
import play.mvc.Controller;

public class Application extends Controller {

    public static void index() {
        render();
    }
    
    public static void recuperaPasswordAccount() {
		  render();
	  }
	  
	public static void confirmRecuperaPasswordAccount(@Required String username, @Required @Email String email) {
		if (validation.hasErrors()) {
			renderTemplate("Application/recuperaPasswordAccount.html", username, email);
		}
		Utente utente = Utente.find("byUsernameAndEmail", username, email).first();
		if(utente == null) {
			flash.error("nessun utente trovato con i dati inseriti");
			renderTemplate("Application/recuperaPasswordAccount.html", username, email);
		}
		SimpleEmail simpleEmail = new SimpleEmail();
		try {
			simpleEmail.setFrom("salvo.tosto.21@gmail.com");
			simpleEmail.addTo(email);
			simpleEmail.setSubject("Recupero Password");
			simpleEmail.setMsg(utente.password);
			Mail.send(simpleEmail);
		} catch (EmailException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		renderTemplate("Application/confirmRecuperaPasswordAccount.html", username, email);
	}

}
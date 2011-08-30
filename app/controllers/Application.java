package controllers;

import play.*;
import play.data.validation.Email;
import play.data.validation.Error;
import play.data.validation.Required;
import play.libs.Mail;
import play.mvc.*;

import java.util.*;
import java.util.concurrent.Future;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import models.*;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		renderTemplate("Application/confirmRecuperaPasswordAccount.html", username, email);
	}

}
package controllers;

import models.Utente;
import play.db.jpa.GenericModel.JPAQuery;
import play.mvc.*;

public class CogeMobile extends Controller {

    public static void index() {
    	render();
    }
    
    public static void auth(String username, String psw) {
    	System.out.println("username: " + username);
    	System.out.println("psw: " + psw);
    	String risposta = "si";
    	render(risposta);
    }

}

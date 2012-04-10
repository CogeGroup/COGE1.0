package secure;


import utility.EncryptionUtility;
import models.Utente;
import controllers.Application;
import controllers.Secure;
 
public class Security extends Secure.Security {
    
    static boolean authenticate(String username, String password) {
        Utente user = Utente.find("byUsername", username).first();
        return user != null && user.password.equals(EncryptionUtility.encrypt(password));
    }    
    
    
    static boolean check(String profile) {
    	
    	//System.out.println("metodo check nostro..."  );  	
      	String requestPath = Application.request.controller + "/" + Application.request.actionMethod;
      	//System.out.println(requestPath);
      	Utente user = Utente.find("byUsername", session.get("username")).first();
      
      	return user.isAuthorized(requestPath);
        
    }  
    
    
    static void onCheckFailed(String profile) {
        //renderText("ahooooo...");
    	
    	renderTemplate("errors/forbidden.html");
    }
}
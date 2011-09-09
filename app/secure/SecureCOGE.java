package secure;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import controllers.Application;
import controllers.Secure;

import models.Utente;
import play.Play;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import play.utils.Java;

public class SecureCOGE extends Secure {

    @Before(unless={"login", "authenticate", "logout"})
    static void checkAccess() throws Throwable {
    	System.out.println("SECURE COGE");
    	
    	//System.out.println("-------DENTRO SecureCOGE " + Application.request.controller + "/" + Application.request.actionMethod );
        // Authent
        if(!session.contains("username")) {
            flash.put("url", "GET".equals(request.method) ? request.url : "/"); // seems a good default
            System.out.println(flash.get("url"));
            login();
        }
        else{
        	
        	boolean hasProfile = (Boolean)secure.Security.check("");
            if (!hasProfile){
            	secure.Security.onCheckFailed("");
            }
        	
        }
      
    }

  
}

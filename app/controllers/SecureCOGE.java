package controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import models.Utente;
import play.Play;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import play.utils.Java;

public class SecureCOGE extends Secure {

    @Before(unless={"login", "authenticate", "logout"})
    static void checkAccess() throws Throwable {
    	
    	System.out.println("-------DENTRO SecureCOGE " + Application.request.controller + "/" + Application.request.actionMethod );
        // Authent
        if(!session.contains("username")) {
            flash.put("url", "GET".equals(request.method) ? request.url : "/"); // seems a good default
            login();
        }
        else{
        	
        	boolean hasProfile = (Boolean)controllers.Security.check("");
            if (!hasProfile){
            	controllers.Security.onCheckFailed("");
            }
        	
        }
      
    }

  
}

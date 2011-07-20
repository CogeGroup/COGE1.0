package controllers;

import play.*;
import play.modules.paginate.ValuePaginator;
import play.mvc.*;

import java.util.*;

import models.*;
public class AccountController extends Controller {

    public static void home() {
        render();
    }
    
    
	  public static void listUtenti() {
		  //lista degli utenti in render
		  List<Utente> listaUtenti = Utente.all().fetch();
		   ValuePaginator<Utente> paginator = new ValuePaginator(listaUtenti);
		   render(paginator);
	    }

}

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import models.Cliente;
import models.Commessa;
import models.Risorsa;
import models.Tariffa;
import play.test.Fixtures;
import play.test.UnitTest;


public class RapportoAttivitaTest  extends UnitTest {
	private Risorsa r1;
	private Risorsa r2;
	private Commessa cm1;
	private Commessa cm2;
	private Cliente cl1;
	private Date dataIniz;

	@Test
	public void ricercaRisorsaPerCommesse() throws ParseException{
		
		List<Commessa> listaCommesse = Commessa.trovaCommessePerRisorsa(7,2011,r1);
		 assertFalse(listaCommesse.isEmpty());
		assertEquals(cm1, listaCommesse.get(0));
		assertEquals(cm2, listaCommesse.get(1));
	}
	
	@Test
	public void ricercaRisorsaconDueCommesse() throws ParseException{
	    
//		List<Tariffa> listaTariffe = Tariffa.findAll();
//		for(Tariffa t:listaTariffe){
//		System.out.println(t.risorsa);
//		}
		List<Commessa> listaCommesse = Commessa.trovaCommessePerRisorsa(7,2011,r1);
		 assertFalse(listaCommesse.isEmpty());
		assertEquals(cm1, listaCommesse.get(0));
		assertEquals(cm2, listaCommesse.get(1));
		assertEquals(2,listaCommesse.size());
		
	}
	@Test
	public void ricercaRisorsaConUnaCommesse() throws ParseException{
	    

		List<Commessa> listaCommesse = Commessa.trovaCommessePerRisorsa(7,2011,r2);
		 assertFalse(listaCommesse.isEmpty());
		assertEquals(cm2, listaCommesse.get(0));
		assertEquals(1,listaCommesse.size());
		
	}
	
 @Before
	public void setup() throws ParseException {
		Fixtures.deleteAll();
		
		r1 = new Risorsa("122","a","pippo", "pippo",new Date()).save();
		 r2 = new Risorsa("123","b","risorsa2", "risorsa2",new Date()).save();
		  cl1 = new Cliente("cod","nome");
	      cl1.save();
	       cm1 = new Commessa("desc","cod",true);
	       cm2 = new Commessa("desc","cod",true);
	       cm1.cliente=cl1;
	       cm2.cliente=cl1;
	       cm1.save();
	       cm2.save();
	       
		Date dataIniz = new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011");
		new Tariffa(dataIniz,8000.0f,r1,cm1).save();
		new Tariffa(dataIniz,8000.0f,r1,cm2).save();
		new Tariffa(dataIniz,8000.0f,r2,cm2).save();

	}
	
	 
}

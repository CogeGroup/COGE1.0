import org.junit.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import play.test.*;
import models.*;

public class BasicTest extends UnitTest {
	
	 Commessa cm1 = null;
     Commessa cm2 = null;
     Cliente cl1 = null;

    
 @Test
 public void testCalcoloRicavoPerRisorsa() throws ParseException{
    	
    	Risorsa r1 = Risorsa.find("byMatricola", "a").first();
    	
    	 new Tariffa(new Date(),8000.0f,r1,cm1).save();
         new Tariffa(new Date(),8000.0f,r1,cm2).save();
         DettaglioRapportoAttivita dt1 = new DettaglioRapportoAttivita(10,1,cm1);
         //DettaglioRapportoAttivita dt2 = new DettaglioRapportoAttivita(2,1,cm2);
         DettaglioRapportoAttivita dt3 = new DettaglioRapportoAttivita(10,1,cm2);
         RapportoAttivita ra1 = new RapportoAttivita("07","2011",true,r1);
         ra1.addDettRappAtt(dt1);
         ra1.addDettRappAtt(dt3);
         ra1.save();
    	assertEquals(20000f,r1.calcolaRicavo(7, 2011),0.01f);
    	
    }
 
	 @Test
	 public void testCalcoloRicavoPerCommessa() throws ParseException{
		 
		 Risorsa r1 = Risorsa.find("byMatricola", "a").first();
		
		 new Tariffa( new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),8000.0f,r1,cm1).save();
		 new Tariffa( new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),8000.0f,r1,cm2).save();
		 
		 DettaglioRapportoAttivita dt1 = new DettaglioRapportoAttivita(20,1,cm1);
		 DettaglioRapportoAttivita dt2 = new DettaglioRapportoAttivita(20,1,cm2);
		 RapportoAttivita ra1 = new RapportoAttivita("07","2011",true,r1);
	         ra1.addDettRappAtt(dt1);
	         ra1.addDettRappAtt(dt2);
	         ra1.save();
		 
		 
		 assertEquals( 20000f,cm1.calcolaRicavo(7,2011),0.01f);
		 
	 }
	 @Test
	 public void testCalcoloRicavoPerCliente() throws ParseException{
		 
		 Risorsa r1 = Risorsa.find("byMatricola", "a").first();
		
		 new Tariffa( new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),8000.0f,r1,cm1).save();
		 new Tariffa( new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),8000.0f,r1,cm2).save();
		 
		 DettaglioRapportoAttivita dt1 = new DettaglioRapportoAttivita(10,1,cm1);
		 DettaglioRapportoAttivita dt2 = new DettaglioRapportoAttivita(10,1,cm2);
		 RapportoAttivita ra1 = new RapportoAttivita("07","2011",true,r1);
	         ra1.addDettRappAtt(dt1);
	         ra1.addDettRappAtt(dt2);
	         ra1.save();
		 
		 assertEquals( 20000f,cl1.calcolaRicavo(7,2011),0.01f);
		 
	 }
	 
	 
	 
 
 @Before
 public void setup() throws ParseException{
	  
     Risorsa r1 = new Risorsa("a","a","pippo", "pippo",new Date());
     Risorsa r2 = new Risorsa("b","b","risorsa2", "risorsa2",new Date());
     
     TipoRapportoLavoro tipoRappLav1 = new TipoRapportoLavoro("tipoRap1","cod");
     TipoRapportoLavoro tipoRappLav2 = new TipoRapportoLavoro("tipoRap2","cod2");
     tipoRappLav1.save();
     tipoRappLav2.save();
     
     RapportoLavoro rl1 = new RapportoLavoro(new SimpleDateFormat("dd/MM/yyyy").parse("01/06/2011"),tipoRappLav1,r1);
     rl1.dataFine = new SimpleDateFormat("dd/MM/yyyy").parse("30/06/2011");
     RapportoLavoro rl2 = new RapportoLavoro(new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),tipoRappLav2,r1);
     r1.addRapportoLavoro(rl1);
     r1.addRapportoLavoro(rl2);
     r1.save();
     
     
     
     cl1 = new Cliente("cod","nome");
     cl1.save();
     
      cm1 = new Commessa("desc","cod",true);
      try {
		cm1.dataInizioCommessa = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2011");
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      cm2 = new Commessa("desc","cod",true);
      cm1.cliente=cl1;
      cm2.cliente=cl1;
     cm1.save();
     cm2.save();
     CommessaACorpo cAc1 = new CommessaACorpo("desccorpo","codCorpo",true,200.00f);
     cAc1.cliente=cl1;
     cAc1.save();
     float importo = 100;
     Costo cs1 = new Costo(importo,new Date(),r1);
     cs1.save();
     Ruolo ruolo1 = new Ruolo("admin");
     Utente u1 = new Utente("a","a",true);
     u1.addRuolo(ruolo1);
     u1.risorsa=r1;
     u1.save();
     }   
     

}

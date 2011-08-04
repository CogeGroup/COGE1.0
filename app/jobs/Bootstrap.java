package jobs;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

import models.*;

import play.jobs.*;
import models.Job;
import play.test.Fixtures;
	 
@OnApplicationStart
public class Bootstrap extends play.jobs.Job {
	
	 Commessa cm1 = null;
     Commessa cm2 = null;
     Cliente cl1 = null;
	    
    public void doJob() {
    	
    	Fixtures.deleteDatabase();
    	
    	//leggo file YAML
    	if(Risorsa.count()==0){
    		Fixtures.loadModels("initial-data.yml");
    	}
    	
    	
//      System.out.println(".....init"); 
//      try {
//	   setup();
//	} catch (ParseException e1) {
//		// TODO Auto-generated catch block
//		e1.printStackTrace();
//	}
//      
//      try {
//    	  testCalcoloRicavoPerRapportoLavoro();
//	} catch (ParseException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
      
      
    }
    
    public void testCalcoloTariffePerCommessa() throws ParseException{
    	
    	Risorsa r1 = Risorsa.find("byMatricola", "a").first();
    	
    	 new Tariffa(new Date(),8000.0f,r1,cm1).save();
         new Tariffa(new Date(),8000.0f,r1,cm2).save();
         
         
         
         DettaglioRapportoAttivita dt1 = new DettaglioRapportoAttivita(10,1,cm1);
         //DettaglioRapportoAttivita dt2 = new DettaglioRapportoAttivita(2,1,cm2);
         DettaglioRapportoAttivita dt3 = new DettaglioRapportoAttivita(10,1,cm2);
         
         
         RapportoAttivita ra1 = new RapportoAttivita("01","2011",true,r1);
         ra1.addDettRappAtt(dt1);
         //ra1.addDettRappAtt(dt2);
         ra1.addDettRappAtt(dt3);
         ra1.save();
    	
    	
    	//System.out.println("calcolo:" + r1.calcolaRicavo(1, 2011));
    	
    	
    	
    }
    
    
 public void testCalcoloTariffePerCommessaEData() throws ParseException{
	 
	 
    	
    	Risorsa r1 = Risorsa.find("byMatricola", "a").first();
    	
    	 Tariffa trf1 = new Tariffa(new SimpleDateFormat("dd/MM/yyyy").parse("01/06/2011"),3000.0f,r1,cm1);
    	 trf1.dataFine = new SimpleDateFormat("dd/MM/yyyy").parse("30/06/2011");
    	 trf1.save();
    			 
    			 
    	 
    	 Tariffa trf2 = new Tariffa(new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),8000.0f,r1,cm1).save();
    	 trf2.save();
         
         
         //int oreLavorate, int giorno,Commessa commessa
         DettaglioRapportoAttivita dt1 = new DettaglioRapportoAttivita(10,1,cm1);
         //DettaglioRapportoAttivita dt2 = new DettaglioRapportoAttivita(2,1,cm2);
         DettaglioRapportoAttivita dt3 = new DettaglioRapportoAttivita(10,2,cm1);
         
         
         RapportoAttivita ra1 = new RapportoAttivita("07","2011",true,r1);
         ra1.addDettRappAtt(dt1);
         //ra1.addDettRappAtt(dt2);
         ra1.addDettRappAtt(dt3);
         ra1.save();
    	
    	
    	System.out.println("calcolo:" + r1.calcolaRicavo(7, 2011));
    	
    	
    	
    }
      
 public void testCalcoloRicavoPerRapportoLavoro() throws ParseException{
	 
	 
 	
 	Risorsa r1 = Risorsa.find("byMatricola", "a").first();
 	Risorsa r2 = Risorsa.find("byMatricola", "b").first();
 	TipoRapportoLavoro tp1 = TipoRapportoLavoro.find("byDescrizione","tipoRap1").first();
 	TipoRapportoLavoro tp2 = TipoRapportoLavoro.find("byDescrizione","tipoRap2").first();
 	tp2.save();
 	
 	 Tariffa trf1 = new Tariffa(new SimpleDateFormat("dd/MM/yyyy").parse("01/06/2011"),3000.0f,r1,cm1);
 	 trf1.dataFine = new SimpleDateFormat("dd/MM/yyyy").parse("30/06/2011");
 	 trf1.save();		 
 	 Tariffa trf2 = new Tariffa(new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),8000.0f,r1,cm1).save();
 	 trf2.save();
 	 
 	 Tariffa trf3 = new Tariffa(new SimpleDateFormat("dd/MM/yyyy").parse("01/06/2011"),1000.0f,r2,cm1);
 	 trf3.dataFine = new SimpleDateFormat("dd/MM/yyyy").parse("30/06/2011");
 	 trf3.save();
 			 
 			 
 	 
 	 Tariffa trf4 = new Tariffa(new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),8000.0f,r2,cm1).save();
 	 trf4.save();
      
      //int oreLavorate, int giorno,Commessa commessa
      DettaglioRapportoAttivita dt1 = new DettaglioRapportoAttivita(10,1,cm1);
      DettaglioRapportoAttivita dt3 = new DettaglioRapportoAttivita(10,2,cm1);
      
      DettaglioRapportoAttivita dt4 = new DettaglioRapportoAttivita(10,2,cm1);
      
      
      RapportoAttivita ra1 = new RapportoAttivita("07","2011",true,r1);
      ra1.addDettRappAtt(dt1);
      ra1.addDettRappAtt(dt3);
      ra1.save();
      
      //seconda Risorsa
      RapportoAttivita ra2 = new RapportoAttivita("06","2011",true,r2);
      ra2.addDettRappAtt(dt4);
      ra2.save();
 	
 	
 	System.out.println("calcolo:" + r1.calcolaRicavoPerTipoRapportoLavoro(7, 2011,tp1));
 	
 	
 	
 }
 
 
 
      public void setup() throws ParseException{
    	  
      Risorsa r1 = new Risorsa("122","a","pippo", "pippo", new SimpleDateFormat("dd/MM/yyyy").parse("01/06/2011"));
      Risorsa r2 = new Risorsa("123","b","risorsa2", "risorsa2",new SimpleDateFormat("dd/MM/yyyy").parse("01/06/2011"));
      
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
      RapportoLavoro rl3 = new RapportoLavoro(new SimpleDateFormat("dd/MM/yyyy").parse("01/06/2011"),tipoRappLav1,r2);
      r2.addRapportoLavoro(rl3);
      r2.save();
      
      cl1 = new Cliente("COD","nome",true);
      cl1.save();
      
       cm1 = new Commessa("desc","COMMESSA1",true);
       try {
		cm1.dataInizioCommessa = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2011");
	} catch (ParseException e) {
		e.printStackTrace();
	}

      cm2 = new Commessa("desc","COMMESSA2",true);
      
      try {
  		cm2.dataInizioCommessa = new SimpleDateFormat("dd/MM/yyyy").parse("07/07/2011");
  	} catch (ParseException e) {
  		e.printStackTrace();
  	}

      cm1.cliente=cl1;
      cm2.cliente=cl1;
      cm1.save();
      cm2.save();
      
      
      CommessaACorpo cAc1 = new CommessaACorpo("desccorpo","ACORPO",true,200.00f);
      try {
    	  cAc1.dataInizioCommessa = new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011");
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
      cAc1.cliente=cl1;
      cAc1.save();
      
  
     
      
      float importo = 100;
      Costo cs1 = new Costo(importo,new Date(),r1);
      cs1.save();
      
      Ruolo ruolo1 = new Ruolo("admin").save();
      Ruolo ruolo2 = new Ruolo("segreteria").save();
      
      
      //job da assegnare a tutti gli utenti
      Job j0 = new Job("LogInController/*").save();
      //jobs del ruolo admin
      Job j1 = new Job("AccountController/*");
      Job j2 = new Job("ClientiController/*");
      Job j3 = new Job("CommesseController/*");
      Job j4 = new Job("CostiController/*").save();
      Job j5 = new Job("RendicontoAttivitaController/*");
      Job j6 = new Job("RisorseController/*");
      Job j7 = new Job("TariffeController/*");
      Job j8 = new Job("RapportoLavoroController/*");
      Job j9 = new Job("TipoRapportoLavoroController/*");
      
      j0.addRuolo(ruolo1);
      j0.addRuolo(ruolo2);
      j0.save();
      j1.addRuolo(ruolo1);
      j1.save();
      j2.addRuolo(ruolo1);
      j2.save();
      j3.addRuolo(ruolo1);
      j3.save();
      j4.addRuolo(ruolo1);
      j4.save();
      j5.addRuolo(ruolo1);
      j5.save();
      j6.addRuolo(ruolo1);
      j6.save();
      j7.addRuolo(ruolo1);
      j7.save();
      j8.addRuolo(ruolo1);
      j8.save();
      j9.addRuolo(ruolo1);
      j9.save();
      
      Utente u1 = new Utente("a","a",true);
      
      u1.addRuolo(ruolo1);
      
      u1.risorsa=r1;
      u1.save();
      
//      Ruolo r = Ruolo.findById(1);
//      r.remove();
//      r.save();
//      
      
      
      
    
      //jobs associati al ruolo segreteria
      Job j10 = new Job("AccountController/index");
      Job j11 = new Job("AccountController/listUtenti");
      Job j12 = new Job("ClientiController/list");
      Job j13 = new Job("ClientiController/show");
      Job j14 = new Job("CommesseController/list");
      Job j15 = new Job("CommesseController/show");
      Job j16 = new Job("RendicontoAttivitaController/*");
      Job j17 = new Job("RisorseController/list");
      Job j18 = new Job("RisorseController/index");
      Job j19 = new Job("RisorseController/show");
      Job j20 = new Job("RisorseController/listRapportoLavoro");
      Job j21 = new Job("TariffeController/list");
      Job j22 = new Job("RapportoLavoroController/list");
      Job j23 = new Job("RapportoLavoroController/show");
      Job j24 = new Job("CostiController/list");
      Job j25 = new Job("TipoRapportoLavoroController/index");
      
     
      j10.addRuolo(ruolo2);
      j10.save();
      j11.addRuolo(ruolo2);
      j11.save();
      j12.addRuolo(ruolo2);
      j12.save();
      j13.addRuolo(ruolo2);
      j13.save();
      j14.addRuolo(ruolo2);
      j14.save();
      j15.addRuolo(ruolo2);
      j15.save();
      j16.addRuolo(ruolo2);
      j16.save();
      j17.addRuolo(ruolo2);
      j17.save();
      j18.addRuolo(ruolo2);
      j18.save();
      j19.addRuolo(ruolo2);
      j19.save();
      j20.addRuolo(ruolo2);
      j20.save();
      j21.addRuolo(ruolo2);
      j21.save();
      j22.addRuolo(ruolo2);
      j22.save();
      j23.addRuolo(ruolo2);
      j23.save();
      j24.addRuolo(ruolo2);
      j24.save();
      j25.addRuolo(ruolo2);
      j25.save();
      
      
      
     
      
      Utente u2 = new Utente("s","s",true);
      u2.addRuolo(ruolo2);
      
      u2.risorsa=r2;
      u2.save();
      
      Tariffa t1 = new Tariffa(new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2011"),100,r2,cm1);
      t1.dataFine = new SimpleDateFormat("dd/MM/yyyy").parse("31/08/2011");
      Tariffa t2 = new Tariffa(new SimpleDateFormat("dd/MM/yyyy").parse("01/09/2011"),200,r2,cm1);
      t1.save();
      t2.save();
      
    
      }   
      
      
      
 
    
}


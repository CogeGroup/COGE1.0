import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import models.Cliente;
import models.Commessa;
import models.CommessaACorpo;
import models.Costo;
import models.DettaglioRapportoAttivita;
import models.RapportoAttivita;
import models.RapportoLavoro;
import models.Risorsa;
import models.Ruolo;
import models.Tariffa;
import models.TipoRapportoLavoro;
import models.Utente;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class CostoTest extends UnitTest {
	
	private Risorsa r1;
	private Risorsa r2;
	
	

	@Test
	 public void testDataFinePresente(){
		
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.DATE, 1);
		
		 Costo costo = new Costo(60.0f, now, r1);
		 costo.dataFine = cal.getTime();
		 costo.save();
		 Costo costo2 = new Costo(50.0f, now, r1);
		 assertFalse(costo2.validateAndSave());
		 //assertEquals(0, Costo.count()) ;
		 
	 }
	
	@Test
	 public void testDataFineNull(){
		
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.DATE, 1);
		
		 Costo costo = new Costo(60.0f, now, r1);
		 costo.save();
		 Costo costo2 = new Costo(50.0f, cal.getTime(), r1);
		 assertFalse(costo2.validateAndSave());
		 //assertEquals(0, Costo.count()) ;
		 
	 }
	
	@Test
	 public void testDataFineInPeriodoEsistente(){
		
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.DATE, 7);
		
		 Costo costoChiuso = new Costo(60.0f, now, r1);
		 costoChiuso.dataFine = cal.getTime();
		 costoChiuso.save();
		 Costo costoToInsert = new Costo(50.0f, now, r1);
		 cal.add(Calendar.DATE, -3);
		 costoToInsert.dataFine = cal.getTime();
		 assertFalse(costoToInsert.validateAndSave());
		 //assertEquals(0, Costo.count()) ;
		 
	 }
	
	@Test
	 public void testInserimentoPeriodoPrecedenteAPeriodoAperto(){
		
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		
		 Costo costoAperto = new Costo(60.0f, now, r1);
		 costoAperto.save();
		 cal.add(Calendar.DATE, -10);
		 Costo costoToInsert = new Costo(50.0f,cal.getTime() , r1);
		 cal.add(Calendar.DATE, 2);
		 costoToInsert.dataFine = cal.getTime();
		 assertTrue(costoToInsert.validateAndSave());
		 //assertEquals(0, Costo.count()) ;
		 
	 }
	
	@Test
	 public void testInserimentoPeriodoInternoAPeriodoAperto(){
		
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		
		 Costo costoAperto = new Costo(60.0f, now, r1);
		 costoAperto.save();
		 cal.add(Calendar.DATE, -10);
		 Costo costoToInsert = new Costo(50.0f,cal.getTime() , r1);
		 cal.add(Calendar.DATE, 12);
		 costoToInsert.dataFine = cal.getTime();
		 assertFalse(costoToInsert.validateAndSave());
		 //assertEquals(0, Costo.count()) ;
		 
	 }
	
	@Test
	 public void testInserimentoPeriodoPiuEsteso(){
		
		Calendar cal = Calendar.getInstance();
		Date now = new Date();
		cal.setTime(now);
		cal.add(Calendar.DATE, 1);
		Date nowPlus1 = cal.getTime();
		cal.add(Calendar.DATE, 6);
		Date nowPlus6 = cal.getTime();
		cal.add(Calendar.DATE, 8);
		Date nowPlus8 = cal.getTime();
		
		 Costo periodoMinore = new Costo(60.0f, nowPlus1, r1);
		 periodoMinore.dataFine = nowPlus6;
		 periodoMinore.save();
		 Costo costoToInsert = new Costo(50.0f,now, r1);
		 costoToInsert.dataFine = nowPlus8;
		 assertFalse(costoToInsert.validateAndSave());
		 //assertEquals(0, Costo.count()) ;
		 
	 }
	
	

	@Before
	public void setup() throws ParseException {
		Fixtures.deleteAll();

		r1 = new Risorsa("a", "a", "pippo", "pippo", new Date());
		r2 = new Risorsa("b", "b", "risorsa2", "risorsa2", new Date());

		r1.save();
		r2.save();
	}

}

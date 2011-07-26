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

import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class CostoTest extends UnitTest {
	
	private Risorsa r1;
	private Risorsa r2;

	@Test
	 public void testDataCostoDentroPeriodoChiuso(){
		 Risorsa bob = Risorsa.find("byMatricola", "123").first();
		 Costo costo = new Costo(50.0f, new DateMidnight(2011,10,30).toDate(), bob);
		 assertFalse(costo.validateAndSave());
	 }
	
	@Test
	 public void testDataFineNull(){
		Risorsa bonnie = Risorsa.find("byMatricola", "537").first();
		Costo costo = new Costo(50.0f, new DateMidnight(2011, 3, 2).toDate(),bonnie);
		assertFalse(costo.validateAndSave());
	 }
	
	@Test
	 public void testDataFineInPeriodoEsistente(){
		Risorsa bob = Risorsa.find("byMatricola", "123").first();
		Costo costo = new Costo(50.0f, new DateMidnight(2011, 4, 20).toDate(),bob);
		costo.dataFine = new DateMidnight(2011, 5, 5).toDate();
		assertFalse(costo.validateAndSave());
	 }
	
	@Test
	 public void testInserimentoPeriodoPrecedenteAPeriodoAperto(){
		Risorsa bob = Risorsa.find("byMatricola", "123").first();
		Costo costo = new Costo(50.0f, new DateMidnight(2011, 4, 20).toDate(),bob);
		costo.dataFine = new DateMidnight(2011, 4, 30).toDate();
		assertTrue(costo.validateAndSave());
	 }
	
	@Test
	 public void testInserimentoCostoDataFineInternaAPeriodoAperto(){
		
		Risorsa jack = Risorsa.find("byMatricola", "666").first();
		Costo costo = new Costo(50.0f, new DateMidnight(2011, 3, 2).toDate(),jack);
		costo.dataFine = new DateMidnight(2011, 9, 3).toDate();
		assertFalse(costo.validateAndSave());
	 }
	
	@Test
	public void modificaCostoAperto(){
		Risorsa jack = Risorsa.find("byMatricola", "666").first();
		jack.extractLastCosto().dataFine = new DateMidnight(2011, 10, 1).toDate();
		assertTrue(jack.extractLastCosto().validateAndSave());
	}
	
	@Test
	 public void testInserimentoPeriodoPiuEsteso(){
		
		Risorsa bob = Risorsa.find("byMatricola", "123").first();
		Costo costo = new Costo(50.0f, new DateMidnight(2011, 8, 29).toDate(),bob);
		costo.dataFine = new DateMidnight(2011, 11, 1).toDate();
		assertFalse(costo.validateAndSave());
	 }
	
	@Test
	public void inserisciCostoSenzaRisorsa() {
		Costo costoNew = new Costo(100f, new Date(), null);
		assertFalse(costoNew.validateAndSave());
	}
	
	@Test
	public void inserisciImportoNull() {
		Costo costoNew = new Costo(null, new Date(), r1);
		assertFalse(costoNew.validateAndSave());
	}
	
	@Test
	public void cancellaCosto(){
		long costiTotali = Costo.count();
		Risorsa risorsa = Risorsa.find("byMatricola", "123").first();
		Costo toDelete = risorsa.listaCosti.get(0);
		toDelete.delete();
		assertEquals(costiTotali-1, Costo.count());
	}
	
	@Test
	public void dataFineMaggioreDataInizio(){
		Risorsa bob = Risorsa.find("byMatricola", "123").first();
		Costo costo = new Costo(50.0f, new DateMidnight(2011, 11, 30).toDate(),bob);
		costo.dataFine = new DateMidnight(2011, 11, 01).toDate();
		assertFalse(costo.validateAndSave());
	}
	
	@After
	public void tearDown(){
		Fixtures.deleteAll();
	}
	
	@Before
	public void setup() throws ParseException {
		Fixtures.loadModels("data.yml");

		r1 = new Risorsa("a", "a", "pippo", "pippo", new Date());
		r2 = new Risorsa("b", "b", "risorsa2", "risorsa2", new Date());

		r1.save();
		r2.save();
	}

}

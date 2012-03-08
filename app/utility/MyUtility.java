package utility;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyUtility {

//------------------------ UTILITY RISORSA - RAPPORTO LAVORO
    
    public static List<Integer> createListaAnni() {
    	List<Integer> listaAnni = new ArrayList<Integer>();
    	Calendar calendar = Calendar.getInstance();
    	int annoCorrente = calendar.get(Calendar.YEAR);
    	for(int i = annoCorrente - 20; i <= annoCorrente + 20; i++) {
    		listaAnni.add(i);
    	}
    	return listaAnni;
    }
    
    public static Date getPrimoDelMese(Date data) {
    	return MeseEdAnnoToDataInizio(getMeseFromDate(data),getAnnoFromDate(data));
    }
    
    //crea una data con giorno 1 relativa a mese ed anno
    public static Date MeseEdAnnoToDataInizio(int mese, int anno) {
    	return new GregorianCalendar(anno, mese, 1).getTime();
    }
    
    //crea una data con l'ultimo giorno associato a mese ed anno
    public static Date MeseEdAnnoToDataFine(int mese, int anno) {
    	Calendar calendar = new GregorianCalendar(anno, mese, 1);
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    	return calendar.getTime();
    }
    
    //prendi il mese a partire da data
    public static int getMeseFromDate(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	return calendar.get(Calendar.MONTH);
    }
    //prendi l'anno a partire da data
    public static int getAnnoFromDate(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	return calendar.get(Calendar.YEAR);
    }
    
    public static Date subOneDay(Date original) {
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(original);
		 calendar.add(Calendar.DAY_OF_YEAR, -1);
		 return calendar.getTime();
	 }
    
    public static Date subOneMonth(Date original) {
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(original);
		 calendar.add(Calendar.MONTH, -1);
		 return calendar.getTime();
	 }
    
    public static Date addOneMonth(Date original) {
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(original);
		 calendar.add(Calendar.MONTH, +1);
		 return calendar.getTime();
	}
    
    public static String dateToString(Date date) {
    	return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }
    
    public static String dateToString(Date date, String formato) {
    	return new SimpleDateFormat(formato).format(date);
    }
    
    //calcola i giorni partendo dalle ore
    public static Double calcolaGiorni(Double ore){
    	Double totaleGiorni=0.0;
    	 NumberFormat fmt = NumberFormat.getInstance();
         fmt.setMaximumFractionDigits(2);
        
      if(ore!=0){
    		totaleGiorni=ore/8;
    	}
    	 Number number = null;
		try {
			number = fmt.parse(fmt.format(totaleGiorni));
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return number.doubleValue();
    }
    
    public static List calcolaMesi(Date dataInizio, Date dataFine) {
    	List lista = new ArrayList();
    	Map<String,Integer> mappa = new HashMap<String,Integer>();
    	while(dataInizio.before(dataFine)){
    		mappa = new HashMap<String,Integer>();
    		mappa.put("mese", getMeseFromDate(dataInizio) + 1);
	    	mappa.put("anno", getAnnoFromDate(dataInizio));
	    	lista.add(mappa);
    		dataInizio = addOneMonth(dataInizio);
    	}
    	return lista;
    }
    
    public static String getStringMese(int n) {
		switch (n) {
			case 1:
				return "gennaio";
			case 2:
				return "febbraio";
			case 3:
				return "marzo";
			case 4:
				return "aprile";
			case 5:
				return "maggio";
			case 6:
				return "giugno";
			case 7:
				return "luglio";
			case 8:
				return "agosto";
			case 9:
				return "settembre";
			case 10:
				return "ottobre";
			case 11:
				return "novembre";
			case 12:
				return "dicembre";
		}
		return "";
	} 


	public static String cleanStaff(String staff) {
		String[] staffArray = staff.split(",");
		String newStaff = "- ";
		List<String> list = Arrays.asList(staffArray);
		Set<String> set = new HashSet<String>(list);
		String[] result = new String[set.size()];
		set.toArray(result);
		for (String s : result) {
			newStaff += s + " - ";
		}
		return newStaff;
	}
	
	public static List<Map> order(List<Map> lista, String campo) {
		return lista;
	}
}

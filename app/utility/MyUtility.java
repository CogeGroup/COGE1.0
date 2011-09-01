package utility;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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

}

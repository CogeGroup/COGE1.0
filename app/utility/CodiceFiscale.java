package utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CodiceFiscale {
	
	private static Map<Character, Integer> dispariPerCaratteri = new HashMap<Character, Integer>();
	private static Map<Character, Integer> pariPerCaratteri = new HashMap<Character, Integer>();
	private static final char[] caratteriDiControllo = new char[] {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	// METODO DI VALIDAZIONE DEL CODICE FISCLALE
	public static boolean validazioneCodiceFiscale(String codiceFiscale){
		if(codiceFiscale == null || codiceFiscale.equals("")){
			return false;
		}
		if(codiceFiscale.length() < 16){
			return false;
		}
		codiceFiscale = codiceFiscale.toUpperCase();
		if(codiceFiscale.charAt(15) != codiceDiControllo(codiceFiscale)){
			return false;
		}
		
		return true;
	}
	
	private static char codiceDiControllo (String codiceFiscale){
		caricaMappaDispari();
		caricaMappaPari();
		int totale = 0;
		
		Set setCaratteriDispari = dispariPerCaratteri.entrySet();
		Set setCaratteriPari = pariPerCaratteri.entrySet();

		for (int i = 1; i < codiceFiscale.length(); i++) {
			char carattere = codiceFiscale.charAt(i-1);
			if((i % 2) != 0){
				// dispari
				Iterator it=setCaratteriDispari.iterator();
			    while(it.hasNext()) {
			    	Map.Entry<Character,Integer> m =(Map.Entry)it.next();
			    	char key=(char) m.getKey();
			    	int value=(int) m.getValue();
		            if(key == carattere){
		            	totale += value;
		            }
			    }
			} else {
				// pari
				Iterator it=setCaratteriPari.iterator();
			    while(it.hasNext()) {
			    	Map.Entry<Character,Integer> m =(Map.Entry)it.next();
			    	char key=(char) m.getKey();
			    	int value=(int) m.getValue();
		            if(key == carattere){
		            	totale += value;
		            }
			    }
			}
		}
		int resto = totale % 26;
		return caratteriDiControllo[resto];
	}
	
	private static void caricaMappaDispari(){
		dispariPerCaratteri = new HashMap<Character, Integer>();
		dispariPerCaratteri.put('0', 1);
		dispariPerCaratteri.put('1', 0);
		dispariPerCaratteri.put('2', 5);
		dispariPerCaratteri.put('3', 7);
		dispariPerCaratteri.put('4', 9);
		dispariPerCaratteri.put('5', 13);
		dispariPerCaratteri.put('6', 15);
		dispariPerCaratteri.put('7', 17);
		dispariPerCaratteri.put('8', 19);
		dispariPerCaratteri.put('9', 21);
		
		dispariPerCaratteri.put('A', 1);
		dispariPerCaratteri.put('B', 0);
		dispariPerCaratteri.put('C', 5);
		dispariPerCaratteri.put('D', 7);
		dispariPerCaratteri.put('E', 9);
		dispariPerCaratteri.put('F', 13);
		dispariPerCaratteri.put('G', 15);
		dispariPerCaratteri.put('H', 17);
		dispariPerCaratteri.put('I', 19);
		dispariPerCaratteri.put('J', 21);
		dispariPerCaratteri.put('K', 2);
		dispariPerCaratteri.put('L', 4);
		dispariPerCaratteri.put('M', 18);
		dispariPerCaratteri.put('N', 20);
		dispariPerCaratteri.put('O', 11);
		dispariPerCaratteri.put('P', 3);
		dispariPerCaratteri.put('Q', 6);
		dispariPerCaratteri.put('R', 8);
		dispariPerCaratteri.put('S', 12);
		dispariPerCaratteri.put('T', 14);
		dispariPerCaratteri.put('U', 16);
		dispariPerCaratteri.put('V', 10);
		dispariPerCaratteri.put('W', 22);
		dispariPerCaratteri.put('X', 25);
		dispariPerCaratteri.put('Y', 24);
		dispariPerCaratteri.put('Z', 23);
	}
	private static void caricaMappaPari(){
		pariPerCaratteri = new HashMap<Character, Integer>();
		pariPerCaratteri.put('0', 0);
		pariPerCaratteri.put('1', 1);
		pariPerCaratteri.put('2', 2);
		pariPerCaratteri.put('3', 3);
		pariPerCaratteri.put('4', 4);
		pariPerCaratteri.put('5', 5);
		pariPerCaratteri.put('6', 6);
		pariPerCaratteri.put('7', 7);
		pariPerCaratteri.put('8', 8);
		pariPerCaratteri.put('9', 9);
		
		pariPerCaratteri.put('A', 0);
		pariPerCaratteri.put('B', 1);
		pariPerCaratteri.put('C', 2);
		pariPerCaratteri.put('D', 3);
		pariPerCaratteri.put('E', 4);
		pariPerCaratteri.put('F', 5);
		pariPerCaratteri.put('G', 6);
		pariPerCaratteri.put('H', 7);
		pariPerCaratteri.put('I', 8);
		pariPerCaratteri.put('J', 9);
		pariPerCaratteri.put('K', 10);
		pariPerCaratteri.put('L', 11);
		pariPerCaratteri.put('M', 12);
		pariPerCaratteri.put('N', 13);
		pariPerCaratteri.put('O', 14);
		pariPerCaratteri.put('P', 15);
		pariPerCaratteri.put('Q', 16);
		pariPerCaratteri.put('R', 17);
		pariPerCaratteri.put('S', 18);
		pariPerCaratteri.put('T', 19);
		pariPerCaratteri.put('U', 20);
		pariPerCaratteri.put('V', 21);
		pariPerCaratteri.put('W', 22);
		pariPerCaratteri.put('X', 23);
		pariPerCaratteri.put('Y', 24);
		pariPerCaratteri.put('Z', 25);
	}
}
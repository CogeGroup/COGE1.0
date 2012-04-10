package utility;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


public class EncryptionUtility {
	
	
	private static  byte[] SALT_BYTES = {(byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,(byte)0x56, (byte)0x35, (byte)0xE3, (byte)0x03};
   
	private  static int ITERATION_COUNT = 19;
	
   //TODO spostare sul config.
	public static String passPhrase = "o%:(s@mwvB@sK0WyffC4";
	
	
	public static String encrypt (String str) {
		return encrypt(passPhrase, str);
	}

	
	public static String decrypt (String str) {
		return decrypt(passPhrase, str);
	}
	
		
	public static String encrypt (String passPhrase, String str) {
		Cipher ecipher = null;
		try {
			// Creare la key
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(),
					SALT_BYTES, ITERATION_COUNT);
			SecretKey key = SecretKeyFactory.getInstance(
					"PBEWithMD5AndDES").generateSecret(keySpec);
			ecipher = Cipher.getInstance(key.getAlgorithm());
			
			// Preparare i parametri per la cifratura
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(
					SALT_BYTES, ITERATION_COUNT);
			
			// Creare la cifratura
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
		} catch (javax.crypto.NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (java.security.NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (java.security.InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		
		try {
			// Incapsulare l'array di bytes usando utf-8
			byte[] utf8 = str.getBytes("UTF8");
			
			// Codifica
			byte[] enc = ecipher.doFinal(utf8);
			
			// Codificare bytes in base64 per ritornare l'array
			return new sun.misc.BASE64Encoder().encode(enc);
		} catch (javax.crypto.BadPaddingException e) {
		e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		}
		
		return null;
	}
	
	public static String decrypt(String passPhrase, String str) {
		Cipher dcipher = null;

 
		try {
			str = str.trim();
			// Creare la key
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(),
					SALT_BYTES, ITERATION_COUNT);
			SecretKey key = SecretKeyFactory.getInstance(
					"PBEWithMD5AndDES").generateSecret(keySpec);
			dcipher = Cipher.getInstance(key.getAlgorithm());
			
			// Preparare i parametri per la cifratura
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(
					SALT_BYTES, ITERATION_COUNT);
			
			// Creare la cifratura
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (javax.crypto.NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (java.security.NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (java.security.InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		
		try {
			// Decodifica base64 per ottenere bytes
			byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
			
			// Decodifica
			byte[] utf8 = dcipher.doFinal(dec);
			
			// Ritorna usando utf-8
			return new String(utf8, "UTF8");
		} catch (javax.crypto.BadPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main (String args[]){
		System.out.println(EncryptionUtility.encrypt("12345678"));
		
	}


	

}
package common;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherManager {
	
	private Cipher aes;
	private SecretKeySpec key;
	
	public CipherManager(String passPhrase, String saltIn) {
		byte[] salt = saltIn.getBytes();
		int iterations = 1000;
		SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			SecretKey tmp = factory.generateSecret(new PBEKeySpec(passPhrase.toCharArray(), salt, iterations, 128));
			key = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public byte[] encrypt(byte[] clearBytes) {
		System.out.println("[Crypto] encrypting...");
		try {
			aes.init(Cipher.ENCRYPT_MODE, key);
			return aes.doFinal(clearBytes);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] decrypt(byte[] cypherBytes) {
		System.out.println("[Crypto] decrypting...");
		try {
			aes.init(Cipher.DECRYPT_MODE, key);
			return aes.doFinal(cypherBytes);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
}

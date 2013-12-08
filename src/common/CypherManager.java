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

/**
 * Tool to encrypt and decrypt files. Create and store the key.
 * Use the symetric process.
 */
public class CypherManager {
	
	private Cipher aes;
	private SecretKeySpec key;
	
	public CypherManager(String passPhrase, String saltIn) {
		byte[] salt = saltIn.getBytes();
		int iterations = 1000;
		// Create the key based on passPhrase(=password) and a salt to encrypt and decrypt files.
		SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			char[] passPhraseCharArray = null;
			if(passPhrase != null) {
				passPhraseCharArray =passPhrase.toCharArray();
			}
			SecretKey tmp = factory.generateSecret(new PBEKeySpec(passPhraseCharArray, salt, iterations, 128));
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

	/**
	 * Encrypt clear bytes.
	 */
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

	/**
	 * Decrypt cyphered bytes.
	 */
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

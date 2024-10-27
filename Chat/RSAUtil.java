package Chat;

import java.security.*;
import javax.crypto.Cipher;
import java.util.Base64;

public class RSAUtil {
    // Encrypt a message using the public key
    public String encrypt(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Encrypt the message
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());

        // Encode the encrypted message using Base64
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt the Base64-encoded message using RSA
    public String decrypt(String encryptedMessage, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // Decode the encrypted message from Base64
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);

        // Decrypt the message
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Convert decrypted bytes to string and return
        return new String(decryptedBytes);
    }

    // Method to generate a new RSA Key Pair
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);  // RSA key size (2048 bits)
        return keyGen.generateKeyPair();
    }
}

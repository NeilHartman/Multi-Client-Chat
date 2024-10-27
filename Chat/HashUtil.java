package Chat;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    private MessageDigest md;

    // Constructor initializes MessageDigest for SHA-256
    public HashUtil() throws NoSuchAlgorithmException {
        // Initialize MessageDigest with SHA-256 algorithm
        md = MessageDigest.getInstance("MD5");
    }

    // Thread-safe method to hash a given input string
    public synchronized String hash(String input) {
        // Reset the MessageDigest before use
        md.reset();
        
        // Hash the input string and get the byte array
        byte[] messageDigest = md.digest(input.getBytes());
 
        // Convert byte array into signum representation
        BigInteger no = new BigInteger(1, messageDigest);

        // Convert message digest into hex value
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    // Thread-safe method to verify if the input string matches the hash
    public synchronized boolean verifyHash(String input, String hash) {
        // Hash the input and compare with the provided hash
        String inputHash = hash(input);
        return inputHash.equals(hash);
    }
}

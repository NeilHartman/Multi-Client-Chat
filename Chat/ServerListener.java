package Chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class ServerListener extends Thread {
    private DataInputStream dis;
    private RSAUtil rsaUtil;
    private Client client;
    private PrivateKey clientPrivateKey;
    private PublicKey clientPublicKey;
    private volatile boolean running = true;

    // Constructor that initializes the input stream and RSA utility
    public ServerListener(DataInputStream dis, DataOutputStream dos, RSAUtil rsaUtil, Client client) throws NoSuchAlgorithmException, IOException {
        this.dis = dis;
        this.rsaUtil = rsaUtil;
        this.client = client;

        KeyPair ClientkeyPair = this.rsaUtil.generateKeyPair();
        clientPrivateKey = ClientkeyPair.getPrivate();
        clientPublicKey = ClientkeyPair.getPublic();

        ObjectOutputStream oos = new ObjectOutputStream(dos);
        oos.writeObject(clientPublicKey);
    }

    @Override
    public void run() {
        try {
            while(running) {
                // Receive and decrypt the message from the server
                String encryptedMessage = dis.readUTF();
                String decryptedMessage = rsaUtil.decrypt(encryptedMessage ,clientPrivateKey);

                if (decryptedMessage.equals(Codes.CLIENT_REGISTER_APPROVED)) {
                    client.registerResult(true);  // Notify client of success
                } else if (decryptedMessage.equals(Codes.CLIENT_REGISTER_DECLINED)) {
                    client.registerResult(false);  // Notify client of failure
                }

                // Handle login responses
                else if (decryptedMessage.equals(Codes.CLIENT_LOGIN_APPROVED)) {
                    client.loginResult(true);  // Notify client of success
                } else if (decryptedMessage.equals(Codes.CLIENT_LOGIN_DECLINED)) {
                    client.loginResult(false);  // Notify client of failure
                }

                // Handle incoming chat messages
                else if (decryptedMessage.equals(Codes.CLIENT_GET_MESSAGE)) {
                    String encryptedChatMessage = dis.readUTF();
                    String decryptedChatMessage = rsaUtil.decrypt(encryptedChatMessage, clientPrivateKey);
                    client.displayMessage(decryptedChatMessage);  // Display message in GUI
                }
            }
            closeResources();
        }
        catch (Exception e) {
        } 
    }
    public String getMessage() throws Exception{
        String encryptedMessage = dis.readUTF();
        return rsaUtil.decrypt(encryptedMessage ,clientPrivateKey);
    }

    public void stopServerListener(){
        running = false;
    }

    private void closeResources() {
        try {
            if (dis != null) {
                dis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

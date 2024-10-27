package Chat;

import java.net.*;
import java.io.*;
import java.security.PublicKey;

public class Client {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private RSAUtil rsaUtil;
    private PublicKey serverPublicKey;
    private ClientGUI gui;  // Reference to the ClientGUI for updating messages
    private String username = ""; 
    private ServerListener Slisten;

    // Constructor to connect to the server and set up communication
    public Client(String address, int port) {
        try {
            // Establish connection to the server
            socket = new Socket(address, port);

            // Set up streams for communication
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            ObjectInputStream ois = new ObjectInputStream(dis);

            // Get the server's public key for RSA encryption
            serverPublicKey = (PublicKey) ois.readObject();
            rsaUtil = new RSAUtil();

            // Launch a thread to listen for server responses
            Slisten = new ServerListener(dis, dos, rsaUtil, this);
            new Thread(Slisten).start();  // Start the listener thread

        } catch (Exception e) {
            if (gui != null) {
                gui.appendMessage("Connection Error: " + e.getMessage());
            } else {
                System.out.println("Connection Error: " + e.getMessage());
            }
        }
    }

    // Method to send encrypted messages to the server
    public void sendMessage(String message) {
        try {
            // Encrypt the message using RSA
            String encryptedMessage = rsaUtil.encrypt(message, serverPublicKey);
            dos.writeUTF(encryptedMessage);  // Send the encrypted message to the server
            dos.flush();
        } catch (Exception e) {
            if (gui != null) {
                gui.appendMessage("Error sending message: " + e.getMessage());
            }
        }
    }
    public void Register(String username, String password) throws Exception {
        // Encrypt the message using RSA
        String encryptedMessage = rsaUtil.encrypt(Codes.SERVER_REGISTER, serverPublicKey);
        dos.writeUTF(encryptedMessage);
        sendMessage(username);
        sendMessage(password);
        this.username = username;
    }
    public void Login(String username, String password) throws Exception {
        // Encrypt the message using RSA
        String encryptedMessage = rsaUtil.encrypt(Codes.SERVER_LOGIN, serverPublicKey);
        dos.writeUTF(encryptedMessage);
        sendMessage(username);
        sendMessage(password);

    }
    public void getChatMessage(String message) throws Exception{
        String encryptedMessage = rsaUtil.encrypt(Codes.CLIENT_MESSAGE,serverPublicKey);
        dos.writeUTF(encryptedMessage);
        sendMessage((this.getUsername() + ": " + message));
    }

    protected void registerResult(boolean success) {
        if (gui != null) {
            gui.registrationResult(success);  // Notify the GUI about the registration result
        }
    }

    // This method will be called from the ServerListener when the login result is received
    protected void loginResult(boolean success) {
        if (gui != null) {
            gui.loginResult(success);  // Notify the GUI about the login result
        }
    }


    public String getUsername() {
        return username;  // Now it is safe to update the GUI
    }

    // Method to set the GUI reference after creating the client
    public void setGui(ClientGUI gui) {
        this.gui = gui;
    }

    // Method to append a message to the GUI (from the receiver thread)
    public void displayMessage(String message) {
        if (gui != null) {
            gui.appendMessage(message);
        }
    }
    public void disconnect() throws Exception{
        sendMessage(Codes.CLIENT_DISCONNECT);
        if (Slisten != null){
            Slisten.stopServerListener();
            Slisten.join();
        }
        if(dos != null) dos.close();
        if(socket != null && socket.isConnected()) socket.close();
        
    }
}

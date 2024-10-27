package Chat;

import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private Server serverObj;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private HashUtil hashUtil;
    private RSAUtil rsaUtil;
    private PrivateKey serverPrivateKey;  // Server's private key for decrypting client messages
    private PublicKey clientPublicKey;
    private String clientUsername; 
    private boolean loggedIn = false; 


    // Constructor to initialize the client socket and RSA utility
    public ClientHandler(Socket socket,Server serverObj, PrivateKey serverPrivateKey) throws NoSuchAlgorithmException {
        this.socket = socket;
        this.serverObj = serverObj;
        this.serverPrivateKey = serverPrivateKey;
        hashUtil = new HashUtil();
        rsaUtil = new RSAUtil();
    }

    // The run method that will handle client communication
    @Override
    public void run() {
        try {
            // Create input stream for the client
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            ObjectInputStream ois = new ObjectInputStream(dis);

            // Get the server's public key for RSA encryption
            clientPublicKey = (PublicKey) ois.readObject();
            String encryptedMessage = "";
            // Continuously listen for encrypted messages from the client
            while(true) {
                encryptedMessage = dis.readUTF();  // Read encrypted message from client
                String decryptedMessage = rsaUtil.decrypt(encryptedMessage, serverPrivateKey);  // Decrypt the message
                System.out.println("Decrypted Client Message: " + decryptedMessage);

                // Handle specific commands from the client
                switch (decryptedMessage) {
                    case Codes.CLIENT_DISCONNECT:
                        loggedIn = false;
                        disconnect();
                    case Codes.SERVER_LOGIN:
                        String strUsernameLog = rsaUtil.decrypt(dis.readUTF(), serverPrivateKey);
                        String strPasswordLog = rsaUtil.decrypt(dis.readUTF(), serverPrivateKey);
                        
                        // Debugging: Print the decrypted values
                        if(serverObj.userExist(strUsernameLog)){
                            String passwordLogHash = hashUtil.hash(strPasswordLog);
                            if(serverObj.LoginCheck(strUsernameLog, passwordLogHash)){
                                sendMessage(Codes.CLIENT_LOGIN_APPROVED);
                                loggedIn = true;
                                clientUsername = strUsernameLog;
                            }
                            else{
                                sendMessage(Codes.CLIENT_LOGIN_DECLINED);
                            }
                        }
                        else{
                            sendMessage(Codes.CLIENT_LOGIN_DECLINED);
                        }
                        break;
                    case Codes.SERVER_REGISTER:
                        // Decrypt the username and password received from the client
                        String strUsernameReg = rsaUtil.decrypt(dis.readUTF(), serverPrivateKey);
                        String strPasswordReg = rsaUtil.decrypt(dis.readUTF(), serverPrivateKey);
                        
                        // Debugging: Print the decrypted values
                        
                        if(!serverObj.userExist(strUsernameReg)){
                            String passwordRegHash = hashUtil.hash(strPasswordReg);
                            serverObj.addUser(strUsernameReg, passwordRegHash);
                            sendMessage(Codes.CLIENT_REGISTER_APPROVED);
                            loggedIn = true;
                            clientUsername = strUsernameReg;
                            System.out.println("Users map after registration: " + serverObj.getUsers());
                        }
                        else{
                            sendMessage(Codes.CLIENT_REGISTER_DECLINED);
                            System.out.println("nothing has changed in users!");
                        }
                        // Debugging: Print the current users after registration
                        break; 
                    case Codes.CLIENT_MESSAGE:
                        if(loggedIn){
                            decryptedMessage = rsaUtil.decrypt(dis.readUTF(), serverPrivateKey);
                            String[] decryptedMessageSplited = decryptedMessage.split(" "); // in order to get the message after the username

                            if(decryptedMessageSplited[1].equals("/whisper")){
                                if(serverObj.userExist(decryptedMessageSplited[2])){
                                    if(serverObj.directChatMessage(Codes.CLIENT_GET_MESSAGE,this,decryptedMessageSplited[2])){
                                        String directMessage = ""; 
                                        for(int i = 3; i < decryptedMessageSplited.length;i++) directMessage += (decryptedMessageSplited[i] + " ");
                                        serverObj.directChatMessage(clientUsername + " whisper: " + directMessage,this,decryptedMessageSplited[2]);
                                        sendMessage(Codes.CLIENT_SEND_MESSAGE);
                                    }
                                    else{
                                        sendMessage(Codes.CLIENT_GET_MESSAGE);
                                        sendMessage("The user isn't logged in");
                                    }

                                }
                                else{
                                    sendMessage(Codes.CLIENT_GET_MESSAGE);
                                    sendMessage("This user doesn't exist!");
                                }
                                
                                
                            }
                            else if(decryptedMessageSplited[1].equals("/connectedUsers")){
                                serverObj.getConnectedUsers(this);
                            }
                            else{   
                                serverObj.broadcastChatMessage(Codes.CLIENT_GET_MESSAGE,this);
                                serverObj.broadcastChatMessage(decryptedMessage,this);
                                sendMessage(Codes.CLIENT_SEND_MESSAGE);
                        }
                            }
                            
                    default:

                }
            }

        } catch (Exception e) {
            System.out.println("Client Error: " + e);
        } finally {
            // Close resources
            try {
                if (dis != null) dis.close();
                if (dos != null) dos.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e);
            }
        }
    }
    public void sendMessage(String message) throws Exception {
            // Encrypt the message using RSA
            String encryptedMessage = rsaUtil.encrypt(message, clientPublicKey);
            dos.writeUTF(encryptedMessage);  // Send the encrypted message to the server
            dos.flush();
        
    }
    public Boolean isLoggedIn(){
        return loggedIn;
    }
    public String getClientUsername(){
        return clientUsername;
    }
    private void disconnect() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && socket.isConnected()) socket.close();
            serverObj.removeClient(this);  // Notify the server to remove this client handler
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }
}

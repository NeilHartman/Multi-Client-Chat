package Chat;

import java.net.*;
import java.io.*;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class Server {
    private static ServerSocket serverSocket = null;
    private static RSAUtil rsaUtil;
    private static  PrivateKey serverPrivateKey;
    private static PublicKey serverPublicKey;
    private static List<ClientHandler> clientHandlers; // List to store client handlers
    protected static ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();



    // Constructor with port
    public Server(int port) throws NoSuchAlgorithmException {
        try {
            // Initialize RSAUtil and generate the RSA keys for the server
            rsaUtil = new RSAUtil();
            KeyPair ServerkeyPair = rsaUtil.generateKeyPair();
            serverPrivateKey = ServerkeyPair.getPrivate();
            serverPublicKey = ServerkeyPair.getPublic();
            clientHandlers = new ArrayList<>();

            // Start the server socket
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            // Infinite loop to accept multiple clients
            while (true) {
                System.out.println("Waiting for a client...");
                Socket clientSocket = serverSocket.accept(); // Accept a client connection
                System.out.println("Client connected.");

                // Send the public key to the client (so client can encrypt messages using it)
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(serverPublicKey); // Send server's public key to the client

                // Create a new ClientHandler for each client and pass the client socket and server's private key
                ClientHandler clientHandler = new ClientHandler(clientSocket,this ,serverPrivateKey);

                // Add the client handler to the list (for managing multiple clients)
                clientHandlers.add(clientHandler);

                // Start the client handler in a new thread to handle communication with this client
                clientHandler.start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            // Close the server socket if necessary
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
    public ConcurrentHashMap<String, String> getUsers() {
        return users;
    }
    public void addUser(String username, String password) {
        getUsers().put(username, password);
    }

    public boolean userExist(String username) {
        return getUsers().containsKey(username);
    }
    public boolean LoginCheck(String username, String password) {
        return users.get(username).equals(password);
    }

    public void broadcastChatMessage(String message, ClientHandler sender) throws Exception {
        for (ClientHandler clientHandler : clientHandlers) {
            if(clientHandler != sender && clientHandler.isLoggedIn()) {  // Don't send the message back to the sender
                try {
                    clientHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean directChatMessage(String message,ClientHandler sender, String recieverUsername) throws Exception {
        for (ClientHandler clientHandler : clientHandlers) {
            if(clientHandler.getClientUsername().equals(recieverUsername) && clientHandler.isLoggedIn() && clientHandler != sender) {  // Don't send the message back to the sender
                try {
                    clientHandler.sendMessage(message);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    public void getConnectedUsers(ClientHandler sender) throws Exception {
        String connectedUsersString = "";
        for (ClientHandler clientHandler : clientHandlers) {
            if(clientHandler.isLoggedIn() && clientHandler != sender) {  // Don't send the message back to the sender
                connectedUsersString += (clientHandler.getClientUsername() + " ");
            }
        }
        try {
            if(connectedUsersString.equals("")){
                connectedUsersString = "No one except you is connected!";
            }
            sender.sendMessage(Codes.CLIENT_GET_MESSAGE);
            sender.sendMessage(connectedUsersString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }
    
    // Main method to start the server
    public static void main(String[] args) throws NoSuchAlgorithmException {
        int port = 5000; // Define a port to run the server
        new Server(port); // Start the server
    }
}

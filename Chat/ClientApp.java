package Chat;

public class ClientApp {
    public static void main(String[] args) {
        // Create the client object that connects to the server
        Client client = new Client("127.0.0.1", 5000);
        
        // Create the GUI and link it with the client
        ClientGUI clientGui = new ClientGUI(client);
        client.setGui(clientGui);  // Set the GUI for message updates
    }
}


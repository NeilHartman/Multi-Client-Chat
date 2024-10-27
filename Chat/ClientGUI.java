package Chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI {
    private JFrame frame;
    private JTextArea textArea;
    private JTextField inputField;
    private JFrame loginFrame;
    private Client client;

    // Constructor to show the login/register window first
    public ClientGUI(Client client) {
        this.client = client;  // Store the client object
        showLoginRegisterWindow();
    }

    // Method to display the login/register window
    private void showLoginRegisterWindow() {
        this.loginFrame = new JFrame("Login/Register");

        // Create components
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Panel for holding the components
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));  // Adjust grid layout for username, password, and buttons
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginFrame.add(panel);
        loginFrame.setSize(300, 200);  // Adjust size for the new password field
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);  // Center the window
        loginFrame.setVisible(true);

        // Action listener for the login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passwordField.getPassword());  // Retrieve password as a string

                if (!username.isEmpty() && !password.isEmpty()) {
                    // Handle login logic here (send login info to server)
                    try {
                        client.Login(username,password);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Username or Password cannot be empty.");
                }
            }
        });

        // Action listener for the register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passwordField.getPassword());  // Retrieve password as a string

                if (!username.isEmpty() && !password.isEmpty()) {
                    // Handle registration logic here (send registration info to server)
                    try {
                        client.Register(username,password);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Username or Password cannot be empty.");
                }
            }
        });
        
    }
    protected void registrationResult(boolean success) {
        if(success) {
            loginFrame.dispose();  // Close the login window
            showChatWindow();     // Show the chat window
        } else {
            JOptionPane.showMessageDialog(loginFrame, "Username already exists!");
        }
    }

    // This method is called by the client when login result is available
    protected void loginResult(boolean success) {
        if(success) {
            loginFrame.dispose();  // Close the login window
            showChatWindow();      // Show the chat window
        } else {
            JOptionPane.showMessageDialog(loginFrame, "This user does not exist or password is incorrect!");
        }
    }

    // Method to display the main chat window after login or registration
    private void showChatWindow() {
        // Create the chat window
        frame = new JFrame("Client Chat");
        textArea = new JTextArea();
        textArea.setEditable(false);  // Prevent the user from editing the text area
        JScrollPane scrollPane = new JScrollPane(textArea);

        inputField = new JTextField();
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                if (!message.isEmpty()) {
                    try {
                        sendMessage(message);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    inputField.setText("");  // Clear the input field after sending the message
                }
            }
        });

        // Add the components to the frame
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);  // Center the window
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    client.disconnect();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }  // Call the disconnect method in Client when closing the window
            }
        });
    }

    // Method to send the message via the client
    private void sendMessage(String message) throws Exception {
        client.getChatMessage(message);  // Call the client class to handle the message sending
        appendMessage("You: " + message);  // Display the message in the text area
    }

    // Method to append received messages to the text area
    public void appendMessage(String message) {
        textArea.append(message + "\n");
    }
}

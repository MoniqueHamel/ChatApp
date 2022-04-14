package client.gui;

import client.Client;

import javax.swing.*;
import java.awt.*;

public class LoginScreen {

    Client client;
    private JFrame loginFrame = new JFrame("Login/Register");
    private JLabel loginFailedLabel = new JLabel();
    JTextField usernameInputField = new JTextField();
    JPasswordField passwordInputField = new JPasswordField();
    JPanel loginPanel = new JPanel();
    JLabel usernameLabel = new JLabel("Enter a username:");
    JLabel passwordLabel = new JLabel("Enter a password: ");
    JButton registerButton = new JButton("Register");
    JButton loginButton = new JButton("Login");

    public LoginScreen(Client client){
        this.client = client;
        setupLoginScreen();
    }

    private void setupLoginScreen(){
        loginFrame.getRootPane().setDefaultButton(loginButton);
        arrangeComponents();
        addActionListenersToButtons();
        loginFailedLabel.setVisible(false);
        loginFrame.setVisible(true);
        loginFrame.setSize(600, 300);
        loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void arrangeComponents(){
        loginFrame.setLayout(new GridBagLayout());
        GridBagConstraints loginPanelConstraints = createGridBagConstraints(0, 0, 1, 1, false);
        loginFrame.add(loginPanel, loginPanelConstraints);

        loginPanel.setLayout(new GridBagLayout());

        GridBagConstraints loginFailedLabelConstraints = createGridBagConstraints(0, 0, 1, 1, true);
        loginFailedLabelConstraints.gridwidth = 2;

        GridBagConstraints usernameLabelConstraints = createGridBagConstraints(0, 1, 0.2, 0.2, true);

        GridBagConstraints usernameInputFieldConstraints = createGridBagConstraints(1, 1, 0.8,0.8, false);
        usernameInputFieldConstraints.fill = GridBagConstraints.HORIZONTAL;

        GridBagConstraints passwordLabelConstraints = createGridBagConstraints(0, 2, 0.2, 0.2, true);

        GridBagConstraints passwordInputFieldConstraints = createGridBagConstraints(1, 2, 0.8, 0.8, false);
        passwordInputFieldConstraints.fill = GridBagConstraints.HORIZONTAL;

        GridBagConstraints loginButtonConstraints = createGridBagConstraints(0, 3, 0, 0, false);

        GridBagConstraints registerButtonConstraints = createGridBagConstraints(1, 3, 0, 0, false);

        loginPanel.add(loginFailedLabel, loginFailedLabelConstraints);
        loginPanel.add(usernameLabel, usernameLabelConstraints);
        loginPanel.add(usernameInputField, usernameInputFieldConstraints);
        loginPanel.add(passwordLabel, passwordLabelConstraints);
        loginPanel.add(passwordInputField, passwordInputFieldConstraints);
        loginPanel.add(registerButton, registerButtonConstraints);
        loginPanel.add(loginButton, loginButtonConstraints);
    }

    private void addActionListenersToButtons(){
        loginButton.addActionListener(actionEvent -> sendLoginDetails());

        registerButton.addActionListener(actionEvent -> sendRegistrationDetails());
    }

    private void sendLoginDetails(){
        loginFailedLabel.setVisible(false);
        String username = usernameInputField.getText().trim();
        String password = String.valueOf(passwordInputField.getPassword());
        client.sendLoginDetails(username, password);
    }

    private void sendRegistrationDetails(){
        loginFailedLabel.setVisible(false);
        String username = usernameInputField.getText().trim();
        String password = String.valueOf(passwordInputField.getPassword());
        if (username.equals("") || username.startsWith("#") || password.equals("")){
            showLoginFailedMessage("Invalid username and/or password!");
        } else {
            client.sendRegistrationDetails(username, password);
        }
    }

    public void showLoginFailedMessage(String message){
        loginFailedLabel.setText(message);
        loginFailedLabel.setForeground(Color.RED);
        loginFailedLabel.setVisible(true);
    }

    public void dispose(){
        loginFrame.dispose();
    }

    private GridBagConstraints createGridBagConstraints(int x, int y, double xWeight, double yWeight, boolean fill){
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.weightx = xWeight;
        c.weighty = yWeight;
        c.insets = new Insets(5, 5, 5, 5);
        if (fill == true){
            c.fill = GridBagConstraints.BOTH;
        }

        return c;
    }
}

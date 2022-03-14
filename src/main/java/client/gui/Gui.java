package client.gui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Gui {

    private final Client client;
    private JFrame mainFrame = new JFrame("ChatApp");
    private JFrame loginFrame;
    private JTextArea messageBox;
    private JTextArea inputArea;
    private JPanel northPanel;
    private JPanel southPanel;

    public static void main(String args[]){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Gui mainGui = new Gui();
        //mainGui.display();

    }

    public Gui(Client client){
        this.client = client;
    }

    public void displayLoginView(){
        mainFrame.setLayout(new GridBagLayout());
        setupGui();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400, 400);
        mainFrame.setVisible(false);
        loginFrame = new JFrame("Choose your username!");
        loginFrame.setLayout(new GridBagLayout());
        JPanel loginPanel = new JPanel(new GridBagLayout());
        JTextField usernameInputField = new JTextField();
        JLabel usernameLabel = new JLabel("Enter a username:");
        JButton chooseUsernameButton = new JButton("Choose username");

        GridBagConstraints left = new GridBagConstraints();
        setGridBagConstraints(left, 0, 0, 0.2, 0.2, true);
        GridBagConstraints right = new GridBagConstraints();
        setGridBagConstraints(right, 1, 0, 0.8, 0.8, false);
        right.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints south = new GridBagConstraints();
        setGridBagConstraints(south, 1, 1, 0, 0, false);
        GridBagConstraints c = new GridBagConstraints();
        setGridBagConstraints(c, 0, 0, 1, 1, false);

        loginPanel.add(usernameLabel, left);
        loginPanel.add(usernameInputField, right);
        loginPanel.add(chooseUsernameButton, south);
        loginFrame.add(loginPanel, c);
        loginFrame.setVisible(true);
        loginFrame.setSize(600, 300);

        chooseUsernameButton.addActionListener(actionEvent -> {
            loginFrame.setVisible(false);
            client.setClientUsername(usernameInputField.getText());
            displayMainView();
        });
    }

    public void displayMainView(){
        mainFrame.setVisible(true);

    }

    private void setupGui(){
        northPanel = new JPanel(new GridBagLayout());
        southPanel = new JPanel(new GridBagLayout());
        GridBagConstraints top = new GridBagConstraints();
        setGridBagConstraints(top, 0, 0, 1.0, 0.9, true);

        GridBagConstraints bottom = new GridBagConstraints();
        setGridBagConstraints(bottom, 0, 1, 1.0, 0.1, true);

        setupAndAddMessageBox();
        setupAndAddInputArea();
        setupAndAddSendMessageButton();

        mainFrame.add(northPanel, top);
        mainFrame.add(southPanel, bottom);
    }

    private void setupAndAddMessageBox(){
        messageBox = new JTextArea();
        messageBox.setEditable(false);
        messageBox.setLineWrap(true);

        GridBagConstraints north = new GridBagConstraints();
        setGridBagConstraints(north, 0, 0, 1.0, 1.0, true);

        northPanel.add(new JScrollPane(messageBox), north);
    }

    private void setupAndAddInputArea(){
        inputArea = new JTextArea();
        inputArea.setLineWrap(true);

        GridBagConstraints left = new GridBagConstraints();
        setGridBagConstraints(left, 0, 0, 1.0, 1.0, true);

        southPanel.add(new JScrollPane(inputArea), left);
    }

    private void setupAndAddSendMessageButton(){
        JButton sendMessageButton = new JButton("Send Message");

        GridBagConstraints right = new GridBagConstraints();
        setGridBagConstraints(right, 1, 0, 0.0, 0.0, false);

        southPanel.add(sendMessageButton, right);

        sendMessageButton.addActionListener(actionEvent -> {
            String text = inputArea.getText();
            client.sendMessage(text);
            //String output = client.getClientUsername() + ": " + text;
            inputArea.setText("");
            //messageBox.append("\n" + output);
        });

    }

    public void appendTextToMessageBox(String text){
        messageBox.append("\n" + text);
    }

    private void setGridBagConstraints(GridBagConstraints c, int x, int y, double xWeight, double yWeight, boolean fill){
        c.gridx = x;
        c.gridy = y;
        c.weightx = xWeight;
        c.weighty = yWeight;
        c.insets = new Insets(5, 5, 5, 5);
        if (fill == true){
            c.fill = GridBagConstraints.BOTH;
        }
    }
}

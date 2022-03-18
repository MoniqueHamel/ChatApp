package client.gui;

import client.Client;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Gui {

    private final Client client;
    private JFrame mainFrame = new JFrame();
    private JFrame loginFrame;
    private JTextArea messageBox;
    private JTextArea inputArea;
    private JPanel northPanel;
    private JPanel southPanel;
    private JPanel westPanel;
    private JList<String> listOfActiveUsers;
    ActiveUsersModel userListModel = new ActiveUsersModel();
    private String selectedUser = "Global";

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
        // Map Enter key to chooseUsernameButton
        loginFrame.getRootPane().setDefaultButton(chooseUsernameButton);

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
            mainFrame.setTitle("ChatApp - " + client.getClientUsername());
            displayMainView();
        });
    }

    public void displayMainView(){
        mainFrame.setVisible(true);
    }

    private void setupGui(){
        northPanel = new JPanel(new GridBagLayout());
        southPanel = new JPanel(new GridBagLayout());
        westPanel = new JPanel();

        GridBagConstraints top = new GridBagConstraints();
        setGridBagConstraints(top, 1, 0, 1.0, 0.9, true);

        GridBagConstraints bottom = new GridBagConstraints();
        setGridBagConstraints(bottom, 1, 1, 1.0, 0.1, true);

        GridBagConstraints left = new GridBagConstraints();
        setGridBagConstraints(left, 0, 0,1, 1, true);
        left.gridheight = 2;

        setupAndAddMessageBox();
        setupAndAddInputArea();
        setupAndAddSendMessageButton();
        setupAndAddOnlineClientsPanel();

        mainFrame.add(northPanel, top);
        mainFrame.add(southPanel, bottom);
        mainFrame.add(westPanel, left);
    }

    private void setupAndAddOnlineClientsPanel(){
        userListModel.add("Global");
        listOfActiveUsers = new JList<>(userListModel);
        listOfActiveUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listOfActiveUsers.setSelectedValue("Global", true);
        userListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent listDataEvent) {

            }

            @Override
            public void intervalRemoved(ListDataEvent listDataEvent) {

            }

            @Override
            public void contentsChanged(ListDataEvent listDataEvent) {
                listOfActiveUsers.setSelectedValue(selectedUser, true);
            }
        });
        listOfActiveUsers.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    switchChat();
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        setGridBagConstraints(c, 0, 0, 0, 0, true);

        westPanel.add(listOfActiveUsers);
    }

    private void switchChat(){
        String text = messageBox.getText();
        client.saveChat(selectedUser, text);
        selectedUser = listOfActiveUsers.getSelectedValue();
        text = client.loadChat(selectedUser);
        messageBox.setText(text);
    }

    public void addUserToActiveUserList(String user){
        userListModel.add(user);
    }

    public void removeUserFromActiveUserList(String user){
        userListModel.remove(user);
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

        String SEND_TEXT = "send-text";
        String INSERT_BREAK = "insert-break";

        InputMap input = inputArea.getInputMap();
        KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
        KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
        input.put(shiftEnter, INSERT_BREAK);
        input.put(enter, SEND_TEXT);

        ActionMap actions = inputArea.getActionMap();
        actions.put(SEND_TEXT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage();
            }
        });

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
            sendMessage();
        });
    }

    private void sendMessage(){
        String text = inputArea.getText();
        client.sendMessage(text, listOfActiveUsers.getSelectedValue());
        inputArea.setText("");
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

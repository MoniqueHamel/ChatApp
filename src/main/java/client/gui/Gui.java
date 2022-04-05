package client.gui;

import client.Client;
import client.UserInfo;
import common.Message;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Gui {

    private final Client client;
    private JFrame mainFrame = new JFrame();
    private JFrame loginFrame;
    private JTextArea messageBox;
    private JTextArea inputArea;
    private JPanel northPanel;
    private JPanel southPanel;
    private JPanel westPanel;
    private JList<UserInfo> listOfActiveUsers;
    ActiveUsersModel userListModel = new ActiveUsersModel();
    private String selectedUser = Message.GLOBAL;
    private JLabel loginFailedLabel = new JLabel();
    JTextField usernameInputField;


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
        usernameInputField = new JTextField();
        JPasswordField passwordInputField = new JPasswordField();
        JLabel usernameLabel = new JLabel("Enter a username:");
        JLabel passwordLabel = new JLabel("Enter a password: ");
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");
        // Map Enter key to chooseUsernameButton
        loginFrame.getRootPane().setDefaultButton(registerButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loginFailedLabel.setVisible(false);
                String username = usernameInputField.getText().trim();
                String password = String.valueOf(passwordInputField.getPassword());
                client.sendLoginDetails(username, password);
            }
        });

        registerButton.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loginFailedLabel.setVisible(false);
                String username = usernameInputField.getText().trim();
                String password = String.valueOf(passwordInputField.getPassword());
                if (username.equals("") || username.startsWith("#") || password.equals("")){
                    showLoginFailedMessage("Invalid username and/or password!");
                } else {
                    client.sendRegistrationDetails(username, password);
                }
            }
        }));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        setGridBagConstraints(labelConstraints, 0, 0, 1, 1, true);
        labelConstraints.gridwidth = 2;
        GridBagConstraints left = new GridBagConstraints();
        setGridBagConstraints(left, 0, 1, 0.2, 0.2, true);
        GridBagConstraints right = new GridBagConstraints();
        setGridBagConstraints(right, 1, 1, 0.8, 0.8, false);
        right.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints west = new GridBagConstraints();
        setGridBagConstraints(west, 0, 2, 0.2, 0.2, true);
        GridBagConstraints east = new GridBagConstraints();
        setGridBagConstraints(east, 1, 2, 0.8, 0.8, false);
        east.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints leftSouth = new GridBagConstraints();
        setGridBagConstraints(leftSouth, 0, 3, 0, 0, false);
        GridBagConstraints south = new GridBagConstraints();
        setGridBagConstraints(south, 1, 3, 0, 0, false);
        GridBagConstraints c = new GridBagConstraints();
        setGridBagConstraints(c, 0, 0, 1, 1, false);

        loginPanel.add(loginFailedLabel, labelConstraints);
        loginFailedLabel.setVisible(false);
        loginPanel.add(usernameLabel, left);
        loginPanel.add(usernameInputField, right);
        loginPanel.add(passwordLabel, west);
        loginPanel.add(passwordInputField, east);
        loginPanel.add(registerButton, south);
        loginPanel.add(loginButton, leftSouth);
        loginFrame.add(loginPanel, c);
        loginFrame.setVisible(true);
        loginFrame.setSize(600, 300);
    }

    public void displayMainView(){
        loginFrame.setVisible(false);
//        client.setClientUsername(usernameInputField.getText());
        mainFrame.setTitle("ChatApp - " + client.getClientUsername());
        mainFrame.setVisible(true);
        inputArea.requestFocusInWindow();
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
        userListModel.add(Message.GLOBAL, true);
        listOfActiveUsers = new JList<>(userListModel);
        listOfActiveUsers.setCellRenderer(new ActiveUserListRenderer());
        listOfActiveUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listOfActiveUsers.setSelectedValue(Message.GLOBAL, true);
        userListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent listDataEvent) {

            }

            @Override
            public void intervalRemoved(ListDataEvent listDataEvent) {

            }

            @Override
            public void contentsChanged(ListDataEvent listDataEvent) {
                listOfActiveUsers.setSelectedValue(new UserInfo(selectedUser, true), true);
            }
        });
        listOfActiveUsers.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    switchChat();
                    inputArea.requestFocusInWindow();
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        setGridBagConstraints(c, 0, 0, 1, 1, true);

        westPanel.add(listOfActiveUsers);
    }

    private void switchChat(){
        String text = messageBox.getText();
        client.saveChat(selectedUser, text);
        selectedUser = listOfActiveUsers.getSelectedValue().username;
        text = client.loadChat(selectedUser);
        messageBox.setText(text);
    }

    public void addUserToActiveUserList(String user, boolean isOnline){
        if (!user.equals(client.getClientUsername())){
            SwingUtilities.invokeLater(() -> userListModel.add(user, isOnline));
        }
    }

    public void removeUserFromActiveUserList(String user){
//        userListModel.add(user, false);
        SwingUtilities.invokeLater(() -> userListModel.setAsInactive(new UserInfo(user, true)));
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
        String text = inputArea.getText().trim();
        if (text.equals("")) return;
        client.sendUserMessage(text, listOfActiveUsers.getSelectedValue().username);
        inputArea.setText("");
        inputArea.requestFocusInWindow();
    }

    public void appendTextToMessageBox(String text){
        messageBox.append(text + "\n");
    }

    public void showLoginFailedMessage(String message){
        loginFailedLabel.setText(message);
        loginFailedLabel.setForeground(Color.RED);
        loginFailedLabel.setVisible(true);
    }

    public String getSelectedUser() {
        return selectedUser;
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

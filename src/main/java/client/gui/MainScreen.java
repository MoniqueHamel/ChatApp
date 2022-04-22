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

public class MainScreen {

    private final Client client;
    private JFrame mainFrame = new JFrame();
    private JTextArea messageBox;
    private JTextArea inputArea;
    private JPanel northPanel;
    private JPanel southPanel;
    private JPanel westPanel;
    private JList<UserInfo> listOfActiveUsers;
    JButton newChatroomButton = new JButton("Create chatroom");
    JButton addMemberButton = new JButton("Add member");
    ActiveUsersModel userListModel = new ActiveUsersModel();
    private String selectedUser = Message.GLOBAL;
    private JLabel loginFailedLabel = new JLabel();

    public static void main(String args[]){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Gui mainGui = new Gui();
        //mainGui.display();

    }

    public MainScreen(Client client){
        this.client = client;
        mainFrame.setLayout(new GridBagLayout());
        setupGui();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(700, 400);
        displayMainView();
    }


    private void displayMainView(){
        mainFrame.setTitle("ChatApp - " + client.getClientUsername());
        mainFrame.setVisible(true);
        inputArea.requestFocusInWindow();
    }

    private void setupGui(){
        northPanel = new JPanel(new GridBagLayout());
        southPanel = new JPanel(new GridBagLayout());
        westPanel = new JPanel(new GridBagLayout());

        GridBagConstraints top = new GridBagConstraints();
        setGridBagConstraints(top, 1, 0, 0.8, 0.9, true);

        GridBagConstraints bottom = new GridBagConstraints();
        setGridBagConstraints(bottom, 1, 1, 0.8, 0.1, true);

        GridBagConstraints left = new GridBagConstraints();
        setGridBagConstraints(left, 0, 0,0.2, 1.0, true);
        left.gridheight = 2;

        setupAndAddNewChatroomButton();
        setupAndAddOnlineClientsPanel();
        setupAndAddNewChatroomMemberButton();
        setupAndAddMessageBox();
        setupAndAddInputArea();
        setupAndAddSendMessageButton();

        mainFrame.add(northPanel, top);
        mainFrame.add(southPanel, bottom);
        mainFrame.add(westPanel, left);
    }

    private void setupAndAddOnlineClientsPanel(){
        userListModel.add(Message.GLOBAL, true);
        listOfActiveUsers = new JList<>(userListModel);
        Dimension d = listOfActiveUsers.getPreferredScrollableViewportSize();
        d.width = newChatroomButton.getPreferredSize().width;
//        listOfActiveUsers.setPreferredSize(d);
        listOfActiveUsers.setFixedCellWidth(d.width);
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
        setGridBagConstraints(c, 0, 1, 1.0, 0.8, true);

        westPanel.add(listOfActiveUsers, c);
    }

    private void switchChat(){
        String text = messageBox.getText();
        client.saveChat(selectedUser, text);
        selectedUser = listOfActiveUsers.getSelectedValue().username;
        text = client.loadChat(selectedUser);
        messageBox.setText(text);
        if(selectedUser.startsWith("#") && !selectedUser.equals(Message.GLOBAL)){
            addMemberButton.setVisible(true);
        } else {
            addMemberButton.setVisible(false);
        }
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

    private void setupAndAddNewChatroomButton(){
        GridBagConstraints c = new GridBagConstraints();
        setGridBagConstraints(c, 0, 0, 1.0, 0.1, false);

        westPanel.add(newChatroomButton, c);

        newChatroomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String chatroomName = "#" + JOptionPane.showInputDialog("Enter chatroom name: ");
                client.sendMessage(Message.chatroomCreated(client.getClientUsername(), null, chatroomName));
            }
        });
    }

    private void setupAndAddNewChatroomMemberButton(){
        GridBagConstraints c = new GridBagConstraints();
        setGridBagConstraints(c, 0, 2, 1.0, 0.1, false);

        westPanel.add(addMemberButton, c);
        addMemberButton.setVisible(false);

        addMemberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Object[] users = userListModel.getProperUsers().toArray(new Object[0]);
                String memberName = (String) JOptionPane.showInputDialog(mainFrame, "Select user to add to chatroom: ", "Invite user",
                        JOptionPane.PLAIN_MESSAGE, null, users, users[0]);
                client.sendMessage(Message.inviteUser(client.getClientUsername(), memberName, getSelectedUser()));
            }

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

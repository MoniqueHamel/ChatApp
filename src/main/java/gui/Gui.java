package gui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Gui {

    private final Client client;
    private JFrame frame;
    private JTextArea messageBox;
    private JTextArea inputArea;
    private JButton sendMessageButton;
    JPanel northPanel;
    JPanel southPanel;

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

    public void display(){
        frame = new JFrame("ChatApp");
        frame.setLayout(new GridBagLayout());
        frame.setVisible(true);
        setupGui();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
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

        frame.add(northPanel, top);
        frame.add(southPanel, bottom);
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
        sendMessageButton = new JButton("Send Message");

        GridBagConstraints right = new GridBagConstraints();
        setGridBagConstraints(right, 1, 0, 0.0, 0.0, false);

        southPanel.add(sendMessageButton, right);

        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String text = inputArea.getText();
                String response = client.sendMessage(text);
                inputArea.setText("");
                messageBox.setText(messageBox.getText() + "\n" + text + "\n" + response);
            }
        });

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

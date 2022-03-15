package client;

import client.gui.Gui;
import common.Message;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private ExecutorService exec;
    Gui gui;

    public void startConnection(String ip, int port){
        try {
            clientSocket = new Socket(ip, port);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        gui = new Gui(this);
        gui.displayLoginView();

        exec = Executors.newSingleThreadExecutor();
        exec.execute(this::readMessage);
    }

    public void sendMessage(String msg){
        try {
            Message message = Message.createUserMessage(username, msg);
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessage(){
        try {
            Message message;
            try {
                while((message = (Message) in.readObject()) != null) {
                    switch (message.type){
                        case USER_MESSAGE:
                            gui.appendTextToMessageBox(message.sender + ": " + message.message);
                            break;
                        case USER_LEFT:
                            gui.appendTextToMessageBox(message.sender + " has left the chat!");
                            gui.removeUserFromActiveUserList(message.sender);
                            break;
                        case USER_JOINED:
                            gui.appendTextToMessageBox(message.sender + " has joined the chat!");
                            gui.addUserToActiveUserList(message.sender);
                            break;
                        case ADD_ACTIVE_USER:
                            gui.addUserToActiveUserList(message.sender);
                            break;
                    }
                }
            } catch (EOFException e){
                System.exit(0);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void stopConnection(){
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClientUsername(String username){
        this.username = username;
        sendMessage(username);
    }

    public String getClientUsername(){
        return username;
    }

    public static void main(String[] args){
        Client client = new Client();
        client.startConnection("localhost", 4444);
    }
}
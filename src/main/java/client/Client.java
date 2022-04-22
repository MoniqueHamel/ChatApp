package client;

import client.gui.LoginScreen;
import client.gui.MainScreen;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private ExecutorService exec;
    private Map<String, String> chatMap;
    MainScreen mainScreen;
    LoginScreen loginScreen;

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public void startConnection(String ip, int port){
        try {
            clientSocket = new Socket(ip, port);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        chatMap = new HashMap<>();
//        mainScreen = new MainScreen(this);
        loginScreen = new LoginScreen(this);
//        mainScreen.displayLoginView();

        exec = Executors.newSingleThreadExecutor();
        exec.execute(this::readMessage);
    }

    public void sendMessage(Message msg){
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUserMessage(String msg, String destination){
        sendMessage(Message.createUserMessage(username, destination, msg));
    }

    public void sendLoginDetails(String username, String password){
        sendMessage(Message.attemptLogin(username, password));
    }

    public void sendRegistrationDetails(String username, String password){
        sendMessage(Message.attemptRegistration(username, password));
    }

    public void readMessage(){
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Set<String>> setTypeReference = new TypeReference<>() {};
        try {
            Message message;
            try {
                while((message = (Message) in.readObject()) != null) {
                    log.info("{} message from {} to {}", message.type.name(), message.sender, message.destination);
                    switch (message.type){
                        case USER_MESSAGE:
                            String sender = message.sender.equals(username) ? "You" : message.sender;
                            appendMessageToChat(message.sender, message.destination, message.message);
                            if (isPrivateChatRoom(message.destination) && mainScreen.getSelectedUser().equals(message.destination)){
                                mainScreen.appendTextToMessageBox(sender + ": " + message.message);
                                continue;
                            }
                            if (mainScreen.getSelectedUser().equals(Message.GLOBAL)){
                                if (message.destination.equals(Message.GLOBAL)){
                                    mainScreen.appendTextToMessageBox(sender + ": " + message.message);
                                }
                            } else if (mainScreen.getSelectedUser().equals(sender)){
                                if(message.destination.equals(username)){
                                    mainScreen.appendTextToMessageBox(sender + ": " + message.message);
                                }
                            } else if (message.sender.equals(username)){
                                mainScreen.appendTextToMessageBox(sender + ": " + message.message);
                            }
                            break;
                        case USER_LEFT:
                            //gui.appendTextToMessageBox(message.sender + " has left the chat!");
                            if(!message.sender.equals(username)) {
                                mainScreen.removeUserFromActiveUserList(message.sender);
                            }
                            break;
                        case USER_JOINED:
                            //gui.appendTextToMessageBox(message.sender + " has joined the chat!");
                            if(!message.sender.equals(username)) {
                                mainScreen.addUserToActiveUserList(message.sender, true);
                            }
                            break;
                        case ADD_ACTIVE_USER:
                            mainScreen.addUserToActiveUserList(message.sender, true);
                            break;
                        case LOGIN_SUCCESSFUL:
                        case REGISTER_SUCCESSFUL:
                            setClientUsername(message.destination);
                            mainScreen = new MainScreen(this);
                            loginScreen.dispose();
                            break;
                        case LOGIN_FAILED:
                        case REGISTER_FAILED:
                            loginScreen.showLoginFailedMessage(message.message);
                            break;
                        case REGISTERED_USERS_LIST:
                            Set<String> registeredUsers = mapper.readValue(message.message, setTypeReference);
                            log.info("isEventDispatchThread = {}", SwingUtilities.isEventDispatchThread());
                            registeredUsers.forEach((user) -> {
                                mainScreen.addUserToActiveUserList(user, false);
                            });
                            break;
                        case CHATROOM_CREATED:
                            mainScreen.addUserToActiveUserList(message.message, true);
                            break;
                        case USER_CHATROOM_LIST:
                            Set<String> privateChatrooms = mapper.readValue(message.message, setTypeReference);
                            log.info("isEventDispatchThread = {}", SwingUtilities.isEventDispatchThread());
                            privateChatrooms.forEach((chatroomId) -> {
                                mainScreen.addUserToActiveUserList(chatroomId, true);
                            });
                            break;
                        case INVITE_USER:
                            mainScreen.addUserToActiveUserList(message.message, true);
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

    private boolean isPrivateChatRoom(String name) {
        return name != null && !name.equals(Message.GLOBAL) && name.startsWith("#");
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
//        sendMessage(username, null);
    }

    public String getClientUsername(){
        return username;
    }

    public void saveChat(String user, String text){
        chatMap.put(user, text);
    }

    public void appendMessageToChat(String username, String destination, String text){
        String sender = username.equals(this.username) ? "You" : username;
        if (destination.equals(Message.GLOBAL)){
            String chat = String.format("%s%s: %s\n", chatMap.get(Message.GLOBAL), sender, text);
            log.info("Append message \"{}\". From {} to {}.", text, username, destination);
            chatMap.put(Message.GLOBAL, chat);
        } else {
            String existingChat = chatMap.get(username) == null ? "" : chatMap.get(username);
            String chat = String.format("%s%s: %s\n", existingChat, sender, text);
            log.info("Append message \"{}\". From {} to {}.", text, username, destination);
            log.info("Chat: {}", chat);
            chatMap.put(username, chat);
            chatMap.put(destination, chat);
        }
    }

    public String loadChat(String user){
        return chatMap.get(user);
    }

    public static void main(String[] args){
        Client client = new Client();
        client.startConnection("localhost", 4444);
    }
}

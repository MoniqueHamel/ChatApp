package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class ClientHandler implements Runnable {
    private Server server;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isActive = true;
    private Jedis jedis = new Jedis("localhost", 6379);
    String username;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(Socket socket, Server server) {
        this.server = server;
        this.clientSocket = socket;
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    if (msg.type == Message.MessageType.LOGIN_MESSAGE) {
                        authenticateUser(msg);
                    } else if (msg.type == Message.MessageType.REGISTER_MESSAGE) {
                        registerUser(msg);
                    } else {
                        server.messageQueue.add(msg);
                    }
                }
            } catch (EOFException ignored) {
            }

            isActive = false;
            in.close();
            out.close();
            clientSocket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isClientActive() {
        return isActive;
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticateUser(Message msg) {
        if (server.checkLoginDetails(msg.sender, msg.message)) {
            if(!server.isUserOnline(msg.sender)){
                sendMessage(Message.loginSuccessful(msg.sender));
                username = msg.sender;
                log.info("User {} has logged in successfully.", msg.sender);
                server.registerClientHandler(this);
                sendMessage(Message.registeredUsersList(username, server.getRegisteredUsers()));
                server.messageQueue.add(Message.userJoined(username));

                List<String> chatroomList = getClientChatrooms();
                chatroomList.forEach((chatroom) -> {
                    log.info("Chatroom: {}", chatroom);
                    getChatHistory(chatroom).forEach(this::sendMessage);
                });


            } else {
                sendMessage(Message.loginFailed(msg.sender, "User already logged in."));
                log.info("User {} unable to log in. Reason: Already signed in.", msg.sender);
            }
        } else {
            sendMessage(Message.loginFailed(msg.sender, "Wrong username and/or password."));
            log.info("User {} unable to log in. Reason: Invalid credentials.", msg.sender);
        }

    }

    private List<String> getClientChatrooms(){
        Set<String> chatroomIdsSet = jedis.smembers("chatroomIds");
        List<String> clientChatroomList = new ArrayList<>();
        chatroomIdsSet.forEach((chatroomId)->{
            if (chatroomId.contains(username) || chatroomId.equals(Message.GLOBAL)) {
                clientChatroomList.add(chatroomId);
            }
        });
        return clientChatroomList;
    }

    private List<Message> getChatHistory(String chatroomId){
        List<Message> messageList = new ArrayList<>();
        List<String> jedisMessageList = jedis.lrange(chatroomId + "_chatHistory", 0, -1);
        jedisMessageList.forEach((message) -> {
            try {
                Message msg = mapper.readValue(message, Message.class);
                messageList.add(msg);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return messageList;
    }

    private void registerUser(Message msg) {
        if (server.checkRegistrationDetails(msg.sender)) {
            sendMessage(Message.registerSuccessful(msg.sender));
            username = msg.sender;
            String password = msg.message;
            log.info("User {} has registered successfully.", msg.sender);
            UserCredentials userCredentials = new UserCredentials(username, password);
            server.registerNewUser(userCredentials);
            server.registerClientHandler(this);
            sendMessage(Message.registeredUsersList(username, server.getRegisteredUsers()));
            server.messageQueue.add(Message.userJoined(username));
        } else {
            sendMessage(Message.registerFailed(msg.sender));
            log.info("User {} unable to register.", msg.sender);
        }
    }




}

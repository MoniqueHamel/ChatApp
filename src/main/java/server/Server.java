package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Chatroom;
import common.Message;
import common.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class Server{
    private ServerSocket serverSocket;
    private ExecutorService exec;
    private ScheduledExecutorService messageService;
    private ScheduledExecutorService clientChecker;
    private Map<String, ClientHandler> clientMap;
    private Map<String, UserCredentials> userProfileMap;
    private Map<String, Chatroom> chatroomMap = new HashMap<>();
    Queue<Message> messageQueue;
    private static final String USERS_FILE = "usersFile.txt";
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public void start(int port) {
        clientMap = new ConcurrentHashMap<>();
        exec = Executors.newCachedThreadPool();
        messageService = Executors.newSingleThreadScheduledExecutor();
        clientChecker = Executors.newSingleThreadScheduledExecutor();
        messageQueue = new ConcurrentLinkedQueue<>();

        readUserProfiles();

        clientChecker.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Iterator<String> iterator = clientMap.keySet().iterator();
                while(iterator.hasNext()){
                    String username = iterator.next();
                    ClientHandler handler = clientMap.get(username);
                    if(!handler.isClientActive()){
                        iterator.remove();
                        messageQueue.add(Message.userLeft(username));
                        log.info("User {} has disconnected.", username);
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        handleMessageQueue();

        handleNewClients(port);
    }

    private void handleMessageQueue(){
        messageService.scheduleAtFixedRate(() -> {
            if (!messageQueue.isEmpty()){
                Message message = messageQueue.remove();
                if(message.destination == null || message.destination.equals(Message.GLOBAL)){
                    if (Message.GLOBAL.equals(message.destination)){
                        Chatroom.addMessageToChatHistory(Message.GLOBAL, message);
                    }
                    clientMap.forEach((username, handler)->{
                        handler.sendMessage(message);
                        if (message.type == MessageType.USER_JOINED) {
                            if(!username.equals(message.sender)) {
                                clientMap.get(message.sender).sendMessage(Message.addActiveUser(username));
                            }
                        }
                    });
                } else {
                    String chatroomId = Chatroom.getChatroomIdFor(message.sender, message.destination);
                    boolean chatroomExists = Chatroom.doesChatroomExist(chatroomId);
                    log.info("Chatroom {} exists: {}", chatroomId, chatroomExists);
                    if (!chatroomExists){
                        Chatroom.createChatroom(chatroomId, Arrays.asList(message.sender, message.destination));
                    }
                    Chatroom.addMessageToChatHistory(chatroomId, message);
                    sendDirectMessage(message, message.sender, message.destination);
                }

            }
        },0,100, TimeUnit.MILLISECONDS);

    }

    private void sendDirectMessage(Message msg, String sender, String destination){
        clientMap.get(sender)
                .sendMessage(msg);
        clientMap.get(destination)
                .sendMessage(msg);
    }

    private void handleNewClients(int port){
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                ClientHandler newClientHandler = new ClientHandler(serverSocket.accept(), this);
                exec.execute(newClientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        exec.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkLoginDetails(String username, String password){
        return userProfileMap.containsKey(username) && password.equals(userProfileMap.get(username).password);
    }

    public boolean isUserOnline(String username){
        return clientMap.containsKey(username);
    }

    public boolean checkRegistrationDetails(String username){
        return !userProfileMap.containsKey(username);
    }

    private void saveUsers(){
        try {
            List<String> userJsons = new ArrayList<>();
            userProfileMap.forEach((username, userCredentials) -> {
                try {
                    userJsons.add(mapper.writeValueAsString(userCredentials));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
            Files.write(Paths.get(USERS_FILE), userJsons);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readUserProfiles(){
        userProfileMap = new HashMap<>();
        if (!Files.exists(Paths.get(USERS_FILE))) return;
        try {
            List<String> userJsons = Files.readAllLines(Paths.get(USERS_FILE));
            List<String> usernames = new ArrayList<>();
            userJsons.forEach((json) -> {
                try {
                    UserCredentials userCredentials = mapper.readValue(json, UserCredentials.class);
                    userProfileMap.put(userCredentials.username, userCredentials);
                    usernames.add(userCredentials.username);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
            Chatroom.createChatroom(Message.GLOBAL, usernames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerClientHandler(ClientHandler ch){
        clientMap.put(ch.username, ch);
    }

    public void registerNewUser(UserCredentials userCredentials){
//        jedis.sadd("chatroomIds", Message.GLOBAL);
        Chatroom.addUserToChatroom(Message.GLOBAL, userCredentials.username);
        userProfileMap.put(userCredentials.username, userCredentials);
        saveUsers();
    }

    public String getRegisteredUsers(){
        try {
            return mapper.writeValueAsString(userProfileMap.keySet());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args){
        Server server = new Server();
        server.start(4444);
    }
}


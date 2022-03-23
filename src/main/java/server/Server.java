package server;

import client.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Message;
import common.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Server{
    private ServerSocket serverSocket;
    private ExecutorService exec;
    private ScheduledExecutorService messageService;
    private ScheduledExecutorService clientChecker;
    private Map<String, ClientHandler> clientMap;
    private Map<String, User> userProfileMap;
    Queue<Message> messageQueue;
    private static final String USERS_FILE = "usersFile.json";
    private static final Logger log = LoggerFactory.getLogger(Server.class);

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
                if(message.destination == null || message.destination.equals("Global")){
                    clientMap.forEach((username, handler)->{
                        handler.sendMessage(message);
                    });
                } else {
                    clientMap.get(message.sender)
                                    .sendMessage(message);
                    clientMap.get(message.destination)
                            .sendMessage(message);
                }

            }
        },0,100, TimeUnit.MILLISECONDS);

    }

    private void handleNewClients(int port){
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                ClientHandler newClientHandler = new ClientHandler(serverSocket.accept(), this);
                clientMap.put(newClientHandler.username, newClientHandler);
                clientMap.forEach((username, handler) -> {
                    handler.sendMessage(Message.userJoined(newClientHandler.username));
                    if(!username.equals(newClientHandler.username)) {
                        newClientHandler.sendMessage(Message.addActiveUser(username));
                    }
                    log.info("User {} has connected.", newClientHandler.username);
                });
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

    private static class ClientHandler implements Runnable {
        private Server server;
        private Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean isActive = true;
        String username;

        public ClientHandler(Socket socket, Server server){
            this.server = server;
            this.clientSocket = socket;
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
//                username = ((Message) in.readObject()).sender;
                authenticateUser();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            try {
                try {
                    while(true){
                        server.messageQueue.add((Message) in.readObject());
                    }
                } catch(EOFException ignored){}

                isActive = false;
                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public boolean isClientActive(){
            return isActive;
        }

        public void sendMessage(Message message){
            try {
                out.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void authenticateUser(){
            try {
                Message msg = (Message) in.readObject();
                if (msg.type == MessageType.LOGIN_MESSAGE){
                    if (server.checkLoginDetails(msg.sender, msg.message)){
                        sendMessage(Message.loginSuccessful(msg.sender));
                        username = msg.sender;
                        log.info("User {} has logged in successfully.", msg.sender);
                    } else {
                        sendMessage(Message.loginFailed(msg.sender));
                        log.info("User {} unable to log in.", msg.sender);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private void saveUsers(List<User> listOfUsers){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(USERS_FILE), listOfUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readUserProfiles(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            TypeReference<List<User>> typeRef = new TypeReference<List<User>>() {};
            List<User> list = mapper.readValue(Paths.get(USERS_FILE).toFile(), typeRef);
            userProfileMap = list.stream().collect(Collectors.toMap(User::getUsername, User->User));
            userProfileMap.forEach((username, user)->{
                System.out.println(username);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Server server = new Server();
        server.start(4444);
    }
}


package server;

import common.Message;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class Server{
    private ServerSocket serverSocket;
    private ExecutorService exec;
    private ScheduledExecutorService messageService;
    private ScheduledExecutorService clientChecker;
    private Map<String, ClientHandler> clientMap;
    Queue<Message> messageQueue;

    public void start(int port) {
        clientMap = new ConcurrentHashMap<>();
        exec = Executors.newCachedThreadPool();
        messageService = Executors.newSingleThreadScheduledExecutor();
        clientChecker = Executors.newSingleThreadScheduledExecutor();
        messageQueue = new ConcurrentLinkedQueue<>();

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
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        messageService.scheduleAtFixedRate(() -> {
            if (!messageQueue.isEmpty()){
                Message message = messageQueue.remove();
                System.out.println(message);
                clientMap.forEach((username, handler)->{
                    handler.sendMessage(message);
                });
            }
        },0,100, TimeUnit.MILLISECONDS);

        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                ClientHandler newClientHandler = new ClientHandler(serverSocket.accept(), this);
                clientMap.put(newClientHandler.username, newClientHandler);
                System.out.println(clientMap.size());
                clientMap.forEach((username, handler) -> {
                    handler.sendMessage(Message.userJoined(newClientHandler.username));
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
                username = ((Message) in.readObject()).sender;
            } catch (IOException | ClassNotFoundException e) {
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
    }

    public static void main(String[] args){
        Server server = new Server();
        server.start(4444);
    }
}

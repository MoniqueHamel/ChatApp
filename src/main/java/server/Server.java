package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService exec;

    public void start(int port) {
        exec = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port);
            while (true)
                exec.execute(new ClientHandler(serverSocket.accept()));
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
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket){
            this.clientSocket = socket;
        }

        public void run(){
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while((inputLine = in.readLine()) != null){
                    if ("quit".equals(inputLine)){
                        out.println("goodbye!");
                        break;
                    }
                    out.println(inputLine);
                }

                in.close();
                out.close();
                clientSocket.close();

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

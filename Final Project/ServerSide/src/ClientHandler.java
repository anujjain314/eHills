/*
 * EE422C Final Project submission by
 * Anuj
 * aaj2447
 * 17805
 * Fall 2021
 * Slip days used: 2
 */

import java.util.Observable;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Observer;

public class ClientHandler implements Runnable, Observer {

    protected int clientNum;
    private final Server server;
    protected Socket clientSocket;
    protected BufferedReader fromClient;
    protected PrintWriter toClient;

    // ClientHandler constructor
    protected ClientHandler(Server server, Socket clientSocket, int clientNum) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientNum = clientNum;
        try {
            toClient = new PrintWriter(this.clientSocket.getOutputStream());
            fromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Sends command to client
    protected void sendToClient(String commandString) {
        System.out.println("Sending to client: " + commandString);
        toClient.println(commandString);
        toClient.flush();
    }

    @Override
    public void run() {
        String input;
        String output;
        try {
            while ((input = fromClient.readLine()) != null) {
                System.out.println("Received from client: " + input);
                output = server.processRequest(input);
                sendToClient(output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection no longer exists for Client #" + clientNum + ".");
        }
    }

    @Override
    public void update(Observable o, Object arg) { this.sendToClient((String) arg); }
}
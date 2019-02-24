import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    /* ChatClient constructor
     * @param server - the ip address of the server as a string
     * @param port - the port number the server is hosted on
     * @param username - the username of the user connecting
     */
    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /**
     * Attempts to establish a connection with the server
     * @return boolean - false if any errors occur in startup, true if successful
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("Server might not be running or, is currently down for maintenance!");
            return false;
        }

        // Attempt to create output stream
        try {
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Attempt to create input stream
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Create client thread to listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /*
     * Sends a string to the server
     * @param msg - the message to be sent
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            return;
        }
    }



    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        ChatClient client;
        if(args.length==3){
            client = new ChatClient(args[2], Integer.parseInt(args[1]), args[0]);
        }
        else if(args.length==2){
            client = new ChatClient("localhost", Integer.parseInt(args[1]), args[0]);
        }
        else if(args.length==1){
            client = new ChatClient("localhost", 1500, args[0]);
        }
        else{
            client = new ChatClient("localhost", 1500, "CS 180 Student");
        }

        // Create your client and start it

        client.start();

        // Send an empty message to the server
        ChatMessage Chatobj = new ChatMessage(10, " ", " ");
        Scanner s = new Scanner(System.in);

        try{
            while(client.socket.isConnected()){

                String input = s.nextLine();
                String[] arr = input.split(" ");

                if(arr[0].equals("/logout")){
                    System.out.println("You have been logged out.");
                    Chatobj=new ChatMessage(1, "has logged out.");
                    client.sendMessage(Chatobj);
                    try{
                        client.sInput.close();
                        client.sOutput.close();
                        client.socket.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }

                else if(arr[0].equalsIgnoreCase("/list")){
                    Chatobj=new ChatMessage(3);
                }

                else if(arr[0].equalsIgnoreCase("/msg")){
                    if(arr.length==1){
                        System.out.println("Provide a user for DM followed by a message!");
                    }
                    else if(arr.length==2){
                        System.out.println("Provide a message for user!");
                    }
                    else{
                        String sentance="";
                        for(int i=2; i<arr.length; i++){
                            sentance+=arr[i]+" ";
                        }
                        Chatobj=new ChatMessage(2, sentance, arr[1]);
                    }
                }

                else if(arr[0].equalsIgnoreCase("/ttt")){
                    if(arr.length==1){
                        System.out.println("Provide a user to start a ttt!");
                    }
                    else if(arr.length==2){
                        Chatobj=new ChatMessage(4, " ", arr[1]);
                    }
                    else if(arr.length==3){
                        Chatobj=new ChatMessage(4, arr[2], arr[1]);
                    }
                    else{
                        System.out.println("Invalid input!");
                    }

                }
                else{
                    String sentance="";
                    for(int i=0; i<arr.length; i++){
                        sentance+=arr[i]+" ";
                    }
                    Chatobj=new ChatMessage(0, sentance);
                }

                client.sendMessage(Chatobj);
            }
            System.out.println("Server has shut down.");
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }

    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while(socket.isConnected()){
                try {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                } catch (IOException | ClassNotFoundException e) {
                    return;
                }
            }

        }
    }
}
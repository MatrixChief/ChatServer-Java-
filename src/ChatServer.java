import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

final class ChatServer {
    private static int uniqueId = 0;
    // Data structure to hold all of the connected clients
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;			// port the server is hosted on
    private static int count;

    /**
     * ChatServer constructor
     * @param port - the port the server is being hosted on
     */
    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true){
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                if(clients.size()==0){
                    clients.add((ClientThread) r);
                    Thread t = new Thread(r);
                    t.start();
                }
                else if(checkValidity((ClientThread) r, clients)){
                    clients.add((ClientThread) r);
                    Thread t = new Thread(r);
                    t.start();
                }
                else{
                    ((ClientThread) r).close();
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized boolean checkValidity(ClientThread clientThread, List<ClientThread> clients){
        for(ClientThread thread : clients){
            if(thread.username.equals(clientThread.username)){
                return false;
            }
        }
        return true;
    }

    private synchronized void broadcast(String message){
        System.out.println(generateDate()+"   "+message);

        for(ClientThread clientThread : clients){
            clientThread.writeMessage(message);
        }
    }

    private synchronized void remove(int id){
        for(int i=0; i<clients.size(); i++){
            if(clients.get(i).id==id){
                clients.remove(i);
            }
        }
    }


    private synchronized void directMessage(String message, String username, String recipient){
        message = username+"-->"+recipient+": "+message;
        int count=0;
        for(ClientThread clientThread : clients){
            if(!clientThread.username.equals(recipient)){
                count++;
                if(count==clients.size()){
                    for(ClientThread thread : clients){
                        if(thread.username.equals(username)){
                            System.out.println(generateDate()+"   "+username+" tried to dm a user not on the server!");
                            thread.writeMessage("User not found!");
                        }
                    }
                }
            }
            else{
                System.out.println(generateDate()+"   "+message);
                clientThread.writeMessage(message);
                for(ClientThread thread : clients){
                    if(thread.username.equals(username)){
                        thread.writeMessage(message);
                    }
                }
            }
        }
    }

    private synchronized String getList(String username){
        String list="List of users:\n";
        int count=0;
        for(ClientThread clientThread : clients){
            if(!clientThread.username.equalsIgnoreCase(username)){
                count++;
                list += Integer.toString(count)+") "+clientThread.username+"\n";
            }
        }
        System.out.println(generateDate()+"   "+username+" called list");
        return generateDate()+"   "+list;
    }

    private synchronized String generateDate(){
        SimpleDateFormat date = new SimpleDateFormat("EEE MMM dd HH:mm:ss");
        return date.format(new Date());
    }

    private synchronized TicTacToeGame startTTT(String username, String recipient){
        return new TicTacToeGame(username,recipient);
    }

    private synchronized void playTTT(String username, String recipient, String turn){
        TicTacToeGame tttGame=null;
        if (this.count==0){
            tttGame=startTTT(username,recipient);
            count++;
        }
        else{

        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server;
        if(args.length==1){
            server = new ChatServer(Integer.parseInt(args[0]));
        }
        else if(args.length==0){
            server = new ChatServer(1500);
        }
        else{
            server = new ChatServer(1500);
        }

        server.start();
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;                  // The socket the client is connected to
        ObjectInputStream sInput;       // Input stream to the server from the client
        ObjectOutputStream sOutput;     // Output stream to the client from the server
        String username;                // Username of the connected client
        ChatMessage cm;                 // Helper variable to manage messages
        int id;

        /*
         * socket - the socket the client is connected to
         * id - id of the connection
         */
        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                this.username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private boolean writeMessage(String msg){
            if(this.socket.isConnected()){
                try {
                    sOutput.writeObject(generateDate()+"   "+msg+"\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            else{
                return false;
            }
        }

        private void close(){
            try{
                this.socket.close();
                this.sInput.close();
                this.sOutput.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client

            System.out.println(generateDate()+"   "+this.username +" has connected to the server.");
            try {
                sOutput.writeObject(generateDate()+"   "+"You have connected to the server.\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            while(this.socket.isConnected()){
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    return;
                }

                if(cm.getType()==0){
                    broadcast(this.username+": "+cm.getMsg());
                }
                else if(cm.getType()==1){
                    remove(this.id);
                    broadcast(this.username+" "+cm.getMsg());
                    this.close();
                }
                else if(cm.getType()==2){
                    if(this.username.equals(cm.getRecipient())){
                        try{
                            sOutput.writeObject(generateDate()+"   "+"You cannot DM yourself.");
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        directMessage(cm.getMsg(), this.username, cm.getRecipient());
                    }
                }
                else if(cm.getType()==3){
                    try{
                        sOutput.writeObject(getList(this.username));
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
                else if(cm.getType()==4){
                    playTTT(this.username,cm.getRecipient(),cm.getMsg());
                }
            }
        }
    }
}
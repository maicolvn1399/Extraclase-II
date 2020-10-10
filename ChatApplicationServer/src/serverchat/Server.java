/**
 * Michael Valverde Navarro
 * Carné: 2020044189
 * Curso: Algoritmos y estructuras de datos I
 * 2 semestre 2020
 *
 */
package serverchat;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import javax.swing.JOptionPane;

/**
 * class to handle the connection from the server's side
 * @author Michael Valverde
 */
public class Server extends Thread{
    
    /**
     * SockerServer that has the function to listen when a client is connected to include them in the chat 
     */
    private ServerSocket serverSocket;
    
    /**
     * List of all the threads of communications, for each client there's an instance of 
     * one of those threads
     */
    LinkedList<ClientThread> clients;
    
    /**
     * Variable to store the window that manages the GUI of the server 
     */
    private final WindowServer window;
    
    /**
     * Variable to store the port that the server will use to listen 
     */
    private final String port;
    
    /**
     * Variable to diferentiate the multiple clients that connect, if two users connect with the same user name 
     * this variable will diferentiate them 
     */
    static int correlative;
    
    /**
     * Constructor of the Server 
     * @param port 
     * @param window
     */
    public Server(String port, WindowServer window){
        correlative = 0;
        this.port = port;
        this.window = window;
        clients = new LinkedList<>();
        this.start();
        
    }//End constructor 
    
    /**
     * This method runs on the infinite loop that has as a function to listen permanently waiting for new 
     * connections
     */
    public void run(){
        try{
            serverSocket = new ServerSocket(Integer.valueOf(port));
            window.addInitializedServer();
            while(true){
                ClientThread h;
                Socket socket;
                socket = serverSocket.accept();
                System.out.println("New connection incoming" + socket);
                h = new ClientThread(socket, this);
                h.start();
                
            }//End while 
        }catch(Exception ex){
            JOptionPane.showMessageDialog(window, "The server could not start\n"
                    + "you might have entered an invalid port\n"
                    + "This application will close");
            System.exit(0);
        }//End catch 
    }//End run()
    
    /**
     * Loop that returns a list with IDs of all clients that are connected 
     * @return 
     */
    LinkedList<String> getConnectedUsers(){
        LinkedList<String> connectedUsers = new LinkedList<>();
        clients.stream().forEach(c -> connectedUsers.add(c.getID()));
        return connectedUsers;
    }//End getConnectedUsers()
    
    /**
     * Method that adds a line to the log of the GUI of the server 
     * @param text 
     */
    void addLog(String text){
        window.addLog(text);
    }//End addLog()

    
}//End class 


/**
 * Michael Valverde Navarro
 * Carné: 2020044189
 * Curso: Algoritmos y estructuras de datos I
 * 2 semestre 2020
 *
 */
package clientchat;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import javax.swing.JOptionPane;

/**Java class used for the client
 *
 * @author Michael Valverde 
 */
public class Client extends Thread{
    
    /**
    *Socket used to communicate with the server
    */
    private Socket socket;
    
    /**
    *Stream for sending messages to the server
    */
    private ObjectOutputStream objectOutputStream;
    
    /**
     * Stream for receiving messages from the server or client
     */
    private ObjectInputStream objectInputStream;
    
    /**
     * Window used as GUI for the client 
     */
    private WindowClient window;
    
    /**
     * ID to identify the client inside the chat
     */
    private String ID;
    
    /**
     * Variable used to determine if the client is listening or not to the server, it runs once the thread with the client starts
     */ 
    private boolean listening;
    
    /**
     * Variable that stores the IP of the host of the server 
     */
    private String host;
    
    /**
     * variable that stores the port in which the server listens to the clients
     */
    private int port;
    
    /**
    * Class Constructor
    * @param window
    * @param host 
    * @param port
    * @param name
    */
    
    Client(WindowClient window,String host, int port, String name){
        this.window = window;
        this.host = host;
        this.port = port;
        this.ID = name;
        listening = true;
        this.start();
        
    }//End class constructor 
    
    /**
    * Method to run the thread of communication of the client's side
    */
    
    public void run(){
        try{
            socket = new Socket(host,port);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Successfull connection");
            this.sendConnectionRequest(ID);
            this.listen();
            
            
        }catch(UnknownHostException ex){
            JOptionPane.showMessageDialog(window, "Server unknown, you may not have entered a valid IP\n"
                    + "or the server is not running \n"
                    + "This app will shut down");
            System.exit(0);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(window, "Input/Output error, you may not have entered a valid IP\n"
                    + "or you entered a invalid port\n"
                    + "The server may not be running\n"
                    + "This app will shut down");
            
            System.exit(0);
            
        }//End catch
    }//End run()
    
    /**
    *Method that closes the socket and the streams of communication
    */
    public void disconnect(){
        try{
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
            listening = false;
            
        }catch(Exception ex){
            System.err.println("Error while closing the elements of communication");
        }//End catch
        
    }//End disconnect()
    
    /**
    *Method to send a message to some other client to the server 
    *@param receiving_client
    *@param message 
    */
    public void sendMessage(String receiving_client, String message){
        LinkedList<String> list = new LinkedList<>();
        //Type
        list.add("MESSAGE");
        //sender 
        list.add(ID);
        //receiving client 
        list.add(receiving_client);
        //Message to send
        list.add(message);
        
        try{
            objectOutputStream.writeObject(list);
            
        }catch(IOException ex){
            System.out.println("Writing or reading error when trying to send the message to the server");
        }//End catch
    }//End sendMessage()
    
    /**
    *Method that listens to the server 
    */
    public void listen(){
        try{
            while(listening){
                Object aux = objectInputStream.readObject();
                if(aux != null){
                    if(aux instanceof LinkedList){
                        //if a linkedList is received then this method is executed
                        execute((LinkedList<String>)aux);
                    }else{
                        System.err.println("An unknown object has been sent to the socket");
                    }//End else
                    
                }else{
                    System.err.println("A null has been received through the socket");
                }//End else
                
                
            }//End while
            
        }catch(Exception e ){
            JOptionPane.showMessageDialog(window, "The connection with the server has been lost\n"
                    + "This chat will end\n"
                    + "The will shut down");
            System.exit(0);
            
        }//End catch
    }//End listen()
    
    /**
    *Method that executes instructions according to the message that the client gets from the server
    *@param list
    */
    
    public void execute(LinkedList<String> list){
        // 0 - First element on a list is the type 
        String type = list.get(0);
        switch(type){
            case "CONNECTION_ACCEPTED":
                // 1 - ID of the new user 
                // 2 ... n //IDs of the clients currently online 
                ID = list.get(1);
                window.initializedSession(ID);
                for (int i = 2; i < list.size(); i++) {
                    window.addContact(list.get(i));
                }//End for
                break;
            case "NEW_USER_CONNECTED":
                // 1 - ID of the user that just connected
                window.addContact(list.get(1));
                break;
            case "DISCONNECTED_USER":
                // 1 - ID of the user that just connected
                window.deleteContact(list.get(1));
                break;
            case "MESSAGE":
                // 1 - sender 
                // 2 - receiver 
                // 3 - message 
                window.addMessage(list.get(1),list.get(3));
                break;
            default:
                break;
        }//End switch
    }//End execute()
    
    /**
    *Method that makes the server ask for permission in order to add the new client to the list of clients
    *@param ID
    *
    */
    
    private void sendConnectionRequest(String ID){
       LinkedList<String> list = new LinkedList<>();
       //type 
       list.add("CONNECTION_REQUEST");
       // requesting client 
       list.add(ID);
       try{
           objectOutputStream.writeObject(list);
       }catch(IOException ex){
           System.out.println("Error reading and writing message to the server");
       }//End catch
    }//End sendConnectionRequest()
    
    
    /**
    *When the client window is closed, the server has 
    *to be notified that some client has disconnected
    *so it can delete that client from the clients list 
    *and all the other remaining clients can delete that client 

    */
    
    void confirmDisconnection(){
        LinkedList<String> list = new LinkedList<>();
        //type 
        list.add("DISCONNECTION_REQUEST");
        //Requesting client 
        list.add(ID);
        try{
            objectOutputStream.writeObject(list);
        }catch(IOException ex){
            System.out.println("Error reading and writing message to the server");
        }//End catch
    }//End confirmDisconnection()
    
    /**
    *Method that returns the ID of the client 
    */
    String getID(){
        return ID;
    }//End getID()
    
    

    
}//End client class

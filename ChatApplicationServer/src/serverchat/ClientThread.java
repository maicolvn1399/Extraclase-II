/**
 * Michael Valverde Navarro
 * Carné: 2020044189
 * Curso: Algoritmos y estructuras de datos I
 * 2 semestre 2020
 *
 */
package serverchat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *Class that makes threads, these threads listen to what the clients say 
 * there's a thread for every client connected to a server 
 *each thread can listen to one specific client
 * @author Michael Valverde 
 */
public class ClientThread extends Thread{
    
    /**
     * Socket used for communication
     */
    private final Socket socket;
    
    /**
     * Stream to send objects
     */
    private ObjectOutputStream objectOutputStream;
    
    /***
     * Stream to receive objects 
     */
    private ObjectInputStream objectInputStream;
    
    /**
     * Server that belongs to this thread 
     */
    private Server server;
    
    /**
     * ID of the client in which the client uses to communicate
     */
    private String ID;
    
    /**
     * Boolean that is true when the thread is listening for new connections, otherwise is false 
     */
    private boolean listening;

    /**
     * Attribute for the logger
     */
    private static Logger log = LoggerFactory.getLogger(ClientThread.class.getClass());


    /**
     * Constructor of this class
     * @param socket
     * @server
     */
    public ClientThread(Socket socket, Server server){
        this.server = server;
        this.socket = socket;
        try{
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            log.info("Ejecutando try del método ClientThread()");
            
        }catch(IOException ex){
            log.error(ex.getMessage(),ex);
        }//End catch
    }//End constructor 
        
        /**
         * Method in charge to close the socket that is communicating
         */
        public void disconnect(){
            try{
                socket.close();
                listening = false;
                log.info("Ejecutando try del método disconnect()");
                
            }catch(IOException ex){
                log.error(ex.getMessage(),ex);
                
        }//End catch
    }//End disconnect()
        
        /**
         * Overwrite of the Thread method, here's where the infinite cycle is assembled
         */
        public void run(){
            try{
                listen();
                log.info("Ejecutando try del método run()");
            }catch(Exception ex){
                log.error(ex.getMessage(),ex);
            }//End catch
            disconnect();
        }//End run()
        
        /**
         * Method that is constantly listening to everything that is sent by the client 
         */
        public void listen(){
            listening = true;
            while(listening){
                try{
                    Object aux=objectInputStream.readObject();
                    if(aux instanceof LinkedList){
                        execute((LinkedList<String>)aux);
                        log.info("Ejecutando bloque try del método listen()");
                    }//End if 
                }catch(Exception e){
                    log.error(e.getMessage(),e);
                }//End catch
            }//End while
        }//End listen()
        
        /**
         * Method that makes different actions according to what the socket
         * has received
         * @param list
         */
        public void execute(LinkedList<String> list){
            // 0 - the first element is the type 
            String type = list.get(0);
            switch(type){
                case "CONNECTION_REQUEST":
                    // 1 - ID of the new user
                    confirmConnection(list.get(1));
                    break;
                case "DISCONNECTION_REQUEST":
                    // 1 - ID of the new user
                    confirmDisconnection();
                    break;
                case "MESSAGE":
                    // 1 - Sender 
                    // 2 - Receiver 
                    // 3 - Message
                    String receiver = list.get(2);
                    server.clients
                            .stream()
                            .filter(h -> (receiver.equals(h.getID())))
                            .forEach((h) -> h.sendMessage(list));
                    break;
                default:
                    break;
       
            }//End switch 
        }//End execute 
        
        /**
         * Method to send a message through the socket 
         * @param list 
         */
        private void sendMessage(LinkedList<String> list){
            try{
                objectOutputStream.writeObject(list);
                log.info("Ejecutando bloque try de sendMessage()");
            }catch(Exception ex){
                log.error(ex.getMessage(),ex);

            }//End catch
        }//End sendMessage()
        
        /**
         * Once a new client is connected, this method notifies all the clients in the room 
         * that there's a new client to be added to their contacts
         * @param ID
         */
        private void confirmConnection(String ID){
            Server.correlative++;
            this.ID = Server.correlative+" - "+ID;
            LinkedList<String> list = new LinkedList<>();
            list.add("CONNECTION_ACCEPTED");
            list.add(this.ID);
            list.addAll(server.getConnectedUsers());
            sendMessage(list);
            server.addLog("\nNEW_USER_CONNECTED: "+this.ID);
            //send all the clients the name of the new user connected except for the user himself
            LinkedList<String> auxList = new LinkedList<>();
            auxList.add("NEW_USER_CONNECTED");
            auxList.add(this.ID);
            server.clients
                    .stream()
                    .forEach(client -> client.sendMessage(auxList));
            server.clients.add(this);
            
                  
        }//End confirmConnection
        
        /**
         * Method that returns the ID of the client
         * @return 
         */
        public String getID(){
            return ID;
        }//End getID()
        
        /**
         * Method that invokes when the user wants to leave the chat, if this happen, it has to 
         * inform the rest of the users that they cannot send messages to that client
         */
        private void confirmDisconnection(){
            LinkedList<String> auxList = new LinkedList<>();
            auxList.add("DISCONNECTED_USER");
            auxList.add(this.ID);
            server.addLog("\nThe client \""+this.ID+"\" has disconnected");
            this.disconnect();
            for(int i = 0; i<server.clients.size();i++){
                if(server.clients.get(i).equals(this)){
                    server.clients.remove(i);
                    break;
                }//End if 
            }//End for 
            server.clients
                    .stream()
                    .forEach(h -> h.sendMessage(auxList));
        }//End confirmDisconnection()
                
                
}//End class 

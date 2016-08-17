/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yustchang;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;


/**
 *
 * @author boyco
 */

public class IPC_Client_Initialize {

    private GUI_Model model;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    public int initialSocket; 
    public int newSocket;
    private static final String IP_ADDRESS = "localhost";
    
    public IPC_Client_Initialize(GUI_Model model){

        this.model = model;
        ini();
        
    }
    
    private void ini(){
          
        socket = new Socket();
          
        initialSocket = 2000;          
        newSocket = 2500;
    }
    
    private void print(String input){
    
        System.out.println(input);
    
    }
    
    public void setupMRICommandSocket(){
      
        try {
  
            InetAddress address = InetAddress.getByName(IP_ADDRESS);
            InetSocketAddress socketAddress = new InetSocketAddress(address,initialSocket);
            //  InetSocketAddress socketAddress = new InetSocketAddress("10.0.1.1",15555);
            socket.connect(socketAddress);
            out = new DataOutputStream(socket.getOutputStream());
        
            print(" connected to server !!");

           }catch(Exception e){
           System.out.println(e);
           socket = new Socket();
       } 

        
    }
    
    
    public void setupMRIDataSocket(){

        // let MRI send data to here
        try{
            
            out.writeUTF("INIT 56267A110651\n");
            print("Connection command was sent to MRI");
                
            out.writeUTF("CONN 1 "+ Integer.toString(newSocket) +"\n");
            // print("New socket command was sent to MRI");
            
        } catch(Exception e){
           e.printStackTrace();
        }
           
    }
    
    public void sendSTARTMessage(){
     
        try{
            
            out.writeUTF("START 1\n");
            print("START command was sent to MRI");
            
        } catch(Exception e){
           e.printStackTrace();
        }
    
    }


}

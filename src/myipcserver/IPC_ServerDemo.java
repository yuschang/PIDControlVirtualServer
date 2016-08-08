/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myipcserver;

import javax.swing.SwingUtilities;

/**
 *
 * @author boyco
 */
public class IPC_ServerDemo {
    
    public static void main(String args[]){

   
        SwingUtilities.invokeLater(new Runnable(){
              public void run(){

                IPC_ServerGUI clientGUI = new IPC_ServerGUI();  
                IPC_ServerControl SeverControl = new IPC_ServerControl(clientGUI);

                clientGUI.setController(SeverControl);

            }



      });
    
              

 

    }
    
}

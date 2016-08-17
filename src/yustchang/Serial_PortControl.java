/*

 * this controller is wrote for comunicate with arduino board.
 */
package yustchang;

import com.fazecast.jSerialComm.*;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author boyco
 */
public class Serial_PortControl {
    
    private GUI_Model model;
    private SerialPort comPort;
    private OutputStream serialOut;
            
    private PrintWriter serialStringOut;
    
    public Serial_PortControl(){
    
    this.model = model;
    
    }
    
    private void print(String input){
        System.out.println(input);
    }
    
    public void connectSerial(){
   
        try{
        
        
        comPort =  SerialPort.getCommPorts()[0];
        print("!! Serial Port: "+ comPort.getDescriptivePortName());   
        print("!! Serial Port: "+ comPort.getSystemPortName());
      
        comPort.openPort();
        comPort.setBaudRate(9600);
        serialOut = comPort.getOutputStream();
  
        
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 10, 0);
        } catch(Exception e){
        
            print("Error: Serial Port is not connected!!");
        }
       }
   
    
    public void serialSendMessage(double input){
        
   
        
        try{
          
            String strVal = Integer.toString((int)Math.round(input));
            
            strVal = strVal.concat("\n");
            
         //    serialOut.write(strVal.getBytes("ISO-8859-1"));
            serialOut.write(strVal.getBytes("utf-8"));
   
            print("!! Serial: Serial was sent out: _"+ strVal+"_");
   

        } catch(Exception e){
        
            print("!! Serial: fail to sent out data!");
        
        }
        
   
    }
    
    public void closeSerial(){
    
        comPort.closePort();
    
    }
    
    
}




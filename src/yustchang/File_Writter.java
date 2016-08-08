/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yustchang;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author boyco
 */
public class File_Writter {
    

    private GUI_Model model;
    
    private String [] stringData1D;
    private BufferedWriter bw;

    public double [] currentLog;
            
    File actualFile; // file address
    
    public File_Writter(GUI_Model model){     
        
        this.model = model;
        
        stringData1D = new String [10];
        currentLog = new double [10];
    } 
    
    private void print(String input){
    
        System.out.println(input);
    }

    
    
    
            
    public void logfileOgnizer(String dirName, String fileName){
    
           // String dirName = "C:\\Users\\boyco\\Desktop\\";
           //  String fileName = "arrayData1D.txt";     
            File dir = new File (dirName);
            actualFile = new File (dir, fileName);
            
    }
            
    
        
    public void logDataConstructor(){
    
         
     // currentLog[0] = model.roi_start_point;
     // currentLog[1] = model.roi_window_size;
     // currentLog[2] = model.max_temp;
     // currentLog[3] = model.mean_temp; 
     
       /*
     model.currentLogData[4] = model.pGain;
     model.currentLogData[5] = model.iGain;
     model.currentLogData[6] = model.pidOutput;
      */  
    }
   
    
    public void double2String1D (){
       
        for (int i =0; i < currentLog.length; i++ ){
                  String mystring  = String.valueOf(currentLog[i]);
                  stringData1D[i] = mystring;
        }

    }
  

    
    public void logSaveData(){
        
        //  logDataConstructor()
        currentLog[0] = model.roi_start_point;
        currentLog[1] = model.roi_window_size;
        currentLog[2] = model.targetTemp;
        currentLog[3] = model.targetDose; 
        currentLog[4] = model.max_temp;
        currentLog[5] = model.mean_temp;
        currentLog[6] = model.thermalDose;
        currentLog[7] = model.pidOutput;        
        currentLog[8] = model.pGain;
        currentLog[9] = model.iGain;        
                
        // double2String1D ()
        for (int i =0; i < currentLog.length; i++ ){
                  String mystring  = String.valueOf(currentLog[i]);
                  stringData1D[i] = mystring;
        }
        
        // logDataConstructor();
        // double2String1D();
        
                    try {
                        print("Attempting To Save Array Contents To File...");
                        bw = new BufferedWriter(new FileWriter(actualFile, true));

                        for(int i = 0; i < stringData1D.length; i++){           
                                bw.write(" " + stringData1D[i]);
                                // System.out.println("[File Writer] here is the numbers "+ stringData1D[i]);
                              //  bw.newLine();  // More Platform-independent that using write("\n");
                        }     
                        bw.newLine();
                        bw.flush();
                        bw.close();
                        
                        print("Saved Array To File Successfully...");
                    } catch (IOException e) {
                        print("Couldnt Save Array To File... ");
                        e.printStackTrace();
                    }
       
       
    }

    
    public void tmapSaveData(){
    
        try {
            print("Attempting To Save Array Contents To File...");
            bw = new BufferedWriter(new FileWriter(actualFile, true));

            for(int i = 0; i < model.tmapData.length; i++){
                for(int j=0; j< model.tmapData[0].length;j ++){
                    bw.write(model.tmapData[i][j]+",");
                }
                bw.newLine();
            }
            bw.flush();
            bw.close();

            print("Saved Array To File Successfully...");
        } catch (IOException e) {
            print("Couldnt Save Array To File... ");
            e.printStackTrace();
        }
  
    }
    
    

}

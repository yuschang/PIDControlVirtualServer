/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yustchang;


import javax.swing.Timer;




public class GUI_Controller {
    
    private GUI_Model model;
    private GUI_View view;
    private Timer timer;
    private PID_Controller pidControl;
    private  IPC_Client_Initialize ipcCLIENT_ini;
    
    private GUI_Chart chart;
    private File_Writter fileWrite;
    private File_Writter fileWrite2;  
    private IPC_ClientControl ipcCLIENT; 
    private GUI_View_parameters view_parameter;
    private Serial_PortControl serialPort;
 
    
    // constructor
    public GUI_Controller(GUI_View view, GUI_Model model, IPC_ClientControl ipcCLIENT,  IPC_Client_Initialize ipcCLIENT_ini, PID_Controller pidControl,
                GUI_View_parameters view_parameter,Serial_PortControl serialPort){
      
        this.model = model;
        this.view = view;
        this.pidControl = pidControl;
        this.chart = chart;
        this.fileWrite = fileWrite;
        this.ipcCLIENT = ipcCLIENT;
        this.view_parameter = view_parameter;
        this.serialPort = serialPort;
        this.ipcCLIENT_ini = ipcCLIENT_ini;

        // chart = new GUI_Chart("Moniter");
        
         fileWrite = new File_Writter(model);
        //  fileWrite2 = new File_Writter(model);
        //  model.stopLoopFlag = true;
         fileWrite.logfileOgnizer("C:\\Users\\boyco\\Documents\\[Java_Import_folder]\\logdata\\","logData.txt");

         fileWrite.initialFile();
    }
    
    
    ///  new code starts here after 3.2 version. Large modification was applied in this version
    
    
    private void print(String importstring){
    
        System.out.println(importstring);

    }
    
    public void  initializeThesocket(){
        
         ipcCLIENT_ini.setupMRICommandSocket();
         ipcCLIENT_ini.setupMRIDataSocket();
        
         print("Connect to mri was called");
     }          
    
    
    public void connectArduino(){
        
        serialPort.connectSerial();
     
    }
     
     
    public void startToRun(){
        
        // ipcCLIENT.sendText("10101010");

        ipcCLIENT_ini.sendSTARTMessage();
        ipcCLIENT.startSocket();   
  

    }

    
    public void setParameters(){
        print("parameter setup was called");
        view_parameter.setVisible(true);
        
       //  model.initializeCEM();

    }
    
    public void stopAndInitialization(){
    
        print("initializing all the data");
        
        /*
        model.setInitialTmap();  
        model.roi_tmapData = model.roiCropper(model.tmapData,model.roi_start_point, model.roi_window_size);
        model.roi_tmapImag = model.arrary2BuffImage(model.roi_tmapData, 20);
        model.MaxTempDose(model.roi_tmapData);

        updateStatus();
        view.resetChart();
        */
        ipcCLIENT.initializeAllDicomFile();
        view.setMyText("File initialization was called !!");

    }
    
   
    
    public void updateStatus(){
        
        model.updateTracker();
        pidControl.setNewDutyCycle(model.max_temp);
        
        
        //model.pidOutput = pidControl2.doPID(model.max_temp);
        serialPort.serialSendMessage(model.pidOutput);
        
        fileWrite.logSaveData();
        
        model.resizeTmapForPlot(model.croppedTmapSize[0],model.croppedTmapSize[1],5);
        view.plotImagesJLB1(model.ResizedTmap) ;
        view.plotImagesJLB2(model.sCaleImage(model.roi_tmapImag,256,256));
        view.plotImagesJLB3(model.sCaleImage(model.roi_doseMapImag,256,256));
        view.updateChart(model.mean_temp,model.max_temp);
        print("Update for current frame is finished... ");

    };
    
}

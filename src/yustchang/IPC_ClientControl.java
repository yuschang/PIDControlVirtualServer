package yustchang;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


class IPC_ClientControl implements Runnable {


    private GUI_Controller control;
    private GUI_Model model;
    private File_Writter fileWrite;
    private IPC_Client_Initialize ipcCLIENT_ini;
     
    private Thread thread;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    
    public int currentFrameN;
    public int currentFrameNPluss;
    public  boolean SeiralTriggered;

    private static final String IP_ADDRESS = "localhost";
    
    private int dataLength;
    private byte[] firstfourByte;
    private byte[] importedByteData;
        
    public int fileCount;
    public int frameCount;
    public  FileOutputStream fs;
    
    private volatile boolean stop;
    
    
    public IPC_ClientControl(GUI_Model model, IPC_Client_Initialize ipcCLIENT_ini){
        
        this.model= model;
        this.ipcCLIENT_ini = ipcCLIENT_ini;
        init(); 
        frameCount = 1;
        fileCount = 1;
        this.fileWrite = fileWrite;
        
       // initializeAllDicomFile();
     
    }
    
    
    private static void print( String msg){
        System.out.println(msg); 
    }
    
    public void setController(GUI_Controller control){
        this.control = control;
    }
    

    public void init(){
     
     dataLength = 0;   
     socket = new Socket();
     thread = new Thread(this);
     currentFrameN = 0;
     currentFrameNPluss= 0;
     SeiralTriggered = false;
     stop = false;
               
    }
    
    
    public void startSocket(){
        
    try{
       if(!socket.isConnected()){
            InetAddress address = InetAddress.getByName(IP_ADDRESS);
            InetSocketAddress socketAddress = new InetSocketAddress(address,ipcCLIENT_ini.newSocket);
            socket.connect(socketAddress);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            if(!(thread.isAlive())){
             thread = new Thread(this);
            }
            thread.start();
            
            ipcCLIENT_ini.sendSTARTMessage();
      
            print(" connect call sent to the server !!");
        }
       }catch(Exception e){
           System.out.println(e);
           socket = new Socket();
       } 

    }
    
        
    public void sendText(String sedingTxt){
               
        try{  
            out.writeUTF(sedingTxt);    
            
        }catch(Exception e){
           e.printStackTrace();
        }
           
    }
    
    
    
    public void writeByteData2Dcm(byte[] fileArray, String fileName){
                    
                String TotalFileName = model.filesWillSavedDirectory + fileName;
                //BufferedOutputStream bs;
                try{
                    fs = new FileOutputStream(new File(TotalFileName));
                    try{
                  
                     fs.write(fileArray,0,fileArray.length);

                    }finally{
                       fs.flush();
                       fs.close();
                     //  fs = null;
                    }
                    
                } catch(Exception e){
                    print("error occured during file saving");
                } 

    }
    
    public void initializeAllDicomFile(){

        String dir = "C:\\Users\\boyco\\Documents\\[Java_Import_folder]\\newdcm";
         

        while(deleteFilesInthefolder(dir)>=1){
            while(deleteFilesInthefolder(dir)>=1){
                deleteFilesInthefolder(dir);    
            }
        }

    }
    
    private int deleteFilesInthefolder(String folderAddress){
        
        int count = 0;
           
        File fileAdrees = new File (folderAddress);
        String [] fileList = fileAdrees.list();
       
        System.out.println( "Data numer to be deleted: " + fileAdrees.list().length); 
        if(fileList.length >=1 ){
           
            for(int i = 0; i<fileAdrees.list().length; i++){
                  
                File fileAdreesN = new File(folderAddress,fileList[i]);
                
                fileAdreesN.delete();
                count++;
                System.out.println( fileList[i] + " was deleted ! ");
                
            }
             System.out.println( "total number of deleteded file is: "+ count);
 
        }
    return count-fileAdrees.list().length;
    }
    
    
    
    public static int byteArrayToInt(byte[] bytes){
    return  /* bytes[3] & 0xFF |
            (bytes[2] & 0xFF) << 8 |
            (bytes[1] & 0xFF) << 16 |
            (bytes[0] & 0xFF) << 24;*/
            
          ((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) |
          ((bytes[2] & 0xff) << 8)  | (bytes[3] & 0xff);
    }
        
    
    public void readByteFromSocket2(int fileCount){
        
        
            // read first four bytes to know the image size
            firstfourByte = new byte[4];
            try{
                in.read(firstfourByte, 0, 4);
                dataLength = byteArrayToInt(firstfourByte);
                
                print("--> image size of "+ fileCount +" is "+ dataLength + " Bytes");
                
            }catch(IOException e){
                print(" Socket error: can not read data size (first four bytes)");
            }
            
            // byte array of the data
            byte[] data = new byte[dataLength];
            
            // read the first
            byte[] first4zeros = new byte[4];
            Arrays.fill(first4zeros, (byte)0);
            
            // **** byte array append method 1
            int bytes_recd  = 4;
            // copy first 4 bytes
            System.arraycopy(first4zeros, 0, data, 0, bytes_recd);
            
            while(bytes_recd < dataLength){
           
                int tmpReadDataSize = Math.min(dataLength - bytes_recd , 2048);
                byte chunk[] = new byte[tmpReadDataSize];

                try {
                    int nBytesRead = in.read(chunk, 0, tmpReadDataSize);
                    System.arraycopy(chunk, 0, data, bytes_recd, nBytesRead);
                    
                    bytes_recd += nBytesRead;
                    
                } catch (IOException ex) {
                    System.err.println(" Socket error: can not read byte data after reading image data size");
                }

            }
            
            String fileName = fileCount+".dcm";
            writeByteData2Dcm(data,fileName);
            
            
            print("the dcm iamge "+ fileCount +" was saved <--");

            
    }
    
    
    // this method for update whole calculation which called from ClientControler
    public void processCurrentFrame(){
      
        print("** Start to process current tmap frame");
        model.dcm_folder_update();
        print("** Current Frame number is "+ frameCount);
         
        if(frameCount == 1){

            model.setInitialTmap();  
            model.roi_tmapData = model.roiCropper(model.tmapData,model.roi_start_point, model.roi_window_size);
            model.roi_tmapImag = model.arrary2BuffImage(model.roi_tmapData, 20);
            model.MaxTempDose(model.roi_tmapData);
    
           // fileWrite.logSaveData();
            control.updateStatus();
            
            
        } else {
            
            int fn1 = (frameCount-1)*3-1;
            model.dcmRealImagnFileReader(fn1);  // fn1 and fn1+1 => phase1
            model.phaseCalculator();    
            float[] phase1 = model.phase;
            print("** Phase 1 was calculated");

            int fn2 = frameCount*3-1;                   // fn1+3 and fn1+3+1 => phase1
            model.dcmRealImagnFileReader(fn2);
            model.phaseCalculator();
            float[] phase2 = model.phase;
            print("** Phase 2 was calculated");
            
            // Tmap data and image
            model.tmapConvertor(phase1,phase2);            
            print(" ** tmap frame calculation on " + fn1 + " and " + fn2);

            // crop ROI data and convert to image
            model.roi_tmapData = model.roiCropper(model.tmapData,model.roi_start_point, model.roi_window_size);
            model.roi_tmapImag = model.arrary2BuffImage(model.roi_tmapData, 5);

            model.MaxTempDose(model.roi_tmapData);
            print("** Tmax is : " + model.max_temp);
            print("** Dose is : " + model.thermalDose);

           //  fileWrite.logSaveData();
            
            control.updateStatus();
        
        }


        
        // for tmap save
        
        // fileWrite2.logfileOgnizer("C:\\Users\\boyco\\Documents\\[Java_Import_folder]\\tmap\\","tmp"+ipcCLIENT.frameCount+".txt");
        // fileWrite2.tmapSaveData();

    
    }
    

    public void stopCalledFromView(){
        stop = true;

    }

    
    public void run(){

        print("Client controller has been called!! ");
           while(true){
         
            
            while(model.stopProcess == false ){
            String printTxt = "\n" + "==> Start to read Frame No. " + frameCount;
            print(printTxt);    
            readByteFromSocket2(fileCount);
            readByteFromSocket2(fileCount+1);
            readByteFromSocket2(fileCount+2);
          
            processCurrentFrame();
                           
            fileCount += 3;
            frameCount ++;
            
            if(model.stopProcess == true ) {
                print("==> process stopped !! ");
                break; 
            }
            
            }
            
        }
           

    }
    

}






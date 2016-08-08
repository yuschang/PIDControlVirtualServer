/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myipcserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.Timer;
// import java.util.Timer;
// import java.util.TimerTask;
import edu.virginia.neuro.dicom.*;
import edu.virginia.neuro.dicom.part10.DicomFileReader;

import java.io.BufferedOutputStream;
import java.io.File;

import java.io.FileNotFoundException;
import java.nio.file.Files;

/**
 *
 * @author boyco
 */
public class IPC_ServerControl implements Runnable {

    private IPC_ServerGUI view;

    private ServerSocket socket;
    private Socket you;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread thread;
    private PrintStream vmOUT;
    private Timer timer;
    private boolean runvmLoop;

    public JFileChooser file_chooser;

    public File[] filesInDirectory;
    public ImageVolume current_realImg;
    public ImageVolume current_imaginImg;
    private BufferedOutputStream bos;

    public boolean SeiralTriggered;


    
    public IPC_ServerControl(IPC_ServerGUI view) {
        this.view = view;
        init();

    }

    private void init() {
        thread = new Thread(this);
        view.setVisible(true);
        runvmLoop = false;
        SeiralTriggered = false;

    }

    public void dcm_folder_loader(boolean mode_realtimePIDcontrol) {

        file_chooser = new JFileChooser();
        file_chooser.showOpenDialog(null);
        file_chooser.setMultiSelectionEnabled(true);

        // file list in the folder 
        filesInDirectory = file_chooser.getCurrentDirectory().listFiles();

        // file name sorting using additional library  AlphanumFileComparator
        Arrays.sort(filesInDirectory, new AlphanumFileComparator());

    }

    public void dcm_folder_loader2() {

        file_chooser = new JFileChooser("C:\\Users\\boyco\\Documents\\[Java_Import_folder]\\dcm");
        filesInDirectory = file_chooser.getCurrentDirectory().listFiles();

        // file name sorting using additional library  AlphanumFileComparator
        Arrays.sort(filesInDirectory, new AlphanumFileComparator());

    }

    public void dicomObjectReadWriteSend2(int fileIdx) {

        // DicomObjectReader(filesInDirectory[0]);
        // read the dicom objcet
        DicomObjectReader dor = null;
        DicomObject obj = null;

        try {
            DicomFileReader dfr = new DicomFileReader(filesInDirectory[fileIdx]);
            dor = new DicomObjectReader(dfr);
            obj = dor.read();

            System.out.println(obj);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println(e);
        } catch (DicomException e) {
            System.out.println("Error loading DICOM file");
        }

        byte[] fileArray;
        try {
            // read byte data from dicom file
            fileArray = Files.readAllBytes((filesInDirectory[fileIdx]).toPath());
            int fileIntSize = fileArray.length;
            view.setMyText("the data is " + String.valueOf(fileIntSize) + " byte");
            System.out.println("the data is " + String.valueOf(fileIntSize) + " byte");

            // write file size as byte data into the  dicom data
            byte[] fileByteSize = intToByteArray(fileIntSize);

            // for type coverting check
            if (byteArrayToInt(fileByteSize) == fileIntSize) {

                System.out.println("integer and byte array converting is checked");

                // send the byte array to cleint through soket 
                for (int i = 0; i < fileByteSize.length; i++) {
                    fileArray[i] = fileByteSize[i];
                }

                out.write(fileArray);
                System.out.println("Bytes data have been sent out");

            };

        } catch (IOException ex) {
            view.setMyText("Unable to read the Dicom byte data");
        }

    }

    public byte[] intToByteArray(int value) {
        return new byte[]{
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value};

    }

    public int byteArrayToInt(byte[] bytes) {
        return /* bytes[3] & 0xFF |
            (bytes[2] & 0xFF) << 8 |
            (bytes[1] & 0xFF) << 16 |
            (bytes[0] & 0xFF) << 24;*/ ((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16)
                | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);

    }

    public void startSocket() {

        try {
            view.setMyText("Waiting for clients...");
            socket = new ServerSocket(2000);
            you = socket.accept();
            view.setMyText("yustchang: connnetion received from " + you.getInetAddress().getHostName() + " : " + you.getPort());
            in = new DataInputStream(you.getInputStream());
            out = new DataOutputStream(you.getOutputStream());

            if (!thread.isAlive()) {
                
                thread = new Thread((Runnable) this);
            }
            thread.start();

        } catch (Exception e) {
            System.out.println(e);
            try {
                socket = new ServerSocket(2000);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    public void sendText(String sedingTxt) {

        try {
            out.writeUTF(sedingTxt);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
  

    public void vmRUNsendDCM() {

        
        double frameRT = Double.parseDouble(view.frameRate) * 1000;
        int delay = (int) frameRT;
               
       //

        ActionListener timerTask = new ActionListener(){
      
        private int secondCount = 0;
        private int fnCount = 0;
            @Override
            public void actionPerformed(ActionEvent e){
                
                if(fnCount >= (filesInDirectory.length-3)){
                    timer.stop();
                }
                
                    view.setMyText(secondCount + " millisecond");
                    System.out.println(secondCount + " millisecond");

                    dicomObjectReadWriteSend2(fnCount);
                    dicomObjectReadWriteSend2(fnCount + 1);
                    dicomObjectReadWriteSend2(fnCount + 2);

                    // vmOUT.println( String.valueOf(secondCount));
                    secondCount += delay;
                    fnCount += 3;
                
            
            }
            
        };
        
        timer = new Timer(delay, timerTask);
        timer.start();
        
        
      /*
        try {
            timer = new Timer(delay, new ActionListener() {

                private int secondCount = 0;
                private int fnCount = 0;

                public void actionPerformed(ActionEvent e) {


                    if(fnCount>= filesInDirectory.length){           
                        timer.stop();
                    }
  
                    view.setMyText(secondCount + " millisecond");
                    System.out.println(secondCount + " millisecond");

                    dicomObjectReadWriteSend2(fnCount);
                    dicomObjectReadWriteSend2(fnCount + 1);
                    dicomObjectReadWriteSend2(fnCount + 2);

                    // vmOUT.println( String.valueOf(secondCount));
                    secondCount += delay;
                    fnCount += 3;
                    
                    

                }
            });

            timer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
      */
  
    }

 
    private void chatFunction(boolean doChat) {

        String msg = null;

        while (doChat == true) {

            try {
                msg = in.readUTF();

                view.setMyText("Client Sent: " + msg);

            } catch (IOException e) {
                e.printStackTrace();

                try {
                    socket = new ServerSocket(2000);

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                break;
            }

        }

    }

    public void serialTringerChecker() {

        try {
            String msg = in.readUTF();
            view.setMyText("Client Sent: " + msg);
            // to read out the integer from string method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < msg.length(); i++) {

                if (Character.isDigit(msg.charAt(i))) {
                    sb.append(msg.charAt(i));
                }
            }
            int tmp = Integer.parseInt(sb.toString());

            // System.out.println(currentFrameN*12);
            if (tmp == 10101010) {
                System.out.println(" Triggered ");
                SeiralTriggered = true;
            }
        } catch (IOException ex) {
            Logger.getLogger(IPC_ServerControl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void checkSocketInAndRun() {

        view.frameRate = "2";
        view.setMyText("Auto setting was activated: ");
        view.setMyText("Frame rate was automatically setted to " + view.frameRate + "Fr/ Sec");

        // auto setup the file folder and frame rate
        dcm_folder_loader2();

        vmRUNsendDCM();
        view.setMyText("The loop is start to run");

    }
    
    

    public void run() {

        boolean condition1 = true;

        while (condition1 == true) {

            serialTringerChecker();
            if (SeiralTriggered = true) {

                checkSocketInAndRun();
                condition1 = false;
            }
            // run the periodic server

            System.out.println("Something wrong with the chat income checker");
        }

    }

}


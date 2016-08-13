/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yustchang;


import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Arrays;
import javax.swing.JFileChooser;

/**
 *
 * @author boyco
 */
public class GUI_Model {
                 
   public String filesWillSavedDirectory;
   public File [] filesInDirectory;
   public float[] phase;
   public float[][] tmep;
   public float[][] tmapData;
   public float[][] roi_tmapData;
   public double[][] roi_doseMap;
   public float[][] g_roi_tmapData;
   public double[][] g_roi_doseMap;

   public ImageVolume current_realImg;
   public ImageVolume current_imaginImg;
   public BufferedImage tmap_Imag;
   public BufferedImage roi_tmapImag;
   public BufferedImage ResizedTmap;
   public BufferedImage roi_doseMapImag;
   
   public float initialTemp;
   public int myCounter;
   public int fnTracker;
   public int roi_start_point;
   public int roi_window_size;
   public double max_temp;
   public double mean_temp;
   public double pid_Input;
   public double max_temp_based_dose;

   public float baseTemp;
   
   public int[] croppedTmapSize;
   public int[] rectangCoordi;
   public int[][] crossCoordi;
   public int[][] crossCoordi_cem;   
   public int[] maxTempCoordi;
   public int[] maxDoseCoordi;
   public int[] g_maxTempCoordi;
   public int[] g_maxDoseCoordi;
   
   public double targetTemp;
   public double targetDose;
   public double pGain;
   public double iGain;
   public double dGain;
   public double pidOutput;
   public double thermalDose;
   
   public double [] currentLogData;
   
   public boolean stopProcess;
   public boolean stopLoopFlag;
   public boolean roiGaussianFilterRequired;
   public boolean hotspotTrackedTriger;
   
   
   public File folder;
   //GUI chart part data
   public static final int SUBPLOT_COUNT = 2;
   
   // serial talk frame number
 
   
   public GUI_Model(){
   
       filesWillSavedDirectory = "C:\\Users\\boyco\\Documents\\[Java_Import_folder]\\newdcm\\";
       folder = new File(filesWillSavedDirectory);
      
       maxDoseCoordi = new int[2];
       g_maxDoseCoordi = new int[2];
       // baseTemp = 36;
       hotspotTrackedTriger = false;
       tmapData = new float[256][256];
       croppedTmapSize = new int[2];
       maxTempCoordi = new int[2];
       g_maxTempCoordi = new int[2];
      
       rectangCoordi = new int[4];
       roiGaussianFilterRequired = false;
       stopProcess = false;
       
       thermalDose = 0;
       
       
   }
    // *******************************************************/
    // following part are shows the dode for iamge processing
    // *******************************************************/
    
   

    private void print(String input){

        System.out.println(input);

    }
    
                
 

    public void dcm_folder_loader(boolean mode_realtimePIDcontrol) {

        JFileChooser file_chooser = new JFileChooser();
        file_chooser.showOpenDialog(null);
        file_chooser.setMultiSelectionEnabled(true);
 
        // file list in the folder 
        filesInDirectory = file_chooser.getCurrentDirectory().listFiles();
        
        // file name sorting using additional class AlphanumFileComparator
        Arrays.sort(filesInDirectory, new AlphanumFileComparator() );
        
    }
    
    
    
    public void dcm_folder_update(){
        
        // File folder = new File(address);
        filesInDirectory = folder.listFiles();
        
        // file name sorting using additional class AlphanumFileComparator
        Arrays.sort(filesInDirectory, new AlphanumFileComparator() );
    
        System.out.println( "there have "+filesInDirectory.length+ " files in the folder");
       
    }
   
         
    
    public void dcmRealImagnFileReader(int fn){
        
       // the amplitude, real and imagine image were stored in 0, 1, 2 order respectory
        DicomImageLoader loader = new DicomImageLoader();
        System.out.println(" Start to read in Real imag: No. "+ fn);
        current_realImg = loader.load(filesInDirectory[fn-1], null);
        
       // DicomImageLoader loader2 = new DicomImageLoader();
        System.out.println(" Start to read in Imagin imag: No. " +(fn+1));
        current_imaginImg = loader.load(filesInDirectory[fn], null);
       //  imageCanvas2.setImage(current_imaginImg);     
       
    }

    public void phaseCalculator() {
          
        // phase calculation based on readed real and imaginary data 
        short[] real = (short[])current_realImg.getData();
        short[] img = (short[])current_imaginImg.getData();
        
        phase = new float[img.length];
 
        for (int i = 0; i < img.length ; i ++) {
              phase[i] = (float)Math.atan2(img[i], real[i]);
        }   
        System.out.println("** The Phase in 1D array is " + phase.length + " long ");
    } 
    
    public void setInitialTmap(){
        
        int width = 256;
        int height = 256;

        for(int i=0; i<width; i++)
            for(int j=0; j<height; j++)
                tmapData[i][j] = baseTemp;
        
 
        System.out.println("Tmap was filled by iniital temp");

        
        tmap_Imag = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster my_raster = tmap_Imag.getRaster();

        for(int i = 0 ; i < width ; i++){
            for(int j = 0; j< height; j++){
               int rgb = (int)tmapData[i][j];
               //  tmap_Imag.setRGB(i,j,rgb);  
                 my_raster.setSample(i,j,0,rgb);
            }
        }
        System.out.println("ImageProcessing: initial Tmap was set");
        
    }
 
    public void tmapConvertor(float [] phase1, float [] phase2) {

        int width = 256;
        int height = 256;

        // ** constant calculation
        float c_sign = -1;
        double echoTime = 12.7720;  // need to be modified 
        float c_constant = (float)(2 * Math.PI * 42.57e6 * 3 * 0.00909e-6 * echoTime * 1e-3 )*c_sign;

        // create local variabel dPhase
        float dPhase[] = new float[phase1.length];
        // float tmpdPhase;
        
        // ** dPhase calculation and Correct for wrapping phase
        if(phase1.length == phase2.length){
            
               for (int i = 0 ; i < phase1.length ; i++){
                
                   float tmpdPhase = phase2[i] - phase1[i];                
                    if (tmpdPhase > Math.PI){
                        dPhase[i] = (float) (tmpdPhase - 2*Math.PI);
                    }else if (tmpdPhase < -Math.PI){   
                        dPhase[i] = (float) (tmpdPhase + 2*Math.PI);
                    }else{
                        dPhase[i] = tmpdPhase;
                    }
                    
                }
        }
        
  
     
        // ** temperature calculation
        // tmapData = new float[width][height];
        for(int i = 0 ; i < width ; i++){
            for(int j = 0; j< height; j++){
               // tmapData[i][j] = ((phase2[i*width + j] - phase1[i*width + j])/c_constant);
               // float tmpTemp = dPhase[i*width + j]/c_constant;
               tmapData[i][j] = tmapData[i][j] - dPhase[i*width + j]/c_constant;
            }
        }  
        

       //  tmap_Imag = arrary2BuffImage(tmapData, 20);
        System.out.println("** Tmap size is: " + width + " by " + height);
  
   
      // plot the buffered image on Jlabel
      //  jLabel1.setIcon(new ImageIcon(tmap_Imag));

    }
    
    public float[][] roiCropper(float[][] input, int roi_Center_coord, int range){
    

    int roi_size = range*2+1;
    System.out.println("** ROI size is: " + roi_size + " by " + roi_size);
    
    float[][] ouputData = new float[roi_size][roi_size];

    for(int i = 0; i < roi_size; i ++){
        for(int j = 0; j < roi_size; j ++){
                   ouputData[i][j] = input[roi_start_point-range+i][roi_start_point-range+j];
            }
        }

    return ouputData;
    }
    
 
    
    public BufferedImage arrary2BuffImage(float[][] input, int amplify){
               
        BufferedImage returnImage = new BufferedImage(input.length, input[0].length, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster my_raster = returnImage.getRaster();

        for(int i = 0 ; i < input.length; i++){
                for(int j = 0; j< input[0].length; j++){
                   int rgb = (int)input[i][j]*amplify;
                    // roi_tmapImag.setRGB(i,j,rgb);  
                    my_raster.setSample(i,j,0,rgb);
                }
            }

    return returnImage;
    }
    
    
    
    public void resizeTmapForPlot(int center, int range , int amplify){
        
        ResizedTmap = sCaleImage(arrary2BuffImage(roiCropper(tmapData,center,range), amplify),255,255);

    }
    
    

    public BufferedImage sCaleImage(BufferedImage src, int w, int h) {  

          BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
          int x, y;
          int ww = src.getWidth();
          int hh = src.getHeight();
          for (x = 0; x < w; x++) {
            for (y = 0; y < h; y++) {
              int col = src.getRGB(x * ww / w, y * hh / h);
              img.setRGB(x, y, col);
            }
          }
          print("** Scaling was performed **");
          return img;
        
    }
  
    public void MaxTempDose(float[][] importTmap){

        
    /// new method
    // find max tmep
    double maxAvt = 0;  
    double g_maxAvt = 0; 
    double SumTemp = 0;
    int numbeCount = 0;
    double g_avTemp = 0;
    
    int startPoint= roi_start_point-roi_window_size;
    int endPoint = roi_start_point+roi_window_size+1;
    // roi_doseMap = new double[roi_window_size*2+1][roi_window_size*2+1];
    print("** ROI widow : Start point: "+ startPoint + " , End point: "+ endPoint);
    
    // max/ mean Temp
    for(int i=startPoint; i<endPoint; i++){
        for(int j=startPoint; j<endPoint; j++ ){
            numbeCount ++;
            if(roiGaussianFilterRequired){       
              
                double avTemp = tmapData[i][j];
                roi_doseMap[i-startPoint][j-startPoint] += getPixelDose(avTemp); // dose calculation
                SumTemp += avTemp;
                if (avTemp > maxAvt){
                    maxAvt = avTemp;
                    maxTempCoordi[0] = j;
                    maxTempCoordi[1] = i;
                }
                
                // for gaussian filtered CEM map
                g_avTemp = gaussian3(i,j,tmapData);
                g_roi_doseMap[i-startPoint][j-startPoint] += getPixelDose(g_avTemp); 
                
                if(g_avTemp > g_maxAvt){   
                    g_maxAvt = g_avTemp;
                    g_maxTempCoordi[0] = j;
                    g_maxTempCoordi[1] = i;
                }
            
            }else{
                double avTemp = tmapData[i][j];
                roi_doseMap[i-startPoint][j-startPoint] += getPixelDose(avTemp); // dose calculation

                SumTemp += avTemp;
                if (avTemp > maxAvt){
                    maxAvt = avTemp;
                    maxTempCoordi[0] = j;
                    g_maxTempCoordi[0] = j;
                    maxTempCoordi[1] = i;
                    g_maxTempCoordi[1] = i;
                }
            }
         
        }
    }
     
    // mean_temp = SumTemp/ numbeCount;
    // max_temp = maxAvt;
    if(roiGaussianFilterRequired == true){
        mean_temp = g_maxAvt;
    
    }else{
        mean_temp = SumTemp/ numbeCount;
    }

    max_temp = tmapData[g_maxTempCoordi[1]][g_maxTempCoordi[0]];

    roi_doseMapImag = arrary2BuffImage(double2Float(roi_doseMap),10);
    
    maxDoseTracker();
    
    max_temp_based_dose += getPixelDose(max_temp);
    
    
    }
    
    
    private void maxDoseTracker(){
    
    int startPoint= roi_start_point-roi_window_size;
    int endPoint = roi_start_point+roi_window_size+1;
    
    // Max dose
    double maxDose =0;


    if( max_temp > (targetTemp-2) ){
    
        if (hotspotTrackedTriger== false){
            print("within 2 degrees of target temperature, finding hotspot");
                for(int i=startPoint; i<endPoint; i++){
                    for(int j=startPoint; j<endPoint; j++ ){
                        double dose = roi_doseMap[i-startPoint][j-startPoint];
                        if ( dose> maxDose){
                            maxDose = dose;
                            maxDoseCoordi[0]= j;
                            maxDoseCoordi[1]= i;                           
                        }
                    }    
                 }
                 hotspotTrackedTriger = true;
            
        } else{

            print("** within 2 degrees of target temperature, have already locked in hotspot");
            print("** Tmax coordi of filtered roi is x: "+ maxDoseCoordi[0]+ " , y :" + maxDoseCoordi[1]);

            for(int i= maxDoseCoordi[1]-startPoint-2; i<maxDoseCoordi[1]-startPoint+2; i++){
                    for(int j= maxDoseCoordi[0]-startPoint-2; j< maxDoseCoordi[0]-startPoint+2; j++ ){
                        if(i<0) i = 0;
                        if(j<0) j = 0;
                        if(i>=(roi_window_size*2+1)) i = roi_window_size*2;
                        if(j>=(roi_window_size*2+1)) j = roi_window_size*2;
                        
                        // print("Trying to read maxDose pixel on x: " + j + ", y: " + i);
                        double dose = roi_doseMap[i][j];
                        
                        if (maxDose < dose){
                            maxDose = dose;
                        }
                                  
                    }
                }
       
        }
        
    }else{ 
        print("** Finding the max Dose spot on ROI");
        for(int i=startPoint; i<endPoint; i++){
            for(int j=startPoint; j<endPoint; j++ ){
                double dose = roi_doseMap[i-startPoint][j-startPoint];
                if ( dose> maxDose){
                        maxDose = dose;
                        maxDoseCoordi[0]= j;
                        maxDoseCoordi[1]= i;   
                        
                }
            }    
        }
        
        hotspotTrackedTriger = false;
       
    }
    
    thermalDose = maxDose ;

 
    }
    
 
        
    private float[][] double2Float(double[][] input){
    
        float[][] output = new float[input.length][input[0].length];
        for(int i =0; i<input.length; i++)
            for(int j=0; j<input[0].length; j++)
                output[i][j] = (float)input[i][j];

    return output;
    }
 
    private double[][] float2Double(float[][] input){
    
        double[][] output = new double[input.length][input[0].length];
        for(int i =0; i<input.length; i++)
            for(int j=0; j<input[0].length; j++)
                output[i][j] = (double)input[i][j];

    return output;
    }
    
    
    
    private double getPixelDose(double pixeltemp){
    
        double pixelDose =0;
    
        if (pixeltemp <43){
            pixelDose = (3.7/60)*Math.pow(0.25, (43-pixeltemp)) ;
        }else{
            pixelDose = (3.7/60)*Math.pow(0.5, (43-pixeltemp)) ;     
        }
        
    return pixelDose;
    }
    
    public void updateTracker(){
        
    rectangCoordi = rectangleCoordiConvertor();     
    
    if(roiGaussianFilterRequired == true){
        // tracker based on gaussian filtered tmap
       crossCoordi = crossCoordiConvertor( g_maxTempCoordi ,10); 
    }else{
        
       crossCoordi = crossCoordiConvertor( maxTempCoordi ,10); 
    }
       
       
       crossCoordi_cem = crossCoordiConvertor( maxDoseCoordi , 5);

    }
    
    private int[] rectangleCoordiConvertor(){
    
       int rectSize = (roi_window_size*2+1)*256/(croppedTmapSize[1]*2+1)/2;
       int startPoint = croppedTmapSize[0] ;
       int [] rectangcoordi = new int [4];
       
       rectangcoordi[0] = startPoint-rectSize;
       rectangcoordi[1] = startPoint-rectSize;
       rectangcoordi[2] = rectSize*2+1;
       rectangcoordi[3] = rectSize*2+1;
    
       return rectangcoordi;   
    }
    
    
    private int[][] crossCoordiConvertor(int [] inputCoordi, int crossLength){

       int start_point = roi_start_point - roi_window_size; 
       int[][] crossCoordiReturn = new int[2][4];
       
       int y_cordi = 256/(roi_window_size*2+1)*(inputCoordi[0]- start_point);
       int x_cordi = 256/(roi_window_size*2+1)*(inputCoordi[1]- start_point); 
  
       // horizental line
       crossCoordiReturn[0][0] = (int)(x_cordi - crossLength);
       crossCoordiReturn[0][1] = (int)y_cordi;
       crossCoordiReturn[0][2] = (int)x_cordi + crossLength;
       crossCoordiReturn[0][3] = (int)y_cordi;

       // vertical line
       crossCoordiReturn[1][0] = (int)x_cordi;
       crossCoordiReturn[1][1] = (int)(y_cordi - crossLength);
       crossCoordiReturn[1][2] = (int)x_cordi;
       crossCoordiReturn[1][3] = (int)y_cordi + crossLength;     
    
       return crossCoordiReturn;
    }
    
 
    
    private double gaussian3(int x, int y, float[][] matrix){
    
        double gaussianAv = 0;
        gaussianAv += 4*matrix[x][y];
        gaussianAv += 2*matrix[x-1][y];
        gaussianAv += 2*matrix[x+1][y];
        gaussianAv += 2*matrix[x][y-1];
        gaussianAv += 2*matrix[x][y+1];
        gaussianAv += matrix[x+1][y+1];
        gaussianAv += matrix[x+1][y-1];
        gaussianAv += matrix[x-1][y+1];
        gaussianAv += matrix[x-1][y-1];
        
      return (gaussianAv/16);
              
    }
       
    ///  For Dose calcualtion

    public double getCooldownDose(double targetedTemp){
    
        double a = targetedTemp - baseTemp; 
        double b = -0.0292;
        double t = Math.log((43-targetedTemp)/a)/b;
        double n = Math.floor(t);
        double cumulativeDose = 0;
        for(int i = 0; i < n; i++){
            double temp = (a*Math.exp(b*i))+ targetedTemp;
            double d = (3.7/60)*(Math.pow(0.5, (43-temp)));
            cumulativeDose += d;
        }

    return cumulativeDose;
    }
    

            

       
   
}



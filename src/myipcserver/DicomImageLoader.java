package myipcserver;

import java.io.*;
import java.util.*;

import edu.virginia.neuro.dicom.*;
import edu.virginia.neuro.util.*;
import edu.virginia.neuro.dicom.part10.*;
//import edu.virginia.neuro.imageio.ljpeg.*;
import java.awt.image.*;
import javax.imageio.ImageIO;


/**
 *
 * @author  jsnell
 */
public class DicomImageLoader implements ImageLoader {
    
    /** Creates a new instance of DicomImageLoader */
    public DicomImageLoader() {
    }
    
    public ImageVolume load(File file, ProgressListener listener) {
        ImageVolume image = null;
        
        try {
        if (file.getName().endsWith(".vol")) {
            
            BufferedReader br= new BufferedReader( new InputStreamReader( new FileInputStream(file)), 4096);
            String cookieStr = br.readLine();
            if (cookieStr.equals("dicomserieshack")) {
                String prefix = br.readLine();
                String suffix = "";
                int startimage;
                int endimage;
                if (prefix.equals("list")) {
                    startimage = 1;
                    endimage = Integer.parseInt(br.readLine());
                    System.out.println("range 1 - " + endimage);
                }
                else {
                    suffix = br.readLine();
                    startimage = Integer.parseInt(br.readLine());
                    endimage = Integer.parseInt(br.readLine());
                }
                
                String[] entries=null;
                if (prefix.equals("list")) {
                    entries = new String[endimage-startimage+1];
                    System.out.println("entry count " + (endimage-startimage+1));
 
                    for (int i=startimage; i<=endimage; i++) {
                        entries[i-startimage] = br.readLine().trim();
                        System.out.println(entries[i-startimage]);
                    }
                }
                
                float firstSliceLocation = 0.0f;
                
                for (int i=startimage; i<=endimage; i++) {
                    
                    File sliceFile;
                    
                    if (!prefix.equals("list")) {
                        sliceFile = new File(file.getParent() + file.separator + buildFileName(i, prefix, suffix));
                    }
                    else {
                        String listentry = entries[i-startimage];
                        sliceFile = new File(file.getParent() + file.separator + listentry);
                    }
                    
                    DicomObjectReader dor = null;
                    DicomObject obj = null;
                    try {
                        dor = new DicomObjectReader(new DicomFileReader(sliceFile));
                        obj = dor.read();
                    }
                    catch (FileNotFoundException e) {
                        System.out.println("File not found: " + sliceFile.getPath());
                        listener.percentDone("DICOM Error", -1);
                        return null;
                    }
                    catch (IOException e) {
                        System.out.println(e);
                        listener.percentDone("IO Error.", -1);
                        return null;
                    }
                    catch (DicomException e) {
                        System.out.println("Error loading DICOM file");
                        listener.percentDone("File not found.", -1);
                        return null;
                    }
                    
                    if (i==startimage) {
                        int cols = obj.getVR("Columns").getIntValue();
                        int rows = obj.getVR("Rows").getIntValue();

                        float xres = obj.getVR("PixelSpacing").getFloatValue(0);
                        float yres = obj.getVR("PixelSpacing").getFloatValue(1);
                        
                        float sliceThickness = obj.getVR("SliceThickness").getFloatValue();
                        firstSliceLocation = obj.getVR("SliceLocation").getFloatValue();
                        
                        float zres = sliceThickness;
                        System.out.println(cols + " x " + rows + " x ");

                        int bitsPerPixel = obj.getVR("BitsStored").getIntValue();
                        System.out.println("BPP: " + bitsPerPixel);
                        
                        if (bitsPerPixel == 8) {
                            image = new ImageVolume(ImageVolume.BYTE_VOXEL, cols, rows, endimage-startimage+1, 0);
                        }
                        else if (bitsPerPixel > 8 && bitsPerPixel <= 16) {
                            image = new ImageVolume(ImageVolume.SHORT_VOXEL, cols, rows, endimage-startimage+1, 0);
                        }
                        
                        image.setResolution(0, xres);
                        image.setResolution(1, yres);
                        image.setResolution(2, zres);
                    }
                    
                    if (i==startimage+1) {
                        float secondSliceLocation = obj.getVR("SliceLocation").getFloatValue();
                        image.setResolution(2, Math.abs(firstSliceLocation - secondSliceLocation));
                        System.out.println(image.getResolution(0) + " x " + image.getResolution(1) + " x " + image.getResolution(2));
                    }
                    
                    float sliceLocation = obj.getVR("SliceLocation").getFloatValue();
                    System.out.println("slice " + i + " " + sliceLocation);
                    System.out.println(obj.getVR("TriggerTime"));
                    
                  //  System.out.println("Slice thickness: " + obj.getVR("SliceThickness").getFloatValue());

                    short[] voxelData = (short[])image.getData();
                    byte[] sliceData = obj.getVR("PixelData").getValueBytes();
                    int frameSize = image.getDim(0) * image.getDim(1);
                    int offset = (i-startimage)*frameSize;
                    
                    for (int v=0; v<frameSize; v++) {
                        voxelData[offset + v] = (short)((sliceData[v*2] & 0xff) << 8 | (sliceData[v*2 + 1] & 0xff));
                    }
                    
                    if (listener != null) {
                        listener.percentDone("Loading dicom image data...", (int)Math.round((double)(i-startimage+1)/(endimage-startimage+1)*100.0));
                    }
                 }
            }
            
            return image;
        }
        }
        catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.percentDone("Error loading dicom image series", -1);
            }
            image = null;
            return image;
        }
        
        if (listener != null) {
            listener.percentDone("Loading dicom image data...", 0);
        }
        
        DicomObjectReader dor = null;
        DicomObject obj = null, metaInfo = null;
        try {
            DicomFileReader dfr = new DicomFileReader(file);
            dor = new DicomObjectReader(dfr);
            obj = dor.read();
            
            metaInfo = dfr.getMetaInfo();
            System.out.println(obj);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found.");
            listener.percentDone("DICOM Error", -1);
            return null;
        }
        catch (IOException e) {
            System.out.println(e);
            listener.percentDone("IO Error.", -1);
            return null;
        }
        catch (DicomException e) {
            System.out.println("Error loading DICOM file");
            listener.percentDone("File not found.", -1);
            return null;
        }
        
        int cols = obj.getVR("Columns").getIntValue();
        int rows = obj.getVR("Rows").getIntValue();
        
        float xres = 1.0f;
        float yres = 1.0f;
        try {
            xres = obj.getVR("PixelSpacing").getFloatValue(0);
            yres = obj.getVR("PixelSpacing").getFloatValue(1);
        }
        catch (NullPointerException e) {}
        
        float zres = 0.0f;
        System.out.println(cols + " x " + rows + " x ");
        System.out.println(xres + " x " + yres + " x " + zres);
        
        int bitsPerPixel = obj.getVR("BitsStored").getIntValue();
        System.out.println("BPP: " + bitsPerPixel);
        
        int frameCount=0;
        try {
            frameCount= obj.getVR("NumberOfFrames").getIntValue();
        }
        catch(NullPointerException e) {}
        
        System.out.println("Frames: " + frameCount);
        String photometricInterp = obj.getVR("PhotometricInterpretation").getStringValue();
        System.out.println("Photometric Interp: " + photometricInterp);

        if (bitsPerPixel == 8 && photometricInterp.startsWith("MONOCHROME")) {
            image = new ImageVolume(ImageVolume.BYTE_VOXEL, cols, rows, 1, frameCount);
        }
        else if (bitsPerPixel == 8 && !photometricInterp.startsWith("MONOCHROME")) {
            image = new ImageVolume(ImageVolume.RGBA_VOXEL, cols, rows, 1, frameCount);
        }
        else if (bitsPerPixel > 8 && bitsPerPixel <= 16) {
            image = new ImageVolume(ImageVolume.SHORT_VOXEL, cols, rows, 1, frameCount);
        }
        
        image.setResolution(0, xres);
        image.setResolution(1, yres);
        image.setResolution(2, zres);
        
        if (bitsPerPixel <= 8) {
            byte[] voxelData = null;
            int[] voxelRGBAData = null;
            InPlaceByteArrayOutputStream dest;
            
            if (photometricInterp.startsWith("MONOCHROME")) {
                voxelData = (byte[])image.getData();
                dest = new InPlaceByteArrayOutputStream(voxelData);
            }
            else {
                voxelRGBAData = (int[])image.getData();
                dest = new InPlaceByteArrayOutputStream(voxelRGBAData);
            }
            
            byte[] sliceData = obj.getVR("PixelData").getValueBytes();
            
           String syntax = metaInfo.getVR("TransferSyntaxUID").getStringValue();
            
            if (sliceData == null || sliceData.length == 0) {
                System.out.println("native pixel data array is null");
                List frames = obj.getVR("PixelData").getImageFrames();
                System.out.println("Found " + frames.size()  + " encapsulated frames.");
                Iterator fiter = frames.iterator();
                int count = 0;
                while (fiter.hasNext()) {
                    if (listener != null)
                        listener.percentDone("Decoding Multiframe image", (int)((float)count/frames.size()*100.0));

                    System.out.println("Decoding frame " + count++);
                    byte[] frame = (byte[])fiter.next();
                    ByteArrayInputStream src = new ByteArrayInputStream(frame);
                    
                    if (syntax.equals(UID.JPEGBaseline.toString())) {
                        BufferedImage bufimage;
                        
                        try {
                            bufimage = ImageIO.read(src);
                            //System.out.println(image.getRaster().getNumBands() + " " + image.getWidth() + ", " + image.getHeight());

                            if (!photometricInterp.startsWith("MONOCHROME")) {
                                int buf[] = (int[])bufimage.getRaster().getDataElements(0, 0, cols, rows, null);
                                for (int y=0; y<rows; y++) {
                                    for (int x=0; x<cols; x++) {
                                        int value = buf[y*cols + x];
                                        int a = (value & 0xff000000)>>>24;
                                        int b = (value & 0xff0000)>>>16;
                                        int g = (value & 0xff00)>>>8;
                                        int r = (value & 0xff);
                                        dest.write( a );
                                        dest.write( r );
                                        dest.write( g );
                                        dest.write( b );
                                    }
                                }
                            }
                            else {
                                byte buf[] = (byte[])bufimage.getRaster().getDataElements(0, 0, cols, rows, null);
                                for (int y=0; y<rows; y++) {
                                    for (int x=0; x<cols; x++) {
                                        int value = buf[y*cols + x] & 0xff;
                                        dest.write( value );
                                    }
                                }
                            }
                        }
                        catch(IOException e) {
                        }
                        
                    }
                }
            }
            else { // native pixel encoding
                int offset = 0;
                for (int v=0; v<rows*cols; v++) {
                    voxelData[offset + v] = sliceData[v];
                }
            }
        }
        else if (bitsPerPixel > 8 && bitsPerPixel <= 16) {
            short[] voxelData = (short[])image.getData();
            byte[] sliceData = obj.getVR("PixelData").getValueBytes();
            
            String syntax = metaInfo.getVR("TransferSyntaxUID").getStringValue();
            
            if (syntax.equals(UID.JPEGLossless.toString())) {
//                LJPEGDecoder decoder = new LJPEGDecoder();
//                List frames = obj.getVR("PixelData").getImageFrames();
//                System.out.println("Image has " + frames.size() + " frames");
//                byte[] frame = (byte[])frames.get(0);
//                
//                ByteArrayInputStream src = new ByteArrayInputStream(frame);
//                InPlaceByteArrayOutputStream dest = new InPlaceByteArrayOutputStream(voxelData);;
//                decoder.setInputStream(src);
//                decoder.setOutputStream(dest);
//
//                try {
//                    decoder.decodeImageHeader();
//                    decoder.decodeImageData();
//                }
//                catch (IOException e) {
//                }
            }
            else {  
                int offset = 0;
                for (int v=0; v<rows*cols; v++) {
                    voxelData[offset + v] = (short)((sliceData[v*2] & 0xff) << 8 | (sliceData[v*2 + 1] & 0xff));
                }
            }
        }
        
        
        if (listener != null)
            listener.percentDone("Ready.", -1);
        
        return image;
    }
    
    public int probe(File file) {

        int result1 = probeForDicomFile(file);
        int result2 = probeForDicomSeriesIndexFile(file);
        
        return Math.max(result1, result2);
    }
    
    private int probeForDicomSeriesIndexFile(File file)
    {
        String cookieStr="";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));       
            cookieStr = br.readLine();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (cookieStr.equals("dicomserieshack")) {
            System.out.println("Looks like a hacked dicom directory index");
            return 100;
        }
        else {
            System.out.println("Not a hacked dicom directory index");
        }
        
        return 0;
    }
    
    private int probeForDicomFile(File file)
    {
        System.out.println("Probing for a DICOM file");
        
        DataInputStream is;
        try {
            is = new DataInputStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found.");
            return 0;
        }
        byte[] hdr = new byte[128];
        byte[] cookie = new byte[4];
        
        try {
            is.readFully(hdr);
            is.readFully(cookie);
        }
        catch (IOException io) {
            System.out.println("IOException");
            return 0;
        }
        
        for (int i=0; i<4; i++) {
            try {
            System.out.print(new String(cookie, "8859_1"));
            }
            catch (UnsupportedEncodingException e) {
            }
            System.out.println("");
        }
        
        if (cookie[0] == 'D' && cookie[1] == 'I' && cookie[2] == 'C' && cookie[3] == 'M') {
            System.out.println("Looks like a DICOM header.");
            return 100;
        }
        else {
            System.out.println("Not a DICOM header.");
        }
        
        if (is != null) {
            try {
                is.close();
            }
            catch (IOException e) {
            }
        }
        
        return 0;
    }
    
    private String buildFileName(int i, String prefix, String suffix) {
        StringBuffer buf = new StringBuffer(32);
        
        buf.append(prefix);
        
        if (i<10)
            buf.append("0");
        if (i<100)
            buf.append("0");
        if (i<1000)
            buf.append("0");
        
        buf.append(Integer.toString(i));
        buf.append(suffix);
        
        return buf.toString();
    }
    
    private String siemensSubString(String source, String key, int length) {
        int start = source.indexOf(key);
        if (start >= 0) {
            start += key.length();
            return source.substring(start, start+length);
        }
        return "";
    }
    
}

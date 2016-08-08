package myipcserver;

import java.io.*;
import java.net.*;
import java.math.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.color.*;
import java.nio.*;
import javax.imageio.*;

public class ImageVolume {
    private int dims[] = new int[4];
    private float resolution[] = new float[4];
    private int totalSamples;
    private int pixType;
    private Object m_data;
    private Map attributes;
    
    private ByteBuffer m_buffer;
    
    public static final int BYTE_VOXEL = 1;
    public static final int SHORT_VOXEL = 2;
    public static final int INT_VOXEL = 4;
    public static final int FLOAT_VOXEL = 8;
    public static final int RGBA_VOXEL = 16;
    public static final int COMPLEX_VOXEL = 32;
    
    static {
        testAllocStrategy();
    }
    
    public ImageVolume(int voxtype, int x, int y, int z, int t) {
        attributes = new java.util.TreeMap();
        
        pixType = voxtype;
        totalSamples = x * y;
        if (z>0) totalSamples *= z;
        if (t>0) totalSamples *= t;
        
        dims[0] = Math.max(x, 1);
        dims[1] = Math.max(y, 1);
        dims[2] = Math.max(z, 1);
        dims[3] = Math.max(t, 1);
        
        resolution[0] = 1.0f;
        resolution[1] = 1.0f;
        resolution[2] = 1.4f;
        resolution[3] = 1.0f;
        
        alloc();
    }
    
    public int getPixelType() { return pixType; }
    
    public ImageVolume createMatchingVolume(int voxtype) {
        ImageVolume image = new ImageVolume(voxtype, getDim(0), getDim(1), getDim(2), getDim(3));
        for (int i=0; i<4; i++)
            image.setResolution(i,  getResolution(i));
        return image;
    }
    
    public static void testAllocStrategy() {
        try {
        ByteBuffer buf = ByteBuffer.allocateDirect(256);
        byte[] tmp = buf.array();
        }
        catch(Exception e) {
            System.out.println("*** Direct buffers can't be mapped to java byte arrays in this VM, oh well.");
            return;
        }
        System.out.println("Direct NIO buffers seem to work in this VM!");
    }
    
    public void writeJPEGSlice(String filename, int slice) {
        BufferedImage image = new BufferedImage(dims[0], dims[1], BufferedImage.TYPE_BYTE_GRAY);
        byte buf[] = new byte[dims[0] * dims[1]];
        short source[] = (short[])m_data;
        int offset = slice*dims[0]*dims[1];
        for (int i=0; i<dims[0]*dims[1]; i++) {
            buf[i] = (byte)(source[i + offset] / 3.5);
        }
        
        image.getRaster().setDataElements(0, 0, dims[0], dims[1], buf);
        
        try {
            File outputfile = new File(filename);
            ImageIO.write(image, "jpg", outputfile);

        }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        
    }
    
    public void readJPEGSlice(String filename, int slice) {
        InputStream fis;
        DataInputStream dis;
        
        try {
            URL url = new URL(filename);
            BufferedImage image;// = new BufferedImage(dims[0], dims[1], BufferedImage.TYPE_BYTE_GRAY);
            
            image = ImageIO.read(url);
            //System.out.println(image.getRaster().getNumBands() + " " + image.getWidth() + ", " + image.getHeight());
            
            byte buf[] = (byte[])image.getRaster().getDataElements(0, 0, dims[0], dims[1], null);
            short dest[] = (short[])m_data;
            int offset = slice*dims[0]*dims[1];
            float value[] = new float[3];
            for (int y=0; y<dims[1]; y++) {
                for (int x=0; x<dims[0]; x++) {
                    
                    image.getRaster().getPixel(x, y, value);
                    dest[y*dims[0]+x+offset] = (short)(value[0]*3);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    public int getDim(int dimension) {
        return dims[dimension];
    }
    
    public int getDimensionSize(int dimension) {
        return dims[dimension];
    }
    
    public void setResolution(int dimension, float res) {
        resolution[dimension] = res;
    }
    
    public float getResolution(int dimension) {
        return resolution[dimension];
    }
    
    public int getPixelOffset(int x, int y, int z) {
        int width = dims[0];
        int height = dims[1];
        int depth = dims[2];
        
        if (x<0) x = 0;
        else if (x>=width) x = width-1;
        
        if (y<0) y = 0;
        else if (y>=height) y = height-1;
        
        if (z<0) z = 0;
        else if (z>=depth) z = depth-1;
        
        return z * width * height +  y * width + x;
    }
        
    public void LoadURL(String urlpath, int frame) {
        InputStream fis;
        DataInputStream dis;
        
        try{
            URL url = new URL(urlpath);
            fis = url.openStream();
            
            dis = new DataInputStream(fis);
            dis.skipBytes(8192);
            int offset;
            short data[] = (short[])m_data;
            
            byte framebuf[] = new byte[256*256*2];
            dis.readFully(framebuf);
            
            ByteArrayInputStream bufstr = new ByteArrayInputStream(framebuf);
            DataInputStream dibufstr = new DataInputStream(bufstr);
            
            for (int row=0; row<256; row++) {
                offset = row*256 + frame*256*256;
                for (int col=0; col<256; col++) {
                    int lo, hi;
                    lo = dibufstr.readByte();
                    hi = dibufstr.readByte();
                    data[col + offset] = (short)((hi) << 8 | (lo & 0xf0));
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    public Object getData() {
        return m_data;
    }
    
    public ByteBuffer getByteBuffer() {
        return m_buffer;
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
    private void alloc() {
        switch (pixType) {
            case BYTE_VOXEL:
                m_buffer = null;//ByteBuffer.allocateDirect(totalSamples);
                m_data = new byte[totalSamples];
                break;
            case SHORT_VOXEL:
                m_buffer = null;//ByteBuffer.allocate(totalSamples*2);
                m_data = new short[totalSamples];
                break;
            case INT_VOXEL:
            case RGBA_VOXEL:
                m_buffer = null;//ByteBuffer.allocateDirect(totalSamples*4);
                m_data = new int[totalSamples];
                break;
            case FLOAT_VOXEL:
                m_buffer = null;//ByteBuffer.allocateDirect(totalSamples*4);
                m_data = new float[totalSamples];
                break;
        };
    }
    
    private void dealloc() {
        m_buffer = null;
        m_data = null;
    }
    
    
    public Graphics2D getGraphics(int slice) {
        
        DataBuffer db;
        
        if (this.pixType == ImageVolume.SHORT_VOXEL) {
            short[] pixelData = (short[])this.getData();
            db = new DataBufferUShort(pixelData, getDim(0)*getDim(1), slice * getDim(0) * getDim(1));
        }
        else if (this.pixType == ImageVolume.BYTE_VOXEL) {
            byte[] pixelData = (byte[])this.getData();
            db = new DataBufferByte(pixelData, getDim(0)*getDim(1), slice * getDim(0) * getDim(1));
        }
        else {
            return null;
        }
        
        int[] bandoffsets = {0};
                
        WritableRaster wr = Raster.createBandedRaster(db, getDim(0), getDim(1), getDim(0), bandoffsets, bandoffsets, new Point(0,0));
        
        ColorSpace cs = ColorSpace.getInstance(ICC_ColorSpace.CS_GRAY);
        
        ComponentColorModel cm;
        
        if (this.pixType == ImageVolume.SHORT_VOXEL) {
            cm = new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_USHORT);
        }
        else if (this.pixType == ImageVolume.BYTE_VOXEL) {
            cm = new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
        }
        else {
            return null;
        }
        
        BufferedImage img = new BufferedImage(cm, wr, false, null);
        
        return (Graphics2D)img.getGraphics();
    }
}

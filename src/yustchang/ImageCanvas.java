package yustchang;


import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.math.*;
import javax.imageio.ImageIO;
import javax.swing.*;


public class ImageCanvas extends JComponent
implements MouseListener, MouseMotionListener, ComponentListener, ImageVolumeModifierListener {
    private ImageVolume theImage;
    private int[] pixels;
    private int[] overlaypixels;
    private short[] source;
    private int[] lut;
    private float mag, nMag, xoff, yoff;
    private int center, window;
    private int slice, nSlices;
    private int buttonDown, xdown, ydown;
    private BufferedImage myOffScreenImage, overlay;
    private Graphics myOffScreenGraphics;
    private MemoryImageSource myImageSource;
    private ImageCanvasListener listener = null;
    private int crosshair[] = new int[2];
    private boolean showOverlay = false;
    private boolean mouseClickedInCenter = false;
    
    public ImageCanvas() {
        mag = 1.0f;
        nMag = 1.0f;
        xoff = yoff = 0.0f;
        center = 230;
        window = 512;
        slice = 0;
        nSlices = 0;
        
        lut = new int[65536];
        computeLUT();
        
        enableEvents(0xffffffff);
        
        requestFocus();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        
        crosshair[0] = crosshair[1] = 256;
        
    }
    
    public void addListener(ImageCanvasListener obj) {
        listener = obj;
    }
    
    public ImageCanvasListener getListener() { return listener; }
    
    public ImageVolume getImage() { return theImage; }
    
    public void setImage(ImageVolume i) {
        theImage = i;
        
        if (theImage == null) {
            System.out.println("ImageCanvas:nulling source ptr");
            source = null;
            pixels = null;
            return;
        }
        
        nSlices = theImage.getDimensionSize(2) * theImage.getDimensionSize(3);
        
        try {
            source = (short[])theImage.getData();
        } catch (ClassCastException e) {
            source = null;
        }
        
        initPipeline();
        finalRender();
        
        if (listener != null) {
            listener.canvasImageSet(new ImageCanvasEvent(this));
        }
    }
    
    public void setSlice(int s) {
        slice = s;
    }
    
    public int getSlice() {
        return slice;
    }
    
    public int getCenter() { return center; }
    public int getWindow() { return window; }
    public float getMagnification() { return mag; }
    public float getPanX() { return xoff; }
    public float getPanY() { return yoff; }
    
    private void initPipeline() {
        Dimension mySize = getSize();
        if (mySize.width <= 0 || mySize.height <= 0) return;
        
        if (theImage != null) {
            int iwidth = theImage.getDimensionSize(0);
            int iheight = theImage.getDimensionSize(1);
            float xres = theImage.getResolution(0);
            float yres = theImage.getResolution(1);
            float xratio = (float) mySize.width / iwidth / xres;
            float yratio = (float) mySize.height /  iheight / yres;
            nMag = Math.min(xratio, yratio);
        }
        else {
            nMag = 1.0f;
        }
        
        pixels = new int[mySize.width * mySize.height];
        overlaypixels = new int[mySize.width * mySize.height];
        //myImageSource = null;
        //myImageSource = new BufferedImage(mySize.width, mySize.height, BufferedImage.TYPE_INT_RGB);
        //myOffScreenImage.flush();
        myOffScreenImage = new BufferedImage(mySize.width, mySize.height, BufferedImage.TYPE_INT_RGB);
        overlay = new BufferedImage(mySize.width, mySize.height, BufferedImage.TYPE_INT_ARGB);
        renderSlow();
        update(getGraphics());
    }
    
    public void finalRender() {
        
        renderSlow();
        update(getGraphics());
    }
    
    public void setZoom(float z) {
        mag = z;
    }
    
    public void setPan(float xoffset, float yoffset) {
        xoff = xoffset;
        yoff = yoffset;
    }
    
    public void setCenterWindow(int c, int w) {
        center = c;
        window = w;
        computeLUT();
    }
    
    public void computeLUT() {
        for (int i=0; i<65536; i++) {
            int value = (int)Math.max(0, (Math.min((float)(i-center)/(window/2.0)*128 + 127, 255)));
            lut[i] = 0xff000000 | (value << 16) | (value << 8) | value;
        }
    }
    
    private void render() {
        
        try {
            
            Dimension mySize = getSize();
            
            if (myOffScreenImage == null) return;
            if (pixels == null || theImage == null || source == null ||
                slice < 0 || slice >= nSlices) {
                myOffScreenImage.getGraphics().clearRect(0, 0, mySize.width, mySize.height);
                return;
            }
            
            
            float m_magnification = mag * nMag;
            float m_panX = xoff;
            float m_panY = yoff;
            float m_slice = 0;
            int rwidth = mySize.width;
            int rheight = mySize.height;
            int iwidth = theImage.getDim(0);
            int iheight = theImage.getDim(1);
            int islices = theImage.getDim(2);
            int sliceOffset = slice * iwidth * iheight;
            float xres = theImage.getResolution(0);
            float yres = theImage.getResolution(1);
            int pindex;
            
            float c = 1.0f/m_magnification;
            float xc = c/xres;
            float yc = c/yres;
            
            int xo, yo, rowOffset;
            
            int pixelIndex, pixelValue;
            
            //
            // Loop over each row in the window update rectangle
            /////////////////////////////////////////////////////
            
            float panYpre = m_panY/yres + iheight/2;
            float panXpre = m_panX/xres + iwidth/2;
            float halfwidth = rwidth/2.0f;
            
            for (int y = 0; y < rheight; y++) {
                
                yo = (int)((y-rheight/2.0f)*yc + panYpre);
                rowOffset = sliceOffset + yo*iwidth;
                pindex = y*rwidth;
                
                //
                // Loop over each column in the window update rectangle
                ////////////////////////////////////////////////////////
                for (int x = 0; x < rwidth; x++) {
                    xo = (int)((x-halfwidth)*xc + panXpre);
                    if (xo>=0 && yo>=0 && xo<iwidth && yo<iheight) {
                        pixelValue = source[xo + rowOffset];
                        pixels[pindex+x] = lut[pixelValue  & 0x0fff ];
                                        /*
                                        if (pixelValue > 1200) // Bone
                                        {
                                          overlaypixels[pindex+x] = 0x2f0000ef;
                                        }
                                        else if (pixelValue > 1005 && pixelValue < 1045) // Brain
                                        {
                                          overlaypixels[pindex+x] = 0x2f00ef00;
                                        }
                                        else if (pixelValue > 1045 && pixelValue < 1080) // HEM
                                        {
                                          overlaypixels[pindex+x] = 0x3fff0000;
                                        }
                                         */
                        
                        if ((pixelValue & 0x2000) != 0) {
                            overlaypixels[pindex+x] = 0x3f0000ff;
                        }
                        else if ((pixelValue & 0x1000) != 0) {
                            overlaypixels[pindex+x] = 0x3fff0000;
                        }
                        else if ((pixelValue & 0x4000) != 0) {
                            overlaypixels[pindex+x] = 0x3f00ff00;
                        }
                        else {
                            overlaypixels[pindex+x] = 0;
                        }
                        
                    }
                    else {
                        //pixels[pindex+x] = 0xff0000cf;
                        pixels[pindex+x] = 0;
                        overlaypixels[pindex+x] = 0;
                    }
                }
                /////////////////////////////////////////////////////////
                //
                //
                
            }
            
            //myOffScreenImage = createImage(myImageSource);
            //myImageSource.newPixels();
            //myOffScreenImage.flush();
            myOffScreenImage.getRaster().setDataElements(0, 0, myOffScreenImage.getWidth(), myOffScreenImage.getHeight(), pixels);
            overlay.getRaster().setDataElements(0, 0, overlay.getWidth(), overlay.getHeight(), overlaypixels);
            
        }
        catch(Exception e) {
        }
    }
    
    private void renderSlow() {
        
        try {

            Dimension mySize = getSize();
            
            if (myOffScreenImage == null) return;
            if (pixels == null || theImage == null || source == null ||
                slice < 0 || slice >= nSlices) {
                myOffScreenImage.getGraphics().clearRect(0, 0, mySize.width, mySize.height);
                return;
            }
            
            float m_magnification = mag * nMag;
            float m_panX = xoff;
            float m_panY = yoff;
            float m_slice = 0;
            int rwidth = mySize.width;
            int rheight = mySize.height;
            int iwidth = theImage.getDim(0);
            int iheight = theImage.getDim(1);
            int islices = theImage.getDim(2);
            int sliceOffset = slice * iwidth * iheight;
            float xres = theImage.getResolution(0);
            float yres = theImage.getResolution(1);
            int pindex;
            
            short p1, p2, p3, p4;
            int x1, x2, y1, y2;
            float a, a2, b, b2;
            
            float c = 1.0f/m_magnification;;
            float xc = c/xres;
            float yc = c/yres;
            
            float xo, yo;
            int rowOffset, rowOffset2;
            
            //
            // Loop over each row in the window update rectangle
            /////////////////////////////////////////////////////
            
            float panYpre = m_panY/yres + iheight/2;
            float panXpre = m_panX/xres + iwidth/2;
            float halfwidth = rwidth/2.0f;
            
            for (int y = 0; y < rheight; y++) {
                
                yo = ((y-rheight/2.0f)*yc + panYpre - 0.5f);
                y1 = (int)Math.floor(yo);
                y2 = y1 + 1;
                b = yo - y1;
                b2 = 1.0f - b;
                
                rowOffset = sliceOffset + y1*iwidth;
                rowOffset2 = sliceOffset + y2*iwidth;
                pindex = y*rwidth;
                
                //
                // Loop over each column in the window update rectangle
                ////////////////////////////////////////////////////////
                for (int x = 0; x < rwidth; x++) {
                    xo = ((x-halfwidth)*xc + panXpre - 0.5f);
                    x1 = (int)Math.floor(xo);
                    x2 = x1 + 1;
                    a = xo - x1;
                    a2 = 1.0f - a;
                    
                    p1 = p2 = p3 = p4 = 1;
                    
                    if (x1<0 || x1>=iwidth) p1 = p3 = 0;
                    if (x2<0 || x2>=iwidth) p2 = p4 = 0;
                    if (y1<0 || y1>=iheight) p1 = p2 = 0;
                    if (y2<0 || y2>=iheight) p3 = p4 = 0;
                    
                    if (p1>0) p1 = (short)(source[x1 + rowOffset] & 0xfff);
                    if (p2>0) p2 = (short)(source[x2 + rowOffset] & 0xfff);
                    if (p3>0) p3 = (short)(source[x1 + rowOffset2] & 0xfff);
                    if (p4>0) p4 = (short)(source[x2 + rowOffset2] & 0xfff);
                    
                    pixels[pindex+x] = lut[(int)(a2*b2*p1 + a*b2*p2 + a2*b*p3 + a*b*p4) & 0x0fff];
                    
                }
                /////////////////////////////////////////////////////////
                //
                //
                
            }
            
            //myOffScreenImage = createImage(myImageSource);
            //myImageSource.newPixels();
            //myOffScreenImage.flush();
            myOffScreenImage.getRaster().setDataElements(0, 0, myOffScreenImage.getWidth(), myOffScreenImage.getHeight(), pixels);
            overlay.getRaster().setDataElements(0, 0, overlay.getWidth(), overlay.getHeight(), overlaypixels);
            
        }
        catch(Exception e) {
        }
    }
    
    //public void paintComponent(Graphics g) {
    //        super.paintComponent(g);
    //}
    
    public void paint(Graphics g) {
        if (myOffScreenImage == null) {
            initPipeline();
            if (myOffScreenImage == null) {
                return;
            }
        }
        
        Graphics2D g2 = (Graphics2D)myOffScreenImage.getGraphics();
        if (showOverlay) {
            g2.drawImage(overlay, 0, 0, this);
        }

        if (g != null && myOffScreenImage != null) {
        g.drawImage(myOffScreenImage, 0, 0, this);
        }
        
        if (listener != null) {
            listener.canvasPainted(new ImageCanvasEvent(this, g));
        }
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
        //if ((flags & ImageObserver.ALLBITS) != 0)
        paint(this.getGraphics());
        return true;
    }
    
    public Graphics2D getOverlayGraphics() { return  (Graphics2D)overlay.getGraphics(); }
    public Graphics2D getOffscreenGraphics() { return  (Graphics2D)myOffScreenImage.getGraphics(); }

    
    public float[] convertScreenToImage(float coord[]) {
        float rx = coord[0];
        float ry = coord[1];
        int rwidth = this.getWidth();
        int rheight = this.getHeight();
        int iwidth = theImage.getDimensionSize(0);
        int iheight = theImage.getDimensionSize(1);
        float xres = theImage.getResolution(0);
        float yres = theImage.getResolution(1);
        float sx = mag * nMag * xres;
        float sy = mag * nMag * yres;
        float tx = (rwidth - iwidth * sx) / 2.0f - xoff*nMag*mag;
        float ty = (rheight - iheight * sy) / 2.0f - yoff*nMag*mag;
        float[] result = new float[2];
        if (sx != 0.0f && sy != 0.0f) {
            result[0] = (rx - tx) / sx;
            result[1] = (ry - ty) / sy;
        }
        return result;        
    }
    
    public float[] convertImageToScreen(float coord[]) {
        float ix = coord[0];
        float iy = coord[1];
        int rwidth = this.getWidth();
        int rheight = this.getHeight();
        int iwidth = theImage.getDimensionSize(0);
        int iheight = theImage.getDimensionSize(1);
        float xres = theImage.getResolution(0);
        float yres = theImage.getResolution(1);
        float sx = mag * nMag * xres;
        float sy = mag * nMag * yres;
        float tx = (rwidth - iwidth * sx) / 2.0f - xoff*nMag*mag;
        float ty = (rheight - iheight * sy) / 2.0f - yoff*nMag*mag;
        float[] result = new float[2];
        result[0] = sx * ix + tx + 0.5f;
        result[1] = sy * iy + ty + 0.5f;
        return result;
    }
    
    public void setCrosshair(int x, int y) {
        crosshair[0] = x;
        crosshair[1] = y;
    }
    
    public void drawCrosshair(Graphics g) {
        int x, y;
        float coord[] = new float[2];
        coord[0] = crosshair[0];
        coord[1] = crosshair[1];
        try {
            coord = convertImageToScreen(coord);
        }
        catch (Exception e) {
            return;
        }
        x = (int)(coord[0]+0.5);
        y = (int)(coord[1]+0.5);
        
        g.setColor(new Color(200, 0, 0, 200));
        g.drawLine(x, 0, x, getHeight());
        g.drawLine(0, y, getWidth(), y);
    }
    
    public void mouseClicked(java.awt.event.MouseEvent evt) {
        
        boolean metaDown = (evt.getModifiersEx() & MouseEvent.META_DOWN_MASK) != 0;
        
        //if (evt.getButton() == 3) {
        if (buttonDown == 1 && evt.isShiftDown() && !metaDown) {
            center = evt.getX()*4;
            window = evt.getY()*4;
            computeLUT();
            render();
            update(getGraphics());
            if (listener != null) {
                float coord[] = new float[2];
                coord[0] = evt.getX();
                coord[1] = evt.getY();
                coord = convertScreenToImage(coord);
                listener.canvasCW(new ImageCanvasEvent(this, evt, evt.getX(), evt.getY(), coord[0], coord[1]));
            }
        }
        else if (buttonDown == 1 && mouseClickedInCenter && !metaDown) {
            xoff = -(evt.getX() - xdown)/(mag*nMag);
            yoff = -(evt.getY() - ydown)/(mag*nMag);
            computeLUT();
            render();
            update(getGraphics());
        }
        else if (buttonDown ==1 && !mouseClickedInCenter && !metaDown) {
            mag = 1.0f + evt.getY() / 100.0f;
            computeLUT();
            render();
            update(getGraphics());
        }
    //    else {
            
            if (listener != null) {
                float coord[] = new float[2];
                coord[0] = evt.getX();
                coord[1] = evt.getY();
                coord = convertScreenToImage(coord);
                listener.mouseClicked(new ImageCanvasEvent(this, evt, evt.getX(), evt.getY(), coord[0], coord[1]));
            }
    //    }
    }
    
    public void mouseDragged(java.awt.event.MouseEvent evt) {
         boolean metaDown = (evt.getModifiersEx() & MouseEvent.META_DOWN_MASK) != 0;
       
//        if (buttonDown == 2 || buttonDown == 3) {
        if (buttonDown == 1 && evt.isShiftDown() && !metaDown) {
            center = evt.getX()*4;
            window = evt.getY()*4;
            computeLUT();
            render();
            update(getGraphics());
            if (listener != null) {
                float coord[] = new float[2];
                coord[0] = evt.getX();
                coord[1] = evt.getY();
                coord = convertScreenToImage(coord);
                listener.canvasCW(new ImageCanvasEvent(this, evt, evt.getX(), evt.getY(), coord[0], coord[1]));
            }
        }
        else if (buttonDown == 1 && mouseClickedInCenter && !metaDown) {
            xoff = -(evt.getX() - xdown)/(mag*nMag);
            yoff = -(evt.getY() - ydown)/(mag*nMag);
            computeLUT();
            render();
            update(getGraphics());
        }
        else if (buttonDown == 1 && !mouseClickedInCenter && !metaDown) {
            mag = 1.0f + evt.getY() / 100.0f;
            computeLUT();
            render();
            update(getGraphics());
        }
        else {
            float coord[] = new float[2];
            coord[0] = evt.getX();
            coord[1] = evt.getY();
            coord = convertScreenToImage(coord);
            if (listener != null) {
                listener.mouseDragged(new ImageCanvasEvent(this, evt, evt.getX(), evt.getY(), coord[0], coord[1]));
            }
            update(getGraphics());
        }
    }
    
    public void mouseReleased(java.awt.event.MouseEvent evt) {
        if (evt.isShiftDown() || evt.isAltDown() || evt.isControlDown()) {
            if (listener != null) {
                listener.canvasConfig(new ImageCanvasEvent(this));
            }
        }
        else {
            float coord[] = new float[2];
            coord[0] = evt.getX();
            coord[1] = evt.getY();
            coord = convertScreenToImage(coord);
            if (listener != null) {
                listener.mouseReleased(new ImageCanvasEvent(this, evt, evt.getX(), evt.getY(), coord[0], coord[1]));
            }
        }
        finalRender();
    }
    
    public void mousePressed(MouseEvent evt) {
        Point pt = new Point(evt.getX(), evt.getY());
        Point c = new Point(getWidth()/2, getHeight()/2);
        double dist = pt.distance(c);
        mouseClickedInCenter = false;
        
        if (dist < 100.0)
            mouseClickedInCenter = true;
        
        buttonDown = evt.getButton();        
        xdown = evt.getX();
        ydown = evt.getY();
       
        if (evt.isShiftDown() || evt.isAltDown() || evt.isControlDown()) {
            // don't report this since the canvas is consuming this event
            // for its own purposes. Only mouse press without modifier keys
            // gets reported
        }
        else {
            float coord[] = new float[2];
            coord[0] = evt.getX();
            coord[1] = evt.getY();
            coord = convertScreenToImage(coord);
            if (listener != null) {
                listener.mousePressed(new ImageCanvasEvent(this, evt, evt.getX(), evt.getY(), coord[0], coord[1]));
            }
        }
    }
    
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseMoved(MouseEvent e){}
    /**
     * Invoked when component has been resized.
     */
    public void componentResized(ComponentEvent e) {
        initPipeline();
    }
    
    /**
     * Invoked when component has been moved.
     */
    public void componentMoved(ComponentEvent e) {}
    
    /**
     * Invoked when component has been shown.
     */
    public void componentShown(ComponentEvent e) {}
    
    /**
     * Invoked when component has been hidden.
     */
    public void componentHidden(ComponentEvent e) {}
    
    public void setShowOverlay(boolean bShowOverlay) {
        showOverlay = bShowOverlay;
        update();
    }
    
    public void SaveScreenshot() {
        FileOutputStream os;
        File ofile = new File("/home/jsnell/screenshot.jpg");
        try {
            os = new FileOutputStream(ofile);
        }
        catch(FileNotFoundException e) {
            return;
        }
        
        try {
            ImageIO.write(myOffScreenImage, "jpg", os);
            os.close();
        }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        
    }
    
    // ImageVolumeModifierListener methods
    //////////////////////////////////////
    public void update() {
        renderSlow();
        update(getGraphics());
    }
    
    public void updateQuick() {
        render();
        update(getGraphics());
    }

    void setImage(BufferedImage tmap_Imag) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

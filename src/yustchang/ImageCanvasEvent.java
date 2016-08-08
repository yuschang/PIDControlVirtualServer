/*
 * ImageCanvasEvent.java
 *
 * Created on January 14, 2003, 11:09 AM
 */

package yustchang;


import java.util.EventObject;
import java.awt.Graphics;
import java.awt.event.*;

/**
 *
 * @author  jsnell
 */
public class ImageCanvasEvent extends EventObject {
    
    private int canvasx, canvasy;
    private float imagex, imagey;
    private Graphics gc;
    private MouseEvent mouseEvent;
    
    /** Creates a new instance of ImageCanvasEvent */
    public ImageCanvasEvent(Object source) {
        super(source);
    }
    
    public ImageCanvasEvent(Object source, Graphics g) {
        super(source);
        gc = g;
    }
    
    public ImageCanvasEvent(Object source, MouseEvent mouseevt, int cx, int cy, float ix, float iy) {
         super(source);
         canvasx = cx;
         canvasy = cy;
         imagex = ix;
         imagey = iy;
         mouseEvent = mouseevt;
    }
    
    public ImageCanvasEvent(Object source, Graphics g, int cx, int cy, float ix, float iy) {
         super(source);
         canvasx = cx;
         canvasy = cy;
         imagex = ix;
         imagey = iy;
         gc = gc;
    }
    
    public int getCanvasX() { return canvasx; }
    public int getCanvasY() { return canvasy; }
    public float getImageX() { return imagex; }
    public float getImageY() { return imagey; }
    public Graphics getCanvasGraphics() { return gc; }
    public MouseEvent getMouseEvent() { return mouseEvent; }
}

/*
 * ImageCanvasListener.java
 *
 * Created on February 26, 2003, 11:26 AM
 */

package yustchang;


import java.util.EventListener;

/**
 *
 * @author  jsnell
 */

public interface ImageCanvasListener extends EventListener {
    public void mousePressed(ImageCanvasEvent evt);
    public void mouseClicked(ImageCanvasEvent evt);
    public void mouseReleased(ImageCanvasEvent evt);
    public void mouseDragged(ImageCanvasEvent evt);
    public void canvasPainted(ImageCanvasEvent evt);
    public void canvasZoomed(ImageCanvasEvent evt);
    public void canvasPanned(ImageCanvasEvent evt);
    public void canvasCW(ImageCanvasEvent evt);
    public void canvasConfig(ImageCanvasEvent evt);
    public void canvasImageSet(ImageCanvasEvent evt);
}
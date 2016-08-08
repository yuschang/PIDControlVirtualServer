
package yustchang;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author john
 */
public class SimpleDicomViewer {

 
     /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI(ImageVolume image) {
        //Create and set up the window.
        JFrame frame = new JFrame("Simple Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        ImageCanvas canvas = new ImageCanvas();
        frame.getContentPane().add(canvas);
        
        canvas.setImage(image);

        //Display the window.
        frame.setLocation(512, 512);
        frame.getContentPane().setPreferredSize(new Dimension(512, 512));
        frame.pack();
        frame.setVisible(true);
    }

   /* public static void main(String[] args) {
        javax.swing.JFileChooser fileDlg = new javax.swing.JFileChooser();
        fileDlg.setMultiSelectionEnabled(false);
        fileDlg.showOpenDialog(null);
        File file = fileDlg.getSelectedFile();
        
        DicomImageLoader loader = new DicomImageLoader();
        final ImageVolume image = loader.load(file, null);
        
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(image);
            }
        });
    }  */
    
}

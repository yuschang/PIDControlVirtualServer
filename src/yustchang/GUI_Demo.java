/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yustchang;

import javax.swing.SwingUtilities;



/**
 *
 * @author boyco
 */
public class GUI_Demo {
   
    
   // private Base_Frame guiVIEW = new Base_Frame();
                         
    /**
     * @param args the command line arguments
     */

    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI_View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI_View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI_View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI_View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
      
        // System.setProperty("sun.java2d.dpiaware", "false");
        //  -Dsun.java2d.dpiaware=false
        
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
              
                GUI_Model model = new GUI_Model();
                GUI_View view = new GUI_View(model);
                GUI_View_parameters view_parameter = new GUI_View_parameters(model);
                PID_Controller pidControl = new PID_Controller(model);
                
                IPC_ClientControl ipcCLIENT = new IPC_ClientControl(model);
      
                GUI_Controller controller = new GUI_Controller(view, model, ipcCLIENT, pidControl, view_parameter);
                // for 2 way referencing of VIEW and CONTROL class
                view.setController(controller);

                ipcCLIENT.setController(controller);

                // new Base_Frame().setVisible(true);
                view.setVisible(true);

          }
      
      
      
      });
      
        
      
    }
    

    
}

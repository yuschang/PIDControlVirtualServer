/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yustchang;

/**
 *
 * @author boyco
 */
public class PID_Controller {
    
   private GUI_Model model;

    
   private double integralState = 0;
   private double derivativeState = 0;
   
   private double integralMax = 100;
   private double integralMin = -100;
   
   double proportionalTerm;
   double integralTerm;
    
    
    public PID_Controller( GUI_Model model){
        this.model = model;     
    }
    

    public void killPID(){
        model.pidOutput = 10;
    }
   
    
    public void updatePID(double error, double current){
        
       System.out.println("The PID controller is called");
       // Proportional component
       proportionalTerm = model.pGain*error;
       
       // Integral component
       integralState += error;
           if(integralState> integralMax){
               integralState = integralMax; 
           }
           else if(integralState<integralMin){
               integralState=integralMin;
           }
       integralTerm = model.iGain*integralState;
    
       //Derivative component
       double derivativeTerm = model.dGain*(current-derivativeState);
       derivativeState= current;
       
       model.pidOutput = (proportionalTerm + integralTerm + derivativeTerm);
           if (model.pidOutput<5){
               model.pidOutput = 5;
           } else if(model.pidOutput >95){
               model.pidOutput = 95;
           }
           
     System.out.println("PID: P term is " + proportionalTerm + "PID: I term is " + integralTerm);
     System.out.println("PID: Output is " + model.pidOutput);
     
    }
       
    public void setNewDutyCycle(double currentTemp){
    
        if(model.thermalDose < model.targetDose){
            
            updatePID((model.targetTemp-currentTemp),currentTemp);
         
        }else{
            
            model.pidOutput = 5;
            
        }
              
    
    }
    
    
    
    
    
}

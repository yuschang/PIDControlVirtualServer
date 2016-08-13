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

   private int dt = 0; // cycle time, ms
   private double cycleTime=0;  
   private int cycleCount=0;   
       
   private double integralState = 0;
   private double derivativeState = 0;
   
   private double integralMax = 10;
   private double integralMin = -10;
   
   private double highLimit =100;
   private double lowLimit  = 0;
   
   double proportionalTerm =0;
   double integralTerm =0 ;
   double DerivativeTerm = 0;
   double PreviousError = 0;     
   
       
    public PID_Controller( GUI_Model model){
        this.model = model;     
    }
    

    public void killPID(){
        model.pidOutput = 5;
    }
   
    
    public void updatePID(double error, double current){
        
       System.out.println("The PID controller is called");
       // Proportional component
       
        if (this.cycleTime==0) {
            this.cycleTime = System.currentTimeMillis();
        }
               

       proportionalTerm = model.pGain*error;

       integralTerm += model.iGain*error*dt;
    
           if(integralTerm> integralMax){
               integralTerm = integralMax; 
           }
           else if(integralTerm<integralMin){
               integralTerm=integralMin;
           }
           
                  
       //Derivative component
       DerivativeTerm = model.dGain*(error-PreviousError)/dt;
      
       model.pidOutput = (proportionalTerm + integralTerm + DerivativeTerm);
       
       
           if (model.pidOutput<lowLimit){
               model.pidOutput = 5;
           } else if(model.pidOutput >highLimit){
               model.pidOutput = 95;
           }
                 
       PreviousError = error;
        
       cycleCount++;
        
       dt = (int)(System.currentTimeMillis() - this.cycleTime);

       this.cycleTime = System.currentTimeMillis();
       
       System.out.println("PID: P term is " + proportionalTerm + "PID: I term is " + integralTerm);
       System.out.println("PID: Output is " + model.pidOutput);
     
    }
       
    public void setNewDutyCycle(double currentTemp){
    
        /*
        
        if(model.thermalDose <=1){
            
             model.pidOutput = 20;
    
        }else{
            
             if(model.thermalDose <= model.targetDose){
                System.out.println("!! target thermal dose is : "+ model.targetDose);
                System.out.println("!! Current thermal dose is : "+ model.thermalDose);
                 
                updatePID((model.targetTemp-currentTemp),currentTemp);

                System.out.println("!! The error temp in PID is : "+ (model.targetTemp-currentTemp));
                
             }else{
                model.pidOutput = 0;
                System.out.println("PID was turn off because reached target thermal dose!!");
            
             }            
            
        } */
        
         if(model.thermalDose <= model.targetDose){
                System.out.println("!! target thermal dose is : "+ model.targetDose);
                System.out.println("!! Current thermal dose is : "+ model.thermalDose);
                 
                updatePID((model.targetTemp-currentTemp),currentTemp);

                System.out.println("!! The error temp in PID is : "+ (model.targetTemp-currentTemp));
                
             }else{
                model.pidOutput = 0;
                System.out.println("PID was turn off because reached target thermal dose!!");
            
             }            
        
    
    }
    
    
    
    
    
}

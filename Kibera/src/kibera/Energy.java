package kibera;

import kibera.Resident.Identity;

/**
 * This object serves as the agent's energy reservoir
 * 
 * This is adapted from Burke and Stets (2009) Identity Model
 *
 * @author bpint
 *
 */
public class Energy {
    
    /**
     * This function uses the logistic function to calculate resident's energy level as they seek to meet their
     * identity standard.
     * 
     * @param currentEnergy - the agent's current energy level
     * @param aggressionRate - the agent's aggression rate set at initialization
     * @param me - the agent
     * @param kibera
     * 
     * @return the agent's aggression level
     * 
     */ 
    public static double calculateAggression(double currentEnergy, double aggressionRate, Resident me, Kibera kibera) {
        double xMin = 0;
        double xMax = 100;
        double rate = aggressionRate;
        
        //the aggression rate can vary depending on the household's "happiness" level
        //for instance, agent's living in a household meeting its basic needs, will be better able to cope when it struggles to meet its identity standard
        int householdHappiness = me.getHousehold().householdHappiness(kibera);

        if (householdHappiness == 1) {
                rate = rate * (1/3) + rate;
        }
        if (householdHappiness == 2) {
                rate = rate * (2/3) + rate;
        }

        double exp = 0.0;
        double aggressValue = 0.0;
        
        //calcuate the aggression level using the logistic curve and based on its current energy and aggression rate
        exp = 20 / (xMax - xMin + 1) * (currentEnergy - ((xMax - xMin) / 2 + xMin));
        aggressValue = 1 / (1 + Math.exp(-1 * rate * exp));	

        return aggressValue;
    }
    
    /**
     * This function calculates the resident's energy-level based on whether their identity
     * standard was met or not. 
     * 
     * @param me - the agent
     * @param isIdentityStandardMet - boolean identifying if agent succeeded in meeting its identity standard
     * @param kibera
     * 
     */ 
    public static void evaluateEnergy(Resident me, boolean isIdentityStandardMet, Kibera kibera) {

        //do this everytime there is a change in the agents activity        
        double rnEnergyChange = kibera.params.global.getEnergyRateOfChange() * (double) me.getTimeInGoal();
        rnEnergyChange = rnEnergyChange / kibera.params.global.getMinutesInDay();
        
        if (me.getCurrentIdentity() == Identity.Rebel) {
            rnEnergyChange = 0;
        }


        //energy rate of change = change in energy / change in time
        //calcuate updated energy based on whether agent met its identity standard or not
        if (!isIdentityStandardMet) {
            if (me.getEnergy() - rnEnergyChange <= 0) { me.setEnergy(0); }                                  
            else { me.setEnergy(me.getEnergy() - rnEnergyChange); }            
        }     

        else {
            if (me.getEnergy() + rnEnergyChange >= 100) { me.setEnergy(100); }
            else { me.setEnergy(me.getEnergy() + rnEnergyChange); }                               
        }
    }
	
}

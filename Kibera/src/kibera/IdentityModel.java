package kibera;

import kibera.Resident.Employment;
import kibera.Resident.Goal;
import kibera.Resident.Identity;

/**
 * The Identity Model
 * Residents can have one of four primary identities: domestic, student, employee, rebel
 * And one secondary identity: ethnicity
 * 
 * This is adapted from Burke and Stets (2009) Identity Model
 * 
 * @author bpint
 *
 */
public class IdentityModel {
    
    /**
     * Determine if my identity standard has been met
     * The check for whether the standard is met is kept simple in this model (e.g., if resident has a job, employee 
     * identity is met)
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return true if identity standard was met, false otherwise
     * 
     */ 
    public static boolean determineIdentityStandard(Resident me, Kibera kibera) {
        boolean isIdentityStandardMet;

        //if the resident is a very young child, he/she is happy being inactive and staying home
        if (me.getAge() < 6 && me.getMySchool() == null) {            
            me.setCurrentIdentity(Identity.Domestic_Activities);
            isIdentityStandardMet = true;
        }

        else if (me.isInitialRebel() && kibera.params.global.isRemainRebel()) {
            me.setCurrentIdentity(Identity.Rebel);
            isIdentityStandardMet = true;
        }

        //Determine if rebel identity is met
        else if (me.getCurrentGoal() == Goal.Rebel) {
            me.setCurrentIdentity(Identity.Rebel);
            isIdentityStandardMet = true;
        }

        //Is employee identity standard met
        else if (me.getCurrentEmploymentStatus() == Employment.Formal || me.getCurrentEmploymentStatus() == Employment.Informal) {
            me.setCurrentIdentity(Identity.Employer);
            isIdentityStandardMet = true;
        }

        //Is student identity standard met
        else if (me.getMySchool() != null) {
            me.setCurrentIdentity(Identity.Student);
            isIdentityStandardMet = true;
        }

        //If I want to go to school, but could not find an available school, identity standard is not met
        else if (me.searchedForSchool() && me.getMySchool() == null) {

            if (me.getCurrentEmploymentStatus() == Employment.Formal || me.getCurrentEmploymentStatus() == Employment.Informal) {
                isIdentityStandardMet = true;
                me.setCurrentIdentity(Identity.Employer);
            }
            else {
                isIdentityStandardMet = false;
                me.setCurrentIdentity(Identity.Domestic_Activities);
            }
        }

        //If I want to be employed, but could not find employment, identity standard is not met
        else if (me.getCurrentEmploymentStatus() == Employment.Searching) {
            isIdentityStandardMet = false;
            me.setCurrentIdentity(Identity.Domestic_Activities);
        }

        //Is domestic activities identity standard met
        else if (me.getCurrentEmploymentStatus() == Employment.Inactive) {
            if (me.getHousehold().getDailyHouseholdDiscrepancy() < 0) {
                isIdentityStandardMet = false;
            }
            else {
                isIdentityStandardMet = true;
            }
            me.setCurrentIdentity(Identity.Domestic_Activities);
        }

        //otherwise, resident would like to go to school or work, but has not searched for school or employment yet, so still happy
        else {	
            isIdentityStandardMet = true;
        }

        return isIdentityStandardMet;
    }        
}

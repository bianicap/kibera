
package kibera;

import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.File;
import java.io.IOException;

/**
 * Parameters
 * 
 * Basic class that sets the value of all parameters at initialization
 * 
 * 
 * @author bpint
 */
public class Parameters {
    
    GlobalParamters global = new GlobalParamters();
  
    private final static String A_FILE = "-file";

    public Parameters(String[] args) {
        if (args != null) {
            loadParameters(openParameterDatabase(args));
        }
    }

    /**
     * Initialize parameter database from file
     *
     * If there exists an command line argument '-file', create a parameter
     * database from the file specified. Otherwise create an empty parameter
     * database.
     *
     * @param args contains command line arguments
     * @return newly created parameter data base
     *
     * @see loadParameters()
     */
    private static ParameterDatabase openParameterDatabase(String[] args) {
        ParameterDatabase parameters = null;
        for (int x = 0; x < args.length - 1; x++) {
            if (args[x].equals(A_FILE)) {
                try {
                    File parameterDatabaseFile = new File(args[x + 1]);
                    parameters = new ParameterDatabase(parameterDatabaseFile.getAbsoluteFile());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
        if (parameters == null) {
            System.out.println("\nNot in a parameter Mode");//("\nNo parameter file was specified");
            parameters = new ParameterDatabase();
        }
        return parameters;
    }

    private void loadParameters(ParameterDatabase parameterDB) {       
        // global - enterprise
      
        global.setNumResidents(returnIntParameter(parameterDB, "numResidents",
                 global.getNumResidents()));       
        global.setPreferenceforLivingNearLikeNeighbors(returnDoubleParameter(parameterDB, "preferenceforLivingNearLikeNeighbors",
                global.getPreferenceforLivingNearLikeNeighbors()));
        global.setNeighborhood(returnIntParameter(parameterDB, "neighborhood",
                global.getNeighborhood()));     
        global.setSchoolVision(returnIntParameter(parameterDB, "schoolVision",
                global.getSchoolVision()));
        global.setEmploymentVision(returnIntParameter(parameterDB, "employmentVision",
                global.getEmploymentVision()));
        global.setThreshold(returnDoubleParameter(parameterDB, "threshold",
                global.getThreshold()));       
        global.setSchoolCapacity(returnDoubleParameter(parameterDB, "schoolCapacity",
                global.getSchoolCapacity()));
        global.setFormalBusinessCapacity(returnDoubleParameter(parameterDB, "formalBusinessCapacity",
                global.getFormalBusinessCapacity()));
        global.setInformalBusinessCapacity(returnDoubleParameter(parameterDB, "informalBusinessCapacity",
                global.getInformalBusinessCapacity()));        
        global.setProbabilityOfLosingEmployment(returnDoubleParameter(parameterDB, "probabilityOfLosingEmployment",
                global.getProbabilityOfLosingEmployment()));               
        global.setEnergyRateOfChange(returnDoubleParameter(parameterDB, "energyRateOfChange",
                global.getEnergyRateOfChange()));
        global.setAggressionRate(returnDoubleParameter(parameterDB, "aggressionRate",
                global.getAggressionRate()));
        global.setAggressionThreshold(returnDoubleParameter(parameterDB, "aggressionThreshold",
                global.getAggressionThreshold()));
        global.setUniformAggressionRate(returnBooleanParameter(parameterDB, "uniformAggressionRate",
                global.isUniformAggressionRate()));      
        global.setOpinionThreshold(returnDoubleParameter(parameterDB, "opinionThreshold",
                global.getOpinionThreshold()));      
        global.setNumResidentsHearRumor(returnIntParameter(parameterDB, "numResidentsHearRumor",
                global.getNumResidentsHearRumor()));
        global.setNumResidentsHearNewRumor(returnIntParameter(parameterDB, "numResidentsHearNewRumor",
                global.getNumResidentsHearNewRumor()));      
        global.setNumResidentsSpreadRumorTo(returnIntParameter(parameterDB, "numResidentsSpreadRumorTo",
                global.getNumResidentsSpreadRumorTo()));  
        global.setContinueToPropogateOriginalRumor(returnBooleanParameter(parameterDB, "continueToPropogateOriginalRumor",
                global.isContinueToPropogateOriginalRumor()));  
        global.setPropogateNewRumor(returnBooleanParameter(parameterDB, "propogateNewRumor",
                global.isPropogateNewRumor()));
        global.setProportionInitialResidentsRebel(returnDoubleParameter(parameterDB, "proportionInitialResidentsRebel",
                global.getProportionInitialResidentsRebel()));
        global.setRemainRebel(returnBooleanParameter(parameterDB, "remainRebel",
                global.isRemainRebel()));
        global.setRemainRebelAfterNewRumor(returnBooleanParameter(parameterDB, "remainRebelAfterNewRumor",
                global.isRemainRebel())); 
        global.setMinutesInDay(returnIntParameter(parameterDB, "minutesInDay",
                global.getMinutesInDay()));
        global.setRunFullDay(returnBooleanParameter(parameterDB, "runFullDay",
                global.isRunFullDay()));
        global.setTimeNewRumor(returnIntParameter(parameterDB, "timeNewRumor",
                global.getTimeNewRumor()));
        global.setTimeOfFirstActivity(returnIntParameter(parameterDB, "timeOfFirstActivity",
                global.getTimeOfFirstActivity()));
        global.setOtherBasicCosts(returnDoubleParameter(parameterDB, "otherBasicCosts",
               global.getOtherBasicCosts()));
        global.setFormalOutsideKiberaCapacity(returnIntParameter(parameterDB, "formalOutsideKiberaCapacity",
                global.getFormalOutsideKiberaCapacity()));
        global.setInformalOutsideKiberaCapacity(returnIntParameter(parameterDB, "informalOutsideKiberaCapacity",
                global.getInformalOutsideKiberaCapacity()));
        
    }

    public int returnIntParameter(ParameterDatabase paramDB, String parameterName, int defaultValue) {
        return paramDB.getIntWithDefault(new Parameter(parameterName), null, defaultValue);
    }

    public boolean returnBooleanParameter(ParameterDatabase paramDB, String parameterName, boolean defaultValue) {
        return paramDB.getBoolean(new Parameter(parameterName), null, defaultValue);
    }

    double returnDoubleParameter(ParameterDatabase paramDB, String parameterName, double defaultValue) {
        return paramDB.getDoubleWithDefault(new Parameter(parameterName), null, defaultValue);
    }

    public class GlobalParamters {
      
        /** Total number of residents initialized in the model */
        public int numResidents = 117500; //source Marras, 2008
        
        
        /** Household preference for living near a "like" neighbor (a like neighbor is one that shares the same
         * ethnicity. A preference of 50% indicates that the household prefers that at least half of its neighbors share
         * the same ethnicity.
         */
        public double preferenceforLivingNearLikeNeighbors = .5;
        public int neighborhood = 1; //a neighborhood of 1 is equivalent to the Moore neighborhood

        /** Residents vision when searching for a school or employment. A vision of 1 indicates the agent can see
         * search for school/work out to one parcel from its home location.
         */
        public int schoolVision = 35; //the approximate size of two neighborhoods in Kibera
        public int employmentVision = 70; //the approximate size of four neighborhoods in Kibera
        public double threshold = 3;

        /** This is the capacity of students for each school */
        public double schoolCapacity = (.00075 * numResidents);

        /** This is the capacity of employees at a business. Formal businesses in Kibera include hospitals, schools,
         ** and religious facilities. The average number of employees at schools in Kibera are 13 (based on data from Map Kibera)
         ** Informal businesses include selling goods on the street, small restaurants, and markets.
         ** According to United Nations Human Settlement Program (2003) a maximum of 5 to 10 employees is used to define enterprises as informal
         */
        public double formalBusinessCapacity = (.00006 * numResidents);
        public double informalBusinessCapacity = (.00002 * numResidents);
        
        public int formalOutsideKiberaCapacity = 11195; //this value is determined while doing the initial assignment of agent employers
        public int informalOutsideKiberaCapacity = 5583; //this value is determined while doing the initial assignment of agent employers
    

        /** The probability that an agent will lose his/her job each day */
        public double probabilityOfLosingEmployment = 0.01;
        
        public double otherBasicCosts = 0; //other basic monthly costs, e.g., charcoal, clothes

        /** Variables related to an agent's aggression, and thus their energy reservoir. Adapted from
         * Burke and Stets (2001) Identity Model and Green (2001) Frustration-Aggression Hypothesis        
         */     
        public double energyRateOfChange = 50.0; //The rate of change in a resident's energy reservoir
        public double aggressionThreshold = 0.6; //The threshold a resident's aggression must be under in order for the resident to aggress or rebel
        public double aggressionRate = 0.6; //Aggression rate impacts the shape of the logistic curve. This can be either specified and kept the same for all agents, or each agent can be assigned its own rate
        public boolean uniformAggressionRate = true; //Identifies whether to assign all all agents the same aggression rate

        /** Variables associated with rumor propagation, rioting, and social influence */
        public boolean runFullDay = false;
        public int minutesInDay = 1440 - (7 * 60); //have to manually change number of hours in day in TimeManager object
        public int timeOfFirstActivity = 5 * 60; //The first activity begins at 5am

        public double opinionThreshold = 0.1; //Opinion threshold -- max difference between my final opinion and other, that would make me be influenced by other
        public int numResidentsHearRumor = (int) (numResidents * .001); //The number of random residents that hear the rumor initially
        public double proportionInitialResidentsRebel = 0.025; //The initial proportion of residents that heard the rumor that rebel
       
        public int timeNewRumor = minutesInDay * 7 * 4; //The point at which the new rumor can begin to spread (if there is a new rumor in the run)
        public int numResidentsHearNewRumor = (int) 10; //The number of random residents that hear the new rumor (if there is a new rumor in the run)
        public int numResidentsSpreadRumorTo = 1; //The number of residents a resident will spread the rumor to at a goal location (if the resident has heard the rumor)

        /** These variables can be set to true/false depending on what scenarios user would like to run */ 
        public boolean propogateNewRumor = false;
        public boolean continueToPropogateOriginalRumor = true;

        /** This determines if all initial rebels will remain rebels throughout the course of the model run */
        public boolean remainRebel = true;
        public boolean remainRebelAfterNewRumor = true;
        public boolean proximateExperiment = false;
        
        
        /** Getters and setters */
        public int getNumResidents() { return numResidents; }
        public void setNumResidents(int numResidents) { this.numResidents = numResidents; }

        public double getPreferenceforLivingNearLikeNeighbors() { return preferenceforLivingNearLikeNeighbors; }
        public void setPreferenceforLivingNearLikeNeighbors(double preferenceforLivingNearLikeNeighbors) { this.preferenceforLivingNearLikeNeighbors = preferenceforLivingNearLikeNeighbors; }

        public int getNeighborhood() { return neighborhood; }
        public void setNeighborhood(int neighborhood) { this.neighborhood = neighborhood; }

        public int getSchoolVision() { return schoolVision; }
        public void setSchoolVision(int schoolVision) { this.schoolVision = schoolVision; }

        public int getEmploymentVision() { return employmentVision; }
        public void setEmploymentVision(int employmentVision) { this.employmentVision = employmentVision; }

        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        
        public double getSchoolCapacity() { return schoolCapacity; }
        public void setSchoolCapacity(double schoolCapacity) { this.schoolCapacity = schoolCapacity; }

        public double getFormalBusinessCapacity() { return formalBusinessCapacity; }
        public void setFormalBusinessCapacity(double formalBusinessCapacity) { this.formalBusinessCapacity = formalBusinessCapacity; }

        public double getInformalBusinessCapacity() { return informalBusinessCapacity; }
        public void setInformalBusinessCapacity(double informalBusinessCapacity) { this.informalBusinessCapacity = informalBusinessCapacity; }

        public double getProbabilityOfLosingEmployment() { return probabilityOfLosingEmployment; }
        public void setProbabilityOfLosingEmployment(double probabilityOfLosingEmployment) { this.probabilityOfLosingEmployment = probabilityOfLosingEmployment; }

        public double getEnergyRateOfChange() { return energyRateOfChange; }
        public void setEnergyRateOfChange(double energyRateOfChange) { this.energyRateOfChange = energyRateOfChange; }

        public double getAggressionThreshold() { return aggressionThreshold; }
        public void setAggressionThreshold(double aggressionThreshold) { this.aggressionThreshold = aggressionThreshold; }

        public double getAggressionRate() { return aggressionRate; }
        public void setAggressionRate(double aggressionRate) { this.aggressionRate = aggressionRate; }

        public boolean isUniformAggressionRate() { return uniformAggressionRate; }
        public void setUniformAggressionRate(boolean uniformAggressionRate) { this.uniformAggressionRate = uniformAggressionRate; }

        public boolean isRunFullDay() { return runFullDay; }
        public void setRunFullDay(boolean runFullDay) { this.runFullDay = runFullDay; }

        public int getMinutesInDay() { return minutesInDay; }
        public void setMinutesInDay(int minutesInDay) { this.minutesInDay = minutesInDay; }

        public int getTimeOfFirstActivity() { return timeOfFirstActivity; }
        public void setTimeOfFirstActivity(int timeOfFirstActivity) { this.timeOfFirstActivity = timeOfFirstActivity; }

        public double getOpinionThreshold() { return opinionThreshold; }
        public void setOpinionThreshold(double opinionThreshold) { this.opinionThreshold = opinionThreshold; }

        public int getNumResidentsHearRumor() { return numResidentsHearRumor; }
        public void setNumResidentsHearRumor(int numResidentsHearRumor) { this.numResidentsHearRumor = numResidentsHearRumor; }

        public double getProportionInitialResidentsRebel() { return proportionInitialResidentsRebel; }
        public void setProportionInitialResidentsRebel(double proportionInitialResidentsRebel) { this.proportionInitialResidentsRebel = proportionInitialResidentsRebel; }

        public int getTimeNewRumor() { return timeNewRumor; }
        public void setTimeNewRumor(int timeNewRumor) { this.timeNewRumor = timeNewRumor; }

        public int getNumResidentsHearNewRumor() { return numResidentsHearNewRumor; }
        public void setNumResidentsHearNewRumor(int numResidentsHearNewRumor) { this.numResidentsHearNewRumor = numResidentsHearNewRumor; }

        public int getNumResidentsSpreadRumorTo() { return numResidentsSpreadRumorTo; }
        public void setNumResidentsSpreadRumorTo(int numResidentsSpreadRumorTo) { this.numResidentsSpreadRumorTo = numResidentsSpreadRumorTo; }

        public boolean isPropogateNewRumor() { return propogateNewRumor; }
        public void setPropogateNewRumor(boolean propogateNewRumor) { this.propogateNewRumor = propogateNewRumor; }

        public boolean isContinueToPropogateOriginalRumor() { return continueToPropogateOriginalRumor; }
        public void setContinueToPropogateOriginalRumor(boolean continueToPropogateOriginalRumor) { this.continueToPropogateOriginalRumor = continueToPropogateOriginalRumor; }

        public boolean isRemainRebel() { return remainRebel; }
        public void setRemainRebel(boolean remainRebel) { this.remainRebel = remainRebel; }

        public boolean isRemainRebelAfterNewRumor() { return remainRebelAfterNewRumor; }
        public void setRemainRebelAfterNewRumor(boolean remainRebelAfterNewRumor) { this.remainRebelAfterNewRumor = remainRebelAfterNewRumor; }
        
        public double getOtherBasicCosts() { return otherBasicCosts; }
        public void setOtherBasicCosts(double val) { otherBasicCosts = val; }
        
        public int getFormalOutsideKiberaCapacity() { return formalOutsideKiberaCapacity; }
        public void setFormalOutsideKiberaCapacity(int val) { formalOutsideKiberaCapacity = val; }
        
        public int getInformalOutsideKiberaCapacity() { return informalOutsideKiberaCapacity; }
        public void setInformalOutsideKiberaCapacity(int val) { informalOutsideKiberaCapacity = val; }
       
    }
}

package kibera;

import kibera.Resident.Identity;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;

/**
 * The household agent is made up of one or more individual, resident agents
 * 
 * 
 * @author bpint
 *
 */

public class Household implements Steppable {
    
    /** The residents living within the same household */
    private Bag householdMembers;
    public Bag getHouseholdMembers() { return householdMembers; }
    public void addHouseholdMembers(Resident val) { householdMembers.add(val); }
    public void removeHouseholdMember(Resident val) { householdMembers.remove(val); }
    public void setHouseholdMembers(Bag val) { householdMembers = val; }

    /** The home a household is located within */
    private Home home;
    public Home getHome() { return home; }
    public void setHome(Home val) { home = val; }

    /** Total expenditures for household */
    private double dailyHouseholdExpenditures;
    public double getDailyHouseholdExpenditures() { return dailyHouseholdExpenditures; }
    public void setDailyHouseholdExpenditures(double val) { dailyHouseholdExpenditures = val; }

    /** Total monthly income for the household */
    private double householdIncome;
    public double getHouseholdIncome() { return householdIncome; }
    public void setHouseholdIncome(double val) { householdIncome = val; }

    /** The discrepancy between a household's income and its expenditures on a daily basis */
    private double dailyHouseholdDiscrepancy;
    public double getDailyHouseholdDiscrepancy() { return dailyHouseholdDiscrepancy; }
    public void setDailyHouseholdDiscrepancy(double val) { dailyHouseholdDiscrepancy = val; }

    /** Amount of water remaining in household */
    private double remainingWater;
    public double getRemainingWater() { return remainingWater; }
    public void setRemainingWater(double val) { remainingWater = val; }

    /** The daily household income */
    private double dailyHouseholdIncome;
    public double getDailyHouseholdIncome() { return dailyHouseholdIncome; }
    public void setDailyHouseholdIncome(double val) { dailyHouseholdIncome = val; }

    /** The daily household cost for food */
    private double foodCost;
    public void setDailyFoodCost(double val) { foodCost = val; }
    public double getDailyFoodCost() { return foodCost; }

    private double desiredFoodCost;
    public void setDesiredFoodCost(double val) { desiredFoodCost = val; }
    public double getDesiredFoodCost() { return desiredFoodCost; }

    /** The daily household cost for water */
    private double waterCost;
    public void setDailyWaterCost(double val) { waterCost = val; }
    public double getDailyWaterCost() { return waterCost; }

    /** The average daily cost of electricity */
    private double electricCost;
    public void setDailyElectricCost(double val) { electricCost = val; }
    public double getDailyElectricCost() { return electricCost; }
    
    private double desiredElectricCost;
    public void setDesiredElectricCost(double val) { desiredElectricCost = val; }
    public double getDesiredElectricCost() { return desiredElectricCost; }   
    
    /** The average daily cost of transportation if the resident is a student or employee */
    private double transportationCost;
    public void setDailyTransportationCost(double val) { transportationCost = val; }
    public double getDailyTransportationCost() { return transportationCost; }
    
    private double desiredTransportationCost;
    public void setDesiredTransportationCost(double val) { desiredTransportationCost = val; }
    public double getDesiredTransportationCost() { return desiredTransportationCost; }
    
    /** The average daily cost of other basic expenditures */
    private double otherBasicCost;
    public void setDailyOtherBasicCost(double val) { otherBasicCost = val; }
    public double getDailyOtherBasicCost() { return otherBasicCost; }

    /** The daily household cost of sanitation */
    private double sanitationCost;
    public void setDailySanitationCost(double val) { sanitationCost = val; }
    public double getDailySanitationCost() { return sanitationCost; }

    private double desiredSanitationCost;
    public void setDesiredSanitationCost(double val) { desiredSanitationCost = val; }
    public double getDesiredSanitationCost() { return desiredSanitationCost; }
    
    private double desiredOtherBasicCosts;
    public void setDesiredOtherBasicCosts(double val) { desiredOtherBasicCosts = val; }
    public double getDesiredOtherBasicCosts() { return desiredOtherBasicCosts; }

    /** This identifies whether a household had to adjust expenses due to insufficient income */
    public enum AdjustedHouseholdExpenditures { Decreased, Increased, Same};
    Household.AdjustedHouseholdExpenditures adjustedHouseholdExpenditures;
    public Household.AdjustedHouseholdExpenditures getAdjustedHouseholdExpenditures() { return adjustedHouseholdExpenditures; }
    public void setAdjustedHouseholdExpenditures(Household.AdjustedHouseholdExpenditures val) { adjustedHouseholdExpenditures = val; }

    /** Identifies whether had to remove a household member from school to help pay expenses */
    private boolean removedStudentFromSchool;
    public boolean removedStudentFromSchool() { return removedStudentFromSchool; }
    public void removedStudentFromSchool(boolean val) { removedStudentFromSchool = val; }

    /** Time a student left school and began searching for employment due to household need */
    private int timeLeftSchool;
    public int getTimeLeftSchool() { return timeLeftSchool; }
    public void setTimeLeftSchool(int val) { timeLeftSchool = val; }

    /** Identifies whether an inactive resident began searching for employment to help pay expenses */
    private boolean removedFromInactive;
    public boolean removedFromInactive() { return removedFromInactive; }
    public void removedFromInactive(boolean val) { removedFromInactive = val; }

    /** Time an inactive resident began searching for employment due to household need */
    private int timeLeftInactive;
    public int getTimeLeftInactive() { return timeLeftInactive; }
    public void setTimeLeftInactive(int val) { timeLeftInactive = val; }

    /** The current time step in the simulation */
    private int cStep;
    public int getCStep() { return cStep; }
    public void setCStep(int val) { cStep = val; }

    /** The current minute in the day (one day is 1440 minutes or time steps) */
    private int minuteInDay;
    public int getMinuteInDay() { return minuteInDay; }
    public void setMinuteInDay(int val) { minuteInDay = val; }

    Kibera kibera;

    public Household(Home h) {
        this.home = h;
        householdMembers = new Bag();
    }

    public Household() {
        householdMembers = new Bag();
    }

    @Override
    public void step(SimState state) {

        kibera = (Kibera)state;
        cStep = (int) kibera.schedule.getSteps();

        if(cStep < kibera.params.global.getMinutesInDay()) { minuteInDay = cStep; }
        else { minuteInDay = cStep % kibera.params.global.getMinutesInDay(); }
        
        //calcuate all initial household costs at beginning of simulation
        if (cStep == 0) {
            this.calculateDailyFoodCost(kibera);           
            this.calculateDailySanitationCost();
            this.calculateDailyTransportationCost(kibera);
            this.calculateDailyElectriCost();
            this.calculateDailyHouseholdIncome();
            this.setDesiredFoodCost(this.getDailyFoodCost());
            this.setDesiredSanitationCost(this.getDailySanitationCost());
            this.setDesiredElectricCost(this.getDailyElectricCost());
            this.setDesiredTransportationCost(this.getDailyTransportationCost());
            this.setDesiredOtherBasicCosts(this.getDailyOtherBasicCost());
        }

        if (minuteInDay == 0) {
            this.calculateDailyHouseholdExpenditures(kibera);
            this.calculateDailyHouseholdIncome();
            this.calculateDailyHouseholdDiscrepancy(kibera);
            this.adjustHouseholdExpenditures(kibera);	
        }            
    }

    /**
     * 
     * @return the households ethnicity
     */ 
    public String getHouseholdEthnicity() {
        String residentEthnicity = null;

        for (int i = 0; i < householdMembers.numObjs; i++) {
            Resident r = (Resident) householdMembers.objs[i];
            residentEthnicity = r.getEthnicity();           
        }	
        return residentEthnicity;
    }
    
    /**
     * Calculate the households daily electric cost if the home has electricity
     * 
     */
    public void calculateDailyElectriCost() {
        double eCost = this.getHome().getExpectedElectricityCost() / 30;
        this.setDailyElectricCost(eCost);
    }

    /**
     * Calculate the households daily sanitation costs if the home does not have its own sanitation
     * 
     */
    public void calculateDailySanitationCost() {
        double sanitation = 0;		
        double residentSanitationCost = 0;

        if(!home.hasSanitation()) {
            for (int i = 0; i < householdMembers.numObjs; i++) {
                residentSanitationCost = kibera.random.nextInt((int)kibera.sanitationCost * 5);
            }
            sanitation = sanitation + residentSanitationCost;
        }
        this.setDailySanitationCost(sanitation);
    }

    /**
     * Calculate the households daily food costs
     * 
     */
    public void calculateDailyFoodCost(Kibera kibera) {
        double cost;
        double fCost = 0;
        
        //calcuate the food costs per household member
        for (int i = 0; i < householdMembers.numObjs; i++) {
            Resident r = (Resident) householdMembers.get(i);
            
            //if the resident is a student, they get lunch free at school
            if (r.currentIdentity == Identity.Student) {
                cost = kibera.foodCostPerMeal * r.getAdultEquivalent() * 2;
            }
            else {
                cost = kibera.foodCostPerMeal * r.getAdultEquivalent() * 3;
            }
            fCost = fCost + cost;
        }
        this.setDailyFoodCost(fCost);
    }
    
   /**
     * Calculate the households daily water costs
     * 
     */
    public void calculateDailyWaterCost() {
        //run this at the beginning of each day (time 0)
        
        double cost;
        double wCost = 0;
        
        for (int i = 0; i < householdMembers.numObjs; i++) {
            Resident r = (Resident) householdMembers.get(i);
            cost = r.getAdultEquivalent() * kibera.barrelOfWaterCost;
            wCost = wCost + cost;
        }
        
        this.setDailyWaterCost(wCost);
    }
    
    /**
     * Calculate the households daily transportation costs
     * 
     */
    public void calculateDailyTransportationCost(Kibera kibera) {
        double tCost = 0;
        double cost = 0;
 
        for (int i = 0; i < householdMembers.numObjs; i++) {
            Resident r = (Resident) householdMembers.get(i);
            if (r.getCurrentIdentity() == Identity.Employer || r.getCurrentIdentity() == Identity.Student) {
                cost = kibera.transportationCost * r.getAdultEquivalent();
            }
            tCost = tCost + cost;
        }
        this.setDailyTransportationCost(tCost);
    }
    
    /**
     * Calculate any other daily household costs (if applicable)
     * 
     */
    public void calculateDailyOtherBasicCost() {
        double otherCost = 0;
        double cost;
 
        for (int i = 0; i < householdMembers.numObjs; i++) {
            Resident r = (Resident) householdMembers.get(i);
            cost = r.getAdultEquivalent() * kibera.params.global.getOtherBasicCosts();
            otherCost = otherCost + cost;
        }
        this.setDailyOtherBasicCost(otherCost);
    }
    
    /**
     * Calculate the households total costs
     * 
     * @param kibera
     * 
     */
    public void calculateDailyHouseholdExpenditures(Kibera kibera) {
        //run this at the beginning of each day (time 0)
        //calculate daily expenditures
        double rent = home.getHouseRent() / 30;
        this.calculateDailyFoodCost(kibera);
        this.calculateDailyTransportationCost(kibera);
        
        //sum up all household daily costs
        double expenditures = rent + this.getDailyWaterCost() + this.getDailyElectricCost() + this.getDailySanitationCost()
                        + this.getDailyFoodCost() + this.getDailyTransportationCost() + this.getDailyOtherBasicCost();

        this.setDailyHouseholdExpenditures(expenditures);
    }
    
    /**
     * Determine if the household's water supply is low and the household needs to get more water
     * 
     * @param kibera
     * 
     * @return true if the household needs water, false otherwise
     * 
     */
    public boolean needWater(Kibera kibera) {
        if (!home.hasWater() && ((kibera.waterRequirement * householdMembers.numObjs) > remainingWater)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Calculate the household's total income
     * 
     */
    public void calculateHouseholdIncome() {
        double income = 0;
        
        //sum the income of individual household members
        for (int i = 0; i < householdMembers.numObjs; i++) {
            Resident r = (Resident) householdMembers.get(i);
            income = income + r.getResidentIncome();
        }

        this.setHouseholdIncome(income);
    }
    
    /**
     * Calculate the household's discrepancy
     * 
     * @param kibera
     * 
     */
    public void calculateDailyHouseholdDiscrepancy(Kibera kibera) {
        //discrepancy is the disparity between the household's daily income and daily costs
        int discrepancy = (int) (this.getDailyHouseholdIncome() - this.getDailyHouseholdExpenditures());		
        this.setDailyHouseholdDiscrepancy(discrepancy);
    }
    
    /**
     * Calculate the household's daily income
     * 
     */
    public void calculateDailyHouseholdIncome() {
        this.calculateHouseholdIncome();
        //daily is income is the household's total (monthly) income divided by 30
        double income = getHouseholdIncome() / 30;

        if (income <= 1) { income = 1; }
        this.setDailyHouseholdIncome(income);
    }

    /**
     * If daily expenditures exceeds or is below than income, adjust some of the expenditures
     * 
     * @param kibera
     * 
     */
    public void adjustHouseholdExpenditures(Kibera kibera) {
        
        //if household expenditures are higher than household income
        if (this.getDailyHouseholdDiscrepancy() < 0) {

            //if former student has had enough time to search for employment but household discrepancy still exists
             if ((this.removedStudentFromSchool() && (this.getCStep() - this.getTimeLeftSchool() >= kibera.params.global.getMinutesInDay())) ||
                    (this.removedFromInactive() && (this.getCStep() - this.getTimeLeftInactive() >= kibera.params.global.getMinutesInDay()))) {
                if (!home.hasSanitation()) {
                    double sCost;
                    if (getDailyHouseholdDiscrepancy() <= (-1 * getDailySanitationCost())) {
                        sCost = 0;
                    }
                    else {
                        sCost = getDailySanitationCost() - (int) (-1*this.getDailyHouseholdDiscrepancy());
                    }
                    setDailySanitationCost(sCost);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Decreased;
                }
                
                this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                        + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

                //re-calculate household discrepancy
                this.calculateDailyHouseholdDiscrepancy(kibera);

                
                //lower electricity cost if sanitation is not enough
                if (getDailyHouseholdDiscrepancy() < 0) {
                                  
                    if (home.hasElectricity()) {
                        double eCost;
                        if (this.getDailyHouseholdDiscrepancy() <= (-1 * this.getDailyElectricCost())) {
                            eCost = 0;
                        }
                        else {
                            eCost = getDailyElectricCost() - (int) (-1*this.getDailyHouseholdDiscrepancy());
                        }
                    this.setDailyElectricCost(eCost);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Decreased;
                    }
              
                    this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                            + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

                    //re-calculate household discrepancy
                    this.calculateDailyHouseholdDiscrepancy(kibera);
                }
                
                
                //lower other basic costs cost if sanitation and electric is not enough
                if (getDailyHouseholdDiscrepancy() < 0) {
                
                    double oCost;
                    double otherCostsEligileToReduce = this.getDailyOtherBasicCost() * 0.8;
                    
                    if (this.getDailyHouseholdDiscrepancy() <= (-1 * otherCostsEligileToReduce)) {
                        oCost = this.getDailyOtherBasicCost() - otherCostsEligileToReduce;
                    }
                    else {
                        oCost = otherCostsEligileToReduce - (int) (-1*this.getDailyHouseholdDiscrepancy());
                    }
                 
                    this.setDailyOtherBasicCost(oCost);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Decreased;
 
                    //lower other basic costs costs if sanitation and electric is not enough
                    this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                            + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

                    //re-calculate household discrepancy
                    this.calculateDailyHouseholdDiscrepancy(kibera);
                }
                
                //lower transportation cost if sanitation, electric, and other costs is not enough
                if (getDailyHouseholdDiscrepancy() < 0) {
                
                    double tCost;
                    if (this.getDailyHouseholdDiscrepancy() <= (-1 * this.getDailyTransportationCost())) {
                        tCost = 0;
                    }
                    else {
                        tCost = getDailyTransportationCost() - (int) (-1*this.getDailyHouseholdDiscrepancy());
                    }
                    
                    this.setDailyTransportationCost(tCost);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Decreased;
 
                    //lower transportation costs if sanitation and electric is not enough
                    this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                            + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

                    //re-calculate household discrepancy
                    this.calculateDailyHouseholdDiscrepancy(kibera);
                }
                
                
                
                //finally, lower food costs if discrepancy still exists
                if (getDailyHouseholdDiscrepancy() < 0) {
                    //first try removing lunch cost
                    double currentFoodCost = getDailyFoodCost();

                    //amount saved if forego one meal/day
                    //average cost of one meal
                    double avgCostOneMeal = this.getDailyFoodCost() / householdMembers.numObjs; //average cost of food for one day for a resident in household
                    avgCostOneMeal = avgCostOneMeal / 3; //average cost of one meal per resident
                    
                    double foodSavings = currentFoodCost - householdMembers.numObjs * avgCostOneMeal * 2;

                    if ((-1*getDailyHouseholdDiscrepancy()) <= foodSavings ) {
                            setDailyFoodCost(householdMembers.numObjs * avgCostOneMeal * 2);
                    }
                    else { //family will have to skip 2 meals per day
                            setDailyFoodCost(householdMembers.numObjs * avgCostOneMeal);
                    }

                    this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                            + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);
                    
                    this.calculateDailyHouseholdDiscrepancy(kibera);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Decreased;

                }
            }
        }

        //if have more than enough income
        if (this.getDailyHouseholdDiscrepancy() > 0) {
            //if removed sanitation costs previously, add those back
            if (!home.hasSanitation() && (this.getDailySanitationCost() < this.getDesiredSanitationCost())) {
                double sCost;
                if (getDailyHouseholdDiscrepancy() > this.getDesiredSanitationCost()) {
                    sCost = this.getDesiredSanitationCost();
                }
                else {
                    //sanitationCost = getDailySanitationCost() - (int) (((-1*getDailyHouseholdDiscrepancy()) / kibera.getSanitationCost()) * kibera.getSanitationCost());
                    sCost = this.getDesiredSanitationCost() - (this.getDailyHouseholdDiscrepancy());
                }
                setDailySanitationCost(sCost);
                this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Increased;
            }

            //increase food costs if still enough income
            this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                    + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

            //re-calculate household discrepancy
            this.calculateDailyHouseholdDiscrepancy(kibera);

            if (getDailyHouseholdDiscrepancy() > 0) {
                if (this.getDailyFoodCost() < this.getDesiredFoodCost()) {
                    double food;
                    if (this.getDailyHouseholdDiscrepancy() > this.getDesiredFoodCost()) {
                        food = this.getDesiredFoodCost();
                    }
                    else {
                        food = this.getDesiredFoodCost() - (int) (this.getDailyHouseholdDiscrepancy());
                    }
                    this.setDailyFoodCost(food);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Increased;
                }

                this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                        + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

                this.calculateDailyHouseholdDiscrepancy(kibera);              
                
            }
            
            //increase other basic cost if still discrepancy
            if (getDailyHouseholdDiscrepancy() > 0) {
                if (this.getDailyOtherBasicCost() < this.getDesiredOtherBasicCosts()) {
                    double otherCosts;
                    if (this.getDailyHouseholdDiscrepancy() > this.getDesiredOtherBasicCosts()) {
                        otherCosts = this.getDesiredOtherBasicCosts();
                    }
                    else {
                        otherCosts = this.getDesiredOtherBasicCosts() - (int) (this.getDailyHouseholdDiscrepancy());
                    }
                    this.setDailyOtherBasicCost(otherCosts);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Increased;
                }

                this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                        + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

                this.calculateDailyHouseholdDiscrepancy(kibera);
            }
            
            
            //increase electric cost if still discrepancy
            if (getDailyHouseholdDiscrepancy() > 0) {
                if (this.getDailyElectricCost() < this.getDesiredElectricCost()) {
                    double electric;
                    if (this.getDailyHouseholdDiscrepancy() > this.getDesiredElectricCost()) {
                        electric = this.getDesiredElectricCost();
                    }
                    else {
                        electric = this.getDesiredElectricCost() - (int) (this.getDailyHouseholdDiscrepancy());
                    }
                    this.setDailyElectricCost(electric);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Increased;
                }

                this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                        + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

                this.calculateDailyHouseholdDiscrepancy(kibera);
            }
            
            //increase transportation cost if still discrepancy
            if (getDailyHouseholdDiscrepancy() > 0) {
                if (this.getDailyTransportationCost() < this.getDesiredTransportationCost()) {
                    double transportation;
                    if (this.getDailyHouseholdDiscrepancy() > this.getDesiredTransportationCost()) {
                        transportation = this.getDesiredTransportationCost();
                    }
                    else {
                        transportation = this.getDesiredTransportationCost() - (int) (this.getDailyHouseholdDiscrepancy());
                    }
                    this.setDailyTransportationCost(transportation);
                    this.adjustedHouseholdExpenditures = AdjustedHouseholdExpenditures.Increased;
                }

                this.setDailyHouseholdExpenditures(this.getDailyOtherBasicCost() + this.getDailyElectricCost() + this.getDailyFoodCost()
                        + this.getDailySanitationCost() + this.getDailyWaterCost() + this.getHome().getHouseRent() / 30);

                this.calculateDailyHouseholdDiscrepancy(kibera);
            }
        } 
    }
    
    /**
     * Determine the household's "happiness" level. Household's that have no household discrepancy and did not need to
     * cut any expenses are the "happiest", household's that that have no household discrepancy after cutting some costs
     * are somewhat happy, and household that have a household discrepancy after cutting costs are the least happy
     * 
     * @param kibera
     * 
     */
    public int householdHappiness(Kibera kibera) {
    
        int happinessLevel; //0 = not happy, 1 = somewhat happy, 2 = happy

        //If household is able to pay for all expenditures without cutting any costs, then the household is happy
        if (getDailyHouseholdDiscrepancy() >= 0 && (this.adjustedHouseholdExpenditures == AdjustedHouseholdExpenditures.Same
                || this.adjustedHouseholdExpenditures == AdjustedHouseholdExpenditures.Increased)) {
            happinessLevel = 2;
        }
        //Otherwise, household will need to cut costs
        else if (getDailyHouseholdDiscrepancy() >= 0 && this.adjustedHouseholdExpenditures == AdjustedHouseholdExpenditures.Decreased) {
            happinessLevel = 1;
        }
        //If household is unable to pay for necessary expenditures after cutting costs, then household is unhappy
        else {
            happinessLevel = 0;
        }

        return happinessLevel;
    }


    @Override
    public String toString() {
        return home.getStructure().getParcel().toString();
    }	
}

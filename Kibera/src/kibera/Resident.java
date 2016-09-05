package kibera;

import java.util.ArrayList;
import kibera.KiberaBuilder.Node;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Valuable;

/**
 * The resident agent represents the individuals in the model
 * 
 * 
 * @author bpint
 *
 */
public class Resident implements Steppable, Valuable, java.io.Serializable {
	
    /** The unique identifier of a resident */
    private int residentID;
    public int getResidentID() { return residentID; }
    public void setResidentID(int val) { residentID = val; }

    /** The household a resident belongs to */
    private Household household;
    public Household getHousehold() { return household; }
    public void setHousehold(Household val) { household = val; }

    /** The residents current age */
    private int age;
    public int getAge() { return age; }
    public void setAge(int val) { age = val; }
    
    /** The residents assigned ethnicity */
    private String ethnicity;
    public String getEthnicity() { return ethnicity; }
    public void setEthnicity(String val) { ethnicity = val; }

    /** Identifies whether the resident is head of his/her household */
    private boolean isHeadOfHousehold;
    public boolean isHeadOfHousehold() { return isHeadOfHousehold; }
    public void isHeadOfHousehold(boolean val) { isHeadOfHousehold = val; }

    /** The parcel a resident is currently on */
    private Parcel currentPosition;
    public Parcel getPosition() { return currentPosition; }
    public void setPosition(Parcel val) { currentPosition = val; }

    /** The parcel a resident is headed to in order to execute his/her goal */
    private Parcel goalLocation;
    public Parcel getGoalLocation() { return goalLocation; }
    public void setGoalLocation(Parcel val) { this.goalLocation = val; }

    /** Identifies whether a resident is eligible to be a student -- currently this just means they are the right age */
    private boolean isSchoolEligible;
    public boolean isSchoolEligible() { return isSchoolEligible; }
    public void isSchoolEligible(boolean val) { isSchoolEligible = val; }

    /** If resident has found a school, keep going to same school each school day */
    private School mySchool;
    public School getMySchool() { return mySchool; }
    public void setMySchool(School val) { mySchool = val; }

    /** Identity whether a resident searched for a school (the resident may or may not have found a school to attend) */
    private boolean searchedForSchool;
    public boolean searchedForSchool() { return searchedForSchool; }
    public void searchedForSchool(boolean val) { searchedForSchool = val; }

    /** If resident has found informal employment, keep going to business each day */
    private Business myBusinessEmployer;
    public Business getMyBusinessEmployer() { return myBusinessEmployer; }
    public void setMyBusinessEmployer(Business val) { myBusinessEmployer = val; }

    /** If resident found formal employment at a school, keep going to that school each day */
    private School mySchoolEmployer;
    public School getMySchoolEmployer() { return mySchoolEmployer; }
    public void setMySchoolEmployer(School val) { mySchoolEmployer = val; }

    /** If resident found formal employment at a health facility, keep going to that health facility each day */
    private HealthFacility myHealthFacilityEmployer;
    public HealthFacility getMyHealthFacilityEmployer() { return myHealthFacilityEmployer; }
    public void setMyHealthFacilityEmployer(HealthFacility val) { myHealthFacilityEmployer = val; }

    /** If resident found formal employment at a religious facility, keep going to that health facility each day */
    private ReligiousFacility myReligiousFacilityEmployer;
    public ReligiousFacility getMyReligiousFacilityEmployer() { return myReligiousFacilityEmployer; }
    public void setMyReligiousFacilityEmployer(ReligiousFacility val) { myReligiousFacilityEmployer = val; }
    
    private OutsideKibera employmentOutsideKibera;
    public OutsideKibera getEmploymentOutsideKibera() { return employmentOutsideKibera; }
    public void setEmploymentOutsideKibera(OutsideKibera val) { employmentOutsideKibera = val; }
    
    /** If a resident gets a job outside of Kibera, this tracks whether the job is formal or informal */
    public enum EmploymentTypeOutsideKibera { Formal, Informal };
    EmploymentTypeOutsideKibera employmentTypeOutsideKibera;
    public EmploymentTypeOutsideKibera getEmploymentTypeOutsideKibera() { return employmentTypeOutsideKibera; }
    public void setEmploymentTypeOutsideKibera(EmploymentTypeOutsideKibera val) { employmentTypeOutsideKibera = val; }           

    /** The resident's income */
    private double residentIncome;
    public double getResidentIncome() { return residentIncome; }
    public void setResidentIncome(double val) { residentIncome = val; }
    
    /** The resident's adult equivalent which is dependent on age and is used to determine the individuals daily cost for food and
     * other basic expenditures
     */
    private double adultEquivalent;
    public double getAdultEquivalent() { return adultEquivalent; }
    public void setAdultEquivalent(double val) { adultEquivalent = val; }

    /** A resident's gender */
    public enum Gender { male, female };
    Gender gender;
    public Gender getGender() { return gender; }
    public void setGender(Gender val) { gender = val; }	

    /** The set of resident's potential goals */
    public enum Goal { Find_Employment, Go_to_Work, Get_An_Education, Stay_Home, Socialize, Go_to_Religious_Institution, Get_Water, Rebel };
    Goal currentGoal = Goal.Stay_Home;
    public Goal getCurrentGoal() { return currentGoal; }
    public void setCurrentGoal(Goal val) { currentGoal = val; }
    
    public enum AgeGroup { age0to2, age3to5, age6to18, age19andOver };
    AgeGroup ageGroup;
    public AgeGroup getAgeGroup() { return ageGroup; }
    public void setAgeGroup(AgeGroup val) { ageGroup = val; }

    /** The employment status of a resident */
    public enum Employment { Formal, Informal, Searching, Inactive };
    Employment currentEmploymentStatus;
    public Employment getCurrentEmploymentStatus() { return currentEmploymentStatus; }
    public void setCurrentEmploymentStatus(Employment val) { currentEmploymentStatus = val; }

    /** The set of potential identities a resident can have */
    public enum Identity { Student, Employer, Domestic_Activities, Rebel };
    Identity currentIdentity;
    public Identity getCurrentIdentity() { return currentIdentity; }
    public void setCurrentIdentity(Identity val) { currentIdentity = val; }

    /** The set of potential religions */
    public enum Religion { Christian, Muslim, Other };
    Religion religion;
    public Religion getReligion() { return religion; }
    public void setReligion(Religion val) { religion = val; }
    
    /** For christian residents, this helps assign the time they will attend church */
    private int churchRnd;
    public int getChurchRnd() { return churchRnd; }
    public void setChurchRnd(int val) { churchRnd = val; }

    /** Residents energy reservoir, value from 1 to 100 */
    private double energy;
    public double getEnergy() { return energy; }
    public void setEnergy(double val) { energy = val; }

    /** Identifies if the resident was laid off from his/her job */
    private boolean isLaidOff;
    public boolean isLaidOff() { return isLaidOff; }
    public void isLaidOff(boolean val) { isLaidOff = val; }
    
    /** Identifies if the resident had to leave school to find employment */
    private boolean leftSchool;
    public boolean leftSchool() { return leftSchool; }
    public void leftSchool(boolean val) { leftSchool = val; }

    /** Identifies whether resident has attended church/mosque this week */
    private boolean attendedReligiousFacility;
    public boolean attendedReligiousFacility() { return attendedReligiousFacility; }
    public void attendedReligiousFacility(boolean val) { attendedReligiousFacility = val; }

    /** This is the rate of the logistic curve. The higher the rate, the slower someone is to aggress. */
    private double aggressionRate;
    public double getAggressionRate() { return aggressionRate; }
    public void setAggressionRate(double val) { aggressionRate = val; }

    /** This is the current aggression value of the resident. This is a function of the resident's energy reservoir */
    private double aggressionValue;
    public double getAggressionValue() { return aggressionValue; }
    public void setAggressionValue(double val) { aggressionValue = val; }
    
    /** This is the amount of time steps a resident has been performing the current goal */
    private int timeInGoalStart;
    public int getTimeInGoalStart() { return timeInGoalStart; }
    public void setTimeInGoalStart(int val) { timeInGoalStart = val; }
    
    private int timeInGoal;
    public int getTimeInGoal() { return timeInGoal; }
    public void setTimeInGoal(int val) { timeInGoal = val; }
    
    /** This is the day a resident found employment -- resident will start working the following day */
    private int dayFoundEmployment;
    public int getDayFoundEmployment() { return dayFoundEmployment; }
    public void setDayFoundEmployment(int val) { dayFoundEmployment = val; }

    /** the agent's current path to its current goal */
    ArrayList<Parcel> path = null;

    /** The current time step in the simulation */
    private int cStep;
    public int getCStep() { return cStep; }
    public void setCStep(int val) { cStep = val; }

    /** The current minute in the day (one day is 1440 minutes or time steps) */
    private int minuteInDay;
    public int getMinuteInDay() { return minuteInDay; }
    public void setMinuteInDay(int val) { minuteInDay = val; }
    
    /** Identifies when the resident is in transit to its goal location */
    private boolean isInTransit;
    public boolean isInTransit() { return isInTransit; }
    public void isInTransit(boolean val) { isInTransit = val; }
    
    /** Identifies when the resident is at his/her goal location */
    private boolean atGoal;
    public boolean atGoal() { return atGoal; }
    public void atGoal(boolean val) { atGoal = val; }
    
    /** Identifies if its time for any of the available activities */
    private boolean isWorkTime;
    public boolean isWorkTime() { return isWorkTime; }
    public void isWorkTime(boolean val) { isWorkTime = val; }
    
    private boolean isSearchTime;
    public boolean isSearchTime() { return isSearchTime; }
    public void isSearchTime(boolean val) { isSearchTime = val; }
    
    private boolean isSchoolTime;
    public boolean isSchoolTime() { return isSchoolTime; }
    public void isSchoolTime(boolean val) { isSchoolTime = val; }
    
    private boolean isWaterTime;
    public boolean isWaterTime() { return isWaterTime; }
    public void isWaterTime(boolean val) { isWaterTime = val; }
    
    private boolean isMosqueTime;
    public boolean isMosqueTime() { return isMosqueTime; }
    public void isMosqueTime(boolean val) { isMosqueTime = val; }
    
    private boolean isSocializeTime;
    public boolean isSocializeTime() { return isSocializeTime; }
    public void isSocializeTime(boolean val) { isSocializeTime = val; }
    
    /** Identifies whether the resident has already socialized that day */
    private boolean haveSocialized;
    public boolean haveSocialized() { return haveSocialized; }
    public void haveSocialized(boolean val) { haveSocialized = val; }

    Kibera kibera;

    /** The time controller-identifies the hour, day, week */
    private TimeManager timeManager;
    public TimeManager getTimeManager() { return timeManager; }
    public void setTimeManager(TimeManager val) { timeManager = val; }

    /** The number of time steps an agent stays at a given activity/action */
    private int stayingPeriodAtActivity;
    public int getStayingPeriod() { return stayingPeriodAtActivity; }
    public void setStayingPeriod(int val) { stayingPeriodAtActivity = val; }

    /** Indicates whether the resident heard the rumor */
    private boolean heardRumor;
    public boolean heardRumor() { return heardRumor; }
    public void heardRumor(boolean val) { heardRumor = val; }
    
    /** Indicates whether the resident has heard the new rumor */
    private boolean heardNewRumor;
    public boolean heardNewRumor() { return heardNewRumor; }
    public void heardNewRumor(boolean val) { heardNewRumor = val; }

    private boolean changedGoal;
    public boolean changedGoal() { return changedGoal; }
    public void changedGoal(boolean val) { changedGoal = val; }

    /** Identifies if resident is an initial rebel */
    private boolean isInitialRebel;
    public boolean isInitialRebel() { return isInitialRebel; }
    public void isInitialRebel(boolean val) { isInitialRebel = val; }
    
    /** Stores each residents immediate friends for socializing */
    private Bag myImmediateFriends;
    public Bag getMyImmediateFriends() { return myImmediateFriends; }
    public void setMyImmediateFriends(Bag val) { myImmediateFriends = val; }
    public void addMyImmediateFriend(Resident r) { myImmediateFriends.add(r); }
    public void removeImmediateFriend(Resident r) { myImmediateFriends.remove(r); }
    
    /** Stores each residents immediate friends for socializing */
    private Resident myFriend;
    public Resident getMyFriend() { return myFriend; }
    public void setMyFriend(Resident val) { myFriend = val; }
    
    /** Stores each residents connections */
    private Bag myConnections;
    public Bag getMyConnections() { return myConnections; }
    public void setMyConnections(Bag val) { myConnections = val; }
    public void addMyConnection(Resident r) { myConnections.add(r); }
    public void removeMyConnection(Resident r) { myConnections.remove(r); }
    
    private SchoolClass mySchoolClass;
    public SchoolClass getMySchoolClass() { return mySchoolClass; }
    public void setMySchoolClass(SchoolClass val) { mySchoolClass = val; }
    
    /** Stores my religious connections (others I attend church/mosque with) */
    private Bag myReligiousConnections;
    public Bag getMyReligiousConnections() { return myReligiousConnections; }
    public void addMyReligiousConnections(Resident r) { myReligiousConnections.add(r); }
    public void removeMyReligiousConnections(Resident r) { myReligiousConnections.remove(r); }
    
    private ReligiousFacility myReligiousFacility;
    public ReligiousFacility getMyReligiousFacility() { return myReligiousFacility; }
    public void setMyReligiousFacility(ReligiousFacility val) { myReligiousFacility = val; }
    
    public double residentDailyUse;
    
    public Resident(Household h) {
        kibera = null;
        cStep = 0;
        timeManager = new TimeManager();
  
        myImmediateFriends = new Bag();
        myReligiousConnections = new Bag();   
        myConnections = new Bag();
    }

    public Resident() {
        kibera = null;
        cStep = 0;
        timeManager = new TimeManager();
        
        myImmediateFriends = new Bag();
        myReligiousConnections = new Bag();        
        myConnections = new Bag();
    }
	
    @Override
    public void step(SimState state) {

        kibera = (Kibera)state;
        cStep = (int) kibera.schedule.getSteps();
        
        this.isInTransit(false);
        this.atGoal(false);

        if(cStep < kibera.params.global.getMinutesInDay()) { minuteInDay = cStep; }
        else { minuteInDay = cStep % kibera.params.global.getMinutesInDay(); }
      
        if (minuteInDay == 0) {	
            //utilize available once a day (for simplicity)
            if (this.getPosition() == this.getHousehold().getHome().getStructure().getParcel()) {
                ActionSequence.utilizeWater(this, kibera);
            }
            //reset the variable have socialized - residents can socialize once a day
            this.haveSocialized(false);
            
            //update my immediate friends once a day
            myConnections = new Bag(kibera.socialNetwork.getEdgesOut(this));
            this.setMyConnections(myConnections);                    
        }
        
        //if I'm performing a new goal/activity
        if (this.changedGoal()) {
            //check how much time I spent in previous goal
            this.setTimeInGoal(this.getCStep() - this.getTimeInGoalStart());   
            //reset time in goal for new activity/goal
            this.setTimeInGoalStart(cStep);
            
            //if i heard the rumor, propogate it
            if (this.heardRumor || this.heardNewRumor) {
                propogateRumor();
            }
            
            //check if my identity standard was met while performing goal
            boolean isIdentityStandardMet = IdentityModel.determineIdentityStandard(this, kibera);        
            Energy.evaluateEnergy(this, isIdentityStandardMet, kibera); //re-calculate my energy (reservoir)
        }

        move();
       
        //at start of each week, return attended religious faciility to false
        if (this.getTimeManager().currentDayInWeek(cStep) == 1) {
            this.attendedReligiousFacility(false);
        }   
    }

    /**
     * Determine the resident's behavior by running the intensity analyzer and executing the action sequence
     * 
     * 
     */ 
    public void determineBehavior() {		
        Parcel home = this.getHousehold().getHome().getStructure().getParcel();
        
        //if resident is home, determine its next activity/goal
        if (this.getPosition().equals(home)) {          
            currentGoal = IntensityAnalyzer.runIntensityAnalyzer(this, kibera);    
            this.setGoalLocation(ActionSequence.bestActivityLocation(this, home, currentGoal, employmentOutsideKibera, kibera));
            this.setStayingPeriod(ActionSequence.stayingPeriodAtActivity(this, currentGoal, kibera));               

            return;
        }

        //from goal to home
        if (this.getPosition().equals(this.getGoalLocation()) && !this.getGoalLocation().equals(home)) {
            this.setGoalLocation(home);
            this.setCurrentGoal(Goal.Stay_Home);
            this.setStayingPeriod(ActionSequence.stayingPeriodAtActivity(this, currentGoal, kibera));

            return;
         }

        this.setGoalLocation(home);

        return;
    }
    
    /**
     * If I heard the rumor, propagate rumor to other resident(s) while performing an activity
     * 
     * 
     */
    public void propogateRumor() {
        //see who is in same position and randomly pick a resident(s) to tell rumor to
        Parcel myParcel = this.getHousehold().getHome().getStructure().getParcel();

        //get other residents in same location
        ArrayList <Resident> residents = new ArrayList <Resident>();
        residents = myParcel.getResidents();
        
        int i = 0;

        //randomly select a resident to hear the rumor
        if (residents.size() > 0) {
            
            while (i < kibera.params.global.getNumResidentsSpreadRumorTo()) {
            
                int randomResident = kibera.random.nextInt(residents.size());
                Resident r = residents.get(randomResident);

                if (this.heardRumor) {
                    if (this.getCStep() <= kibera.params.global.getTimeNewRumor() || !kibera.params.global.isPropogateNewRumor() || 
                        (kibera.params.global.isContinueToPropogateOriginalRumor())) {
                        r.heardRumor(true);
                    }
                }

                if (this.heardNewRumor) {
                    if (this.getCStep() > kibera.params.global.getTimeNewRumor() && kibera.params.global.isPropogateNewRumor()) {
                        r.heardNewRumor(true);

                    }
                }
                i++;
            }
            
        }

    }
    
    /**
     * Move to my goal
     * 
     * 
     */
    public void move() {
        //determine if the resident should stay at current activity or if its time to move to next goal
        boolean isStay = ActionSequence.shouldResidentStayAtActivity(this);
        
        Parcel homeParcel = this.getHousehold().getHome().getStructure().getParcel();
        
        if(goalLocation == null) { 
            return; 
        }
        //if i'm staying at current goal, then return
        else if ((currentPosition.equals(goalLocation) && isStay)) {
            return; 
        }

        //at your goal- do activity and recalculate goal
        else if (currentPosition.equals(goalLocation)) {
            ActionSequence.performAction(goalLocation, this, kibera);
            determineBehavior();
            path = null;
        }

        else {	
            // move to your goal		
            // make sure we have a path to the goal!
            if (path == null || path.size() == 0) {
                AStar astar = new AStar();
                int curX = currentPosition.getXLocation();
                int curY = currentPosition.getYLocation();
                int goalX = this.getGoalLocation().getXLocation();
                int goalY = this.getGoalLocation().getYLocation();

                path = astar.astarPath(kibera,
                (Node) kibera.closestNodes.get(curX, curY),
                (Node) kibera.closestNodes.get(goalX, goalY));

                if (path != null) {
                    path.add(goalLocation);
                }
            }
        }

        // determine the best location to immediately move *toward*
        Parcel subgoal = goalLocation;

        // It's possible that the agent isn't close to a node that can take it to the center. 
        // In that case, the A* will return null. If this is so the agent should move toward 
        // the goal until such a node is found.

        // If we have a path and should continue to move along it
        if (path != null)  {
            // have we reached the end of an edge? If so, move to the next edge
            if (path.get(0).equals(currentPosition)) {
                path.remove(0);           
            }
            subgoal = path.get(0); 
        }

        // Now move!
        Parcel newLocation = ActionSequence.getNextTile(kibera, subgoal, currentPosition);

        Parcel oldLocation = currentPosition;
        oldLocation.removeResident(this);

        setPosition(newLocation);
        newLocation.addResident(this);
        
        //print when the resident is in transit to its goal location
        this.isInTransit(true);

        //check that residents are being placed on grid and added to parcels
        kibera.world.setObjectLocation(this, new Double2D(newLocation.getXLocation(), newLocation.getYLocation()));          
    }
    
    @Override
    public double doubleValue() {
        // TODO Auto-generated method stub
        return 0;
    }

}

package kibera;

import java.util.TreeMap;

import org.jfree.data.category.DefaultCategoryDataset;

import kibera.Resident.Employment;
import kibera.Resident.Goal;
import kibera.Resident.Identity;
import sim.engine.MakesSimState;

import sim.engine.SimState;
import static sim.engine.SimState.doLoop;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Network;
import sim.util.Bag;

/**
 * Kibera
 * 
 * Basic class used to run model
 * 
 * 
 * @author bpint
 */
public class Kibera extends SimState {

    /** Variables for creating the modeling world */ 
    private static final long serialVersionUID = 1L;
    public ObjectGrid2D landGrid; // The model environment - holds parcels
    public Continuous2D world;
    public SparseGrid2D householdGrid;
    public IntGrid2D roadGrid; // road in grid- for navigation
    public SparseGrid2D facilityGrid;// facilities: schools, health center, borehol etc
    public SparseGrid2D healthFacilityGrid;
    public SparseGrid2D religiousFacilityGrid;
    public SparseGrid2D waterGrid;
    public SparseGrid2D sanitationGrid;
    public SparseGrid2D businessGrid;
    public SparseGrid2D houseGrid;

    private int width;
    private int height;
    public int getWidth() { return width; }
    public void setWidth(int val) { width = val; }
    public int getHeight() { return height; }
    public void setHeight(int val) { height = val; }

    public GeomVectorField roadLinks;

    public SparseGrid2D nodes;
    public ObjectGrid2D closestNodes; // the road nodes closest to each of the locations
    Network roadNetwork = new Network();	
    Network socialNetwork = new Network(false);

    public Continuous2D testPathField = new Continuous2D(1.0, 343, 204);
    public Network testPathNetwork = new Network();
    
    public final Parameters params;

    public Bag parcels;
    public Bag residents;
    public Bag households;
    public Bag structures;
    public Bag homes;

    public Bag allStructureLocations;
    public Bag allBusinessLocations;
    public Bag allHomeLocations;
    public TreeMap <Integer, Neighborhood> allNeighborhoods = new TreeMap <Integer, Neighborhood>();

    public Bag allSchoolLocations;
    public Bag allHealthFacilityLocations;
    public Bag allReligiousFacilityLocations;
    public Bag allWaterPoints;
    public Bag allSanitationLocations;
    public Bag availableParcels;       
    public Bag allHomesAvailable; //keep dynamic list of homes that are still available

    /** Store households together that share same ethnicity */
    public Bag kikuyu;
    public Bag luhya;
    public Bag luo;
    public Bag kalinjin;
    public Bag kamba;
    public Bag kisii;
    public Bag meru;
    public Bag mijikenda;
    public Bag maasai;
    public Bag turkana;
    public Bag embu;
    public Bag other;
    
    /** Max number of structures on a parcel */
    public int maxStructuresPerParcel = 1;    
 
    /** Total number of residents initialized in the model and their attributes */
    public double maleDistribution = 0.613;

    /** Age distribution */
    public double ageAdult = .25; //this is the percentage of total residents (excluding head of households) that are adults
    public double ageChildrenUnder6 = .32; //the percentage of total residents (excluding head of households) under 6
    public double percentOfResidentsUnder6 = .21; //the percentage of total residents that are under 6 and thus cannot be employed
    public double percentOfResidentsUnder19 = .45;	//the percentage of total residents 18 and younger (source - Kianda survey)

    /** Agent ethnic distribution and ethnicities (CIA World Factbook (2013)) */
    public double[] ethnicDistribution = {.21, .14, .12, .12, .12, .06, .05, .05, .02, .01, .01, .09};
    public String[] ethnicities = {"kikuyu", "luhya", "luo", "kalinjin", "kamba", "kisii", "meru", "mijikenda", "maasai", "turkana", "embu", "other"};
 
    /** Variables for creating households and homes (Marras, 2008) */
    public double avgHouseholdSize = 3.25;

    /** The probability that a household will have certain amenities, including water, electricity, and sanitation (Marras, 2008) */
    public double probabilityWater = 0; //probability water is 1.4%. However, no data was found on the monthly cost for those with running water in the home and probability is very low, so setting this to 0%
    public double probabilityElectricity = 0.6329;
    public double probabilitySanitation = 0.0274;
    
    /** Costs associated with daily household expenditures (Marras, 2008) */
    public double[] rentDistribution = {0.0093, .0674, .1814, .1953, .2116, .1081, .0907, .0360, .0244, .0186, .0140, .0093, .0070, .0023, .0128, .0035, .0012, .0035, .0012, .0023};
    public double[] rent = {117.14, 234.27, 351.41, 468.54, 585.68, 702.81, 819.95, 937.08, 1054.22, 1171.35, 1288.49, 1405.62, 1522.76, 1639.89, 1757.03, 1874.17, 1991.30, 2108.44, 2225.57, 2342.71};

    /** Percent of income spent on rent (Gulyani and Talukdar, 2008) */
    public double percentIncomeforRent = .22;
    public double getPercentIncomeforRent() { return percentIncomeforRent; }
    public void setPercentInformeforRent(double val) { percentIncomeforRent = val; }
    
    /** Expected costs associated with home amenities (if available in the home) and basic necessities (Gulyani and Talukdar, 2008) */
    public double electricCost = 286; //monthly cost for electricity
    public double waterCost = 0; //monthly cost for running water
    public double sanitationCost = 5; //cost of using public sanitation (one visit)
    //public double otherBasicCosts = 0; //other basic daily costs, e.g., charcoal, clothes
    public double transportationCost = 9.68; //Daily cost of transportation per day (for students and employed residents)
    public double foodCostPerMeal = 14; //Cost of one meal per person
  
    /** Water requirements and associated costs (Gulyani and Talukdar, 2008) */
    public int waterRequirement = 23;
    public double barrelOfWaterCost = 2.5; //20 liter barrel of water (one person consumes approximately 23 litres of water per day) 
    
    /** Costs for food, transportation, water requirement, and other basic costs are multiplied by the adult equivalent consistent with
     * the residents age (Gulyani and Talukdar, 2008). */
    public double adultEquivalentAge0to4 = .24;
    public double adultEquivalentAge5to14 = .65;
    public double adultEquivalentAge15andOver = 1;
    
    /** This is the average number of classes, based on an average 23 student class (OpenStreetMap, 2013) */
    public int numClassMates = 23;
    
    /** Identifies if all of an employer have reached capacity */
    public boolean haveHealthFacilitiesReachedCapacity = false;
    public boolean haveReligiousFacilitiesReachedCapacity = false;
    public boolean haveSchoolEmployersReachedCapacity = false;
    public boolean haveBusinessesReachedCapacity = false;    
    public boolean haveSchoolsReachedCapacity = false;
    
    /** Informality index - the proportion of jobs in the informal sector (versus formal sector)
    * In Africa, informal sector employs 60% of urban labor force (United Nations Human Settlements Programme, 2003)
    */
    public double informalityIndex = 0.6;
    public double getInformalityIndex() { return informalityIndex; }
    public void setInformatilityIndex(double val) { informalityIndex = val; }
    
    /** Whether to assume all residents are searching for employment at initialization or use empirical employment stats */
    public boolean useEmploymentStats = true;
    
     /** used for scenario tests / model verification **/
    public boolean canResidentsbeLaidOff = true;
    public boolean doSchoolEligibleSearchforEmployment = true;
    public boolean doStudentsLeaveSchooltoSearchforEmployment = true;
    public boolean doInactiveResidentsSearchforEmployment = true;
    public boolean doSchoolEligbibleStudentsSearchforSchool = true;
    public boolean doesHouseholdNeedImpactBehavior = true;
    public boolean doesRumorSpread = true;
    public boolean createRebelsAtInitialization = true;
     
    /** This is used to create text files with model run results */
    public KiberaObserver kObserver;

    private int[] totalAction;
    public void setTotalAction(int[] val) { totalAction = val; }
    public int[] getTotalAction() { return totalAction; }

    private int[] totalIdentity;
    public void setTotalIdentity(int[] val) { totalIdentity = val; }
    public int[] getTotalIdentity() { return totalIdentity; }

    private int[] totalWorking;
    public void setTotalWorking(int[] val) { totalWorking = val; }
    public int[] getTotalWorking() { return totalWorking; }
    
    private int[] totalConnections;
    public void setTotalConnections(int[] val) { totalConnections = val; }
    public int[] getTotalConnections() { return totalConnections; }

    public int[] allResidents;
    public void setAllResidents(int[] val) { allResidents = val; }
    public int[] getAllResidents() { return allResidents; } 

    public int countResidents = 0;

    public static int RUN = 0;
    public static double SWEEP = 0;

    DefaultCategoryDataset dataset = new DefaultCategoryDataset(); //

    public Kibera (long seed, String [] args) {	
        super(seed);
        
        params  = new Parameters(args);
    }

    @Override
    public void start() {
        super.start();

        parcels = new Bag(); //the set of all parcels
        residents = new Bag(); //the set of all residents
        households = new Bag(); //the set of all households
        structures = new Bag(); //the set of all structures
        homes = new Bag(); //the set of all homes

        availableParcels = new Bag();

        allStructureLocations = new Bag(); //the parcel locations of all structures
        allHealthFacilityLocations = new Bag(); //the parcel locations of all health facilities
        allReligiousFacilityLocations = new Bag(); //the parcel locations of all religious facilities (churches/mosques)
        allBusinessLocations = new Bag(); //the parcel locations of all businesses
        allHomeLocations = new Bag(); //the parcel locations of all homes
        allWaterPoints = new Bag(); //the parcel location of all water points
        allSanitationLocations = new Bag(); //the parcel location of all public sanitation locations
        allSchoolLocations = new Bag();

        kikuyu = new Bag();
        luhya = new Bag();
        luo = new Bag();
        kalinjin = new Bag();
        kamba = new Bag();
        kisii = new Bag();
        meru = new Bag();
        mijikenda = new Bag();
        maasai = new Bag();
        turkana = new Bag();
        embu = new Bag();
        other = new Bag();

        allHomesAvailable = new Bag();

        KiberaBuilder.createWorld("src/kibera/z_kibera.txt", "src/kibera/z_roads_cost_distance.txt", "src/kibera/z_schools.txt", "src/kibera/z_health.txt", "src/kibera/z_religion.txt", "src/kibera/z_watsan.txt", this);	

        kObserver = new KiberaObserver(this);
        schedule.scheduleRepeating(kObserver, KiberaObserver.ORDERING, 1.0);
        
        //update data to build charts and write to output files
        Steppable chartUpdater = new Steppable() {
        public void step(SimState state) {

            int[] sumActions = {0, 0, 0, 0, 0, 0, 0, 0};
            int[] sumIdentities = {0, 0, 0, 0, 0, 0};
            int[] sumWorking = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            int[] sumDegreeStats = {0};
            
            for (int i = 0; i < residents.numObjs; i++) {
                Resident r = (Resident)residents.objs[i];
                
                //sum agents by goal
                if (r.getCurrentGoal() == Goal.Stay_Home) { sumActions[0] += 1; }      			
                if (r.getCurrentGoal() == Goal.Go_to_Work) { sumActions[1] += 1; }
                if (r.getCurrentGoal() == Goal.Find_Employment) { sumActions[2] += 1; }
                if (r.getCurrentGoal() == Goal.Get_An_Education) { sumActions[3] += 1; }
                if (r.getCurrentGoal() == Goal.Socialize) { sumActions[4] += 1; }
                if (r.getCurrentGoal() == Goal.Go_to_Religious_Institution) { sumActions[5] += 1; }       
                if (r.getCurrentGoal() == Goal.Get_Water) { sumActions[6] += 1; }
                if (r.getCurrentGoal() == Goal.Rebel) { sumActions[7] += 1; }
                
                //sum agents by identity
                if (r.getCurrentIdentity() == Identity.Domestic_Activities) { sumIdentities[0] += 1; }
                if (r.getCurrentIdentity() == Identity.Employer) { sumIdentities[1] += 1; }
                if (r.getCurrentIdentity() == Identity.Student) { sumIdentities[2] += 1; }
                if (r.getCurrentIdentity() == Identity.Rebel) { sumIdentities[3] += 1; }
                if (r.heardRumor()) { sumIdentities[4] += 1; }
                if (r.heardNewRumor()) { sumIdentities[5] += 1; }
                
                //sum agents by employment status and working location
                if (r.getMyBusinessEmployer() != null) { sumWorking[0] += 1; }
                if (r.getMySchoolEmployer() != null) { sumWorking[1] += 1; }
                if (r.getMyHealthFacilityEmployer() != null) { sumWorking[2] += 1; }
                if (r.getMyReligiousFacilityEmployer() != null) { sumWorking[3] += 1; }
                if (r.getCurrentEmploymentStatus() == Employment.Formal) { sumWorking[4] += 1; }
                if (r.getCurrentEmploymentStatus() == Employment.Informal) { sumWorking[5] += 1; }
                if (r.getCurrentEmploymentStatus() == Employment.Searching) { sumWorking[6] += 1; }
                if (r.getCurrentEmploymentStatus() == Employment.Inactive) { sumWorking[7] += 1; }
                if (r.getCurrentEmploymentStatus() == Employment.Formal && r.getEmploymentOutsideKibera().getEmployees().contains(r)) { sumWorking[8] += 1; }
                if (r.getCurrentEmploymentStatus() == Employment.Informal && r.getEmploymentOutsideKibera().getEmployees().contains(r)) { sumWorking[9] += 1; }
                
                sumDegreeStats[0] += r.getMyConnections().numObjs;
            }
            setTotalAction(sumActions);
            setTotalIdentity(sumIdentities);
            setTotalWorking(sumWorking);
            setTotalConnections(sumDegreeStats);
           
            String actTitle = "Activity"; // row key - activity
            String [] activities = new String[]{"At Home", "Work", "Searhing for Work", "School", "Socialize", "Church", "Water", "Rebel"}; 

            // percentage - agent activity by type
            for ( int i=0; i< sumActions.length; i++){
              dataset.setValue(sumActions[i] * 100/world.getAllObjects().numObjs, actTitle, activities[i]);
            }       		       		
        }        	
    };
    schedule.scheduleRepeating(chartUpdater);			
    }

    public static void main(String[] args) {    	
        
         doLoop(new MakesSimState() {
            @Override
            public SimState newInstance(long seed, String[] args) {
                return new Kibera(seed, args);
            }

            @Override
            public Class simulationClass() {
                return Kibera.class;
            }
        }, args);
      
        System.exit(0);
    }
	
}

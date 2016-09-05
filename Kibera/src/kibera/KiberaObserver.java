package kibera;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import kibera.Resident.AgeGroup;

import kibera.Resident.Identity;
import net.sf.csv4j.CSVWriter;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.network.stats.DegreeStatistics;
import sim.field.network.stats.NetworkStatistics;


/**
 * Kibera Observer writes data to a series of csv files
 * Based on riftland worldobserver class
 * Thanks goes to mcoletti and jbasset
 * 
 * @author bpint
 */
public class KiberaObserver implements Steppable{	    
    private BufferedWriter dataFileBuffer_action; // output file buffer for dataCSVFile_
    private CSVWriter dataCSVFile_action; // CSV file that contains run data
    private BufferedWriter dataFileBuffer_identity; // output file buffer for dataCSVFile_
    private CSVWriter dataCSVFile_identity; // CSV file that contains run data
    private BufferedWriter dataFileBuffer_degreestats; // output file buffer for dataCSVFile_
    private CSVWriter dataCSVFile_degreestats; // CSV file that contains run data

    private BufferedWriter dataFileBuffer_network; // output file buffer for dataCSVFile_
    private CSVWriter dataCSVFile_network; // CSV file that contains run data

    private BufferedWriter dataFileBuffer_working; // output file buffer for dataCSVFile_
    private CSVWriter dataCSVFile_working; // CSV file that contains run data

    private BufferedWriter dataFileBuffer_residents;
    private CSVWriter dataCSVFile_residents;
    
    private BufferedWriter dataFileBuffer_rebels;
    private CSVWriter dataCSVFile_rebels;

    int cStep;
    int minuteInDay;

    Kibera kibera;

    public final static int ORDERING = 3;
    private int step = 0;
    private boolean writeGrid =false;
	
    KiberaObserver(Kibera kibera) {
        //<GCB>: you may want to adjust the number of columns based on these flags.
        // both in createLogFile, and step
        kibera = null;
        startLogFile();
    }

    KiberaObserver() {
        startLogFile();
    }
	
	        
    private void startLogFile() {
        // Create a CSV file to capture data for this run.
        try {
            createLogFile();
            // First line of file contains field names
            String [] header = new String [] {"Job", "Step", "Total Residents", "Domestic", "Employed", "Student", "Rebel", "Heard original rumor", "Heard new rumor", "Run"};
            dataCSVFile_identity.writeLine(header);

            // activity
            String [] header_actions = new String [] {"Job","Step","total residents", "At Home", "Work", "Searching for Work",
                                                            "School", "Socialiazing", "Church", "Getting Water", "Rebel", "Run"};

            dataCSVFile_action.writeLine(header_actions);

            String [] header_network = new String [] {"Job", "Step", "node1", "node2", "weight"};
            dataCSVFile_network.writeLine(header_network);

            String [] header_working = new String [] {"Job", "Step", "Business", "School", "Health Facility", "Religious Facility", 
                "Formal", "Informal", "Searching", "Inactive", "Formal at home", "Informal at home", "Run"};
            dataCSVFile_working.writeLine(header_working);
            
            String [] header_rebels = new String [] {"Job", "Step", "AgeGroup", "HHDiscrepancy", "Identity", "Count" };
            dataCSVFile_rebels.writeLine(header_rebels);

            String[] header_residents = new String [] {"Job", "Step", "Resident", "Age", "Employment status", "Laid off", "Action",  
                "Daily water use", "Remaining water", "Required water",
                "Identity","Is initial rebel", 
                "Heard original rumor", "Heard new rumor", "Energy", "Current Aggression", "Aggression Rate", "Formal Business Capacity", 
                "Informal Business Capacity", "School Capacity", "Has School", "Has formal employer", "Has informal Employer", 
                "Formal income", "Informal income", "Searching income", "Inactive income", "Household Income", "Has electricity", "Has sanitation",
                "Has water", "Household expenditures", "Rent cost", "Water cost", "Electric cost", "Sanitation cost", 
                "Transportation cost", "Other cost", "Food cost", "Discrepancy", "Run", "Preference"};
            
           
            dataCSVFile_residents.writeLine(header_residents);

            String [] header_degreestats = new String [] {"Job", "Step", "max degree", "min degree", "mean degree", "sum degree", "mean connections", "Run"};
            dataCSVFile_degreestats.writeLine(header_degreestats);

            }

        catch (IOException ex) {
            Logger.getLogger(Kibera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    int count = 0;

    public void step(SimState state) {
        kibera  = (Kibera)state;
        cStep = (int) kibera.schedule.getSteps();

        if(cStep < kibera.params.global.getMinutesInDay()){
            minuteInDay = cStep;
        }
        else {
            minuteInDay = cStep % kibera.params.global.getMinutesInDay();	            
        }

        String job = Long.toString(state.job());	        	
        
        //convert resident attribute totals to string
        String totalResidents = Integer.toString(kibera.residents.numObjs);
        String numAtHome = Integer.toString(kibera.getTotalAction()[0]);
        String numAtWork = Integer.toString(kibera.getTotalAction()[1]);
        String numSearchingforWork = Integer.toString(kibera.getTotalAction()[2]);
        String numAtSchool = Integer.toString(kibera.getTotalAction()[3]);
        String numAtFriendsHouse = Integer.toString(kibera.getTotalAction()[4]);
        String numAtChurch = Integer.toString(kibera.getTotalAction()[5]);
        String numAtWater = Integer.toString(kibera.getTotalAction()[6]);
        String numRebelling = Integer.toString(kibera.getTotalAction()[7]);

        String numDomestic = Integer.toString(kibera.getTotalIdentity()[0]);
        String numEmployed = Integer.toString(kibera.getTotalIdentity()[1]);
        String numStudent = Integer.toString(kibera.getTotalIdentity()[2]);
        String numRebel = Integer.toString(kibera.getTotalIdentity()[3]);
        String numHeardRumor = Integer.toString(kibera.getTotalIdentity()[4]);
        String numHeardNewRumor = Integer.toString(kibera.getTotalIdentity()[5]);

        String maxDegree = Integer.toString(DegreeStatistics.getMaxOutDegree(kibera.socialNetwork));
        String minDegree = Integer.toString(DegreeStatistics.getMinOutDegree(kibera.socialNetwork));
        String meanDegree = Double.toString(DegreeStatistics.getMeanOutDegree(kibera.socialNetwork));
        String sumDegrees = Integer.toString(DegreeStatistics.getSumOfDegrees(kibera.socialNetwork));
        String meanConnections = Integer.toString(kibera.getTotalConnections()[0] / kibera.params.global.getNumResidents());

        String numWorkingBusiness = Integer.toString(kibera.getTotalWorking()[0]);
        String numWorkingSchool = Integer.toString(kibera.getTotalWorking()[1]);
        String numWorkingHealthFacility = Integer.toString(kibera.getTotalWorking()[2]);
        String numWorkingReligiousFacility = Integer.toString(kibera.getTotalWorking()[3]);
        String numWorkingFormal = Integer.toString(kibera.getTotalWorking()[4]);
        String numWorkingInformal = Integer.toString(kibera.getTotalWorking()[5]);
        String numWorkingSearching = Integer.toString(kibera.getTotalWorking()[6]);
        String numWorkingInactive = Integer.toString(kibera.getTotalWorking()[7]);
        String numWorkingFormalHome = Integer.toString(kibera.getTotalWorking()[8]);
        String numWorkingInformalHome = Integer.toString(kibera.getTotalWorking()[9]);

        NetworkStatistics.getDensity(kibera.socialNetwork);

        // when to export raster;- everyday at midnight
        writeGrid =true;
        if(kibera.schedule.getSteps() % kibera.params.global.getMinutesInDay() == 5){
           writeGrid =true;
        }
        if (kibera.schedule.getSteps() % kibera.params.global.getMinutesInDay() == 1) {
                writeGrid = true;
        }
        else {
            writeGrid =false;
        }

        String [] data_network = null;
        //DO NOT DELETE -- this creates the adjacency matrix for SNA analysis
        /*if (minuteInDay == 0) {
                String matrix = "";
                Edge[][] edges = kibera.socialNetwork.getAdjacencyMatrix();
                for(int i = 0; i < edges.length; i++){
                        for(int j = i+1; j < edges[0].length; j++){
                                if(edges[i][j] == null)
                                        continue; // don't write it out, it doesn't exist
                                matrix += i + ", " + j + ", " + edges[i][j].info + "\n";
                                String node1 = Integer.toString(i);
                                String node2 = Integer.toString(j);
                                String edgeWeight = Double.toString((Double) edges[i][j].info);
                                data_network = new String [] {job, Integer.toString(this.step), node1, node2, edgeWeight};

                                try {
                                        this.dataCSVFile_network.writeLine(data_network);

                                }
                                catch (IOException ex) {
                            Logger.getLogger(KiberaObserver.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        }
                        //if(i%100 == 0) System.out.print(".");
                }
                //System.out.println(matrix);		        
        }*/

        String [] data_residents = null;
        //DO NOT DELETE -- this writes individual resident data to a file
        /*
        if (minuteInDay == 0) {
            for (int i = 0; i < kibera.residents.numObjs; i++) {
                Resident r = (Resident) kibera.residents.get(i);
                
                //if (r.getCurrentIdentity() == Identity.Rebel) {
                
                    String residents = Integer.toString(r.getResidentID());
                    String age = Integer.toString(r.getAge());

                    String hhExpenditures = Double.toString(r.getHousehold().getDailyHouseholdExpenditures());
                    String hhIncome = Double.toString(r.getHousehold().getDailyHouseholdIncome());
                    String hhDiscrepancy = Double.toString(r.getHousehold().getDailyHouseholdDiscrepancy());

                    String residentIncome = Double.toString((r.getResidentIncome() / 30));
                    String formalIncome = "0";
                    String informalIncome = "0";
                    String searchingIncome = "0";
                    String inactiveIncome = "0";

                    String status = "0";

                    String hasSchool = "0";
                    String hasFormalEmployer = "0";
                    String hasInformalEmployer = "0";

                    String hhWaterCost = Double.toString(r.getHousehold().getDailyWaterCost());
                    String hhElectricCost = Double.toString(r.getHousehold().getDailyElectricCost());
                    String hhSanitationCost = Double.toString(r.getHousehold().getDailySanitationCost());
                    String hhOtherCost = Double.toString(r.getHousehold().getDailyOtherBasicCost());
                    String hhFoodCost = Double.toString(r.getHousehold().getDailyFoodCost());
                    String hhRentCost = Double.toString(r.getHousehold().getHome().getHouseRent() / 30);
                    String hhTransportationCost = Double.toString(r.getHousehold().getDailyTransportationCost());

                    String hasElectricity = Boolean.toString(r.getHousehold().getHome().hasElectricity());
                    String hasSanitation = Boolean.toString(r.getHousehold().getHome().hasSanitation());
                    String hasWater = Boolean.toString(r.getHousehold().getHome().hasWater());

                    String identity = "";
                    String action = "";

                    String laidOff = "False";

                    String rumor = "False";
                    String newRumor = "False";
                    String energy = Double.toString(r.getEnergy());
                    String aggression = Double.toString(r.getAggressionValue());
                    String aggressionRate = Double.toString(r.getAggressionRate());

                    String isInitialRebel = Boolean.toString(r.isInitialRebel());

                    if (r.getCurrentEmploymentStatus() == Employment.Formal) { 
                        formalIncome = residentIncome; 
                        status = "Formal";
                    }
                    if (r.getCurrentEmploymentStatus() == Employment.Informal) { 
                        informalIncome = residentIncome;
                        status = "Informal";
                    }	
                    if (r.getCurrentEmploymentStatus() == Employment.Searching) { 
                        searchingIncome = residentIncome; 
                        status = "Searching";
                    }	
                    if (r.getCurrentEmploymentStatus() == Employment.Inactive) { 
                        inactiveIncome = residentIncome; 
                        status = "Inactive";
                    }

                    if (r.getMySchool() != null) { hasSchool = "1"; }
                    if (r.getMyReligiousFacilityEmployer() != null || r.getMyHealthFacilityEmployer() != null || r.getMySchoolEmployer() != null 
                            || (r.getEmploymentOutsideKibera().getEmployees().contains(r) && r.getCurrentEmploymentStatus() == Employment.Formal)) { hasFormalEmployer = "1"; }
                    if (r.getMyBusinessEmployer() != null || (r.getEmploymentOutsideKibera().getEmployees().contains(r) && r.getCurrentEmploymentStatus() == Employment.Informal)) { hasInformalEmployer = "1"; }

                    if (r.getCurrentGoal() == Goal.Find_Employment) { action = "Find_Employment"; }
                    if (r.getCurrentGoal() == Goal.Get_An_Education) { action = "Go_to_School"; }
                    if (r.getCurrentGoal() == Goal.Get_Water) { action = "Get_Water"; }
                    if (r.getCurrentGoal() == Goal.Go_to_Church) { action = "Go_to_Church"; }
                    if (r.getCurrentGoal() == Goal.Go_to_Work) { action = "Go_to_Work"; }
                    if (r.getCurrentGoal() == Goal.Rebel) { action = "Rebel"; }
                    if (r.getCurrentGoal() == Goal.Socialize) { action = "Socialize"; }
                    if (r.getCurrentGoal() == Goal.Stay_Home) { action = "Stay_Home"; }


                    if (r.getCurrentIdentity() == Identity.Domestic_Activities) { identity = "Domestic_Activities"; }
                    if (r.getCurrentIdentity() == Identity.Employer) { identity = "Employee"; }
                    if (r.getCurrentIdentity() == Identity.Student) { identity = "Student"; }
                    if (r.getCurrentIdentity() == Identity.Rebel) { identity = "Rebel"; }

                    if (r.heardRumor()) { rumor = "True"; }
                    if (r.heardNewRumor()) { newRumor = "True"; }
                    if (r.isLaidOff()) { laidOff = "True"; }

                    //Parcel school = null;
                    //Parcel workBusiness = null;
                    //Parcel workSchool = null;
                    //Parcel workReligion = null;
                    //Parcel workHealth = null;

                    //Parcel home = r.getHousehold().getHome().getStructure().getParcel();
                    //if (r.getMySchool() != null) { school = r.getMySchool().getStructure().getParcel(); }                             
                    //if (r.getMyBusinessEmployer() != null) { workBusiness = r.getMyBusinessEmployer().getStructure().getParcel(); }
                    //if (r.getMySchoolEmployer() != null) { workSchool = r.getMySchoolEmployer().getStructure().getParcel(); }
                    //if (r.getMyReligiousFacilityEmployer() != null) { workReligion = r.getMyReligiousFacilityEmployer().getStructure().getParcel(); }
                    //if (r.getMyHealthFacilityEmployer() != null) { workHealth = r.getMyHealthFacilityEmployer().getStructure().getParcel(); }

                    //String currentLocation = "";

                    //if (r.getPosition().equals(home)) { currentLocation = "Home"; }
                    //if (r.getPosition().equals(school)) { currentLocation = "School"; }
                    //if (r.getPosition().equals(workBusiness)) { currentLocation = "Business Employer"; }
                    //if (r.getPosition().equals(workSchool)) { currentLocation = "School Employer"; }
                    //if (r.getPosition().equals(workReligion)) { currentLocation = "Religious Employer"; }
                    //if (r.getPosition().equals(workHealth)) { currentLocation = "Health Employer"; }

                    //String inTransit = Boolean.toString(r.isInTransit());

                    //String workTime = Boolean.toString(r.isWorkTime());
                    //String searchTime = Boolean.toString(r.isSearchTime());
                    //String schoolTime = Boolean.toString(r.isSchoolTime());
                    //String socializeTime = Boolean.toString(r.isSocializeTime());
                    //String waterTime = Boolean.toString(r.isWaterTime());
                    //String atGoal = Boolean.toString(r.atGoal());

                    String preference = Double.toString(kibera.params.global.getPreferenceforLivingNearLikeNeighbors());

                    String waterDailyUse = Double.toString(r.residentDailyUse);
                    String remainingWater = Double.toString(r.getHousehold().getRemainingWater());
                    String requiredWater = Integer.toString(kibera.waterRequirement * r.getHousehold().getHouseholdMembers().size());

                    data_residents = new String [] {job, Integer.toString(this.step), residents, age, status, laidOff, action, waterDailyUse, 
                        remainingWater, requiredWater,
                        identity, isInitialRebel, rumor, newRumor, energy, aggression, aggressionRate, 
                        Double.toString(kibera.params.global.getFormalBusinessCapacity()), Double.toString(kibera.params.global.getInformalBusinessCapacity()), 
                        Double.toString(kibera.params.global.getSchoolCapacity()), hasSchool, hasFormalEmployer, 
                        hasInformalEmployer, formalIncome, informalIncome, searchingIncome, inactiveIncome, hhIncome, hasElectricity, 
                        hasSanitation, hasWater, hhExpenditures, hhRentCost, 
                        hhWaterCost, hhElectricCost, hhSanitationCost, hhTransportationCost, hhOtherCost, hhFoodCost, hhDiscrepancy, Integer.toString(kibera.RUN), 
                        preference };


                    try {

                        this.dataCSVFile_residents.writeLine(data_residents);

                    }
                    catch (IOException ex) {
                        Logger.getLogger(KiberaObserver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                //}
            }
        }
        */
        
  
        //Used to create an output file with aggregated data by Age groupings, rioter, and household discrepancy
   
        String ageGroup;
        double hhdisc = 0.;
        String hhdiscrepancy;
   
        int cAge0to2NegativeDomestic = 0;
        int cAge0to2PositiveDomestic = 0;
        int cAge0to2NegativeRebel = 0;
        int cAge0to2NegativeStudent = 0;
        int cAge0to2NegativeEmployee = 0;
        int cAge0to2PositiveRebel = 0;
        int cAge0to2PositiveStudent = 0;
        int cAge0to2PositiveEmployee = 0;
        
        int cAge3to5NegativeDomestic = 0;
        int cAge3to5PositiveDomestic = 0;
        int cAge3to5NegativeRebel = 0;
        int cAge3to5NegativeStudent = 0;
        int cAge3to5NegativeEmployee = 0;
        int cAge3to5PositiveRebel = 0;
        int cAge3to5PositiveStudent = 0;
        int cAge3to5PositiveEmployee = 0;
        
        int cAge6to18NegativeDomestic = 0;
        int cAge6to18PositiveDomestic = 0;
        int cAge6to18NegativeRebel = 0;
        int cAge6to18NegativeStudent = 0;
        int cAge6to18NegativeEmployee = 0;
        int cAge6to18PositiveRebel = 0;
        int cAge6to18PositiveStudent = 0;
        int cAge6to18PositiveEmployee = 0;
        
        int cAge19OverNegativeDomestic = 0;
        int cAge19OverPositiveDomestic = 0;
        int cAge19OverNegativeRebel = 0;
        int cAge19OverNegativeStudent = 0;
        int cAge19OverNegativeEmployee = 0;
        int cAge19OverPositiveRebel = 0;
        int cAge19OverPositiveStudent = 0;
        int cAge19OverPositiveEmployee = 0;
        
        //count the number of residents that fit in each category
        for (int i = 0; i < kibera.residents.numObjs; i++) {
            Resident r = (Resident) kibera.residents.get(i);
            
            hhdisc = r.getHousehold().getDailyHouseholdDiscrepancy();
            
            if (hhdisc > 0) { hhdiscrepancy = "Negative"; }
            else { hhdiscrepancy = "Positive"; }
            
            if (r.getAgeGroup() == AgeGroup.age0to2) { ageGroup = "age0to2"; }
            else if (r.getAgeGroup() == AgeGroup.age3to5) { ageGroup = "age3to5"; }
            else if (r.getAgeGroup() == AgeGroup.age6to18) { ageGroup = "age6to18"; }
            else { ageGroup = "age19andOver"; }
            
            if (r.getCurrentIdentity() == Identity.Domestic_Activities) { };
            
            //age group 0 to 2
            if (r.getAgeGroup() == AgeGroup.age0to2 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Domestic_Activities) {
                cAge0to2NegativeDomestic++;
            }
            else if (r.getAgeGroup() == AgeGroup.age0to2 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Rebel) {
                cAge0to2NegativeRebel++;
            }           
             else if (r.getAgeGroup() == AgeGroup.age0to2 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Student) {
                cAge0to2NegativeStudent++;
            }
             else if (r.getAgeGroup() == AgeGroup.age0to2 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Employer) {
                cAge0to2NegativeEmployee++;
            }
            if (r.getAgeGroup() == AgeGroup.age0to2 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Domestic_Activities) {
                cAge0to2PositiveDomestic++;
            }
            else if (r.getAgeGroup() == AgeGroup.age0to2 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Rebel) {
                cAge0to2PositiveRebel++;
            }           
             else if (r.getAgeGroup() == AgeGroup.age0to2 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Student) {
                cAge0to2PositiveStudent++;
            }
             else if (r.getAgeGroup() == AgeGroup.age0to2 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Employer) {
                cAge0to2PositiveEmployee++;
            }
            
            //age group 3 to 5
            if (r.getAgeGroup() == AgeGroup.age3to5 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Domestic_Activities) {
                cAge3to5NegativeDomestic++;
            }
            else if (r.getAgeGroup() == AgeGroup.age3to5 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Rebel) {
                cAge3to5NegativeRebel++;
            }           
             else if (r.getAgeGroup() == AgeGroup.age3to5 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Student) {
                cAge3to5NegativeStudent++;
            }
             else if (r.getAgeGroup() == AgeGroup.age3to5 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Employer) {
                cAge3to5NegativeEmployee++;
            }
            if (r.getAgeGroup() == AgeGroup.age3to5 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Domestic_Activities) {
                cAge3to5PositiveDomestic++;
            }
            else if (r.getAgeGroup() == AgeGroup.age3to5 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Rebel) {
                cAge3to5PositiveRebel++;
            }           
             else if (r.getAgeGroup() == AgeGroup.age3to5 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Student) {
                cAge3to5PositiveStudent++;
            }
             else if (r.getAgeGroup() == AgeGroup.age3to5 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Employer) {
                cAge3to5PositiveEmployee++;
            }
            
            //age group 6 to 18
            if (r.getAgeGroup() == AgeGroup.age6to18 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Domestic_Activities) {
                cAge6to18NegativeDomestic++;
            }
            else if (r.getAgeGroup() == AgeGroup.age6to18 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Rebel) {
                cAge6to18NegativeRebel++;
            }           
             else if (r.getAgeGroup() == AgeGroup.age6to18 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Student) {
                cAge6to18NegativeStudent++;
            }
             else if (r.getAgeGroup() == AgeGroup.age6to18 && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Employer) {
                cAge6to18NegativeEmployee++;
            }
            if (r.getAgeGroup() == AgeGroup.age6to18 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Domestic_Activities) {
                cAge6to18PositiveDomestic++;
            }
            else if (r.getAgeGroup() == AgeGroup.age6to18 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Rebel) {
                cAge6to18PositiveRebel++;
            }           
             else if (r.getAgeGroup() == AgeGroup.age6to18 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Student) {
                cAge6to18PositiveStudent++;
            }
             else if (r.getAgeGroup() == AgeGroup.age6to18 && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Employer) {
                cAge6to18PositiveEmployee++;
            }
            
            //19 and over
            if (r.getAgeGroup() == AgeGroup.age19andOver && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Domestic_Activities) {
                cAge19OverNegativeDomestic++;
            }
            else if (r.getAgeGroup() == AgeGroup.age19andOver && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Rebel) {
                cAge19OverNegativeRebel++;
            }           
             else if (r.getAgeGroup() == AgeGroup.age19andOver && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Student) {
                cAge19OverNegativeStudent++;
            }
             else if (r.getAgeGroup() == AgeGroup.age19andOver && hhdiscrepancy == "Negative" && r.getCurrentIdentity() == Identity.Employer) {
                cAge19OverNegativeEmployee++;
            }
            if (r.getAgeGroup() == AgeGroup.age19andOver && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Domestic_Activities) {
                cAge19OverPositiveDomestic++;
            }
            else if (r.getAgeGroup() == AgeGroup.age19andOver && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Rebel) {
                cAge19OverPositiveRebel++;
            }           
             else if (r.getAgeGroup() == AgeGroup.age19andOver && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Student) {
                cAge19OverPositiveStudent++;
            }
             else if (r.getAgeGroup() == AgeGroup.age19andOver && hhdiscrepancy == "Positive" && r.getCurrentIdentity() == Identity.Employer) {
                cAge19OverPositiveEmployee++;
            }
            
            
            
        }
        
        //convert data to string
        String [] data_rebels1 = new String [] {job, Integer.toString(this.step), "age0to2", "Negative", "Domestic", Integer.toString(cAge0to2NegativeDomestic) };
        String [] data_rebels2 = new String [] {job, Integer.toString(this.step), "age0to2", "Negative", "Rebel", Integer.toString(cAge0to2NegativeRebel) };
        String [] data_rebels3 = new String [] {job, Integer.toString(this.step), "age0to2", "Negative", "Student", Integer.toString(cAge0to2NegativeStudent) };
        String [] data_rebels4 = new String [] {job, Integer.toString(this.step), "age0to2", "Negative", "Employee", Integer.toString(cAge0to2NegativeEmployee) };
        String [] data_rebels5 = new String [] {job, Integer.toString(this.step), "age0to2", "Positive", "Domestic", Integer.toString(cAge0to2PositiveDomestic) };
        String [] data_rebels6 = new String [] {job, Integer.toString(this.step), "age0to2", "Positive", "Rebel", Integer.toString(cAge0to2PositiveRebel) };
        String [] data_rebels7 = new String [] {job, Integer.toString(this.step), "age0to2", "Positive", "Student", Integer.toString(cAge0to2PositiveStudent) };
        String [] data_rebels8 = new String [] {job, Integer.toString(this.step), "age0to2", "Positive", "Employee", Integer.toString(cAge0to2PositiveEmployee) };
        
        String [] data_rebels9 = new String [] {job, Integer.toString(this.step), "age3to5", "Negative", "Domestic", Integer.toString(cAge3to5NegativeDomestic) };
        String [] data_rebels10 = new String [] {job, Integer.toString(this.step), "age3to5", "Negative", "Rebel", Integer.toString(cAge3to5NegativeRebel) };
        String [] data_rebels11 = new String [] {job, Integer.toString(this.step), "age3to5", "Negative", "Student", Integer.toString(cAge3to5NegativeStudent) };
        String [] data_rebels12 = new String [] {job, Integer.toString(this.step), "age3to5", "Negative", "Employee", Integer.toString(cAge3to5NegativeEmployee) };
        String [] data_rebels13 = new String [] {job, Integer.toString(this.step), "age3to5", "Positive", "Domestic", Integer.toString(cAge3to5PositiveDomestic) };
        String [] data_rebels14 = new String [] {job, Integer.toString(this.step), "age3to5", "Positive", "Rebel", Integer.toString(cAge3to5PositiveRebel) };
        String [] data_rebels15 = new String [] {job, Integer.toString(this.step), "age3to5", "Positive", "Student", Integer.toString(cAge3to5PositiveStudent) };
        String [] data_rebels16 = new String [] {job, Integer.toString(this.step), "age3to5", "Positive", "Employee", Integer.toString(cAge3to5PositiveEmployee) };
        
        String [] data_rebels17 = new String [] {job, Integer.toString(this.step), "age6to18", "Negative", "Domestic", Integer.toString(cAge6to18NegativeDomestic) };
        String [] data_rebels18 = new String [] {job, Integer.toString(this.step), "age6to18", "Negative", "Rebel", Integer.toString(cAge6to18NegativeRebel) };
        String [] data_rebels19 = new String [] {job, Integer.toString(this.step), "age6to18", "Negative", "Student", Integer.toString(cAge6to18NegativeStudent) };
        String [] data_rebels20 = new String [] {job, Integer.toString(this.step), "age6to18", "Negative", "Employee", Integer.toString(cAge6to18NegativeEmployee) };
        String [] data_rebels21 = new String [] {job, Integer.toString(this.step), "age6to18", "Positive", "Domestic", Integer.toString(cAge6to18PositiveDomestic) };
        String [] data_rebels22 = new String [] {job, Integer.toString(this.step), "age6to18", "Positive", "Rebel", Integer.toString(cAge6to18PositiveRebel) };
        String [] data_rebels23 = new String [] {job, Integer.toString(this.step), "age6to18", "Positive", "Student", Integer.toString(cAge6to18PositiveStudent) };
        String [] data_rebels24 = new String [] {job, Integer.toString(this.step), "age6to18", "Positive", "Employee", Integer.toString(cAge6to18PositiveEmployee) };
        
        String [] data_rebels25 = new String [] {job, Integer.toString(this.step), "age19Over", "Negative", "Domestic", Integer.toString(cAge19OverNegativeDomestic) };
        String [] data_rebels26 = new String [] {job, Integer.toString(this.step), "age19Over", "Negative", "Rebel", Integer.toString(cAge19OverNegativeRebel) };
        String [] data_rebels27 = new String [] {job, Integer.toString(this.step), "age19Over", "Negative", "Student", Integer.toString(cAge19OverNegativeStudent) };
        String [] data_rebels28 = new String [] {job, Integer.toString(this.step), "age19Over", "Negative", "Employee", Integer.toString(cAge19OverNegativeEmployee) };
        String [] data_rebels29 = new String [] {job, Integer.toString(this.step), "age19Over", "Positive", "Domestic", Integer.toString(cAge19OverPositiveDomestic) };
        String [] data_rebels30 = new String [] {job, Integer.toString(this.step), "age19Over", "Positive", "Rebel", Integer.toString(cAge19OverPositiveRebel) };
        String [] data_rebels31 = new String [] {job, Integer.toString(this.step), "age19Over", "Positive", "Student", Integer.toString(cAge19OverPositiveStudent) };
        String [] data_rebels32 = new String [] {job, Integer.toString(this.step), "age19Over", "Positive", "Employee", Integer.toString(cAge19OverPositiveEmployee) };
            
        String [] data_actions = new String [] {job, Integer.toString(this.step), totalResidents, numAtHome, numAtWork, numSearchingforWork, numAtSchool, numAtFriendsHouse, numAtChurch, numAtWater, numRebelling, Integer.toString(kibera.RUN)};
        String [] data_identities = new String [] {job, Integer.toString(this.step), totalResidents, numDomestic, numEmployed, numStudent, numRebel, numHeardRumor, numHeardNewRumor, Integer.toString(kibera.RUN) };
        String [] data_degreestats = new String [] {job, Integer.toString(this.step), maxDegree, minDegree, meanDegree, sumDegrees, meanConnections, Integer.toString(kibera.RUN)};
        String [] data_working = new String [] {job, Integer.toString(this.step), numWorkingBusiness, numWorkingSchool, numWorkingHealthFacility, numWorkingReligiousFacility, numWorkingFormal, numWorkingInformal, numWorkingSearching, numWorkingInactive, numWorkingFormalHome, numWorkingInformalHome, Integer.toString(kibera.RUN) };
        //DO NOTE DELETE
        //String [] data_residents = new String[] {job, Integer.toString(this.step), residents, hhExpenditures, hhIncome, formalIncome, informalIncome, searchingIncome, inactiveIncome };
        //String [] data_rebeldata = new String [] {job, Integer.toString(this.step), identity, ageGroup, hhdiscrepancy, numResidents };
        
        
        try {	        
            if (cAge0to2NegativeDomestic > 0) this.dataCSVFile_rebels.writeLine(data_rebels1);
            if (cAge0to2NegativeRebel > 0) this.dataCSVFile_rebels.writeLine(data_rebels2);
            if (cAge0to2NegativeStudent > 0) this.dataCSVFile_rebels.writeLine(data_rebels3);
            if (cAge0to2NegativeEmployee > 0) this.dataCSVFile_rebels.writeLine(data_rebels4);
            if (cAge0to2PositiveDomestic > 0) this.dataCSVFile_rebels.writeLine(data_rebels5);
            if (cAge0to2PositiveRebel > 0) this.dataCSVFile_rebels.writeLine(data_rebels6);
            if (cAge0to2PositiveStudent > 0) this.dataCSVFile_rebels.writeLine(data_rebels7);
            if (cAge0to2PositiveEmployee > 0) this.dataCSVFile_rebels.writeLine(data_rebels8);
            
            if (cAge3to5NegativeDomestic > 0) this.dataCSVFile_rebels.writeLine(data_rebels9);
            if (cAge3to5NegativeRebel > 0) this.dataCSVFile_rebels.writeLine(data_rebels10);
            if (cAge3to5NegativeStudent > 0) this.dataCSVFile_rebels.writeLine(data_rebels11);
            if (cAge3to5NegativeEmployee > 0) this.dataCSVFile_rebels.writeLine(data_rebels12);
            if (cAge3to5PositiveDomestic > 0) this.dataCSVFile_rebels.writeLine(data_rebels13);
            if (cAge3to5PositiveRebel > 0) this.dataCSVFile_rebels.writeLine(data_rebels14);
            if (cAge3to5PositiveStudent > 0) this.dataCSVFile_rebels.writeLine(data_rebels15);
            if (cAge3to5PositiveEmployee > 0) this.dataCSVFile_rebels.writeLine(data_rebels16);
            
            if (cAge6to18NegativeDomestic > 0) this.dataCSVFile_rebels.writeLine(data_rebels17);
            if (cAge6to18NegativeRebel > 0) this.dataCSVFile_rebels.writeLine(data_rebels18);
            if (cAge6to18NegativeStudent > 0) this.dataCSVFile_rebels.writeLine(data_rebels19);
            if (cAge6to18NegativeEmployee > 0) this.dataCSVFile_rebels.writeLine(data_rebels20);
            if (cAge6to18PositiveDomestic > 0) this.dataCSVFile_rebels.writeLine(data_rebels21);
            if (cAge6to18PositiveRebel > 0) this.dataCSVFile_rebels.writeLine(data_rebels22);
            if (cAge6to18PositiveStudent > 0) this.dataCSVFile_rebels.writeLine(data_rebels23);
            if (cAge6to18PositiveEmployee > 0) this.dataCSVFile_rebels.writeLine(data_rebels24);
            
            if (cAge19OverNegativeDomestic > 0) this.dataCSVFile_rebels.writeLine(data_rebels25);
            if (cAge19OverNegativeRebel > 0) this.dataCSVFile_rebels.writeLine(data_rebels26);
            if (cAge19OverNegativeStudent > 0) this.dataCSVFile_rebels.writeLine(data_rebels27);
            if (cAge19OverNegativeEmployee > 0) this.dataCSVFile_rebels.writeLine(data_rebels28);
            if (cAge19OverPositiveDomestic > 0) this.dataCSVFile_rebels.writeLine(data_rebels29);
            if (cAge19OverPositiveRebel > 0) this.dataCSVFile_rebels.writeLine(data_rebels30);
            if (cAge19OverPositiveStudent > 0) this.dataCSVFile_rebels.writeLine(data_rebels31);
            if (cAge19OverPositiveEmployee > 0) this.dataCSVFile_rebels.writeLine(data_rebels32);
            
            this.dataCSVFile_action.writeLine(data_actions);	            
            this.dataCSVFile_identity.writeLine(data_identities);
            this.dataCSVFile_degreestats.writeLine(data_degreestats);  
            this.dataCSVFile_working.writeLine(data_working);  
            }
        catch (IOException ex) {
            Logger.getLogger(KiberaObserver.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.step++;
    }

     void finish() {
        try {
            this.dataFileBuffer_rebels.close();
            
            this.dataFileBuffer_action.close();
            this.dataFileBuffer_identity.close();
            this.dataFileBuffer_degreestats.close();
            this.dataFileBuffer_network.close();
            this.dataFileBuffer_working.close();
            this.dataFileBuffer_residents.close();

        }
        catch (IOException ex) {
            Logger.getLogger(KiberaObserver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    //create files
    private void createLogFile() throws IOException {
        long now = System.currentTimeMillis();

        String filename_action = String.format("%ty%tm%td%tH%tM%tS" + "_" + Integer.toString(kibera.RUN), now, now, now, now, now, now, now, now)
            + "_actions.csv";
        String filename_identity = String.format("%ty%tm%td%tH%tM%tS" + "_" + Double.toString(kibera.SWEEP) + "_" + Integer.toString(kibera.RUN), now, now, now, now, now, now, now, now, now)
                    + "_identity.csv";
        String filename_degreestats = String.format("%ty%tm%td%tH%tM%tS" + "_" + Double.toString(kibera.SWEEP) + "_" + Integer.toString(kibera.RUN), now, now, now, now, now, now, now, now)
                    + "_degreestats.csv";
        String filename_network = String.format("%ty%tm%td%tH%tM%tS" + "_" + Double.toString(kibera.SWEEP) + "_" + Integer.toString(kibera.RUN), now, now, now, now, now, now)
                    + "_network.csv";
        String filename_working = String.format("%ty%tm%td%tH%tM%tS" + "_" + Double.toString(kibera.SWEEP) + "_" + Integer.toString(kibera.RUN), now, now, now, now, now, now, now, now, now, now, now, now, now)
                    + "_working.csv";
        String filename_residents = String.format("%ty%tm%td%tH%tM%tS" + "_" + Double.toString(kibera.SWEEP) + "_" + Integer.toString(kibera.RUN), now, now, now, now, now, now, now, now, now, now, 
                now, now, now, now, now, now, now, now, now, now, now, now, now, now, now, now, now, 
                now, now, now, now, now, now, now, now, now, now, now, now, now, now, now)
                    + "_residents.csv";
        String filename_rebels = String.format("%ty%tm%td%tH%tM%tS" + "_" + Double.toString(kibera.SWEEP) + "_" + Integer.toString(kibera.RUN), now, now, now, now, now, now, now, now, now)
                    + "_rebels.csv";
        
        // activity
        this.dataFileBuffer_action = new BufferedWriter(new FileWriter(filename_action));
        this.dataCSVFile_action = new CSVWriter(dataFileBuffer_action);

        this.dataFileBuffer_identity = new BufferedWriter(new FileWriter(filename_identity));
        this.dataCSVFile_identity = new CSVWriter(dataFileBuffer_identity);

        this.dataFileBuffer_degreestats = new BufferedWriter(new FileWriter(filename_degreestats));
        this.dataCSVFile_degreestats = new CSVWriter(dataFileBuffer_degreestats);

        this.dataFileBuffer_network = new BufferedWriter(new FileWriter(filename_network));
        this.dataCSVFile_network = new CSVWriter(dataFileBuffer_network);  

        this.dataFileBuffer_working = new BufferedWriter(new FileWriter(filename_working));
        this.dataCSVFile_working = new CSVWriter(dataFileBuffer_working);  

       this.dataFileBuffer_residents = new BufferedWriter(new FileWriter(filename_residents));
       this.dataCSVFile_residents = new CSVWriter(dataFileBuffer_residents);  
       
       this.dataFileBuffer_rebels = new BufferedWriter(new FileWriter(filename_rebels));
       this.dataCSVFile_rebels = new CSVWriter(dataFileBuffer_rebels);  
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.writeInt(step);

    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        step = in.readInt();

        startLogFile();
    }

}

package kibera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import kibera.Resident.Employment;
import kibera.Resident.Gender;
import kibera.Resident.Identity;
import kibera.Resident.Religion;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;
import sim.util.geo.MasonGeometry;

/**
 * Kibera Builder
 * 
 * Creates the modeling world and the agents
 * 
 * Source of GIS data to build the modeling world was Map Kibera (Hagen, 2011). A project which used OpenStreetMap to 
 * geocode neighborhoods, transportation routes, and locations of facilities, water points, and sanitation
 * 
 * The source of the estimated population of Kibera was the Map Kibera Project (Marras, 2008)
 * 
 * Source of other environmental variables came from the Map Kibera Project (Marras, 2008)
 * Data included the average size of structures, the average number of homes and businesses in a structure
 * 
 * Source of most socioeconomic data to create agents (residents and households) was mainly the Map Kibera Project (Marras, 2008)
 * Data included household size, age distribution, gender distribution
 * 
 * 
 * 
 * @author bpint
 */
public class KiberaBuilder extends Stats {

    //Create a world based on GIS data
    static int gridWidth = 0;
    static int gridHeight = 0;

    static int intNumNeighborhoods = 15;

    /**
     * Create modeling world based on GIS data
     * 
     *
     * @param landFile - identifies the modeling world and boundaries (OpenStreetMap, 2013)
     * @param roadFile - the road network, which includes roads and walking paths (OpenStreetMap, 2013)
     * @param schoolFile - the locations of schools (OpenStreetMap, 2013)
     * @param healthFile - the location of health centers (OpenStreetMap, 2013)
     * @param religionFile - the location of religious institutions (OpenStreetMap, 2013)
     * @param watsanFile - the location of water points and sanitation (OpenStreetMap, 2013)
     * @param kibera
     * 
     *
     */
    public static void createWorld(String landFile, String roadFile, String schoolFile, String healthFile, String religionFile, String watsanFile, Kibera kibera) {
        kibera.parcels.clear();
        kibera.households.clear();
        kibera.residents.clear();
        kibera.structures.clear();
        kibera.homes.clear();
        kibera.availableParcels.clear();
        kibera.allStructureLocations.clear();
        kibera.allBusinessLocations.clear();
        kibera.allHomeLocations.clear();

        kibera.kikuyu.clear();
        kibera.luhya.clear();
        kibera.luo.clear();
        kibera.kalinjin.clear();
        kibera.kamba.clear();
        kibera.kisii.clear();
        kibera.meru.clear();
        kibera.mijikenda.clear();
        kibera.maasai.clear();
        kibera.turkana.clear();
        kibera.embu.clear();
        kibera.other.clear();
        
        //add the land and roads
        createLand(landFile, kibera);
        createRoads(roadFile, kibera);

        kibera.schedule.clear();
        
        //add the structures, facilities, and water points
        addStructures(kibera);
        addFacilities(schoolFile, healthFile, religionFile, kibera);
        addWatsan(watsanFile, kibera);
        
        //add the houesholds to a home
        addHouseholds(kibera);	
        
        if (kibera.doesRumorSpread) {
            rumorPropogation(kibera);
        }
    }
    
    /**
     * Create modeling boundaries based on GIS data
     *
     * @param landFile - identifies the modeling world and boundaries
     * @param kibera
     * 
     *
     */
    private static void createLand(String landFile, Kibera kibera) {		
        try {
            // buffer reader - read ascii file
            BufferedReader land = new BufferedReader(new FileReader(landFile));
            String line;

            // first read the dimensions
            line = land.readLine(); // read line for width
            String[] tokens = line.split("\\s+");
            int width = Integer.parseInt(tokens[1]);
            gridWidth = width;

            line = land.readLine();
            tokens = line.split("\\s+");
            int height = Integer.parseInt(tokens[1]);
            gridHeight = height;

            kibera.setWidth(width);
            kibera.setHeight(height);

            createGrids(kibera, width, height);

            // skip the next four lines as they contain irrelevant metadata
            for (int i = 0; i < 4; ++i) {
                line = land.readLine();
            }

            Neighborhood neighborhood = null;

            //Create the neighborhoods in Kibera
            for (int i = 1; i <= intNumNeighborhoods; ++i) {
                neighborhood = new Neighborhood();
                kibera.allNeighborhoods.put(i, neighborhood);
                neighborhood.setNeighborhoodID(i);
            }

            for (int curr_row = 0; curr_row < height; ++curr_row) {
                line = land.readLine();
                tokens = line.split("\\s+");

                //Column 0 is blank in file, so have to adjust for blank column
                for (int curr_col = 0; curr_col < width; ++curr_col) {
                    int neighborhoodID = Integer.parseInt(tokens[curr_col]);

                    Parcel parcel = null;

                    if (neighborhoodID < 100) {                   	
                        Int2D parcelLocation = new Int2D(curr_col, curr_row);
                        parcel = new Parcel(parcelLocation);

                        kibera.parcels.add(parcel);     

                        parcel.setParcelID(neighborhoodID);

                        neighborhood = kibera.allNeighborhoods.get(neighborhoodID);

                        neighborhood.addParcel(parcel);
                        parcel.setNeighborhood(neighborhood);

                        kibera.landGrid.set(curr_col, curr_row, parcel);                           
                    }
                    else {
                        Int2D parcelLocation = new Int2D(curr_col, curr_row);
                        parcel = new Parcel(parcelLocation);
                        parcel.setParcelID(0);
                        kibera.landGrid.set(curr_col, curr_row, parcel); 
                    }
                }              
            }
            land.close();		
        }
        catch (IOException ex) {
            Logger.getLogger(KiberaBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Add facilities based on GIS data
     *
     * @param schoolFile - the locations of schools (OpenStreetMap, 2013)
     * @param healthFile - the location of health centers (OpenStreetMap, 2013)
     * @param religionFile - the location of religious institutions (OpenStreetMap, 2013)
     * @param kibera
     * 
     *
     */
    public static void addFacilities(String schoolFile, String healthFile, String religionFile, Kibera kibera) {
        try {
            // buffer reader - read ascii file
            BufferedReader facilities = new BufferedReader(new FileReader(schoolFile));
            String line;

            BufferedReader healthFacilities = new BufferedReader(new FileReader(healthFile));
            String healthLine;

            BufferedReader religiousFacilities = new BufferedReader(new FileReader(religionFile));
            String religiousLine;

            // first read the dimensions
            line = facilities.readLine(); // read line for width
            String[] tokens = line.split("\\s+");
            int width = Integer.parseInt(tokens[1]);
            gridWidth = width;

            line = facilities.readLine();
            tokens = line.split("\\s+");
            int height = Integer.parseInt(tokens[1]);
            gridHeight = height;

            //Add the health facilities
            for (int i = 0; i < 6; ++i) {
                healthLine = healthFacilities.readLine();
            }

            HealthFacility healthFacility = null;

            for (int curr_row = 0; curr_row < height; ++curr_row) {
                healthLine = healthFacilities.readLine();

                tokens = healthLine.split("\\s+");

                for (int curr_col = 0; curr_col < width; ++curr_col) {
                    int healthFacilityType = Integer.parseInt(tokens[curr_col]);

                    Parcel parcel = null;
                    double rn = kibera.random.nextDouble();

                    if (healthFacilityType < 100) {                    	
                        Int2D parcelLocation = new Int2D(curr_col, curr_row);						

                        parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);
                        int numStructuresOnParcel = parcel.getStructure().size();

                        if (numStructuresOnParcel == 0) {
                            Structure s = new Structure(parcel);
                            kibera.structures.add(s);
                            healthFacility = new HealthFacility(s, healthFacilityType);
                            s.setParcel(parcel);
                            parcel.addStructure(s);

                            //added -- see if this makes it work
                            s.addHealthFacility(healthFacility);
                            healthFacility.setStructure(s);
                            healthFacility.setFacilityID(healthFacilityType);

                            int employeeCapacity = 0;
                            //Determine capacity of employees of business

                            int capacity = (int) kibera.params.global.getFormalBusinessCapacity();

                            if (rn < (kibera.params.global.getFormalBusinessCapacity() - capacity)) {
                                employeeCapacity = 1 + capacity;
                            }
                            else {
                                employeeCapacity = capacity;
                            }
                            healthFacility.setEmployeeCapacity(employeeCapacity);
                            
                        }
                        else {
                            int rnStructure = 1 + kibera.random.nextInt(numStructuresOnParcel);

                            ArrayList <Structure> structures = parcel.getStructure();

                            int i = 0;
                            for(Structure s : structures) {
                                i++;

                                if (i == rnStructure) {
                                    healthFacility = new HealthFacility(s, healthFacilityType);
                                    s.addHealthFacility(healthFacility);
                                    healthFacility.setStructure(s);
                                    healthFacility.setFacilityID(healthFacilityType);

                                    int employeeCapacity = 0;
                                    int capacity = (int) kibera.params.global.getFormalBusinessCapacity();

                                    //Determine capacity of employees of business                                       
                                    if (rn < (kibera.params.global.getFormalBusinessCapacity() - capacity)) {
                                        employeeCapacity = 1 + capacity;
                                    }
                                    else {
                                        employeeCapacity = capacity;
                                    }

                                    healthFacility.setEmployeeCapacity(employeeCapacity);
                                    
                                }
                            }
                        }
                        kibera.allHealthFacilityLocations.add(parcelLocation);
                        kibera.healthFacilityGrid.setObjectLocation(healthFacility, parcelLocation);   
                    }					
                }
            }

            //Add the religious facilities (church/mosque)
            for (int i = 0; i < 6; ++i) {
                religiousLine = religiousFacilities.readLine();
            }

            ReligiousFacility religiousFacility = null;

            for (int curr_row = 0; curr_row < height; ++curr_row) {
                religiousLine = religiousFacilities.readLine();

                tokens = religiousLine.split("\\s+");

                for (int curr_col = 0; curr_col < width; ++curr_col) {
                    int religiousFacilityType = Integer.parseInt(tokens[curr_col]);

                    Parcel parcel = null;
                    double rn = kibera.random.nextDouble();

                    if (religiousFacilityType < 100) {                    	
                            Int2D parcelLocation = new Int2D(curr_col, curr_row);						

                            parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);
                            int numStructuresOnParcel = parcel.getStructure().size();

                            if (numStructuresOnParcel == 0) {
                                Structure s = new Structure(parcel);
                                kibera.structures.add(s);
                                religiousFacility = new ReligiousFacility(s, religiousFacilityType);
                                s.setParcel(parcel);
                                parcel.addStructure(s);
                                s.addReligiousFacility(religiousFacility);
                                religiousFacility.setStructure(s);
                                religiousFacility.setFacilityType(religiousFacilityType);

                                int employeeCapacity = 0;
                                int capacity = (int) kibera.params.global.getFormalBusinessCapacity();
                            
                                if (rn < (kibera.params.global.getFormalBusinessCapacity() - capacity)) {
                                    employeeCapacity = 1 + capacity;
                                }
                                else {
                                    employeeCapacity = capacity;
                                }
                                religiousFacility.setEmployeeCapacity(employeeCapacity);

                            }

                            else {
                                int rnStructure = 1 + kibera.random.nextInt(numStructuresOnParcel);

                                ArrayList <Structure> structures = parcel.getStructure();

                                int i = 0;
                                for(Structure s : structures) {
                                    i++;

                                    if (i == rnStructure) {
                                        religiousFacility = new ReligiousFacility(s, religiousFacilityType);
                                        s.addReligiousFacility(religiousFacility);
                                        religiousFacility.setStructure(s);
                                        religiousFacility.setFacilityType(religiousFacilityType);
                                     
                                        int employeeCapacity = 0;
                                        int capacity = (int) kibera.params.global.getFormalBusinessCapacity();

                                        //Determine capacity of employees of business                                           
                                        if (rn < (kibera.params.global.getFormalBusinessCapacity() - capacity)) {
                                            employeeCapacity = 1 + capacity;
                                        }
                                        else {
                                            employeeCapacity = capacity;
                                        }
                                        religiousFacility.setEmployeeCapacity(employeeCapacity);

                                    }
                                }
                            }
                            kibera.allReligiousFacilityLocations.add(parcel);
                            kibera.religiousFacilityGrid.setObjectLocation(religiousFacility, parcelLocation);   
                        }					
                    }
                }
			
                //Add all the schools
                //skip the next four lines as they contain irrelevant metadata
                for (int j = 0; j < 4; ++j) {
                    line = facilities.readLine();
                }

                School school = null;

                for (int curr_row = 0; curr_row < height; ++curr_row) {
                    line = facilities.readLine();

                    tokens = line.split("\\s+");

                    for (int curr_col = 0; curr_col < width; ++curr_col) {
                        int facilityID = Integer.parseInt(tokens[curr_col]);

                        Parcel parcel = null;

                        if (facilityID < 100) {                    	
                            Int2D parcelLocation = new Int2D(curr_col, curr_row);						
                            parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);
                            int numStructuresOnParcel = parcel.getStructure().size();

                            if (numStructuresOnParcel == 0) {
                                Structure s = new Structure(parcel);
                                kibera.structures.add(s);
                                school = new School(s, facilityID);
                                s.setParcel(parcel);
                                parcel.addStructure(s);

                                s.addSchool(school);
                                school.setStructure(s);
                                school.setFacilityID(facilityID);

                            }

                            else {
                                int rn = 1 + kibera.random.nextInt(numStructuresOnParcel);

                                ArrayList <Structure> structures = parcel.getStructure();

                                int j = 0;
                                for(Structure s : structures) {
                                    j++;

                                    if (j == rn) {
                                        school = new School(s, facilityID);
                                        s.addSchool(school);
                                        school.setStructure(s);
                                        school.setFacilityID(facilityID);

                                    }
                                }
                            }

                            if (facilityID == 1) {
                                kibera.allSchoolLocations.add(parcel);
                                int studentCapacity = 0;
                                int sCapacity = (int) kibera.params.global.getSchoolCapacity();

                                double rnStudent = kibera.random.nextDouble();

                                if (rnStudent < (kibera.params.global.getSchoolCapacity() - sCapacity)) {
                                    studentCapacity = 1 + sCapacity;
                                }
                                else {
                                    studentCapacity = sCapacity;
                                }

                                school.setSchoolCapacity(studentCapacity);
                                
                                int numClasses = (int) (studentCapacity / kibera.numClassMates);
                                if (numClasses < 1) { numClasses = 1; }
                                
                                //Add classes to school
                                int i = 0;
                                while (i < numClasses) {
                                    SchoolClass schoolClass = new SchoolClass(school);
                                    school.addSchoolClass(schoolClass);
                                    schoolClass.setSchool(school);                                   
                                    
                                    i++;
                                }

                                int employeeCapacity = 0;
                                int eCapacity = (int) kibera.params.global.getFormalBusinessCapacity();

                                //Determine capacity of employees of business
                                double rnEmployee = kibera.random.nextDouble();

                                if (rnEmployee < (kibera.params.global.getFormalBusinessCapacity() - eCapacity)) {
                                    employeeCapacity = 1 + eCapacity;
                                }
                                else {
                                    employeeCapacity = eCapacity;
                                }
                                school.setEmployeeCapacity(employeeCapacity);                              

                            }
                        kibera.facilityGrid.setObjectLocation(school, parcelLocation);                     
                        }
                    }              
                }

                facilities.close();		
                healthFacilities.close();
                religiousFacilities.close();
        }
        catch (IOException ex) {
            Logger.getLogger(KiberaBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Add water points based on GIS data
     *
     * @param watsanFile - the location of water points and sanitation (OpenStreetMap, 2013)
     * @param kibera
     * 
     *
     */
    public static void addWatsan(String watsanFile, Kibera kibera) {
        try {
            // buffer reader - read ascii file
            BufferedReader waterSanitation = new BufferedReader(new FileReader(watsanFile));
            String line;

            // first read the dimensions
            line = waterSanitation.readLine(); // read line for width
            String[] tokens = line.split("\\s+");
            int width = Integer.parseInt(tokens[1]);
            gridWidth = width;

            line = waterSanitation.readLine();
            tokens = line.split("\\s+");
            int height = Integer.parseInt(tokens[1]);
            gridHeight = height;

            //Add the water and sanitation points
            for (int i = 0; i < 4; ++i) {
                line = waterSanitation.readLine();
            }

            WaterPoint waterPoint = null;
            Sanitation sanitation = null;

            for (int curr_row = 0; curr_row < height; ++curr_row) {
                line = waterSanitation.readLine();
                tokens = line.split("\\s+");

                for (int curr_col = 0; curr_col < width; ++curr_col) {
                    int id = Integer.parseInt(tokens[curr_col]);

                    Parcel parcel = null;

                    Int2D parcelLocation = new Int2D(curr_col, curr_row);	
                    parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);

                    if (id == 1) {                    																			
                        waterPoint = new WaterPoint(parcel);
                        waterPoint.setParcel(parcel);
                        parcel.addWaterPoint(waterPoint);
                        kibera.allWaterPoints.add(parcel);
                        kibera.waterGrid.setObjectLocation(waterPoint, parcelLocation);
                    }

                    if (id == 2 || id == 6 || id == 8) {
                        sanitation = new Sanitation(parcel);
                        parcel.addSanitation(sanitation);
                        sanitation.setParcel(parcel);
                        kibera.allSanitationLocations.add(parcel);
                        kibera.sanitationGrid.setObjectLocation(sanitation, parcelLocation);
                    }

                }              
            }
            waterSanitation.close();		
        }
        catch (IOException ex) {
            Logger.getLogger(KiberaBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
	
    /**
     * Add structures to parcels
     * Source of structure size was the Map Kibera Project (Marras, 2008)
     *
     * @param kibera
     * 
     *
     */
    public static void addStructures(Kibera kibera) {
        //add structures while there is still place to put them
        Parcel residingParcel = null;

        for(int i = 0; i < kibera.parcels.numObjs; i++) {
            Parcel p = (Parcel) kibera.parcels.objs[i];
            int id = p.getParcelID();
            if (p.isParcelOccupied(kibera) == false && id > 0) {
                residingParcel = p;
                Structure s = new Structure(residingParcel);

                s.setParcel(residingParcel);
                residingParcel.addStructure(s);
                //determine capacity of each structure
                double shouldAddHouses = kibera.random.nextDouble();
                int numberOfHouses = 5; //average number of houses in a structure

                double shouldAddBusinesses = kibera.random.nextDouble();
                int numberOfBusinesses = 3; //average number of businesses in a structure

                int homeCapacity = 0;
                int businessCapacity = 0;

                if (shouldAddHouses < 0.86) { //add household(s) to structure
                    homeCapacity = numberOfHouses;
                    for (int j = 0; j < numberOfHouses; j++) {
                        addHomes(kibera, s, s.getParcel());
                    }
                }

                if (shouldAddBusinesses < 0.13) { //add business(es) to structure			
                    businessCapacity = numberOfBusinesses;
                    for (int k = 0; k < numberOfBusinesses; k++) {
                        addBusiness(kibera, s, s.getParcel());
                    }
                }

                //home and business capacity of each structure
                s.setStructureCapacity(homeCapacity, businessCapacity);
                kibera.allStructureLocations.add(residingParcel);
                kibera.structures.add(s);
            }

        }
    }

    /**
     * Add households to an available home based on preference and affordability
     * Households will prefer to live near neighbors "like" them based on the preference setting. 
     * This behavior is inspired by the Schelling segregation model.
     *
     * @param kibera
     * 
     *
     */
    public static void addHouseholds(Kibera kibera) {				
        int totalResidents = kibera.params.global.getNumResidents();
        int i = 0;
        boolean isHeadOfHousehold = false;
        int residentID = 0;
        
        //add object to track employees working outside of kibera
        OutsideKibera employerOutsideKibera = new OutsideKibera();
        
        int capacityFormalOutsideKibera = 0;
        int capacityInformalOutsideKibera = 0;
        //minimum amount of income needed to afford the cheapest rent in kibera
        int minAffordability = (int)(kibera.rent[0] / kibera.percentIncomeforRent);

        //Initialize household 
        while (i < totalResidents) {   
            double mean = 3.55;	        	
            double stdev = 1.61;
            
            //determine household size based on lognormal distribution
            double x = Stats.normalToLognormal(Stats.calcLognormalMu(mean, stdev), Stats.calcLognormalSigma(mean, stdev), kibera.random.nextGaussian());
            int householdSize = (int) x;

            //determine household attributes  	
            String hhEthnicity = determineHouseholdEthnicity(kibera);

            //calculate total income for household
            double expectedHouseholdIncome = 0;

            //determine household religion
            double rnReligion = kibera.random.nextDouble();
            Resident.Religion religion;

            //source was CIA World Factobook (2013), Marras (2008), and Pew Forum on Religion & Public Life (2010)
            if (rnReligion < .825) { religion = Religion.Christian; }
            else if (rnReligion < .825 + 111) { religion = Religion.Muslim; }
            else { religion = Religion.Other; }

            Household hh = new Household();
            kibera.households.add(hh);

            //if there are still households searching for a home but all homes are occupied, no more households should be added to simulation
            if (kibera.allHomesAvailable.isEmpty()) { break; }

                for (int j = 0; j < householdSize; j++) {            
                    Resident.Employment employmentStatus = null;

                    if (j == 0) { isHeadOfHousehold = true; }

                    int residentAge = determineResidentAge(kibera, isHeadOfHousehold);
                    Resident.Gender gender = determineResidentGender(kibera);	  
                    
                    //determine employment status
                    if (kibera.useEmploymentStats) { employmentStatus = determineEmploymentStatus(residentAge, gender, kibera); }
                    else { employmentStatus = determineInitialEmploymentStatus(residentAge); }
                    
                    //determine if resident is eligible to go school based on age
                    boolean isSchoolEligible = determineStudentStatus(residentAge, employmentStatus, kibera);   

                    //not all that are school eligible will go to school, many will work in informal sector
                    //assume some that are school eligible will work
                    double income = 0;
                    double rn = kibera.random.nextDouble();
                    double expectedIncome = 0;
                    
                    //assign some to work based on employment status information by gender
                    if (isSchoolEligible && gender == Gender.female) {
                        if (rn < (0.41 * kibera.getInformalityIndex())) {
                            employmentStatus = Employment.Informal;
                            expectedIncome = WealthDistribution.determineIncome(employmentStatus, kibera);
                            employmentStatus = Employment.Inactive;
                        }
                    }

                    if (isSchoolEligible && gender == Gender.male) {
                        if (rn < (0.6 * kibera.getInformalityIndex())) {
                            employmentStatus = Employment.Informal;
                            expectedIncome = WealthDistribution.determineIncome(employmentStatus, kibera);
                            employmentStatus = Employment.Inactive;
                        }
                    }
                    
                    //determine income based on Lorenz curve
                    income = WealthDistribution.determineIncome(employmentStatus, kibera);
                    expectedHouseholdIncome = expectedHouseholdIncome + expectedIncome + income;
                    
                    //determien adult equivalence based on age
                    double adultEquivalent = determineAdultEquivalent(residentAge, kibera);
                    
                    //if the resident is christian, assign a church time
                    int churchRnd = kibera.random.nextInt(4);
                    
                    //create the individual residents
                    addResidents(kibera, residentID, hh, hhEthnicity, residentAge, gender, religion, churchRnd, isHeadOfHousehold, employmentStatus, isSchoolEligible, income, adultEquivalent);

                    residentID += 1;
                  
                    isHeadOfHousehold = false;

                    }
                
                    //if no one in the household is employed, then move into min rent home (adjust expected household income for this)
                    if (expectedHouseholdIncome == 0) {	        		
                            expectedHouseholdIncome = minAffordability;
                    }
                    
                    //find a home to reside
                    Home residingHouse = findHometoPlaceHousehold(kibera, hh, hhEthnicity, expectedHouseholdIncome);

                    hh.setHome(residingHouse);
                    residingHouse.setHousehold(hh);

                    hh.removedStudentFromSchool(false);
                    hh.removedFromInactive(false);

                    hh.setAdjustedHouseholdExpenditures(Household.AdjustedHouseholdExpenditures.Same);

                    //set initial position of residents in household
                    for (int j = 0; j < hh.getHouseholdMembers().size(); j++) {
                        
                        //set initial position and goal of each resident to home
                        Resident r = (Resident) hh.getHouseholdMembers().get(j);        	
                        Parcel initialPosition = residingHouse.getStructure().getParcel();
                        r.setPosition(initialPosition);

                        r.setGoalLocation(hh.getHome().getStructure().getParcel());

                        double jitterX = kibera.random.nextDouble();
                        double jitterY = kibera.random.nextDouble();                    

                        kibera.world.setObjectLocation(r, new Double2D(initialPosition.getXLocation() + jitterX, initialPosition.getYLocation() + jitterY));
                        
                        //give all residents a pointer to object Outside Kibara
                        r.setEmploymentOutsideKibera(employerOutsideKibera);
                        employerOutsideKibera.addNonEmployee(r);
                        
                        //if employment status is employed, then find an employer
                        if (r.getCurrentEmploymentStatus() == Employment.Formal || r.getCurrentEmploymentStatus() == Employment.Informal) {
                            assignEmploymentLocation(r, hh, r.getCurrentEmploymentStatus(), employerOutsideKibera, kibera);
                        }
                        
                        //if eligible to go to school and not employed, then find an available school
                        if (r.isSchoolEligible() && r.getCurrentEmploymentStatus() != Employment.Formal && r.getCurrentEmploymentStatus() != Employment.Informal) {
                            r.searchedForSchool(false);
                            assignSchoolLocation(r, hh, kibera);
                        }
                        
                        //if employment status is employed, set identity to employed
                        if (r.getCurrentEmploymentStatus() == Employment.Formal || r.getCurrentEmploymentStatus() == Employment.Informal) {
                            r.setCurrentIdentity(Identity.Employer);
                        }
                        //if found an available school, set identity to student
                        if (r.getMySchool() != null) {
                            r.setCurrentIdentity(Identity.Student);
                        } 

                        kibera.schedule.scheduleRepeating(r);
                    }                                             

                    kibera.schedule.scheduleRepeating(hh);

                    i = i + householdSize;        
        }
        
        //assign capacity for employers outside kibera based on number of employed residents that did not find a job within kibera
        int numEmployeesOutsideKibera = employerOutsideKibera.getEmployees().numObjs;
        
        for (int j = 0; j < numEmployeesOutsideKibera; j++) {
            Resident res = (Resident) employerOutsideKibera.getEmployees().get(j);
            if (res.getCurrentEmploymentStatus() == Employment.Formal) {
                capacityFormalOutsideKibera++;
            }
            else {
                capacityInformalOutsideKibera++;
            }
        }
        
        kibera.params.global.setFormalOutsideKiberaCapacity(capacityFormalOutsideKibera);
        kibera.params.global.setInformalBusinessCapacity(capacityInformalOutsideKibera);
        	 		
    }
		
    /**
     * Add businesses to structures
     * Source of number of businesses per structure was the Map Kibera Project (Marras, 2008)
     *
     * @param kibera
     * @param structure - the structure the business is located in
     * @param parcel - the parcel the structure is located on
     * 
     *
     */
    public static void addBusiness(Kibera kibera, Structure structure, Parcel parcel) {		
        kibera.allBusinessLocations.add(parcel);
        
        //initialize business
        Business b = new Business(structure);
        structure.addBusinesses(b);
        b.setStructure(structure);

        int employeeCapacity = 0;
        
        //assign business capacity based on average number of employees that can work at an informal business
        double rn = kibera.random.nextDouble();
        int capacity = (int) kibera.params.global.getInformalBusinessCapacity();

        if (rn < (kibera.params.global.getInformalBusinessCapacity() - capacity)) {
            employeeCapacity = 1 + capacity;
        }
        else {
            employeeCapacity = capacity;
        }
        
        //assign capacity to business
        b.setEmployeeCapacity(employeeCapacity);
        kibera.businessGrid.setObjectLocation(structure, parcel.getLocation()); //add business to residing structure and parcel
    }
    
    /**
     * Add homes to structures
     * Source of number of homes per structure was Map Kibera Project (Marras, 2008)
     *
     * @param kibera
     * @param structure - the structure the home is located in
     * @param parcel - the parcel the structure is located on
     * 
     *
     */
    public static void addHomes(Kibera kibera, Structure structure, Parcel parcel) {		
        kibera.allHomeLocations.add(parcel);
        
        //initialize home
        Home h = new Home(structure);
        kibera.homes.add(h);
        structure.addHome(h);
        h.setStructure(structure);

        //Determine the household expenditures related to each home
        //Determine the rent of the house
        h.setHouseRent(determineHomeRent(kibera));

        //Determine if house comes with running water, electricity, and/or sanitation
        double rnWater = kibera.random.nextDouble();
        double rnElectric = kibera.random.nextDouble();
        double rnSanitation = kibera.random.nextDouble();

        boolean hasWater = false;
        boolean hasElectricity = false;
        boolean hasSanitation = false;

        if (rnWater < kibera.probabilityWater) hasWater = true;
        if (rnElectric < kibera.probabilityElectricity) hasElectricity = true;
        if (rnSanitation < kibera.probabilitySanitation) hasSanitation = true;

        //determine household expenditures
        double monthlyElectricCost = 0;

        if (hasElectricity) {
            monthlyElectricCost = kibera.electricCost;
        }

        h.hasWater(hasWater);
        h.hasElectricity(hasElectricity);
        h.hasSanitation(hasSanitation);

        h.setExpectedElectricityCost(monthlyElectricCost);
        h.setExpectedRunningWaterCost(kibera.waterCost);

        kibera.allHomesAvailable.add(h);
        
        //add home to the structure and parcel
        kibera.houseGrid.setObjectLocation(structure, parcel.getLocation());

    }
    
    /**
     * Determine the household ethnicity
     * Source of ethnic distribution was CIA World Factbook (2013)
     *
     * @param kibera
     * 
     *
     */
    private static String determineHouseholdEthnicity(Kibera kibera) {
        String householdEthnicity = null;
        double rn = kibera.random.nextDouble();
        double distCumulative = 0;
        
        //determine household ethnicity based on Kenya's ethnic distribution
        for (int i = 0; i < kibera.ethnicDistribution.length; i++) {
            distCumulative = distCumulative + kibera.ethnicDistribution[i];

            if (rn <= distCumulative && householdEthnicity == null) { 
                householdEthnicity = kibera.ethnicities[i];
            }			
        }
        return householdEthnicity;
    }
    
    /**
     * Assign a monthly rent cost to each home
     * Source of distribution of rent was the Map Kibera Project (Marras, 2008)
     *
     * @param kibera
     * 
     *
     */
    private static double determineHomeRent(Kibera kibera) {
        double homeRent = 0;
        double rn = kibera.random.nextDouble();
        double distCumulative = 0;
        
        //assign rent to each home based on empirical distribution of rent costs
        for (int i = 0; i < kibera.rentDistribution.length; i++) {
            distCumulative = distCumulative + kibera.rentDistribution[i];

            if (rn <= distCumulative && homeRent == 0) {
                homeRent = kibera.rent[i];
                
                double x = 0;
                if (i > 0) {
                    x = kibera.rent[i-1];
                }
                              
                x = homeRent - x;
                x = kibera.random.nextInt((int)(x));
                
                homeRent = homeRent - x;
                break;
            }
        }
        return homeRent;
    }
    
    /**
     * Determine the agent's age
     * Source of age distribution was the Map Kibera Project (Marras, 2008)
     *
     * @param kibera
     * 
     *
     */
    private static int determineResidentAge(Kibera kibera, boolean isHeadOfHousehold) {
	double rn = kibera.random.nextDouble();
        int age = 0;
        
        //make sure the head of household is an adult
        if (isHeadOfHousehold) {
            age = 18 + kibera.random.nextInt(42); // 18-59      	
        }
            
        else {               
            if (rn <= kibera.ageAdult) { age = 18 + kibera.random.nextInt(62); } // adult (18 and over)
            else if (rn <= (kibera.ageAdult + kibera.ageChildrenUnder6)) { age = kibera.random.nextInt(6); } // child under 6
            else { age = 6 + kibera.random.nextInt(12); }  // child (6-17)
        }
        
        return age;
    }
    
    /**
     * Determine the agent's gender
     * Source of gender distribution was the Map Kibera Project (Marras, 2008)
     * 
     * @param kibera
     * 
     *
     */
    private static Resident.Gender determineResidentGender(Kibera kibera) {
        //assign gender based on empirical data
        if (kibera.random.nextDouble() < kibera.maleDistribution) {
            return Resident.Gender.male;
        }
        else {
            return Resident.Gender.female; 
        }
    }
    
    /**
     * Assign adult equivalence based on age. Used to assign daily costs to households.
     * Source was Gulyani and Talukdar (2008)
     *
     * @param kibera
     * 
     *
     */
    private static double determineAdultEquivalent(int age, Kibera kibera) {
        
        if (age < 5) { return kibera.adultEquivalentAge0to4; }      
        else if (age > 4 && age < 15) { return kibera.adultEquivalentAge5to14; }
        else { return kibera.adultEquivalentAge15andOver; }
       
    }
    
    /**
     * If empirical data is not used to determine employment status, then assume everyone begin simulation by searching
     * for employment
     *
     * @param age - the resident's age
     * 
     *
     */
    private static Employment determineInitialEmploymentStatus(int age) {
        //search for employment if over 5 years of age
        if (age < 6) { return Employment.Inactive; }
        else { return Employment.Searching; }
    }
    
    /**
     * If empirical data is used to determine employment status, then assign employment status
     * Source was Kenya National Bureau of Statistics (2009) and United Nations Human Settlements Programme (2003)
     *
     * @param age - the resident's age
     * @param gender - the resident's gender
     * @param kibera
     * 
     *
     */
    private static Employment determineEmploymentStatus(int age, Resident.Gender gender, Kibera kibera) {
        
        //percentage of resident's employed, searching, and inactive by gender is derived from empirical data
        double random = kibera.random.nextDouble();
        double femaleWorking = .41; // 41% of females are employed
        double femaleSearching = .096; // 9.6% of females are seeking work
        double femaleInactive = .431; //43.1% of females are economically inactive
        double femaleUnknown = .063;

        //adjust for all under 6 being inactive
        femaleInactive = femaleInactive - kibera.percentOfResidentsUnder6;

        //adjust so all categories sum to 100%            
        double femaleTotal = femaleWorking + femaleSearching + femaleInactive + femaleUnknown;
        femaleWorking = femaleWorking / femaleTotal;
        femaleSearching = femaleSearching / femaleTotal;
        femaleInactive = femaleInactive / femaleTotal;

        double maleWorking = 0.6; // 60% of males are working
        double maleSearching = .079; // 7.9% of males are seeking work
        double maleInactive = .271; // 27.1% of males are economically inactive
        double maleUnknown = .05;

        //adjust for all under 6 being inactive
        maleInactive = maleInactive - kibera.percentOfResidentsUnder6;

        //adjust so all categories sum to 100%
        double maleTotal = maleWorking + maleSearching + maleInactive + maleUnknown;
        maleWorking = maleWorking / maleTotal;
        maleSearching = maleSearching / maleTotal;
        maleInactive = maleInactive / maleTotal;

        double femaleInformal = femaleWorking * kibera.getInformalityIndex();
        double femaleFormal = femaleWorking - femaleInformal;

        double maleInformal = maleWorking * kibera.getInformalityIndex();
        double maleFormal = maleWorking - maleInformal;

        //too young to work, or if school age will search for school first
        if (age <= 18) {
            return Employment.Inactive;
        }
        
        //assign employment status if femal
        else if (gender == Resident.Gender.female) {
            if (random < femaleFormal) { return Employment.Formal; }
            else if (random < (femaleFormal + femaleInformal)) { return Employment.Informal; }
            else if (random < (femaleFormal + femaleInformal + femaleSearching)) { return Employment.Searching; }
            else if (random < (femaleFormal + femaleInformal + femaleSearching + femaleInactive)) { return Employment.Inactive; }
            else { return Employment.Searching; } //the employment status of 6.3% of females is unknown	
        }
        //assign employment status if male
        else { 
            if (random < maleFormal) { return Employment.Formal; }
            else if (random < (maleFormal + maleInformal)) { return Employment.Informal; }
            else if (random < (maleFormal + maleInformal + maleSearching)) { return Employment.Searching; }
            else if (random < (maleFormal + maleInformal + maleSearching + maleInactive)) { return Employment.Inactive; }
            else { return Employment.Searching; } //the employment status of 5% of males is unknown	
        }

    }
    
    /**
     * Assign an employer to resident's that are employed in the formal and informal sector
     *
     * @param r - the resident
     * @param hh - the household
     * @param employmentStatus - the resident's current employment status
     * @param employerOutsideKibera
     * @param kibera
     * 
     *
     */
    private static void assignEmploymentLocation(Resident r, Household hh, Resident.Employment employmentStatus, OutsideKibera employerOutsideKibera, Kibera kibera) {
        //determine my home location
        int x = hh.getHome().getStructure().getParcel().getXLocation();
        int y = hh.getHome().getStructure().getParcel().getYLocation();

        Bag potentialEmployers = new Bag();
        kibera.landGrid.getNeighborsMaxDistance(x,y,kibera.params.global.getEmploymentVision(),false,potentialEmployers, null, null);

        Bag employerParcelLocations = new Bag();

        Parcel home = hh.getHome().getStructure().getParcel();

        //If employed in formal market search for available position in school, health facility, or religious facility
        if (employmentStatus == Employment.Formal) {
    
            for(Object o: potentialEmployers){
                Parcel p = (Parcel) o;
                    for(int i = 0; i < p.getStructure().size(); i++) {
                    Structure s = (Structure) p.getStructure().get(i);
                    if (s.getHealthFacilities().size() > 0) {
                        for(int j = 0; j < s.getHealthFacilities().size(); j++) {
                            HealthFacility healthFacility = (HealthFacility) s.getHealthFacilities().get(j);
                            if (!healthFacility.isEmployeeCapacityReached()) {
                                employerParcelLocations.add(p);
                            }                                
                        }
                    }
                    if (s.getReligiousFacilities().size() > 0) {
                        for(int j = 0; j < s.getReligiousFacilities().size(); j++) {
                            ReligiousFacility religiousFacility = (ReligiousFacility) s.getReligiousFacilities().get(j);
                            if (!religiousFacility.isEmployeeCapacityReached()) {
                                employerParcelLocations.add(p);

                            }                               
                        }
                    }
                    if (s.getSchools().size() > 0) {
                        for(int j = 0; j < s.getSchools().size(); j++) {
                            School school = (School) s.getSchools().get(j);
                            if (!school.isEmployeeCapacityReached()) {
                                employerParcelLocations.add(p);

                            }


                        }
                    }
                }                        
            }
                
            //if no formal employment was found, resident is assigned an employer outside of kibera
            if (employerParcelLocations.size() == 0) {
                employerParcelLocations.add(hh.getHome().getStructure().getParcel());
            }
        }
            
        //if employed in informal market search for job in informal businesses
        if (employmentStatus == Employment.Informal) {

            for(Object o: potentialEmployers){
                Parcel p = (Parcel) o;
                for(int i = 0; i < p.getStructure().size(); i++) {
                    Structure s = (Structure) p.getStructure().get(i);
                    if (s.getBusinesses().size() > 0) {
                        for(int j = 0; j < s.getBusinesses().size(); j++) {
                            Business business = (Business) s.getBusinesses().get(j);
                            if (!business.isEmployeeCapacityReached()) {
                                employerParcelLocations.add(p);
                            }
                        }
                    }
                }
            }

            if (employerParcelLocations.isEmpty()) {
                employerParcelLocations.add(hh.getHome().getStructure().getParcel());
            }
        }

        //now that we have a set of parcels where potential employers exist, find the one that is closest to home
        Parcel p = bestLocation(home, employerParcelLocations, kibera);

        //in the case that there are multiple employers on the selected parcel, pick just one employer
        Bag employers = new Bag();

        //otherwise find all potential employers on that parcel
        if (employmentStatus == Employment.Formal) {
            for(int i = 0; i < p.getStructure().size(); i++) {
                Structure s = p.getStructure().get(i);
                for (int j = 0; j < s.getSchools().size(); j++) {
                    School school = (School) s.getSchools().get(j);
                    if (!school.isEmployeeCapacityReached()) {
                        employers.add(school);
                    }
                }
                for (int j = 0; j < s.getHealthFacilities().size(); j++) {
                    HealthFacility healthFacility = (HealthFacility) s.getHealthFacilities().get(j);
                    if (!healthFacility.isEmployeeCapacityReached()) {
                        employers.add(healthFacility);
                    }
                }
                for (int j = 0; j < s.getReligiousFacilities().size(); j++) {
                    ReligiousFacility religiousFacility = (ReligiousFacility) s.getReligiousFacilities().get(j);
                    if (!religiousFacility.isEmployeeCapacityReached()) {
                        employers.add(religiousFacility);
                    }
                }
            }
        }
            
        if (employmentStatus == Employment.Informal) {
            for(int i = 0; i < p.getStructure().size(); i++) {
                Structure s = p.getStructure().get(i);
                for (int j = 0; j < s.getBusinesses().size(); j++) {
                    Business business = (Business) s.getBusinesses().get(j);
                    if (!business.isEmployeeCapacityReached()) {
                        employers.add(business); 
                    }
                }
            }
        }
            
        //if no employers were found it because formal/informal employment was not found within kibera
        //this resident will work outside of the slum, but for model purposes they will stay home
        if (employers.isEmpty()) {               
            employers.add(home);
            
            if (r.getCurrentEmploymentStatus() == Employment.Formal) {
                r.setEmploymentTypeOutsideKibera(Resident.EmploymentTypeOutsideKibera.Formal);
            }
            else {
                r.setEmploymentTypeOutsideKibera(Resident.EmploymentTypeOutsideKibera.Informal);
            }
        }
        
        //if more than one available employer was found, randomly pick one
        int numEmployers = employers.size();    	
        int pickEmployer = kibera.random.nextInt(numEmployers) + 1;
        
        //assign resident to employer
        for (int i = 1; i <= numEmployers; i++) {    	
            if (i == pickEmployer) {
                Object o = (Object) employers.get(i-1);
                if (o instanceof Business) {
                    Business myEmployer = (Business) employers.get(i-1);
                    r.setMyBusinessEmployer(myEmployer);
                    myEmployer.addEmployee(r);
                }
                else if (o instanceof School) {
                    School myEmployer = (School) employers.get(i-1);
                    r.setMySchoolEmployer(myEmployer);
                    myEmployer.addEmployee(r);

                }
                else if (o instanceof HealthFacility) {
                    HealthFacility myEmployer = (HealthFacility) employers.get(i-1);
                    r.setMyHealthFacilityEmployer(myEmployer);
                    myEmployer.addEmployee(r);
                }
                else if (o instanceof ReligiousFacility ){ //else the employer is a religious facility
                    ReligiousFacility myEmployer = (ReligiousFacility) employers.get(i-1);
                    r.setMyReligiousFacilityEmployer(myEmployer);
                    myEmployer.addEmployee(r);
                }
                else { //found employment outside of kibera                
                    employerOutsideKibera.removeNonEmployee(r);
                    employerOutsideKibera.addEmployee(r);
                }

            }
        }

    }
    
    /**
     * Assign a school to resident's that are 18 or younger
     *
     * @param r - the resident
     * @param hh - the household
     * @param kibera
     * 
     *
     */
    private static void assignSchoolLocation(Resident r, Household hh, Kibera kibera) {
        Parcel home = hh.getHome().getStructure().getParcel(); 

        //determine if any schools within vision (vision in this case is the average size of two neighborhoods in Kibera)
        //have available space
        //if so, attend school nearest to home

        //determien home location
        int x = hh.getHome().getStructure().getParcel().getXLocation();
        int y = hh.getHome().getStructure().getParcel().getYLocation();
        
        //find parcels within vision
        Bag schoolsInNeighborhood = new Bag();
        kibera.landGrid.getNeighborsMaxDistance(x,y,kibera.params.global.getSchoolVision(),false,schoolsInNeighborhood, null, null);

        Bag schoolParcelLocations = new Bag();
        
        //search for schools within vision that have available capacity
        for(Object o: schoolsInNeighborhood){
            Parcel p = (Parcel) o;
            for(int i = 0; i < p.getStructure().size(); i++) {
            Structure s = (Structure) p.getStructure().get(i);
            if (s.getSchools().size() > 0) {
                for(int j = 0; j < s.getSchools().size(); j++) {
                    School school = (School) s.getSchools().get(j);
                    if (!school.isStudentCapacityReached()) {
                        schoolParcelLocations.add(p); }
                    }
                }
            }
        }
            
        r.searchedForSchool(true);

        if (!schoolParcelLocations.isEmpty()) {
            //if schools were found, then find the parcel closest to home
            Parcel p = bestLocation (home, schoolParcelLocations,kibera);
            Bag schools = new Bag();

            //add schools on parcel closest to home to bag
            for(int i = 0; i < p.getStructure().size(); i++) {
                Structure s = p.getStructure().get(i);
                for (int j = 0; j < s.getSchools().size(); j++) {
                    School school = s.getSchools().get(j);
                    if (!school.isStudentCapacityReached()) {
                        schools.add(school);
                    }
                }
            }
            
            //if multiple schools were found on parcel, then randomly pick one to attned
            int numSchools = schools.size();
            int pickSchool = kibera.random.nextInt(numSchools) + 1;
            for (int i = 1; i <= numSchools; i++) {
                if (i == pickSchool) {
                    //assign school to student
                    School mySchool = (School) schools.get(i-1);
                    r.setMySchool(mySchool);
                    mySchool.addStudents(r);
                    
                    //assign the student to a class within the school
                    int numClasses = mySchool.getSchoolClasses().size();
                    int rnClass = kibera.random.nextInt(numClasses);
                    
                    SchoolClass myClass = (SchoolClass) mySchool.getSchoolClasses().get(rnClass);
                    myClass.addClassmate(r);
                    r.setMySchoolClass(myClass);
                    
                }
            }
        }
    }
    /**
     * Select the nearest parcel to goal
     * 
     * @param parcel - the current position of the agent (home)
     * @param fieldBag - the set of available parcels where an activity can take place
     * @param kibera
     * 
     * @return the parcel corresponding to the nearest location
     * 
     */
    private static Parcel bestLocation (Parcel parcel, Bag fieldBag, Kibera kibera) {
        Bag newLocation = new Bag();
        //go through potential locations and determine the distance between current position and the set of available positions
        double bestScoreSoFar = Double.POSITIVE_INFINITY;
        for (int i = 0; i < fieldBag.numObjs; i++) {
            Parcel positionLocation = ((Parcel) fieldBag.objs[i]);

            double fScore = parcel.distanceTo(positionLocation);
            if (fScore > bestScoreSoFar) {
                continue;
            }

            if (fScore <= bestScoreSoFar) {
                bestScoreSoFar = fScore;
                newLocation.clear();
            }
            newLocation.add(positionLocation);
        }
        //assign the new location as the final parcel
        Parcel p = null;
        if (newLocation != null) {
            int winningIndex = 0;
            if (newLocation.numObjs >= 1) {
                winningIndex = kibera.random.nextInt(newLocation.numObjs);
            }

        p = (Parcel) newLocation.objs[winningIndex];

        }
        return p;
    }
    
    /**
     * Determine if resident is eligible to go to school
     * 
     * @param age - the resident's age
     * @param employmentStatus - the resident's current employment status
     * @param kibera
     * 
     * @return true if the resident is school eligible, false otherwise
     * 
     */
    private static boolean determineStudentStatus(int age, Resident.Employment employmentStatus, Kibera kibera) {

        //students between the ages of 3 and 18 are eligible to go to school (based on empirical information)
        if (age >= 3 && age <= 18) {	
            return true;
        }

        else { return false; }	
    }
    
    /**
     * Create and add residents to the model
     * 
     * @param kibera
     * @param residentID - unique ID to track residents
     * @param hh - the resident's household
     * @param age - the resident's age
     * @param gender - the resident's gender
     * @param religion - the resident's religion
     * @param churchRnd - the day of week resident goes attends church (if applicable)
     * @param isHeadOfHousehold - identifies if the resident is the head of household
     * @param employmentStatus - the resident's current employment status
     * @param isSchoolEligible - identifies if resident is eligible to go to school
     * @param income - the resident's income
     * @param adultEquivalent - resident's adult equivalence   
     * 
     * 
     */	
    private static void addResidents(Kibera kibera, int residentID, Household hh, String ethnicity, int age, Resident.Gender gender, Resident.Religion religion, int churchRnd, boolean isHeadOfHousehold, Resident.Employment employmentStatus, boolean isSchoolEligible, double income, double adultEquivalent) {    	
        Resident r = new Resident(hh);

        kibera.residents.add(r);

        r.setHousehold(hh);
        hh.addHouseholdMembers(r);
        
        //if resident is employed, than is not school eligible
        if (employmentStatus == Employment.Formal || employmentStatus == Employment.Informal) {
            isSchoolEligible = false;
        }
        
        //assign an age group to each resident -- used for reporting purposes
        if (age < 3) { r.setAgeGroup(Resident.AgeGroup.age0to2); }
        else if (age >= 3 && age < 6) { r.setAgeGroup(Resident.AgeGroup.age3to5); }
        else if (age >= 6 && age < 19) { r.setAgeGroup(Resident.AgeGroup.age6to18); }
        else { r.setAgeGroup(Resident.AgeGroup.age19andOver); }
        
        //assign attributes to the resident
        r.setResidentID(residentID);
        r.setAge(age);       
        r.gender = gender;
        r.setEthnicity(ethnicity);	
        r.isHeadOfHousehold(isHeadOfHousehold);
        r.currentEmploymentStatus = employmentStatus;
        r.isSchoolEligible(isSchoolEligible);		
        r.setReligion(religion);
        r.setChurchRnd(churchRnd);
        r.heardRumor(false);
        r.isLaidOff(false);
        r.leftSchool(false);
        r.attendedReligiousFacility(false);
        r.changedGoal(true);
        r.isInitialRebel(false);
        r.haveSocialized(false);
        r.setCurrentGoal(Resident.Goal.Stay_Home);               
        r.setCurrentIdentity(Identity.Domestic_Activities);
        r.setEmploymentOutsideKibera(null);
        r.setDayFoundEmployment(-1);
        r.setAdultEquivalent(adultEquivalent);
        
        //assign aggression rate to each resident
        if (kibera.params.global.isUniformAggressionRate()) {
            r.setAggressionRate(kibera.params.global.getAggressionRate());
        }
        else {
            r.setAggressionRate(kibera.random.nextDouble());
        }
        
        //give each resident a full energy reservoir to begin
        r.setEnergy(100);

        //Give each resident in formal or informal sector a salary
        r.setResidentIncome(income);

        //add each resident to the network of residents
        kibera.socialNetwork.addNode(r);
    }

   /**
     * Find a home for a household
     * Adapted from Schelling (1978) and De Smedt (2009)
     * 
     * @param kibera
     * @param hh - the household
     * @param hhEthnicity - the household's ethnicity
     * @param householdIncome - the household's total income
     * 
     * @return the home of the household
     * 
     */	
    private static Home findHometoPlaceHousehold(Kibera kibera, Household hh, String hhEthnicity, double householdIncome) {
        int i = 0;
        int j = 0;
        IntBag neighborsX = new IntBag(9);
        IntBag neighborsY = new IntBag(9);

        Home home = null;

        //first check if there are any homes available that I can afford
        //if not, I will pick a home with my ethnic preference that I cannot yet afford (income will be adjusted to attempt and meet the expenses later in simulation)
        Bag allAvailableAffordableHomes = new Bag();

        Bag allAvailableSameEthnicity = new Bag(); //Available home near a neighbor of my same ethnicity
        Bag availableNotAffordableButMeetsEthnicDistribution = new Bag();


        //place all homes that I can afford and still available in a bag
        for (int c = 0; c < kibera.allHomesAvailable.numObjs; c++) {
             if ((householdIncome * kibera.getPercentIncomeforRent()) < ((Home) kibera.homes.objs[c]).getTotalExpectedHousingCost()) {
                 allAvailableAffordableHomes.add((Home) kibera.homes.objs[c]);
             }
        }

        //if your the first household, randomly select a structure
        if (kibera.households.numObjs == 1) {
            j = kibera.random.nextInt(kibera.homes.numObjs);
            while (((Home) kibera.homes.objs[j]).isHomeOccupied(kibera) && (householdIncome * kibera.getPercentIncomeforRent()) < ((Home) kibera.homes.objs[j]).getTotalExpectedHousingCost()) {
                j = kibera.random.nextInt(kibera.homes.numObjs);			
            }
            home = (Home) kibera.homes.objs[j];
        }

        //find first household that has my ethnicity	
        else {

            Bag allSameEthnicity = new Bag(); //Households with my same ethnicity

            if (hhEthnicity.equals("kikuyu")) { allSameEthnicity.addAll(kibera.kikuyu); }
            else if (hhEthnicity.equals("luhya")) { allSameEthnicity.addAll(kibera.luhya); }
            else if (hhEthnicity.equals("luo")) { allSameEthnicity.addAll(kibera.luo); }
            else if (hhEthnicity.equals("kalinjin")) { allSameEthnicity.addAll(kibera.kalinjin); }
            else if (hhEthnicity.equals("kamba")) { allSameEthnicity.addAll(kibera.kamba); }
            else if (hhEthnicity.equals("kisii")) { allSameEthnicity.addAll(kibera.kisii); }
            else if (hhEthnicity.equals("meru")) { allSameEthnicity.addAll(kibera.meru); }
            else if (hhEthnicity.equals("mijikenda")) { allSameEthnicity.addAll(kibera.mijikenda); }
            else if (hhEthnicity.equals("maasai")) { allSameEthnicity.addAll(kibera.maasai); }
            else if (hhEthnicity.equals("turkana")) { allSameEthnicity.addAll(kibera.turkana); }
            else if (hhEthnicity.equals("embu")) { allSameEthnicity.addAll(kibera.embu); }
            else if (hhEthnicity.equals("other")) { allSameEthnicity.addAll(kibera.other); }

            //if I do not have any preference for living near like neighbors (i.e. preference = 0), find first home I can afford
            if (kibera.params.global.getPreferenceforLivingNearLikeNeighbors() == 0) {
                Bag availableAffordableHomes = new Bag();
                
                //go through all available homes and determine if I can afford it
                for(int b = 0; b < kibera.allHomesAvailable.numObjs; b++) {
                    Home house = (Home) kibera.allHomesAvailable.get(b);
                    double expectedHomeCosts = house.getTotalExpectedHousingCost();
                    double homeAffordability = householdIncome * kibera.getPercentIncomeforRent();

                    if (!house.isHomeOccupied(kibera) && (homeAffordability >= expectedHomeCosts)) { 
                        availableAffordableHomes.add(house); 
                    }
                }
                //if affordable homes were found, then select one of the affordable homes at random
                if (!availableAffordableHomes.isEmpty()) {
                    int c = kibera.random.nextInt(availableAffordableHomes.size());
                    Home house = (Home) kibera.allHomesAvailable.get(c);
                    home = house;
                }
                //if no affordable homes were found, select any home at random
                else {
                    int c = kibera.random.nextInt(kibera.allHomesAvailable.size());
                    Home house = (Home) kibera.allHomesAvailable.get(c);
                    home = house;    
                }
            }

            //if there are other households in the environment with the same ethnicity and I prefer to live near like neighbors
            else if (allSameEthnicity.size() > 0) {
                //pick a random household of same ethnicity, if have picked all and still don't have a match, move on (home will equal null)
                Bag notSearchedSameEthnicity = new Bag(); //store the households of same ethnicity that we have not searched yet
                notSearchedSameEthnicity.addAll(allSameEthnicity);                                     

                while (!notSearchedSameEthnicity.isEmpty() && home == null) {
                    if (home != null) { break; }

                    int rn = kibera.random.nextInt(notSearchedSameEthnicity.size());                    

                    Household randomHH = (Household) notSearchedSameEthnicity.get(rn);
                    Home randomHome = randomHH.getHome();

                    int jitterX = kibera.random.nextInt(5);
                    int jitterY = kibera.random.nextInt(5);

                    int x = randomHome.getStructure().getParcel().getLocation().x + jitterX;
                    int y = randomHome.getStructure().getParcel().getLocation().y + jitterY;

                    //find available parcels/structures within neighborhood
                    // get all the places I can go.  This will be slow as we have to rely on grabbing neighbors.
                    kibera.landGrid.getNeighborsMaxDistance(x,y,kibera.params.global.getNeighborhood(),false,neighborsX,neighborsY);

                    int len = neighborsX.size();
                    boolean meetsEthnicityPreference = false;

                    Bag availableAffordableHomes = new Bag();
                    Bag availableHomes = new Bag();
                    //go through homes that have neighbors with same ethnicity and find any homes that I can afford
                    for(int a = 0; a < len; a++) {

                        if (home != null) { break; }

                        int neighborX = neighborsX.get(a);
                        int neighborY = neighborsY.get(a);

                        Parcel neighborParcel = (Parcel)kibera.landGrid.get(neighborX, neighborY);
                        int id = neighborParcel.getParcelID();

                        double numNeighbors = 1; //include randomhh home
                        double numNeighborsSameEthnicity = 1; //include randomhh home
                        double ethnicDistribution = 0.0;

                        if (id > 0) {
                            for(int h = 0; h < neighborParcel.getStructure().size(); h++) {
                                Structure s = (Structure) neighborParcel.getStructure().get(h);
                                for(int l = 0; l < s.getHomes().size(); l++) {
                                    Home house = (Home) s.getHomes().get(l);
                                    //add empty homes to a bag
                                    double expectedHomeCosts = house.getTotalExpectedHousingCost();
                                    double homeAffordability = householdIncome * kibera.getPercentIncomeforRent();

                                    numNeighbors = numNeighbors + 1;

                                    if (house.isHomeOccupied(kibera)) {
                                        if (house.getHousehold().getHouseholdEthnicity() == hhEthnicity) {
                                            numNeighborsSameEthnicity = numNeighborsSameEthnicity + 1;
                                        }
                                    }
                                    //if an unoppied home was found, determine if I can afford it
                                    //if so, add it to a bag
                                    if (!house.isHomeOccupied(kibera) && (homeAffordability >= expectedHomeCosts)) { 
                                        availableAffordableHomes.add(house); 
                                    }
                                    //if the home is not occupied, then add to available homes with same ethnicity
                                    if (!house.isHomeOccupied(kibera)) {
                                        availableHomes.add(house);
                                        allAvailableSameEthnicity.add(house);
                                    }
                                }
                                //re-calcuate ethnic distribution of neighborhood
                                ethnicDistribution = numNeighborsSameEthnicity / numNeighbors;
                                //determine if ethnic distribution meets preference
                                if (ethnicDistribution >= kibera.params.global.getPreferenceforLivingNearLikeNeighbors()) {
                                    meetsEthnicityPreference = true;
                               }                                                                                                                                  
                            }				        				        		
                        }			        	
                    }

                    //If this household has no surrounding available homes, remove them from the bag as there is no need to search around this household again in the futre
                    if (availableHomes.isEmpty()) {

                        if (hhEthnicity.equals("kikuyu")) { kibera.kikuyu.remove(randomHH); }
                        else if (hhEthnicity.equals("luhya")) { kibera.luhya.remove(randomHH); }
                        else if (hhEthnicity.equals("luo")) { kibera.luo.remove(randomHH); }
                        else if (hhEthnicity.equals("kalinjin")) { kibera.kalinjin.remove(randomHH); }
                        else if (hhEthnicity.equals("kamba")) { kibera.kamba.remove(randomHH); }
                        else if (hhEthnicity.equals("kisii")) { kibera.kisii.remove(randomHH); }
                        else if (hhEthnicity.equals("meru")) { kibera.meru.remove(randomHH); }
                        else if (hhEthnicity.equals("mijikenda")) { kibera.mijikenda.remove(randomHH); }
                        else if (hhEthnicity.equals("maasai")) { kibera.maasai.remove(randomHH); }
                        else if (hhEthnicity.equals("turkana")) { kibera.turkana.remove(randomHH); }
                        else if (hhEthnicity.equals("embu")) { kibera.embu.remove(randomHH); }
                        else { kibera.other.remove(randomHH); }
                    }

                    if (meetsEthnicityPreference) {
                        //the ethnic distribution is adequate, so lets check if an affordable home exists in this neighborhood
                        if (!availableHomes.isEmpty()) {
                            //no affordable homes exists but happy with ethnic distribution
                            if (availableAffordableHomes.isEmpty()) {
                                availableNotAffordableButMeetsEthnicDistribution.addAll(availableHomes);
                                break;
                            }

                            else {
                                int b = kibera.random.nextInt(availableAffordableHomes.size());
                                home = (Home) availableAffordableHomes.objs[b];
                                break;
                            }
                        }
                    }
                    notSearchedSameEthnicity.remove(randomHH);

                    availableHomes.clear();                       
                    availableAffordableHomes.clear();
                }
            }

            //no one of my ethnicity lives on the landscape yet, so randomly pick a home that I can afford
            else {
                //if there are no affordable homes available, then randomly pick an available home
                if (allAvailableAffordableHomes.isEmpty()) {
                    int b = kibera.random.nextInt(kibera.allHomesAvailable.numObjs);
                    home = (Home) kibera.allHomesAvailable.objs[b];
                }
                //other randomly pick a home that is affordable
                else {
                    int b = kibera.random.nextInt(allAvailableAffordableHomes.numObjs);
                    home = (Home) allAvailableAffordableHomes.objs[b];
                }
            }	
        }

        //there are no households with my ethnicity preference that are affordable
        if (home == null) {
            Bag availableAffordableHomes = new Bag();
            //check to see if any home that I can afford are still available
            for (int c = 0; c < kibera.allHomesAvailable.numObjs; c++) {
                 if ((householdIncome * kibera.getPercentIncomeforRent()) < ((Home) kibera.homes.objs[c]).getTotalExpectedHousingCost()) {
                     availableAffordableHomes.add((Home) kibera.homes.objs[c]);
                 }
            }

            //first see if there were homes that fit my ethnic distribution preference, but were not affordable
            if (!availableNotAffordableButMeetsEthnicDistribution.isEmpty()) {
                int b = kibera.random.nextInt(availableNotAffordableButMeetsEthnicDistribution.size());
                home = (Home) availableNotAffordableButMeetsEthnicDistribution.objs[b];
            }

            //if there are no affordable homes available but there are home of the same ethnicity, then randomly pick an available home near a neighbor of same ethnicity
            else if (availableAffordableHomes.isEmpty() && !allAvailableSameEthnicity.isEmpty()) {
                int b = kibera.random.nextInt(allAvailableSameEthnicity.size());                            
                Home house = (Home) allAvailableSameEthnicity.get(b);                         
                home = house;
            }

            //other randomly pick a home that is affordable
            else if (!availableAffordableHomes.isEmpty()) {
                int b = kibera.random.nextInt(availableAffordableHomes.numObjs);
                home = (Home) availableAffordableHomes.objs[b];
            }

            //else, if there are no affordable homes and no homes near someone of my ethnicity, randomly pick an available home
            else {
                int b = kibera.random.nextInt(kibera.allHomesAvailable.numObjs);
                home = (Home) kibera.allHomesAvailable.objs[b];
            }
        }

        //Add household to correct ethnicity bag
        else if (hhEthnicity.equals("luhya")) { kibera.luhya.add(hh); }
        else if (hhEthnicity.equals("luo")) { kibera.luo.add(hh); }
        else if (hhEthnicity.equals("kalinjin")) { kibera.kalinjin.add(hh); }
        else if (hhEthnicity.equals("kamba")) { kibera.kamba.add(hh); }
        else if (hhEthnicity.equals("kisii")) { kibera.kisii.add(hh); }
        else if (hhEthnicity.equals("meru")) { kibera.meru.add(hh); }
        else if (hhEthnicity.equals("mijikenda")) { kibera.mijikenda.add(hh); }
        else if (hhEthnicity.equals("maasai")) { kibera.maasai.add(hh); }
        else if (hhEthnicity.equals("turkana")) { kibera.turkana.add(hh); }
        else if (hhEthnicity.equals("embu")) { kibera.embu.add(hh); }
        else { kibera.other.add(hh); }

        kibera.allHomesAvailable.remove(home);
        return home;
    }
    
    /**
     * An initial number of agents hear the rumor and become rebellious
     * 
     * @param kibera
     * 
     */	
    private static void rumorPropogation(Kibera kibera) {   
        //pick a random resident(s) to hear the rumor
        int totalResidents = kibera.residents.numObjs;		
        int num = kibera.params.global.getNumResidentsHearRumor();
        int numNewRumor = kibera.params.global.getNumResidentsHearNewRumor();
        
        double numRebel = 0;
        
        //select an initial number of resident to riot/rebel
        if (kibera.createRebelsAtInitialization) {          
            numRebel = kibera.params.global.getProportionInitialResidentsRebel() * (double) kibera.params.global.getNumResidentsHearRumor();
            numRebel = (int) numRebel + 1;
        }
           
        int i = 0;
            
        while (i < num) {
            int ranResident = kibera.random.nextInt(totalResidents);		
            Resident r = (Resident) kibera.residents.get(ranResident);
            r.heardRumor(true);
                    
            if (i<numRebel) {
                //can't rebel if under 6, find next resident
                if (r.getAge() < 6) {
                    if (numRebel <= num) {
                        numRebel = numRebel + 1;
                    }
                }
                else {
                    //assign attributes to resident that rebelled
                    r.setCurrentGoal(Resident.Goal.Rebel);
                    r.isInitialRebel(true);
                    r.setCurrentIdentity(Identity.Rebel);                    
                    r.setCurrentEmploymentStatus(Employment.Inactive);
                    r.setMyBusinessEmployer(null);
                    r.setMyHealthFacilityEmployer(null);
                    r.setMyReligiousFacilityEmployer(null);
                    r.setMySchoolEmployer(null);
                    r.setMySchool(null);
                    
                    if (r.getEmploymentOutsideKibera().getEmployees().contains(r)) {
                        r.getEmploymentOutsideKibera().removeEmployee(r);
                        r.getEmploymentOutsideKibera().addNonEmployee(r);
                    }
                    
                }
            }                 
            i++;                    
        }
        
        int j = 0;
            
        while (j < numNewRumor) {
            int ranResidentNewRumor = kibera.random.nextInt(totalResidents);		
            Resident r = (Resident) kibera.residents.get(ranResidentNewRumor);
            r.heardNewRumor(true);
            
            j++;
        }
        
    }
    
    /**
     * Create the roads and walking paths
     * 
     * @param roadFile - the GIS data with road information (OpenStreetMap, 2013)
     * @param kibera
     * 
     */	
    private static void createRoads(String roadFile, Kibera kibera) {
        try {
            // now read road grid
            BufferedReader roads = new BufferedReader(new FileReader(roadFile));
            String line;

            // first read the dimensions
            line = roads.readLine(); // read line for width
            String[] tokens = line.split("\\s+");
            int width = Integer.parseInt(tokens[1]);
            gridWidth = width;

            line = roads.readLine();
            tokens = line.split("\\s+");
            int height = Integer.parseInt(tokens[1]);
            gridHeight = height;

            // skip the irrelevant metadata
            for (int i = 0; i < 4; i++) {
                line = roads.readLine();
            }

            for (int curr_row = 0; curr_row < height; ++curr_row) {
                line = roads.readLine();
                tokens = line.split("\\s+");

                for (int curr_col = 0; curr_col < width; ++curr_col) {                    
                    int roadID = Integer.parseInt(tokens[curr_col]);

                    if (roadID >= 0) {
                        Parcel parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);
                        parcel.setRoadID(roadID);
                        kibera.roadGrid.set(curr_col, curr_row, roadID);
                    }                   					
                }
            }

            //Import road shapefile
            Bag roadImporter = new Bag();
            roadImporter.add("Type");
            File file=new File("src/kibera/z_Road_Export.shp");
            URL roadShapeUL = file.toURL();
            ShapeFileImporter.read(roadShapeUL, kibera.roadLinks, roadImporter);
            extractFromRoadLinks(kibera.roadLinks, kibera); //construct a newtork of roads
            kibera.closestNodes = setupNearestNodes(kibera);
            roads.close();
        }
        catch (IOException ex) {
                Logger.getLogger(KiberaBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
	
    static void extractFromRoadLinks(GeomVectorField roadLinks, Kibera kibera) {
        Bag geoms = roadLinks.getGeometries();
        Envelope e = roadLinks.getMBR();
        double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
        int xcols = gridWidth - 1, ycols = gridHeight - 1;
          
        // extract each edge
        for (Object o : geoms) {
            MasonGeometry gm = (MasonGeometry) o;
            if (gm.getGeometry() instanceof LineString) {
                readLineString((LineString) gm.getGeometry(), xcols, ycols, xmin, ymin, xmax, ymax, kibera);
            } else if (gm.getGeometry() instanceof MultiLineString) {
                MultiLineString mls = (MultiLineString) gm.getGeometry();
                for (int i = 0; i < mls.getNumGeometries(); i++) {
                    readLineString((LineString) mls.getGeometryN(i), xcols, ycols, xmin, ymin, xmax, ymax, kibera);
                }
            }
        }
    }
    
    /**
     * Converts an individual linestring into a series of links and nodes in the
     * network
     * 
     * @param geometry
     * @param xcols - number of columns in the field
     * @param ycols - number of rows in the field
     * @param xmin - minimum x value in shapefile
     * @param ymin - minimum y value in shapefile
     * @param xmax - maximum x value in shapefile
     * @param ymax - maximum y value in shapefile
     * 
     * 
     */
    static void readLineString(LineString geometry, int xcols, int ycols, double xmin,
        double ymin, double xmax, double ymax, Kibera kibera) {

        CoordinateSequence cs = geometry.getCoordinateSequence();

        // iterate over each pair of coordinates and establish a link between
        // them
        Node oldNode = null; // used to keep track of the last node referenced
        for (int i = 0; i < cs.size(); i++) {

            // calculate the location of the node in question
            double x = cs.getX(i), y = cs.getY(i);
            int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)), yint = (int) (ycols - Math.floor(ycols * (y - ymin) / (ymax - ymin))); // REMEMBER TO FLIP THE Y VALUE

            if (xint >= gridWidth) {
                continue;
            } 
            else if (yint >= gridHeight) {
                continue;
            }

            // find that node or establish it if it doesn't yet exist
            Bag ns = kibera.nodes.getObjectsAtLocation(xint, yint);
            Node n;
            if (ns == null) {    	
            	Int2D parcelLocation = new Int2D((xint), yint);
                n = new Node(new Parcel(parcelLocation));
                kibera.nodes.setObjectLocation(n, xint, yint);
            } 
            else {
                n = (Node) ns.get(0);
            }

            if (oldNode == n) { // don't link a node to itself
                continue;
            }

            // attach the node to the previous node in the chain (or continue if
            // this is the first node in the chain of links)

            if (i == 0) { // can't connect previous link to anything
                oldNode = n; // save this node for reference in the next link
                continue;
            }
           
            int weight = (int) n.location.distanceTo(oldNode.location); // weight is just
            // distance

            // create the new link and save it
            Edge e = new Edge(oldNode, n, weight);
            kibera.roadNetwork.addEdge(e);
            oldNode.links.add(e);
            n.links.add(e);

            oldNode = n; // save this node for reference in the next link
        }
    }
  
    static class Node {
        Parcel location;
        ArrayList<Edge> links;

        public Node(Parcel l) {
            location = l;
            links = new ArrayList<Edge>();
        }
    }

    /**
     * Used to find the nearest node for each space
     * 
     */
    static class Crawler {
        Node node;
        Parcel location;

        public Crawler(Node n, Parcel l) {
            node = n;
            location = l;
        }
    }

    /**
     * Calculate the nodes nearest to each location and store the information
     * 
     * @param closestNodes
     *            - the field to populate
     */
    static ObjectGrid2D setupNearestNodes(Kibera kibera) {      
        ObjectGrid2D closestNodes = new ObjectGrid2D(gridWidth, gridHeight);
        ArrayList<Crawler> crawlers = new ArrayList<Crawler>();

        for (Object o : kibera.roadNetwork.allNodes) {
            Node n = (Node) o;
            Crawler c = new Crawler(n, n.location);
            crawlers.add(c);
        }

        // while there is unexplored space, continue!
        while (crawlers.size() > 0) {
            ArrayList<Crawler> nextGeneration = new ArrayList<Crawler>();
            // randomize the order in which cralwers are considered
            int size = crawlers.size();
            
            for (int i = 0; i < size; i++) {
                // randomly pick a remaining crawler
                int index = kibera.random.nextInt(crawlers.size());
                Crawler c = crawlers.remove(index);             
                // check if the location has already been claimed
                Node n = (Node) closestNodes.get(c.location.getXLocation(), c.location.getYLocation());
                        
                if (n == null) { // found something new! Mark it and reproduce
                    // set it
                    closestNodes.set(c.location.getXLocation(), c.location.getYLocation(), c.node);
                    // reproduce
                    Bag neighbors = new Bag();
                    kibera.landGrid.getNeighborsHamiltonianDistance(c.location.getXLocation(), c.location.getYLocation(),
                            1, false, neighbors, null, null);

                    for (Object o : neighbors) {
                        Parcel l = (Parcel) o;
                        if (l == c.location) {
                            continue;
                        }
                        Crawler newc = new Crawler(c.node, l);
                        nextGeneration.add(newc);
                    }
                }
                // otherwise just die
            }
            crawlers = nextGeneration;
        }
        return closestNodes;
    }
    
    /**
     * Create all the grids for the modeling world
     * 
     * @param kibera
     * @param width - the width in parcels of the modeling world
     * @param height - the height in parcels of the modeling world
     * 
     * 
     */
    private static void createGrids(Kibera kibera, int width, int height) {
        kibera.landGrid = new ObjectGrid2D(width, height);
        kibera.world = new Continuous2D(0.1, width, height);
        kibera.facilityGrid = new SparseGrid2D(width, height);
        kibera.healthFacilityGrid = new SparseGrid2D(width, height);
        kibera.religiousFacilityGrid = new SparseGrid2D(width, height);
        kibera.waterGrid = new SparseGrid2D(width, height);
        kibera.sanitationGrid = new SparseGrid2D(width, height);
        kibera.businessGrid = new SparseGrid2D(width, height);
        kibera.houseGrid = new SparseGrid2D(width, height);

        kibera.roadGrid = new IntGrid2D(width, height);
        kibera.nodes = new SparseGrid2D(width, height);
        kibera.closestNodes = new ObjectGrid2D(width, height);
        kibera.roadLinks =  new GeomVectorField(width, height);
    }

}

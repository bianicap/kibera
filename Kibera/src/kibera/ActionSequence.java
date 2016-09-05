package kibera;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import kibera.Resident.Employment;
import kibera.Resident.EmploymentTypeOutsideKibera;
import kibera.Resident.Goal;
import kibera.Resident.Religion;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * After the Intensity Analyzer has determined the action-guiding motive, the Action Sequence executes the action.
 * Activities include staying home, going to school, going to work, going to a religious institution, visiting friends,
 * rioting/rebelling, getting water, and finding employment
 * 
 * This is adapted from Schmidt's (2000) PECS framework
 * 
 * @author bpint
 *
 */
public class ActionSequence {

    /**
     * Select the best location to perform the activity
     * 
     * @param resident - the agent
     * @param position - the parcel corresponding to the agent's home location
     * @param goal - the agent's current goal
     * @param employerOutsideKibera - used to determine if the agent is employed outside of kibera
     * @param kibera
     * 
     * @return the parcel corresponding to the best activity location
     * 
     */     
    public static Parcel bestActivityLocation(Resident resident, Parcel position, Goal goal, OutsideKibera employerOutsideKibera, Kibera kibera) {
    	Parcel home = resident.getHousehold().getHome().getStructure().getParcel();
    	
        //if the goal is to stay home, then the agent will remain at home
    	if (goal == Goal.Stay_Home) { return home; }   
    	
        //if the goal is to go to school, then assign school on the parcel to the resident, so that resident returns
        //to same school each day
        //if the agent has already been assigend a school, then select the parcel of the assigend school
    	else if (goal == Goal.Get_An_Education) { 
           
            //if student has already been assigned a school, then keep going to that school
            if (resident.getMySchool() != null) {
                    Parcel p = resident.getMySchool().getStructure().getParcel();
                    return p;
            }
            //otherwise, find an available school for the student to attend
            else { 
                Parcel p = bestLocation (home, findSchools(resident, kibera),kibera);
                Bag schools = new Bag();

                for(int i = 0; i < p.getStructure().size(); i++) {
                    Structure s = p.getStructure().get(i);
                    for (int j = 0; j < s.getSchools().size(); j++) {
                        School school = s.getSchools().get(j);
                        schools.add(school);
                    }
                }
                //if there are multiple schools on the same parcel, randomly pick a school to assign to the resident
                int numSchools = schools.size();
                int pickSchool = kibera.random.nextInt(numSchools) + 1;
                for (int i = 1; i <= numSchools; i++) {
                    if (i == pickSchool) {
                        School mySchool = (School) schools.get(i-1);
                        resident.setMySchool(mySchool);
                        mySchool.addStudents(resident);
                        
                        //assign the student to a class within the school
                        int numClasses = mySchool.getSchoolClasses().size();
                        int rnClass = kibera.random.nextInt(numClasses);

                        SchoolClass myClass = (SchoolClass) mySchool.getSchoolClasses().get(rnClass);
                        myClass.addClassmate(resident);
                        resident.setMySchoolClass(myClass);
                    }
                }
                return p;
            }
    	
    	}
        
        //if the goal is to find employment, then perform a search of available employers
    	else if (goal == Goal.Find_Employment) {           
            
            Parcel p = null;
            
            //adjust informality index for residents 18 and under (since 100% of these residents work in the informal sector)           
            double percentageResidentsInformalOnly = kibera.percentOfResidentsUnder19 - 
                    kibera.percentOfResidentsUnder6; //percentage of residents between the ages of 6 and 18 (these are residents that if searching for employment, can only search informal sector)
            double percentageFormal = 1 - kibera.informalityIndex;

            //percentage of informal employees (adjusted for young residents that can only work in the informal sector)
            double percentageInformal = kibera.informalityIndex - percentageResidentsInformalOnly;

            //adjust formal percentage
            percentageFormal = percentageFormal / (percentageFormal + percentageInformal);

            //check if resident found an employer but has not started working yet
            boolean hasEmployer = false;
            if (resident.getMyBusinessEmployer() != null || resident.getMyHealthFacilityEmployer() != null || resident.getMyReligiousFacilityEmployer() != null || 
                    resident.getMySchoolEmployer() != null || resident.getEmploymentOutsideKibera().getEmployees().contains(resident)) {
                hasEmployer = true;
            }
            
            if (!hasEmployer) {                
                //if the resident is over 18, the chance of getting a job in the formal market is equivalent to 1-informaility index
                //as set in initialization
                double rn = kibera.random.nextDouble();

                //if the resident has already been assigned a job, keeping going there   		
                p = bestLocation (home, findPotentialEmployment(resident, rn, employerOutsideKibera, percentageFormal, kibera), kibera);
                Bag employers = new Bag();
                
                //If no parcel is found, a potential employer with availability was not found, this resident will stay home	    		
                if (p == null) { 
                    p = resident.getHousehold().getHome().getStructure().getParcel();
                    resident.setCurrentEmploymentStatus(Employment.Searching);
                    return p;
                }
                //otherwise, a potential employer was found. track the day that the resident found employmnet
                else {
                     resident.setDayFoundEmployment(resident.getTimeManager().dayCount(resident.getCStep()));
                }

                if (resident.currentEmploymentStatus == Employment.Searching) {
                    
                    
                    //search for formal employment if resident is over 18 and random number is less than percentage formal
                    if (resident.getAge() > 18 && rn < percentageFormal) {
                        
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
                        if (employers.isEmpty()) {
        
                           //if no formal employers were found within kibera, check outside of kibera                          
                           if (!employerOutsideKibera.isEmployeeCapacityReached(OutsideKibera.BusinessType.formal, kibera)) {
                                employers.add(employerOutsideKibera);
                                resident.setEmploymentTypeOutsideKibera(EmploymentTypeOutsideKibera.Formal);
                           }
                           //if no formal employers were found anywhere, check informal employers in kibera
                           else {
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
                           //if no informal employers were found in kibera, check outside of kibera
                           if (employers.isEmpty()) {
                                if (!employerOutsideKibera.isEmployeeCapacityReached(OutsideKibera.BusinessType.informal, kibera)) {
                                    employers.add(employerOutsideKibera);
                                    resident.setEmploymentTypeOutsideKibera(EmploymentTypeOutsideKibera.Informal);
                                    
                                }
                           }                          
                        }
                    }
                    //if the resident is 18 or younger, search only for informal employers on the parcel
                    else {
                       
                        for(int i = 0; i < p.getStructure().size(); i++) {
                            Structure s = p.getStructure().get(i);
                            for (int j = 0; j < s.getBusinesses().size(); j++) {
                                Business business = (Business) s.getBusinesses().get(j);
                                if (!business.isEmployeeCapacityReached()) {
                                    employers.add(business);
                                }
                            }
                        }

                        if (employers.isEmpty()) {
                            if (!employerOutsideKibera.isEmployeeCapacityReached(OutsideKibera.BusinessType.informal, kibera)) {
                                employers.add(employerOutsideKibera);
                                resident.setEmploymentTypeOutsideKibera(EmploymentTypeOutsideKibera.Informal);
                              
                            }
                       }                      
                    }                     
                }

                if (!employers.isEmpty()) {
                    int numEmployers = employers.size();    	
                    int pickEmployer = kibera.random.nextInt(numEmployers) + 1;
                    
                    //Resident found employment so is no longer laid off 
                    resident.isLaidOff(false);

                    for (int i = 1; i <= numEmployers; i++) {
                        if (i == pickEmployer) {
                            Object o = (Object) employers.get(i-1);
                            if (o instanceof Business) {
                                Business myEmployer = (Business) employers.get(i-1);
                                resident.setMyBusinessEmployer(myEmployer);
                                myEmployer.addEmployee(resident);
                            }
                            else if (o instanceof School) {
                                School myEmployer = (School) employers.get(i-1);
                                resident.setMySchoolEmployer(myEmployer);
                                myEmployer.addEmployee(resident);
                            }
                            else if (o instanceof HealthFacility) {
                                HealthFacility myEmployer = (HealthFacility) employers.get(i-1);
                                resident.setMyHealthFacilityEmployer(myEmployer);
                                myEmployer.addEmployee(resident);
                            }
                            else if (o instanceof ReligiousFacility ){ //else the employer is a religious facility
                                ReligiousFacility myEmployer = (ReligiousFacility) employers.get(i-1);
                                resident.setMyReligiousFacilityEmployer(myEmployer);
                                myEmployer.addEmployee(resident);
                            }
                            else { //found employment outside of kibera
                               
                                employerOutsideKibera.removeNonEmployee(resident);
                                employerOutsideKibera.addEmployee(resident);                               
                            }
                        }
                    }
                }
            }
            
            //the resident was already assigned an employer but hasn't started working yet
            else {
                
                if (resident.getMyBusinessEmployer() != null) {
                    p = resident.getMyBusinessEmployer().getStructure().getParcel();
                }
                else if (resident.getMyHealthFacilityEmployer() != null) {
                    p = resident.getMyHealthFacilityEmployer().getStructure().getParcel();
                }
                else if (resident.getMyReligiousFacilityEmployer() != null) {
                    p = resident.getMyReligiousFacilityEmployer().getStructure().getParcel();
                }
                else if (resident.getMySchoolEmployer() != null) {
                    p = resident.getMySchoolEmployer().getStructure().getParcel();
                }
                //otherwise resident is employed outside of kibera
                else {
                    p = resident.getHousehold().getHome().getStructure().getParcel();
                }
                //Once a resident has found employment, he/she will be employed the following day
                //determine the current day
                int currentDay = resident.getTimeManager().dayCount(resident.getCStep());
 
                //determine days since found employment
                boolean canWork = false;
                if (resident.getDayFoundEmployment() != -1 && (currentDay - resident.getDayFoundEmployment()) >= 1) {
                    canWork = true;
                }
                
                //assign an employment status based on the results of the search
                if (resident.getCurrentEmploymentStatus() == Employment.Searching && canWork) {

                    if (resident.getMyBusinessEmployer() != null) {
                        resident.setCurrentEmploymentStatus(Employment.Informal);
                    }
                    else if (resident.getMyHealthFacilityEmployer() != null) {
                        resident.setCurrentEmploymentStatus(Employment.Formal);
                    }
                    else if (resident.getMyReligiousFacilityEmployer() != null) {
                        resident.setCurrentEmploymentStatus(Employment.Formal);
                    }
                    else if (resident.getMySchoolEmployer() != null) { //else the resident found a job at a school
                        resident.setCurrentEmploymentStatus(Employment.Formal);
                    }
                    else if (resident.getEmploymentOutsideKibera().getEmployees().contains(resident) && 
                            resident.getEmploymentTypeOutsideKibera() == EmploymentTypeOutsideKibera.Formal) {
                        resident.setCurrentEmploymentStatus(Employment.Formal);
                    }
                    else if (resident.getEmploymentOutsideKibera().getEmployees().contains(resident) && 
                            resident.getEmploymentTypeOutsideKibera() == EmploymentTypeOutsideKibera.Informal) {
                        resident.setCurrentEmploymentStatus(Employment.Informal);
                    }
                    else { //resident did not find employment and will stay home
                        resident.setCurrentEmploymentStatus(Employment.Searching);
                    }

                    if (resident.getCurrentEmploymentStatus() != Employment.Searching) {
                        resident.setResidentIncome(WealthDistribution.determineIncome(resident.getCurrentEmploymentStatus(), kibera));
                    }
                    resident.setDayFoundEmployment(-1);
                }
            }
            
            return p;
    	}
    	
        //if the goal is to go to work, then agent will go to the employer it has been assigned to
    	else if (goal == Goal.Go_to_Work ) {		
            Parcel p = null;
            if (resident.getMyBusinessEmployer() != null) {
                p = resident.getMyBusinessEmployer().getStructure().getParcel();
            }
            else if (resident.getMyHealthFacilityEmployer() != null) {
                p = resident.getMyHealthFacilityEmployer().getStructure().getParcel();
            }
            else if (resident.getMySchoolEmployer() != null) {
                p = resident.getMySchoolEmployer().getStructure().getParcel();
            }
            else if (resident.getMyReligiousFacilityEmployer() != null) { //else resident is employed at religious facility
                p = resident.getMyReligiousFacilityEmployer().getStructure().getParcel();
            }
            else { //resident works outside of slum, but for modeling purposes stays home
                p = resident.getHousehold().getHome().getStructure().getParcel();
            }
            return p; 
    	}
    	
        //if the goal is to visit friends, then determine the friend to socialize with
    	else if (goal == Goal.Socialize) { 
            resident.haveSocialized(true);

            return determineWhereToSociolize(resident, kibera); 
    	}
    	
        //if the goal is to attend a religous institution, then go to assigned facility
        //otherwise, find the nearest religious facility
    	else if (goal == Goal.Go_to_Religious_Institution) {
            
            //if the religious facility has already been assigned
            if (resident.getMyReligiousFacility() != null) {
                Parcel p = resident.getMyReligiousFacility().getStructure().getParcel();
                return p;
            }
            
            //otherwise find all churches and mosque and put them in a bag depending on the religious affiliation
            Bag churches = new Bag();
            Bag mosques = new Bag();
            Parcel bestLocation;

            for (int i = 0; i < kibera.allReligiousFacilityLocations.size(); i++) {
                Parcel p = (Parcel) kibera.allReligiousFacilityLocations.get(i);
                for(int j = 0; j < p.getStructure().size(); j++) {
                    Structure s = p.getStructure().get(j);
                    for(int k = 0; k < s.getReligiousFacilities().size(); k++) {
                        ReligiousFacility r = (ReligiousFacility) s.getReligiousFacilities().get(k);
                        if (r.getFacilityType() == 1) {
                            churches.add(p);
                        }
                        else if (r.getFacilityType() == 2) {
                            mosques.add(p);
                        }
                    }
                }    			
            }
            
            //if the resident is christian, then find the nearest church
            if (resident.getReligion() == Religion.Christian) {
                bestLocation = bestLocation(home, churches, kibera);
                Bag bestChurches = new Bag();

                for(int i = 0; i < bestLocation.getStructure().size(); i++) {
                    Structure s = bestLocation.getStructure().get(i);
                    for (int j = 0; j < s.getReligiousFacilities().size(); j++) {
                        ReligiousFacility r = s.getReligiousFacilities().get(j);
                        if (r.getFacilityType() == 1) {
                            bestChurches.add(r);
                        }
                    }
                }
                //if there are multiple churches on the parcel, then randomly pick a church
                int numChurches = bestChurches.size();
                int pickChurch = kibera.random.nextInt(numChurches) + 1;
                for (int i = 1; i <= numChurches; i++) {
                    if (i == pickChurch) {
                        ReligiousFacility myChurch = (ReligiousFacility) bestChurches.get(i-1);
                        resident.setMyReligiousFacility(myChurch);
                        myChurch.addAttendee(resident);
                    }
                }
            }
            //if the resident is muslim, then find the nearest mosque to attend
            else {
                bestLocation = bestLocation(home, mosques, kibera);
                Bag bestMosques = new Bag();

                for(int i = 0; i < bestLocation.getStructure().size(); i++) {
                    Structure s = bestLocation.getStructure().get(i);
                    for (int j = 0; j < s.getReligiousFacilities().size(); j++) {
                        ReligiousFacility r = s.getReligiousFacilities().get(j);
                        if (r.getFacilityType() == 2) {
                            bestMosques.add(r);
                        }
                    }
                }
                //if there are multiple mosques on the parcel, then randomly pick a mosque on the parcel to attend
                int numMosques = bestMosques.size();
                int pickMosque = kibera.random.nextInt(numMosques) + 1;
                for (int i = 1; i <= numMosques; i++) {
                    if (i == pickMosque) {
                        ReligiousFacility myMosque = (ReligiousFacility) bestMosques.get(i-1);
                        resident.setMyReligiousFacility(myMosque);
                        myMosque.addAttendee(resident);
                    }
                }
            }               
           return bestLocation;          
        }
    	
        //if the goal is to replenish the household's water supply, then go to nearest water point
    	else if (goal == Goal.Get_Water) {
            //go to water, wait in queue, add water amount to water in house, increase hh expenditures
            Parcel p = bestLocation (home, kibera.allWaterPoints, kibera); 		
            double currentWaterLevel = resident.getHousehold().getRemainingWater(); 		
            resident.getHousehold().setRemainingWater(currentWaterLevel + 20);

            //Add water to household expenditure
            resident.getHousehold().getDailyWaterCost();

            return p;  		
    	}
    	
        //if the goal is to riot/rebel, then go to point of congregation
    	else if (goal == Goal.Rebel) {
            //turn red
            //the point where rebels will congregate
            int xCenter = 170;
            int yCenter = 100;
            int jitterX = kibera.random.nextInt(20);
            int jitterY = kibera.random.nextInt(20);
            Parcel parcel = (Parcel)kibera.landGrid.get(xCenter + jitterX, yCenter + jitterY);

            return parcel;
    	}
    		
    	else { return position; }
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
        double bestScoreSoFar = Double.POSITIVE_INFINITY;
        
        //go through potential locations and determine the distance between current position and the set of available positions
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
     * The agent has a goal and is moving towards it, determine the next parcel to walk to 
     * 
     * @param subgoal - the goal location of the agent
     * @param position - the current position of the agent
     * @param kibera
     * 
     * @return the parcel corresponding to the location to move to
     * 
     */
    public static Parcel getNextTile(Kibera kibera, Parcel subgoal, Parcel position) {
    	// move in which direction?
        int moveX = 0, moveY = 0;
 
        int dx = subgoal.getXLocation() - position.getXLocation();
        int dy = subgoal.getYLocation() - position.getYLocation();
        
        if (dx < 0) { moveX = -1; }
        else if (dx < 6) { moveX = dx; }
        else if (dx >= 6) { moveX = 6; }
        
        if (dy < 0) { moveY = -1; }
        else if (dy < 6) { moveY = dy; }
        else if (dy >= 6) { moveY = 6; }

        // can either move in Y direction or X direction: see which is better
        Parcel xmove = ((Parcel) kibera.landGrid.field[position.getXLocation() + moveX][position.getYLocation()]);
        Parcel ymove = ((Parcel) kibera.landGrid.field[position.getXLocation()][position.getYLocation() + moveY]);

        boolean xmoveToRoad = ((Integer) kibera.roadGrid.get(xmove.getXLocation(), xmove.getYLocation())) > 0;       
        boolean ymoveToRoad = ((Integer) kibera.roadGrid.get(ymove.getXLocation(), ymove.getYLocation())) > 0;

        // we are ON the subgoal, so don't move at all!       
        if (moveX == 0 && moveY == 0) { return xmove; } // both are the same result, so just return the xmove (which is identical)      
        else if (moveX == 0) { return ymove; } // this means that moving in the x direction is not a valid move: it's +0                  
        else if (moveY == 0) { return xmove;} // this means that moving in the y direction is not a valid move: it's +0                  
        else if (xmoveToRoad == ymoveToRoad) { //equally good moves: pick randomly between them
            if (kibera.random.nextBoolean()) { return xmove; }
            else { return ymove; }
        }
        
        else if (xmoveToRoad && moveX != 0) { return xmove; } // x is a road: pick it        	      
        else if (ymoveToRoad && moveY != 0) { return ymove; } // y is a road: pick it                    
        else if (moveX != 0) { return xmove; } // move in the better direction       
        else if (moveY != 0) { return ymove; } // yes                  
        else { return ymove; }
    }
    
    /**
     * If the goal is to socialize, then determine which friend to visit
     * Find all residents linked to myself (with the exception of those living in the same household as myself
     * Determine the weight of the link and the physical distance of each pair
     * Calculate overall likelihood of socializing with another resident based on weight and distance
     * Determine residents with highest likelihoods and randomly select one of these friends
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return the parcel corresponding to the location to move to
     * 
     */
    public static Parcel determineWhereToSociolize(Resident me, Kibera kibera) {

        //determine my set of potential friends
        Bag myFriends = new Bag(kibera.socialNetwork.getEdgesOut(me));
        double sumWeight = 0;
        double sumDistance = 0;
        Resident socializeFriend = null; //this is the friend I will socialize with

        //remove anyone living in the same parcel
        for(int i = 0; i < myFriends.size(); i++) {
            Edge e = (Edge)(myFriends.get(i));
            //Get the resident linked to me
            Resident friend = (Resident) e.getOtherNode(me);
            if (friend.getHousehold().getHome().getStructure().getParcel() == me.getHousehold().getHome().getStructure().getParcel()) {
                myFriends.remove(i);
            }
        }
        myFriends.resize(myFriends.size());

        HashMap<Resident, Double> socialize = new HashMap<Resident, Double>();
        ValueComparator bvc = new ValueComparator(socialize);
        TreeMap<Resident, Double> socialize_sorted = new TreeMap<Resident, Double>(bvc);

        //my location
        double x = me.getHousehold().getHome().getStructure().getParcel().getXLocation();
        double y = me.getHousehold().getHome().getStructure().getParcel().getYLocation();
        
        //determine the total distance between me and all my potential friends
        if (myFriends != null) {
            for(int i = 0; i < myFriends.size(); i++) {
                Edge e = (Edge)(myFriends.get(i));
                //Get the resident linked to me
                Resident friend = (Resident) e.getOtherNode(me);
                Double2D friendLocation = kibera.world.getObjectLocation(friend);

                double weight = ((Double)(e.info)).doubleValue();								
                sumWeight = sumWeight + weight;

                double dx = friendLocation.x - x;
                double dy = friendLocation.y - y;
                double distance = Math.sqrt(dx*dx + dy*dy);

                sumDistance = distance + sumDistance;
            }
            //calcuate the likelihood that the agent will socialize with a given friend, which is a function of my distance
            //from the friend (compared to the distance of other friends) and the strenght of the tie between me
            //and my potential friend
            for(int i = 0; i < myFriends.size(); i++) {
                Edge e = (Edge)(myFriends.get(i));
                //Get the resident linked to me
                Resident friend = (Resident) e.getOtherNode(me);
                Double2D friendLocation = kibera.world.getObjectLocation(friend);

                double weight = ((Double)(e.info)).doubleValue();
                double weightStandardize = weight / sumWeight;

                double dx = friendLocation.x - x;
                double dy = friendLocation.y - y;
                double distance = Math.sqrt(dx*dx + dy*dy);
                double distanceStandardize = distance / sumDistance;

                if (sumDistance == 0) { distanceStandardize = 0; }
                else { distanceStandardize = 1 - distanceStandardize; } //take the inverse

                double socializeLikelihood = 0.5 * weightStandardize + 0.5 * distanceStandardize;

                socialize.put(friend, socializeLikelihood);
            }
        }
        
        socialize_sorted.putAll(socialize);

        if (socialize != null) {
            int numFriends = socialize.size();
            int numPotentialFriendstoSocialize = (int)(numFriends * 0.1);

            if (numPotentialFriendstoSocialize <= 0) {
                numPotentialFriendstoSocialize = 1;
            }

            //pick a random number between 0 and the total number of potential friends I could socialize with
            int friendToSocialize = kibera.random.nextInt(numPotentialFriendstoSocialize);
            //get the friend the resident will socialize with
            int i = 0;
            for(Map.Entry<Resident, Double> s : socialize_sorted.entrySet()) {
                if (friendToSocialize == i) {
                    socializeFriend = s.getKey();
                }
                i++;
            }
        }

        Parcel myHome = me.getHousehold().getHome().getStructure().getParcel();

        if (socializeFriend == null) { //no friends to socialize with
            return myHome;
        }

        Parcel friendLocation = socializeFriend.getHousehold().getHome().getStructure().getParcel();
        
        
        //if the friend is not home, then don't go to their house, stay home instead
        if (socializeFriend.getCurrentGoal() != Goal.Stay_Home) {
            return myHome;
        }
        else {          
            //add friend to my immediate friends network
            me.setMyFriend(null);
            socializeFriend.setMyFriend(null);
            
            me.setMyFriend(socializeFriend);
            socializeFriend.setMyFriend(me);
                       
            return friendLocation;
        }

    }
    
    /**
     * When agents reach their goal location, they will interact with those at the same location
     * 
     * @param parcel - the current parcel location of the agent
     * @param me - the agent
     * @param kibera
     * 
     */
    public static void performAction(Parcel parcel, Resident me, Kibera kibera) {
  
    	//determine if there are other agents who are at their goal location and on the same parcel
    	//if so, add a link between the two agents, if link already exists, increase value of link based on time spent together
    	//keep track of all links and their values in the Network object
        Bag myConnections = new Bag();
        Goal goal = me.getCurrentGoal();
               
        switch(goal) {
            case Get_An_Education:
                //connect to other agents in the same student class
                myConnections = me.getMySchoolClass().getClassmates();
                break;
            case Go_to_Work:
                //connect to other agents working with the same employer
                myConnections = connectToMyColleagues(me, kibera);
                break;
            case Find_Employment:
                //connect to agents in household and nearby (since agent will be home most of the time)
                myConnections = connectToMyNeighbors(me, parcel, kibera);
                break;
            case Socialize:
                //connect to friend as well as that friends connections that are at his/her home
                if (me.getMyFriend() != null) {
                    myConnections = connectToSocializeFriends(me, parcel, kibera);
                }    
                break;
            case Get_Water:
                //no interaction is assumed to occur when getting water
                myConnections = null;
                break;
            case Rebel:
                //connect to other rioters at the same locaiton
                myConnections = connectToOtherRebels(me, parcel, kibera);
                break;
            case Go_to_Religious_Institution:
                //randomly select an agent currently at the same religious institution and connect
                connectToOthersAtReligiousFacility(me, kibera);
                myConnections = me.getMyReligiousConnections();
                break;
            case Stay_Home:
                //connect to agents nearby during the day, but at night connect only to those in my household
                if (me.getMinuteInDay() > 1019) {
                    myConnections = me.getHousehold().getHouseholdMembers();
                }
                else {
                    myConnections = connectToMyNeighbors(me, parcel, kibera);
                }
                break;
                
        }
        
        //now that you know all my potential connections based on my activity, connect only to those connections that
        //are currently at the same location (parcel)
        //connection strength is based on teh amount of time spent together      
        double weight = 0;
        double oldWeight = 0;
        
        double stayingPeriod = me.getStayingPeriod() - me.getMinuteInDay();
        
        if (me.getMinuteInDay() == kibera.params.global.getMinutesInDay()-1) {
             stayingPeriod = 1440 - kibera.params.global.getMinutesInDay();
        }
                          
        if (stayingPeriod == 0) { stayingPeriod = 1; }
        
        //if there are other residents at this location other than myself and those resident(s) are also at 
        //their goal location, then create a link between me and the other resident
        if (myConnections != null) {
            for (int i = 0; i < myConnections.size(); i++) {
                
                Resident r = (Resident) myConnections.get(i);
                if ( r != me && r.getGoalLocation() == parcel) {
                    //check if edge already exists between the two residents
                    //if edge exists, increment weight of edge
                    //if edge does not exist, create a new edge
                    Edge e = EdgeFunctions.getEdge(me, r, kibera);
                                      
                    if (e != null) {
                        weight = (.5) * (stayingPeriod / 1440); //multiply by 1/2 because weight is added twice since these are bi-directional edges
                        oldWeight = ((Double)(e.info)).doubleValue();
                        weight = oldWeight + weight;
                        kibera.socialNetwork.updateEdge(e, me, r, weight);
                    }
                    else { //if an edge does not already exist between the two residents
                        weight = stayingPeriod / 1440;
                        Edge edge = new Edge(me, r, weight);
                        kibera.socialNetwork.addEdge(edge); //create a new edge
                    }
                }									
            }
        }
    }
    
    /**
     * determine which other agents are assigned the same employer
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return the set of colleagues
     * 
     */
    public static Bag connectToMyColleagues(Resident me, Kibera kibera) {
        Bag otherCoworkers = new Bag();
        
        if (me.getMyBusinessEmployer() != null) { 
            otherCoworkers = me.getMyBusinessEmployer().getEmployees(); 
        }
        else if (me.getMyHealthFacilityEmployer() != null) { 
            otherCoworkers = me.getMyHealthFacilityEmployer().getEmployees(); 
        }
        else if (me.getMyReligiousFacilityEmployer() != null) { 
            otherCoworkers = me.getMyReligiousFacilityEmployer().getEmployees();
        }
        else if (me.getMySchoolEmployer() != null) { 
            otherCoworkers = me.getMySchoolEmployer().getEmployees(); 
        }
        else {
            otherCoworkers = null;

        }
        
     return otherCoworkers;
    }
    
    /**
     * determine who are the agents that are also rioting/rebelling
     * 
     * @param me - the agent
     * @param parcel - the agent's current position
     * @param kibera
     * 
     * @return the set of other rebels on the same parcel
     * 
     */
    public static Bag connectToOtherRebels(Resident me, Parcel parcel, Kibera kibera) {
        Bag neighbors = new Bag(parcel.getResidents());
        Bag myRebelConnections = new Bag();
        
        for (int i = 0; i < neighbors.size(); i++) {
            Resident r = (Resident) neighbors.get(i);
            if (r.getCurrentGoal() == Goal.Rebel) {
                myRebelConnections.add(r);
            }
        }
        return myRebelConnections;
    }
    
    /**
     * determine my neighbors, these can include agents that are home, working, or rioting on the same parcel
     * 
     * @param me - the agent
     * @param parcel - the agent's current position
     * @param kibera
     * 
     * @return the set of neighbors
     * 
     */
    public static Bag connectToMyNeighbors(Resident me, Parcel parcel, Kibera kibera) {
        Bag neighbors = new Bag(parcel.getResidents());
        Bag myConnections = new Bag();
        
        for (int i = 0; i < neighbors.size(); i++) {
            Resident r = (Resident) neighbors.get(i);
            if (r.getCurrentGoal() == Goal.Find_Employment || r.getCurrentGoal() == Goal.Go_to_Work ||
                    r.getCurrentGoal() == Goal.Stay_Home || r.getCurrentGoal() == Goal.Socialize ||
                    r.getCurrentGoal() == Goal.Rebel) {
                myConnections.add(r);
            }
        }
        return myConnections;       
    }
    
    /**
     * connect to the friend I'm socializing with as well as my friend's friends that are currently at the same location
     * 
     * @param me - the agent
     * @param parcel - the agent's current location
     * @param kibera
     * 
     * @return the friend and set of friend's friends at the same parcel location
     * 
     */
    public static Bag connectToSocializeFriends(Resident me, Parcel parcel, Kibera kibera) {
        Bag neighbors = new Bag(parcel.getResidents());
        Bag myConnections = new Bag();
        Resident friend = me.getMyFriend();
        Bag friendsConnections = new Bag(kibera.socialNetwork.getEdgesOut(friend));
        
        //add friend to my connections
        myConnections.add(friend);
        
        //now add my friends connections that are at the same location as me
        for (int i = 0; i < friendsConnections.size(); i++) {
            Edge e = (Edge)(friendsConnections.get(i));
            Resident friendConnection = (Resident) e.getOtherNode(friend);
            if (neighbors.contains(friendConnection) && friendConnection != me) {
                myConnections.add(friendConnection);
            }
        }
  
        return myConnections;       
    }
    
    /**
     * find another agent to connect with at the same religious institution
     * 
     * @param me - the agent
     * @param kibera
     * 
     */
    public static void connectToOthersAtReligiousFacility(Resident me, Kibera kibera) {
        //select another church or mosque member to connect to
        int numReligiousConnections = 1;
        Bag allReligiousFacility = new Bag(me.getMyReligiousFacility().getAttendees());
        
        int i = 0;

        while (i < numReligiousConnections) {                  
            int rReligion = kibera.random.nextInt(allReligiousFacility.size());		
            Resident r = (Resident) allReligiousFacility.get(rReligion);
            me.addMyReligiousConnections(r);
            r.addMyReligiousConnections(me);
            
            i++;
        }
    }
    
    /**
     * determine if the agent should remain at current goal location
     * 
     * @param me - the agent
     * 
     * @return the parcel corresponding to the location to move to
     * 
     */
    public static boolean shouldResidentStayAtActivity (Resident me) {
       boolean isStay = false;
       
       //if the staying period at this activitiy has not been reached, then stay
       if (me.getCStep() < me.getStayingPeriod()) {
            isStay = true;
       }   
        
        return isStay;
    }
    
    /**
     * determine the staying period at each activity
     * 
     * @param me - the agent
     * @param goal - the agent's current goal
     * @param kibera
     * 
     * @return the time the agent will be done with the current activity
     * 
     */  
    @SuppressWarnings("incomplete-switch")
    public static int stayingPeriodAtActivity(Resident me, Goal goal, Kibera kibera) {
        int period = 0;
        int curMin = me.getMinuteInDay();
        
        switch(goal) {
            case Go_to_Work:          
                period = 6 * 60 + kibera.random.nextInt(4*60-2); 
                break;
            case Find_Employment:
                period = 120 + kibera.random.nextInt(2*60-2);
                break;
            case Get_Water:
                period = 10 + kibera.random.nextInt(50);     
                break;
            case Get_An_Education:
                period = 7 * 60; 
                break;
            case Socialize:
                //don't want to socialize after too late
                period = 60 + kibera.random.nextInt(60-2);
                break;
            case Stay_Home:
                //evaluate afer every step if the agent should stay home
                if (me.isLaidOff() || me.leftSchool()) {
                    period = 1;
                }
                else {
                    period = 1;
                }
                break;
            case Go_to_Religious_Institution:
                if (me.getReligion() == Religion.Muslim) {
                    period = 20 + kibera.random.nextInt(180);
                }
                else { //reisdent is Christian
                    period = 60 + kibera.random.nextInt(60); //stopped
                }
                break;
            case Rebel:
                period = 60 + kibera.random.nextInt(360);
                break;
        }
        return (period + me.getCStep());
    }

    /**
     * determine if there are any available schools within the agent's vision
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return the set of parcels with the available schools (if any)
     * 
     */  
    public static Bag findSchools(Resident me, Kibera kibera) {
        //determine if there are any schools within vision (vision in this case is the average size of two neighborhoods in Kibera)
        //that have available space
        //if so, attend available school nearest to home

        int x = me.getHousehold().getHome().getStructure().getParcel().getXLocation();
        int y = me.getHousehold().getHome().getStructure().getParcel().getYLocation();

        Bag schoolsInNeighborhood = new Bag();
        kibera.landGrid.getNeighborsMaxDistance(x,y,kibera.params.global.getSchoolVision(),false,schoolsInNeighborhood, null, null);

        Bag schoolParcelLocations = new Bag();
        
        //determine if any schools in neighborhood (within vision) have not reached capacity
        for(Object o: schoolsInNeighborhood){
            Parcel p = (Parcel) o;
            for(int i = 0; i < p.getStructure().size(); i++) {
                Structure s = (Structure) p.getStructure().get(i);
                if (s.getSchools().size() > 0) {
                    for(int j = 0; j < s.getSchools().size(); j++) {
                        School school = (School) s.getSchools().get(j);
                        if (!school.isStudentCapacityReached()) {
                            schoolParcelLocations.add(p); 
                        }
                    }
                }
            }
        }
        me.searchedForSchool(true); //note that the agent has now searched for a school

        return schoolParcelLocations;
    }
    
    /**
     * determine if there are any available employers within the agent's vision
     * 
     * @param me - the agent
     * @param rn - a random number
     * @param employerOutsideKibera - determines if employer resides outside of kibera
     * @param percentageFormal - the percentage of agents that can work in the formal sector
     * @param kibera
     * 
     * @return the set of parcels with the available employers (if any)
     * 
     */     
    public static Bag findPotentialEmployment(Resident me, Double rn, OutsideKibera employerOutsideKibera, double percentageFormal, Kibera kibera) {
        int x = me.getHousehold().getHome().getStructure().getParcel().getXLocation();
        int y = me.getHousehold().getHome().getStructure().getParcel().getYLocation();

        Bag potentialEmployers = new Bag();
        kibera.landGrid.getNeighborsMaxDistance(x,y,kibera.params.global.getEmploymentVision(),false,potentialEmployers, null, null);

        Bag employerParcelLocations = new Bag();
        Parcel homeLocation = me.getHousehold().getHome().getStructure().getParcel();
        
        //if agent is an adult and the random number is below the percentage that can work in the formal sector
        if (me.getAge() > 18 && rn < percentageFormal) {
            //first search for employment in the formal market in kibera
            for(Object o: potentialEmployers) {
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

            //if employment was not found within kibera, next search for formal employment outside of kibera
            if (employerParcelLocations.isEmpty()) {
                if (!employerOutsideKibera.isEmployeeCapacityReached(OutsideKibera.BusinessType.formal, kibera)) {                              
                    employerParcelLocations.add(homeLocation);
               }
            }

            //if unable to find a formal employer with availability anywhere, search for informal employment
            if (employerParcelLocations.isEmpty()) {
                for(Object o: potentialEmployers) {
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
                //next search for informal employment outside of kibera
                if (employerParcelLocations.isEmpty()) {
                    if (!employerOutsideKibera.isEmployeeCapacityReached(OutsideKibera.BusinessType.informal, kibera)) {
                        employerParcelLocations.add(homeLocation);
                    }
                }
            }                       
        }

        //otherwise the resident is 18 or younger or will search only the informal sector
        else {
            //search for informal employment in kibera
            for(Object o: potentialEmployers) {
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
            //next search informal employment outside of kibera
            if (employerParcelLocations.isEmpty()) {
                if (!employerOutsideKibera.isEmployeeCapacityReached(OutsideKibera.BusinessType.informal, kibera)) {
                    employerParcelLocations.add(homeLocation);
                }
            }
        }

    return employerParcelLocations;	
    }
    
    /**
     * utilize water in household
     * 
     * @param me - the agent
     * @param kibera
     * 
     */
    public static void utilizeWater(Resident me, Kibera kibera) {
        double dailyUse = kibera.waterRequirement;
        me.residentDailyUse = dailyUse;

        double WaterUsed;
        double remainingWater = me.getHousehold().getRemainingWater(); //determine the water still remaining

        // only uses from family bucket
        if (dailyUse >= remainingWater) { // if the water is not enough, utilize all
            WaterUsed = remainingWater; // tell that there is no water in the house
        } 
        else {
            WaterUsed = dailyUse; // if plenty of water in the house, only use what you want
        }
        //re-calculate remaining water
        me.getHousehold().setRemainingWater(remainingWater - WaterUsed);
    }

}

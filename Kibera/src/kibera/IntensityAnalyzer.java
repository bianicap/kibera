package kibera;

import kibera.Resident.Employment;
import kibera.Resident.Goal;
import kibera.Resident.Religion;
import sim.util.Bag;

/**
 * The Intensity Analyzer determines which motivation is strongest, and thus the action-guiding motive
 * 
 * Agents can perform 1 of 8 activities/goals: Go to work, Find employment, Get an education, Get water,
 * Go to religious institution, Rebel, Socialize, Stay home.
 * 
 * This is adapted from Schmidt's (2000) PECS framework
 * 
 * @author bpint
 *
 */

public class IntensityAnalyzer {

    /**
     * Run the intensity analyzer
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return the agent's action-guiding motive/goal
     * 
     */ 
    public static Goal runIntensityAnalyzer(Resident me, Kibera kibera) {            
        
        //The household need of a resident. Positive if the household has sufficient income, negative if not.   
        double householdNeed = me.getHousehold().getDailyHouseholdDiscrepancy();
        
        int schoolDay = (me.getTimeManager().currentDayInWeek(me.getCStep()) < 6) ? 1 : 0; // school only open from monday to friday ( day 1 to 5 of the week)          
        int formalWorkDay = (me.getTimeManager().currentDayInWeek(me.getCStep()) < 6) ? 1 : 0; 
               
        //determine the times of day activities can be performed
        boolean workTime = isWorkTime(me, formalWorkDay, kibera);
        boolean searchTime = isSearchTime(me, kibera);
        boolean schoolTime = isSchoolTime(me, schoolDay, kibera);
        boolean socializeTime = isSocializeTime(me, schoolDay, formalWorkDay, kibera);
        boolean churchTime = isChurchTime(me, kibera);
        boolean mosqueTime = isMosqueTime(me, kibera);
        boolean waterTime = isWaterTime(me, kibera);
        
        double wWork = 0;
        double wSearch = 0;
        double wSchool = 0;
        double wSocialize = 0;
        double wReligion = 0;
        double wWater = 0;
        double wHome = 0;
        double wRebel = 0;       
        
        //determine the weight/intensity given to each activity
        if (workTime) { wWork = goToWorkIntensity(me, kibera); }
        if (schoolTime) { wSchool = getAnEducationIntensity(me, householdNeed, kibera); }
        if (searchTime && wWork == 0) { wSearch = findEmploymentIntensity(me, schoolTime, householdNeed, kibera, wSchool); } 
        if (wWork == 0 && wSearch == 0) { wReligion = goToReligiousInstitutionIntensity(me, mosqueTime, churchTime, kibera); }      
        wHome = stayHomeIntensity(me, wWork, wSearch, wSchool, wSocialize, wReligion);     
        boolean atHome = me.getPosition().equals(me.getHousehold().getHome().getStructure().getParcel());
        if (waterTime && atHome && wHome > 0) { wWater = getWaterIntensity(me, workTime, schoolTime, kibera); }
        if (socializeTime && wHome > 0) { wSocialize = socializeIntensity(me, kibera); }       
        wRebel = rebelIntensity(me, kibera);
        
        //determine if its the right time of day and day of week to perform the given activity
        me.isWaterTime(waterTime);
        me.isWorkTime(workTime);
        me.isMosqueTime(mosqueTime);
        me.isSchoolTime(schoolTime);
        me.isSearchTime(searchTime);
        me.isSocializeTime(socializeTime);
        me.isWaterTime(waterTime);   
    
        //store the resident's current activity
        Resident.Goal oldGoal = me.currentGoal;
                                                                                                   
        //determine which motive has the highest priority
        double[] activ = new double[8];
        for (int i = 0; i < 8; i++) {
            activ[i] =  kibera.random.nextDouble();// the random value will be less than the above assigned value
        }

        //randomize the index
        for (int i = 0; i < 8; i++) {
            int swapId = kibera.random.nextInt(8);
            if (swapId != i) {
                double temp = activ[i];
                activ[i] = activ[swapId];
                activ[swapId] = temp;
            }
        }
               
        double[] motivePriorityWeight = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        
        //determien the final weight of each activity
        motivePriorityWeight[1] = wHome * activ[0];
        motivePriorityWeight[1] = wSchool * activ[1];
        motivePriorityWeight[2] = wWork * activ [2];
        motivePriorityWeight[3] = wSocialize * activ [3];
        motivePriorityWeight[4] = wSearch * activ [4];
        motivePriorityWeight[5] = wReligion * activ [5];
        motivePriorityWeight[6] = wWater * activ [6];
        motivePriorityWeight[7] = wRebel * activ [7];
        
        int curMotive = 0;
        
        double maximum = motivePriorityWeight[0];   // start with the first value
      
        for (int i=1; i<motivePriorityWeight.length; i++) {
           if (motivePriorityWeight[i] > maximum) {
             maximum = motivePriorityWeight[i];   // new maximum
              curMotive =i;
            }
        }
           
        //Assign the goal the resident will execute
        if (curMotive == 1) { me.currentGoal = Goal.Get_An_Education; }
        else if (curMotive == 2) { me.currentGoal = Goal.Go_to_Work; }
        else if (curMotive == 3) { me.currentGoal = Goal.Socialize; }
        else if (curMotive == 4) { 
            me.currentGoal = Goal.Find_Employment;
            me.currentEmploymentStatus = Employment.Searching;
        }
        else if (curMotive == 5) { 
            me.currentGoal = Goal.Go_to_Religious_Institution; }
        else if (curMotive == 6) { 
            me.currentGoal = Goal.Get_Water; }
        else if (curMotive == 7) {
            me.currentGoal = Goal.Rebel;
        }
        else { me.currentGoal = Goal.Stay_Home; }
        
        if (me.currentGoal != oldGoal) {
            me.changedGoal(true);
        }
        else {
            me.changedGoal(false);
        }
        
 
        return me.currentGoal;
    }
    
    /**
     * Determines if the current time is within working hours
     * 
     * @param me - the agent
     * @param formalWorkDay - 1 if its Monday through Friday, 0 otherwise
     * @param kibera
     * 
     * @return true if its the time of day and day of week to work, false otherwise
     * 
     */ 
    public static boolean isWorkTime(Resident me, int formalWorkDay, Kibera kibera) {
        boolean workTime = false;
    
        int workStart = 60 * 5 + kibera.random.nextInt(60 * 2); // in minute - working hour earliest start
        int workEnd = 60 * 10 + kibera.random.nextInt(60 * 2); // in minute - working hour latest start
        
        //adjust hours if day is shortened not to include overnight hours
        if (!kibera.params.global.isRunFullDay()) {
            workStart = workStart - kibera.params.global.getTimeOfFirstActivity();
            workEnd = workEnd - kibera.params.global.getTimeOfFirstActivity();
        }
        
        //Determine if its working time
        if (me.getMinuteInDay() >= workStart && me.getMinuteInDay() <= workEnd) { //if time is working hours
            if (me.getCurrentEmploymentStatus() == Employment.Informal || (me.getCurrentEmploymentStatus() == Employment.Formal 
                    && formalWorkDay == 1)) {
                workTime = true;
            }
        }  
        
        return workTime;      
    }
    
    /**
     * Determine if the current time is within searching for work time
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return true if its the time of day and day of week to search for employment, false otherwise
     * 
     */ 
    public static boolean isSearchTime(Resident me, Kibera kibera) {
        boolean searchTime = false;
        
        //determine if can start searching for employment
        int searchStart = 60 * 8 + kibera.random.nextInt(60 * 2); // in minute - working hour earliest start
        int searchEnd = 60 * 16 + kibera.random.nextInt(60 * 2); // in minute - working hour latest start
        
        //adjust hours if day is shortened not to include overnight hours
        if (!kibera.params.global.isRunFullDay()) {
            searchStart = searchStart - kibera.params.global.getTimeOfFirstActivity();
            searchEnd = searchEnd - kibera.params.global.getTimeOfFirstActivity();
        }
        
        //determine if its searching for work time
        if (me.getMinuteInDay() >= searchStart && me.getMinuteInDay() <= searchEnd) {
            searchTime = true;
        }       
        return searchTime;
    }
    
    /**
     * Determine if the current time is within school time
     * 
     * @param me - the agent
     * @param schoolDay - 1 if its a school day (Monday through Friday), 0 otherwise
     * @param kibera
     * 
     * @return true if its the time of day and day of week to go to school, false otherwise
     * 
     */ 
    public static boolean isSchoolTime(Resident me, int schoolDay, Kibera kibera) {
        boolean schoolTime = false;
                
        int schoolStart = (60 * 7) + kibera.random.nextInt(60 * 2);
        
        //adjust hours if day is shortened not to include overnight hours
        if (!kibera.params.global.isRunFullDay()) {
            schoolStart = schoolStart - kibera.params.global.getTimeOfFirstActivity();
        }
       
        //determine if its school time and school day
        if (me.getMinuteInDay() == schoolStart && schoolDay == 1) {
            schoolTime = true;
        }       
        return schoolTime;
    }
    
    /**
     * Determine if the current time is within the time agents can socialize
     * 
     * @param me - the agent
     * @param schoolDay - 1 if its a school day (Monday through Friday), 0 otherwise
     * @param formalWorkDay - 1 if its Monday through Friday, 0 otherwise
     * @param kibera
     * 
     * @return true if its the time of day and day of week to socialize, false otherwise
     * 
     */ 
    public static boolean isSocializeTime(Resident me, int schoolDay, int formalWorkDay, Kibera kibera) {       
        boolean socializeTime = false;
        
        int socializeStart = 0;
        int socializeEnd = 0;
        
        //Determine if the resident is a student
        boolean isStudent = false;        
        if (me.getMySchool() != null) {
            isStudent = true;
        }
        
        //if the resident is a student or is employed, can socialize only after work/school hours
        if (isStudent && schoolDay == 1) {
            socializeStart = 60 * 17 + kibera.random.nextInt(60);
            socializeEnd = 60 * 19 + kibera.random.nextInt(60);
            
            //adjust hours if day is shortened not to include overnight hours
            if (!kibera.params.global.isRunFullDay()) {
                socializeStart = socializeStart - kibera.params.global.getTimeOfFirstActivity();
                socializeEnd = socializeEnd - kibera.params.global.getTimeOfFirstActivity();
            }
            
            if (me.getMinuteInDay() >= socializeStart && me.getMinuteInDay() <= socializeEnd) {
        	socializeTime = true;
            }
        }
        
        //if the resident is employed in the formal sector, can only socialize after work hours
        else if (me.getCurrentEmploymentStatus() == Employment.Formal && formalWorkDay == 1 && me.getCurrentGoal() != Goal.Go_to_Work) {
            socializeStart = 60 * 17 + kibera.random.nextInt(60);
            socializeEnd = 60 * 19 + kibera.random.nextInt(60);
            
            //adjust hours if day is shortened not to include overnight hours
            if (!kibera.params.global.isRunFullDay()) {
                socializeStart = socializeStart - kibera.params.global.getTimeOfFirstActivity();
                socializeEnd = socializeEnd - kibera.params.global.getTimeOfFirstActivity();
            }
            
            if (me.getMinuteInDay() >= socializeStart && me.getMinuteInDay() <= socializeEnd) {
        	socializeTime = true;
            }
        }
        
        //if the resident is employed in the informal sector, can only socialize when not working
        else if (me.getCurrentEmploymentStatus() == Employment.Informal && me.getCurrentGoal() != Goal.Go_to_Work) {
            socializeStart = 60 * 17 + kibera.random.nextInt(60);
            socializeEnd = 60 * 19 + kibera.random.nextInt(60);
            
            //adjust hours if day is shortened not to include overnight hours
            if (!kibera.params.global.isRunFullDay()) {
                socializeStart = socializeStart - kibera.params.global.getTimeOfFirstActivity();
                socializeEnd = socializeEnd - kibera.params.global.getTimeOfFirstActivity();
            }
            
            if (me.getMinuteInDay() >= socializeStart && me.getMinuteInDay() <= socializeEnd) {
        	socializeTime = true;
            }
        }
        
        //if not employed or not a student or is not a work day/school day, can socialize anytime during the day
        else {
            socializeStart = 60 * 9 + kibera.random.nextInt(60 * 8);
            socializeEnd = 60 * 19 + kibera.random.nextInt(60);
            
            //adjust hours if day is shortened not to include overnight hours
            if (!kibera.params.global.isRunFullDay()) {
                socializeStart = socializeStart - kibera.params.global.getTimeOfFirstActivity();
                socializeEnd = socializeEnd - kibera.params.global.getTimeOfFirstActivity();
            }
            
            if (me.getMinuteInDay() >= socializeStart && me.getMinuteInDay() <= socializeEnd) {
        	socializeTime = true;
            }
        }      
        return socializeTime;
        
    }
    
    /**
     * Determine if the current time is within the time agents can go to mosque
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return true if its the time of day and day of week to go to mosque, false otherwise
     * 
     */ 
    public static boolean isMosqueTime(Resident me, Kibera kibera) {
        boolean mosqueTime = false;
        
        int mosqueStart1 = 60 * 5; // in minute - mosque time earliest start
        int mosqueEnd1 = 60 * 6; // in minute - mosque time latest start
        
        int mosqueStart2 = 60 * 12; // in minute - mosque time earliest start
        int mosqueEnd2 = 60 * 14; // in minute - mosque time latest start
        
        int mosqueStart3 = 60 * 15; // in minute - mosque time earliest start
        int mosqueEnd3 = 60 * 17; // in minute - mosque time latest start
       
        //adjust hours if day is shortened not to include overnight hours
        if (!kibera.params.global.isRunFullDay()) {
            mosqueStart1 = mosqueStart1 - kibera.params.global.getTimeOfFirstActivity();
            mosqueEnd1 = mosqueEnd1 - kibera.params.global.getTimeOfFirstActivity();
            
            mosqueStart2 = mosqueStart2 - kibera.params.global.getTimeOfFirstActivity();
            mosqueEnd2 = mosqueEnd2 - kibera.params.global.getTimeOfFirstActivity();
            
            mosqueStart3 = mosqueStart3 - kibera.params.global.getTimeOfFirstActivity();
            mosqueEnd3 = mosqueEnd3 - kibera.params.global.getTimeOfFirstActivity();
        }
        
        //determine if its time to go to mosque      
        if (me.getMinuteInDay() > mosqueStart1 && me.getMinuteInDay() < mosqueEnd1 
                || me.getMinuteInDay() > mosqueStart2 && me.getMinuteInDay() < mosqueEnd2
                || me.getMinuteInDay() > mosqueStart3 && me.getMinuteInDay() < mosqueEnd3) {
            mosqueTime = true;
        }
        
        return mosqueTime;
    }
    
    /**
     * Determine if the current time is within the time agents can go to church
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return true if its the time of day and day of week to go to church, false otherwise
     * 
     */ 
    public static boolean isChurchTime(Resident me, Kibera kibera) {
        boolean churchTime = false;
        
        int churchStart1 = 60 * 7; // in minute - church hour earliest start
        int churchEnd1 = 60 * 8; // in minute - church hour latest start
        
        int churchStart2 = 60 * 9; // in minute - church hour earliest start
        int churchEnd2 = 60 * 10; // in minute - church hour latest start
        
        int churchStart3 = 60 * 11; // in minute - church hour earliest start
        int churchEnd3 = 60 * 12; // in minute - church hour latest start
        
        int churchStart4 = 60 * 18; // in minute - church hour earliest start
        int churchEnd4 = 60 * 19; // in minute - church hour latest start
       
        //adjust hours if day is shortened not to include overnight hours
        if (!kibera.params.global.isRunFullDay()) {
            churchStart1 = churchStart1 - kibera.params.global.getTimeOfFirstActivity();
            churchEnd1 = churchEnd1 - kibera.params.global.getTimeOfFirstActivity();
            
            churchStart2 = churchStart2 - kibera.params.global.getTimeOfFirstActivity();
            churchEnd2 = churchEnd2 - kibera.params.global.getTimeOfFirstActivity();
            
            churchStart3 = churchStart3 - kibera.params.global.getTimeOfFirstActivity();
            churchEnd3 = churchEnd3 - kibera.params.global.getTimeOfFirstActivity();
            
            churchStart4 = churchStart4 - kibera.params.global.getTimeOfFirstActivity();
            churchEnd4 = churchEnd4 - kibera.params.global.getTimeOfFirstActivity();
        }
        
        //determine if its church time
        if (me.getChurchRnd() == 1 && (me.getMinuteInDay() > churchStart1 && me.getMinuteInDay() < churchEnd1)) {
            churchTime = true;
        }
        
        if (me.getChurchRnd() == 2 && (me.getMinuteInDay() > churchStart2 && me.getMinuteInDay() < churchEnd2)) {
            churchTime = true;
        }
        
        if (me.getChurchRnd() == 3 && (me.getMinuteInDay() > churchStart3 && me.getMinuteInDay() < churchEnd3)) {
            churchTime = true;
        }
        
        if (me.getChurchRnd() == 4 && (me.getMinuteInDay() > churchStart4 && me.getMinuteInDay() < churchEnd4)) {
            churchTime = true;
        }
        
        return churchTime;
    }
              
    /**
     * Determine if the current time is within the time agents can go get water
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return true if its the time of day and day of week to get water, false otherwise
     * 
     */ 
    public static boolean isWaterTime(Resident me, Kibera kibera) {
        boolean waterTime = false;
        
        int waterStart = 60 * 7;
        int waterEnd = 60 * 18;
        
        //adjust hours if day is shortened not to include overnight hours
        if (!kibera.params.global.isRunFullDay()) {
            waterStart = waterStart - kibera.params.global.getTimeOfFirstActivity();
            waterEnd = waterEnd - kibera.params.global.getTimeOfFirstActivity();
        }
        
        //etermine if can get water (during the day only) */
        if (me.getMinuteInDay() >= waterStart && me.getMinuteInDay() <= waterEnd) {
            waterTime = true;
        }
        
        return waterTime;
    }
    
                  
    /**
     * Determine if the current time is within the time agents can riot
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return true if its the time of day and day of week to riot, false otherwise
     * 
     */ 
    public static boolean isRebelTime(Resident me, Kibera kibera) {
        boolean rebelTime = false;
        
        int rebelStart = 0;
        int rebelEnd = kibera.params.global.getMinutesInDay();
        
        //agents can riot anytime
        if (me.getMinuteInDay() >= rebelStart && me.getMinuteInDay() <= rebelEnd) {
            rebelTime = true;
        }
        
        return rebelTime;
    }
        
        
    /**
     * Determine the agent's current intensity level to go to work
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return work intensity level
     * 
     */ 
    public static double goToWorkIntensity(Resident me, Kibera kibera) {
        
        double wWork = 0;
        boolean isEmployed = false;
        boolean hasAssignedEmployer = false;
        
        //check if the agent is employed
        if (me.currentEmploymentStatus == Employment.Formal || me.currentEmploymentStatus == Employment.Informal) {
            isEmployed = true;
        }
        
        //check if the agent has also been assigned an employer         
        if (me.getMyBusinessEmployer() != null || me.getMyHealthFacilityEmployer() != null || me.getMyReligiousFacilityEmployer() != null || 
                me.getMySchoolEmployer() != null || me.getEmploymentOutsideKibera().getEmployees().contains(me)) {
            hasAssignedEmployer = true;
        }
        
        //if the agent is employed and has an assigned employer
        if (isEmployed && hasAssignedEmployer) { 
 
            wWork = 0.8 + 0.2 * kibera.random.nextDouble();
        		
            //there is a certain probability that the resident might lose his job, which will leave an opening for others
            //searching and will require that the resident search for another job
            if (kibera.random.nextDouble() < kibera.params.global.getProbabilityOfLosingEmployment() && kibera.canResidentsbeLaidOff) {
                //agent lost its job, remove it from current employer
                if (me.getMyBusinessEmployer() != null) {                   
                    Business b = me.getMyBusinessEmployer();
                    me.setMyBusinessEmployer(null);            
                    b.removeEmployee(me);
        	}
        	else if (me.getMyHealthFacilityEmployer() != null) {
                    HealthFacility h = me.getMyHealthFacilityEmployer();
                    me.setMyHealthFacilityEmployer(null);
                    h.removeEmployee(me);
        	}
        	else if (me.getMyReligiousFacilityEmployer() != null) {
                    ReligiousFacility r = me.getMyReligiousFacilityEmployer();
                    me.setMyReligiousFacilityEmployer(null);
                    r.removeEmployee(me);
        	}
        	else if (me.getMySchoolEmployer() != null) {
                    School s = me.getMySchoolEmployer();                  
                    me.setMySchoolEmployer(null);
                    s.removeEmployee(me);
        	}
                else {
                    OutsideKibera k = me.getEmploymentOutsideKibera();
                    k.removeEmployee(me);
                    k.addNonEmployee(me);
                    me.setEmploymentTypeOutsideKibera(null);
                }
                
       
                me.setCurrentEmploymentStatus(Employment.Searching);
        	me.setResidentIncome(0);   			
        	me.isLaidOff(true);	
        	//stay home for now, start searching for a new job after one day
                wWork = 0;
            }
            
        }

        return wWork;
        
    }
    
    /**
     * Determine the agent's current intensity level to go to work
     * 
     * @param me - the agent
     * @param schoolTime - true if the hour is within school hours, false otherwise
     * @param householdNeed the household's income disparity
     * @param kibera
     * @param wSchool - the intensity level for going to school
     * 
     * @return search for work intensity level
     * 
     */ 
    public static double findEmploymentIntensity(Resident me, boolean schoolTime, double householdNeed, Kibera kibera, double wSchool) {       
        double wSearch = 0;
        boolean isEmployed = false;
        boolean isSchoolEligible = false;
        
        
        if (me.currentEmploymentStatus == Employment.Formal || me.currentEmploymentStatus == Employment.Informal) { isEmployed = true; }                              
        if (me.isSchoolEligible() && !isEmployed) { isSchoolEligible = true; }
        
        //if the resident's employment status is searching, search for available employer
        if (me.currentEmploymentStatus == Employment.Searching) {
            wSearch = 0.8 + 0.2 * kibera.random.nextDouble(); 
        }
            
        //if the resident is laid off, he/she will search for new employment
        else if (me.isLaidOff()) {
            wSearch = 0.8 + 0.2 * kibera.random.nextDouble();  
        }
        
        //the resident is eligible to go to school but is not assigned a school and could not find an available school
        else if (isSchoolEligible && me.getMySchool() == null && kibera.doSchoolEligibleSearchforEmployment
                && me.getAge() > 5) {
            if (kibera.doesHouseholdNeedImpactBehavior && householdNeed < 0) { //if there are no schools with available capacity, then resident does not go to school
                //The household income is low, resident will search for employment.            
                wSearch = 0.8 + 0.2 * kibera.random.nextDouble();   
            }
            else if (!kibera.doesHouseholdNeedImpactBehavior) {
                wSearch = 0.8 + 0.2 * kibera.random.nextDouble();
            }
            else {
                wSearch = 0;
            }
        }
        
        //inactive residents (not students) living in a household that does not have adequate income will search for employment
        else if (me.getCurrentEmploymentStatus() == Employment.Inactive && me.getMySchool() == null && me.getAge() > 5
                && kibera.doInactiveResidentsSearchforEmployment) {
            if (kibera.doesHouseholdNeedImpactBehavior && householdNeed < 0) {
                me.getHousehold().removedFromInactive(true);
                me.getHousehold().setTimeLeftInactive(me.getCStep());

                wSearch = 0.8 + 0.2 * kibera.random.nextDouble();
            }
            else if (!kibera.doesHouseholdNeedImpactBehavior) {
                me.getHousehold().removedFromInactive(true);
                me.getHousehold().setTimeLeftInactive(me.getCStep());
                wSearch = 0.8 + 0.2 * kibera.random.nextDouble();
            }
            else {
                wSearch = 0;
            }
        }
        
        //if the household needs more income, the student will leave school and search for a job  
        else if (me.getMySchool() != null && kibera.doStudentsLeaveSchooltoSearchforEmployment && me.getAge() > 5) {
            if (householdNeed < 0 && kibera.doesHouseholdNeedImpactBehavior) { 
                me.leftSchool(true);

                School s = me.getMySchool();
                me.setMySchool(null);
                s.removeStudents(me);
                
                //inform household of when the student left school -- the household will give former student a day to 
                //find a job prior to making other financial adjustments
                me.getHousehold().setTimeLeftSchool(me.getCStep());
                me.getHousehold().removedStudentFromSchool(true);
                
                me.getMySchoolClass().removeClassmate(me);

                wSearch = 0.8 + 0.2 * kibera.random.nextDouble();;
            }
            //if no household need impact, all students will leave school to search for employment (done for testing purposes)
            else if (!kibera.doesHouseholdNeedImpactBehavior) {
                me.leftSchool(true);

                School s = me.getMySchool();
                me.setMySchool(null);
                s.removeStudents(me);

                //inform household of when the student left school -- the household will give former student a day 
                //to find a job prior to make other financial adjustments
                me.getHousehold().setTimeLeftSchool(me.getCStep());
                me.getHousehold().removedStudentFromSchool(true);
                
                //remove me from my classmates network
                me.getMySchoolClass().removeClassmate(me);

                wSearch = 0.8 + 0.2 * kibera.random.nextDouble();
            }
            else {
                wSearch = 0;
            }
        }
                	                   
        else {
            wSearch = 0;
        }
        
        return wSearch;
    }
    
    /**
     * Determine the agent's current intensity level for going to school 
     * 
     * @param me - the agent
     * @param householdNeed the household's income disparity
     * @param kibera
     * 
     * @return go to school intensity level
     * 
     */ 
    public static double getAnEducationIntensity(Resident me, double householdNeed, Kibera kibera) {
        
        double wSchool = 0;   
        boolean isEmployed = false;
        boolean isSchoolEligible = false;
        
        //check if the agent is employed
        if (me.currentEmploymentStatus == Employment.Formal || me.currentEmploymentStatus == Employment.Informal) {
            isEmployed = true;
        }
        
        if (me.isSchoolEligible() && !isEmployed) {
            isSchoolEligible = true;
        }     
                    
        //if resident has an assigned school, go to school
        if (me.getMySchool() != null) { 
            wSchool = 0.8 + 0.2 * kibera.random.nextDouble();
        }
            
    
        //if eligible to go to school, household income can support school, and a school is available, 
        //then go to school
        else if (isSchoolEligible && kibera.doSchoolEligbibleStudentsSearchforSchool) {
            if (kibera.doesHouseholdNeedImpactBehavior && householdNeed > 0) { 
                if (ActionSequence.findSchools(me, kibera).size() > 0) {
                    wSchool = 0.8 + 0.2 * kibera.random.nextDouble();
                    me.leftSchool(false);
                    me.setCurrentEmploymentStatus(Employment.Inactive);
                    me.isLaidOff(false);
                    me.getHousehold().removedStudentFromSchool(false);
                    me.searchedForSchool(false);          
                }
            }
            else if (!kibera.doesHouseholdNeedImpactBehavior) {
                if (ActionSequence.findSchools(me, kibera).size() > 0) {
                    wSchool = 0.8 + 0.2 * kibera.random.nextDouble();
                    me.leftSchool(false);
                    me.setCurrentEmploymentStatus(Employment.Inactive);
                    me.isLaidOff(false);
                    me.getHousehold().removedStudentFromSchool(false);
                    me.searchedForSchool(false);  
                }
            }
            else {
                wSchool = 0;
            }
        }           
        else {
            wSchool = 0;
        }
 
        return wSchool;
    }
    
    /**
     * Determine the agent's current intensity level for going to church or mosque
     * 
     * @param me - the agent
     * @param mosqueTime - true if its time to go to mosque, false otherwise
     * @param churchTime - true if its time to go to church, false otherwise
     * @param kibera
     * 
     * @return go to church or mosque intensity level
     * 
     */ 
    public static double goToReligiousInstitutionIntensity(Resident me, boolean mosqueTime, boolean churchTime, Kibera kibera) {
        double wReligion = 0;
        
        //set the times/days residents can go to church or mosque 
        //Christians go to church on Sundays
        //80% of Christians and 91% of Muslims attend church/mosque weekly
        int churchDay = (me.getTimeManager().currentDayInWeek(me.getCStep()) == 7) ? 1 : 0;
        
        //determine the intensity associated with going to church/mosque
        if (kibera.random.nextDouble() < 0.8 && me.religion == Religion.Christian && churchDay == 1 && churchTime && !me.attendedReligiousFacility()) {
            wReligion = .6 + .4 * kibera.random.nextDouble();
            me.attendedReligiousFacility(true);
        }
         
       if (me.religion == Religion.Muslim && kibera.random.nextDouble() > .91 && !me.attendedReligiousFacility()) {
            if (mosqueTime) {

                wReligion = .6 + .4 * kibera.random.nextDouble();
                me.attendedReligiousFacility(true);
            }
        }
       
        return wReligion;
    }
    
   /**
     * Determine the agent's current intensity level for socializing with friends
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return go to socialize intensity level
     * 
     */ 
    public static double socializeIntensity(Resident me, Kibera kibera) {
        double wSocialize = 0;
        
        //determine if agent can socialize, must be older than 5, must be home, and must not have already socialized that day
        if (me.getAge() > 5 && kibera.random.nextDouble() < .5 && me.getCurrentGoal() == Goal.Stay_Home && !me.haveSocialized()) { 
            wSocialize = .4 + .6 * kibera.random.nextDouble(); 
        }
        else { 
            wSocialize = 0; 
        }
        
        return wSocialize;
    }
    
    /**
     * Determine the agent's current intensity level for getting water
     * 
     * @param me - the agent
     * @param workTime - true if its time to go to work
     * @param schoolTime - true if its time to go to school
     * @param kibera
     * 
     * @return go to water intensity level
     * 
     */ 
    public static double getWaterIntensity(Resident me, boolean workTime, boolean schoolTime, Kibera kibera) {
        double wWater = 0;
        
        boolean meEmployed = false;
        boolean meStudent = false;
        
        //if household needs water, determine if I should get water
        if (me.getHousehold().needWater(kibera) && me.getAge() > 15 && me.getCurrentGoal() == Goal.Stay_Home) {
            //loop through residents in my household
            Bag householdMembers = me.getHousehold().getHouseholdMembers();
            
            //if someone in my household is already getting water, then don't get water
            for (int i = 0; i < householdMembers.numObjs; i++) {
                Resident r = (Resident) householdMembers.objs[i];
                if (r.getCurrentGoal() == Goal.Get_Water) {
                    return wWater = 0;//stopped
                }           
            }
                                      
            //check if I'm employed or am a student
            if (me.getMyBusinessEmployer() != null || me.getMyHealthFacilityEmployer() != null || me.getMyReligiousFacilityEmployer() != null || me.getMySchoolEmployer() != null
                    || me.getEmploymentOutsideKibera().getEmployees().contains(me)) {
                meEmployed = true;
            }
            if (me.getMySchool() != null) {
                meStudent = true;
            }
            
            if (!meEmployed && !meStudent) {
                return wWater = 1;//stopped
            }
            else {
                for (int i = 0; i < householdMembers.numObjs; i++) {
                    Resident r = (Resident) householdMembers.objs[i];
                    boolean hhEmployed = false;
                    boolean hhStudent = false;

                    if (r.getMyBusinessEmployer() != null || r.getMyHealthFacilityEmployer() != null || r.getMyReligiousFacilityEmployer() != null || r.getMySchoolEmployer() != null
                            || me.getEmploymentOutsideKibera().getEmployees().contains(r)) {
                        hhEmployed = true;
                    }
                    if (r.getMySchool() != null) {
                        hhStudent = true;
                    }
                    //if someone in my household is not employed and is not a student, they can get water instead of me
                    if (!hhEmployed && !hhStudent) {
                        return wWater = 0;//stopped
                    }

                    //if no one is available to get water, I will but only before or after work/school
                    if (meEmployed && !workTime) {
                        return wWater = 1;//stopped
                    }
                    else if (meStudent && !schoolTime) {
                        return wWater = 1;//stopped
                    }
                    else {
                        return wWater = 0;
                    }
                }         
            }
        }
              
        return wWater;
    }
    
    /**
     * Determine the agent's current intensity level for staying home
     * 
     * @param me - the agent
     * @param wWork - intensity for going to work
     * @param wSearch - intensity for searching for employment
     * @param wSchool - intensity for going to school
     * @param wSocialize - intensity for socializing
     * @param wReligion - intensity for attending a religious institution
     * 
     * @return stay home intensity level
     * 
     */
    public static double stayHomeIntensity(Resident me, double wWork, double wSearch, double wSchool, double wSocialize, double wReligion) {
        double wHome = 0;       
        //if I'm searching for employment, but my energy is low and I have sufficient household income, then became inactive and stay home
        if (me.getEnergy() == 0 && me.getCurrentEmploymentStatus() == Employment.Searching && me.getHousehold().getDailyHouseholdDiscrepancy() > 0) {
            me.setCurrentEmploymentStatus(Employment.Inactive);
            wHome = 1;          
        }
        //if my intensity level for performing all other activities is zero, then stay home
        else if (wSchool == 0 && wWork == 0 && wSocialize == 0 & wReligion == 0 && wSearch == 0) { 
            wHome = 1; 
        }
        else { wHome = 0; } 
        
        return wHome;
     
    }
    
    /**
     * Determine the agent's current intensity level for rioting/rebelling
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return rebel/riot intensity level
     * 
     */
    public static double rebelIntensity(Resident me, Kibera kibera) {
        double wRebel = 0;
        
        //run the social influence model to determine whether agent will riot
        wRebel = SocialInfluenceModel.rebelIntensity(me, kibera);  
        
        return wRebel;
    }
    
}

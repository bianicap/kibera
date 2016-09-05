package kibera;

import sim.field.network.Edge;
import sim.field.network.stats.DegreeStatistics;
import sim.util.Bag;

/**
 *
 * Determines whether a resident decide to riot/rebel or remain peaceful.
 * 
 * This is adaptation of Schmidt's (2000) PECS framework, Stets and Burke (2009) Identity Model, and 
 * Friedkin's (2001) structural approach to social influence theory.
 * 
 * 
 * @author bpint
 */
public class SocialInfluenceModel {
    
    /**
     * Determine resident's intensity to riot 
     * 
     * @param me - the agent
     * @param kibera
     * 
     * @return the intensity level to rebel/riot
     * 
     */  
     public static double rebelIntensity(Resident me, Kibera kibera) {
        
        double wRebel = 0;
        
        //if I was a rebel at initialization, remain a rebel
        if (me.isInitialRebel() && kibera.params.global.isRemainRebel() && me.getCStep() <= kibera.params.global.getTimeNewRumor()) {
            wRebel = 1;
            return wRebel;
        }
        
        if (me.isInitialRebel() && kibera.params.global.isRemainRebel() && (me.getCStep() > kibera.params.global.getTimeNewRumor() && kibera.params.global.isRemainRebelAfterNewRumor())) {               
            wRebel = 1;
            return wRebel;
        }
        
        //if I heard the rumor, determine if my aggression has dipped below the aggression threshold
        //if a resident hears the rumor, whether the resident will act on the rumor depends on its final opinion on the issue and its aggression
        if (me.heardRumor() && (!kibera.params.global.isPropogateNewRumor() || kibera.params.global.isContinueToPropogateOriginalRumor() || me.getCStep() <= kibera.params.global.getTimeNewRumor())) {
            //check my energy reservoir and calculate my current aggression value accordingly
            double aggressValue = Energy.calculateAggression(me.getEnergy(), me.getAggressionRate(), me, kibera);				
            //assign current aggression value to resident
            me.setAggressionValue(aggressValue);

            //check if there is anyone in my network that can influence me to rebel
            boolean hasRebelFriend = false;
            
            //check if aggress value is less than aggress threshold
            //if aggress value is less, the resident becomes aggressive		
            if (aggressValue < kibera.params.global.getAggressionThreshold() && me.getAge() >= 5) { // && 
                
                Bag myConnections = new Bag(kibera.socialNetwork.getEdgesOut(me));
                
                if (!myConnections.isEmpty()) {
                    for(int i = 0; i < myConnections.size(); i++) {
                        Edge e = (Edge)(myConnections.get(i));
                        //Get the resident linked to me
                        Resident connection = (Resident) e.getOtherNode(me);
                        
                        if (connection.getCurrentIdentity() == Resident.Identity.Rebel) {
                            hasRebelFriend = true;
                            break;
                        }
                    }
                }
                //if I have at least one friend/connection that is a rebel, check if I am influenced to rebel as well
                //if I don't have any rebel friends, then don't bother checking if I'm influenced to rebel
                if (hasRebelFriend) {
                    
                    wRebel = determineMyInfluencers(me, myConnections, kibera);                                       
                }
            }
        }
        
        if (me.heardNewRumor() && me.getCStep() > kibera.params.global.getTimeNewRumor() && kibera.params.global.isPropogateNewRumor()) {
            if (me.getCurrentIdentity() == Resident.Identity.Rebel) {              
                Bag myConnections = new Bag(kibera.socialNetwork.getEdgesOut(me)); 
                
                if (!myConnections.isEmpty()) {                  
                    wRebel = determineMyInfluencers(me, myConnections, kibera);   
                }
            }
        }
         
        return wRebel;
    }

    /**
     * Determine if the resident is influenced to riot or if the resident will remain peaceful
     * 
     *  According to Friedkin (1999; 2001), y(final) = (I - AW)^-1 * (I - A) * y(1)
     *  where, y(1) = initial opinion on an issue (a measure is structural equivalence - the more alike the networks of residents are, the 
     *  more likely they are to have a similar opinion)
     *  A = person's susceptibility to influence (measure of susceptibility is indegree centrality)
     *  W = relative interpersonal influence, which is based on the communication/interaction network between residents (measure is density -
     *  total actual edge weight between existing connections (with those of same ethnicity) / total possible edge weight between existing connections)
     *  I = the Identity matrix (diagonal of the matrix is 1s, otherwise 0s)
     * 
     * @param me - the agent
     * @param myConnections - the set of all my connections in my social networks
     * @param kibera
     * 
     * @return 1 if influenced to riot, 0 if will stay peaceful
     * 
     */ 
    public static double determineMyInfluencers(Resident me, Bag myConnections, Kibera kibera) {
         
        double isInfluencedtoRebel = 0;
        
        //determine y(t) = opinion on issue at time t (measure is structural equivalence)
        double yt[] = new double[myConnections.size()+1]; //the size of the array is equal to the number of residents I'm connected to plus myself
        double W[] = new double[myConnections.size()+1];
        int totalTies = myConnections.size();
        double myDegreeCentrality = (double) totalTies;
        double meanDegreeCentrality = DegreeStatistics.getMeanInDegree(kibera.socialNetwork);

        double alpha = 0;

        if (!myConnections.isEmpty()) {

            //my similarity to myself
            double mySimilarity = 1;
            yt[0] = mySimilarity;

            double myStudentIdentity = 0;
            double myEmployeeIdentity = 0;
            double myDomesticIdentity = 0;
            double myRebelIdentity = 0;  
            double myEthnicityIdentity = 0;

            for (int i = 0; i < myConnections.size(); i++) {
            
                //Get the resident linked to me
                Edge myEdge = (Edge)(myConnections.get(i));
                Resident myConnection = (Resident) myEdge.getOtherNode(me);
                
                if (myConnection.getCurrentIdentity() == Resident.Identity.Student) {
                    myStudentIdentity = myStudentIdentity + 1;
                }
                else if (myConnection.getCurrentIdentity() == Resident.Identity.Employer) {
                    myEmployeeIdentity = myEmployeeIdentity + 1;
                }
                else if (myConnection.getCurrentIdentity() == Resident.Identity.Domestic_Activities) {
                    myDomesticIdentity = myDomesticIdentity + 1;
                }
                else {
                    myRebelIdentity = myRebelIdentity + 1;
                }

                if (myConnection.getEthnicity().equals(me.getEthnicity())) {
                    myEthnicityIdentity = myEthnicityIdentity + 1;
                }
            }

            //now compare to my connection's connections 
            for (int i = 0; i < myConnections.size(); i++) {

                //Get the resident linked to me
                Edge myEdge = (Edge)(myConnections.get(i));
                Resident myConnection = (Resident) myEdge.getOtherNode(me);
                
                
                Bag othersConnections = new Bag(kibera.socialNetwork.getEdgesOut(myConnection));
               
                double cStudentIdentity = 0;
                double cEmployeeIdentity = 0;
                double cDomesticIdentity = 0;
                double cRebelIdentity = 0;  
                double cEthnicityIdentity = 0;

                for (int j = 0; j < othersConnections.size(); j++) {
                    Edge cEdge = (Edge)(othersConnections.get(j));
                    Resident cConnection = (Resident) cEdge.getOtherNode(myConnection);
                    
                    if (cConnection.getCurrentIdentity() == Resident.Identity.Student) {
                        cStudentIdentity = cStudentIdentity + 1;
                    }
                    else if (cConnection.getCurrentIdentity() == Resident.Identity.Employer) {
                        cEmployeeIdentity = cEmployeeIdentity + 1;
                    }
                    else if (cConnection.getCurrentIdentity() == Resident.Identity.Domestic_Activities) {
                        cDomesticIdentity = cDomesticIdentity + 1;
                    }
                    else {
                        cRebelIdentity = cRebelIdentity + 1;
                    }

                    if (cConnection.getEthnicity().equals(myConnection.getEthnicity())) {
                        cEthnicityIdentity = cEthnicityIdentity + 1;
                    }

                }
                
                //determine similarity between me and my connection
                
                //get number of ties between myself and my connection that are like mine and divide by total number of 
                //ties between myself and connection         
                myStudentIdentity = myStudentIdentity / myConnections.size();
                myEmployeeIdentity = myEmployeeIdentity / myConnections.size();
                myDomesticIdentity = myDomesticIdentity / myConnections.size();
                myRebelIdentity = myRebelIdentity / myConnections.size();

                cStudentIdentity = cStudentIdentity / othersConnections.size();
                cEmployeeIdentity = cEmployeeIdentity / othersConnections.size();
                cDomesticIdentity = cDomesticIdentity / othersConnections.size();
                cRebelIdentity = cRebelIdentity / othersConnections.size();                  

                double schoolSimilarity = 0;
                double employeeSimilarity = 0;
                double domesticSimilarity = 0;
                double rebelSimilarity = 0;
                
                //now that we have the proportion of ties of each identity, compare my proportion against my connection's proportion
                if (Math.max(myStudentIdentity, cStudentIdentity) > 0) { schoolSimilarity = Math.abs(myStudentIdentity - cStudentIdentity) / Math.max(myStudentIdentity, cStudentIdentity); }                   
                if (Math.max(myEmployeeIdentity, cEmployeeIdentity) > 0) { employeeSimilarity = Math.abs(myEmployeeIdentity - cEmployeeIdentity) / Math.max(myEmployeeIdentity, cEmployeeIdentity); }     
                if (Math.max(myDomesticIdentity, cDomesticIdentity) > 0) { domesticSimilarity = Math.abs(myDomesticIdentity - cDomesticIdentity) / Math.max(myDomesticIdentity, cDomesticIdentity); }                    
                if (Math.max(myRebelIdentity, cRebelIdentity) > 0) { rebelSimilarity = Math.abs(myRebelIdentity - cRebelIdentity) / Math.max(myRebelIdentity, cRebelIdentity); }

                schoolSimilarity = 1 - schoolSimilarity;
                employeeSimilarity = 1 - employeeSimilarity;
                domesticSimilarity = 1 - domesticSimilarity;
                rebelSimilarity = 1 - rebelSimilarity;
                
                //equally weight the similarities across the four primary identities
                schoolSimilarity = schoolSimilarity / 4.;
                employeeSimilarity = employeeSimilarity / 4.;
                domesticSimilarity = domesticSimilarity / 4.;
                rebelSimilarity = rebelSimilarity / 4.;
                
                //sum the total similarities across the four identities
                double totalIdentitySimilarity = schoolSimilarity + employeeSimilarity + domesticSimilarity + rebelSimilarity;
                
                //now do the same for the secondary identity, ethnicity
                //compare how similar I am to my connections in terms of the proportion of ties we have to others that
                //share the same ethnicity
                myEthnicityIdentity = myEthnicityIdentity / myConnections.size();
                cEthnicityIdentity = cEthnicityIdentity / othersConnections.size();

                double ethnicSimilarity = 0;

                if (Math.max(myEthnicityIdentity, cEthnicityIdentity) > 0) { ethnicSimilarity = Math.abs(myEthnicityIdentity - cEthnicityIdentity) / Math.max(myEthnicityIdentity, cEthnicityIdentity); }

                ethnicSimilarity = 1 - ethnicSimilarity;
                
                //overall similarity equally weighs our similarities in terms of primary identities and secondary identity
                yt[i+1] = 0.5 * totalIdentitySimilarity + 0.5 * ethnicSimilarity;

                //calculate alpha (my connection's susceptibility to influence) for all my connections
                double degreeCentrality = (double) othersConnections.size();    

                double a = 0;     
                a = Math.exp(-(degreeCentrality - 2*meanDegreeCentrality));
                a = 1 / (1 + a);
                a = 1 - a;
                a = Math.pow(a, .5);

                W[i+1] = a;

                alpha = alpha + a;
            }
        }

        //calculate a=alpha (my susceptiblity to influence)
        double a = 0;     
        a = Math.exp(-(myDegreeCentrality - 2*meanDegreeCentrality));
        a = 1 / (1 + a);
        a = 1 - a;
        a = Math.pow(a, .5);
        
        //calcuate W (relative interpersonal influence)
        W[0] = 1-a;

        //normalize W so that the row sums to 1
        //the sum of Ws (not including myself) is the same as alpha
        double sumW = alpha;

        for(int i = 1; i<W.length; i++) {
           W[i] = (W[i] / sumW) * (1-W[0]);
        }

        //multiply updated W (or V) by yt to get final y
        double y_final = 0;
        for (int i = 0; i < W.length; i++) {
            y_final = y_final + (W[i] * yt[i]);               
        }

        //determine who of my connections my opinion is most similar to
        double[] difference = new double[yt.length];
        for (int i = 0; i < yt.length; i++) {                
            difference[i]=Math.abs(y_final - yt[i]);
        }
        
        for (int i = 1; i < difference.length; i++) {
            //check if my final opinion on the issue is similar enough to my connections opinion
            if (difference[i] <= kibera.params.global.getOpinionThreshold()) {
   
                Edge e = (Edge)(myConnections.get(i-1));
                Resident influencer = (Resident) e.getOtherNode(me);              
                
                //if our opinions are similar enough and if that connection is a rebel, then I will riot
                if (influencer.heardRumor()) {
                    if (influencer.currentIdentity == Resident.Identity.Rebel) {
                        isInfluencedtoRebel = 1;
                    }
                }
                //if our opinions are similar enough but that connection is not a rebel, then remain peaceful
                if (influencer.heardNewRumor()) {
                    if (influencer.currentIdentity != Resident.Identity.Rebel) {
                        isInfluencedtoRebel = 0;
                    }
                }
            }
        }

        return isInfluencedtoRebel;                                                                                                                         
    }
	   
}

package kibera;

import kibera.Resident.Employment;
import ec.util.MersenneTwisterFast;

/**
 * If a resident is employed, this calculates the resident's income using the Lorenz curve.
 * 
 * @author bpint
 *
 */
public class WealthDistribution {
    public static double determineIncome(Employment employmentStatus, Kibera kibera) {
        MersenneTwisterFast random = new MersenneTwisterFast();	// when using MASON, this is already in SimState
       
        double totalWealth = 14520; //the highest paid person in Kenyan shillings
        
        double income = 0;
        double x = 0;

        if (employmentStatus == Employment.Informal) {
            x = random.nextDouble() * kibera.getInformalityIndex();
        }
        else if (employmentStatus == Employment.Formal){
            x = kibera.getInformalityIndex() + (random.nextDouble() * (1 - kibera.getInformalityIndex()));
        }

        else { //the resident is either inactive or searching for employment
            x = 0;
        }

        double y = 1.7148 * (Math.pow(x, 3)) - 1.0446 * (Math.pow(x, 2)) + 0.3259 * x; //lorenz curve fit
        income = totalWealth * y;
        
        return income;	//this is the monthly salary of a resident			
    }
}
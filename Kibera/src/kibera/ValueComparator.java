package kibera;

import java.util.Comparator;
import java.util.Map;

/**
 * The Value Comparator object is used to compare how likely a resident is to socialize with a particular friend
 * 
 * @author bpint
 *
 */
public class ValueComparator implements Comparator<Resident> {
	
    Map<Resident, Double> base;
    public ValueComparator(Map<Resident, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(Resident a, Resident b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
	 
}

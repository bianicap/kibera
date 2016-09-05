
package kibera;

import kibera.Resident.EmploymentTypeOutsideKibera;
import sim.util.Bag;

/**
 * Tracks the resident's working outside of kibera in the formal and informal sectors
 * 
 * @author bpint
 */
public class OutsideKibera {
    
    /** The employees working outside of Kibera */
    private Bag employees;
    public Bag getEmployees() { return employees; }
    public void addEmployee(Resident val) { employees.add(val); }
    public void removeEmployee(Resident val) { employees.remove(val); }

    /** All other residents */
    private Bag nonEmployees;
    public Bag getNonEmployees() { return nonEmployees; }
    public void addNonEmployee(Resident val) { nonEmployees.add(val); }
    public void removeNonEmployee(Resident val) { nonEmployees.remove(val); }

    /** Identifies whether the business is formal or informal */
    public enum BusinessType { formal, informal };
    OutsideKibera.BusinessType businessType;
    public OutsideKibera.BusinessType getBusienssType() { return businessType; }
    public void setBusinessType(OutsideKibera.BusinessType val) { businessType = val; }

    public OutsideKibera() {		
        employees = new Bag();
        nonEmployees = new Bag();
    }

    //determines if capacity has been reached
    public boolean isEmployeeCapacityReached(BusinessType businessType, Kibera kibera) {
        int numFormalEmployees = 0;
        int numInformalEmployees = 0;
        
        for(int i = 0; i < employees.size(); i++) {
            Resident r = (Resident) employees.get(i);
            
            //count the number of employees that are formal and informal
            if (r.getEmploymentTypeOutsideKibera() == EmploymentTypeOutsideKibera.Formal) {
                numFormalEmployees++;
            }
            
            else {
                numInformalEmployees++;
            }
        }
        
        //determine if capacity is reached for formal employment outside kibera
        if (businessType == BusinessType.formal) {
            if (numFormalEmployees == kibera.params.global.getFormalOutsideKiberaCapacity()) { return true; }
            else { return false; }
        }
        
        //determine if capacity is reached for informal employment outside kibera
        else {
            if (numInformalEmployees == kibera.params.global.getInformalOutsideKiberaCapacity()) { return true; }
            else { return false; }
        }
    }
    
}

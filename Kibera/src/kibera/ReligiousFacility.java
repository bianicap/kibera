package kibera;

import sim.util.Bag;

/**
 * The Religious Facility object are one type of formal employers available in Kibera
 * 
 * @author bpint
 *
 */
public class ReligiousFacility {
    
    /** The structure a facility is located within*/
    private Structure structure;
    public Structure getStructure() { return structure; }
    public void setStructure(Structure val) { structure = val; }

    /** The type of religious facility; 1 = christian, 2 = muslim, 3 = unknown */
    private int religiousFacilityType;
    public int getFacilityType() { return religiousFacilityType; }
    public void setFacilityType(int val) { religiousFacilityType = val; }

    /** The employees working at the religious facility */
    private Bag employees;
    public Bag getEmployees() { return employees; }
    public void addEmployee(Resident val) { employees.add(val); }
    public void removeEmployee(Resident val) { employees.remove(val); }
    
    /** The residents that attend the religious facility */
    private Bag attendees;
    public Bag getAttendees() { return attendees; }
    public void addAttendee(Resident val) { attendees.add(val); }
    public void removeAttendee(Resident val) { attendees.remove(val); }

    /** The maximum number of employees at the school */
    private int employeeCapacity;
    public int getEmployeeCapacity() { return employeeCapacity; }
    public void setEmployeeCapacity(int val) { employeeCapacity = val; }

    /** Identifies whether the business is formal or informal */
    public enum BusinessType { formal, informal };
    BusinessType businessType;
    public BusinessType getBusienssType() { return businessType; }
    public void setBusinessType(BusinessType val) { businessType = val; }
	
    public ReligiousFacility(Structure s, int religiousFacilityType) {
        this.structure = s;
        this.religiousFacilityType = religiousFacilityType;

        employees = new Bag();
        attendees = new Bag();
    }

    //determine if capacity has been reached
    public boolean isEmployeeCapacityReached() {
        int numEmployees = employees.size();

        if (numEmployees == getEmployeeCapacity()) {
            return true;
        }
        else { return false; }
    }
	
}

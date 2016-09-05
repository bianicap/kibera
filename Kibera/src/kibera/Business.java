package kibera;

import sim.util.Bag;

/**
 * The Business object are the informal employers available in Kibera
 * 
 * @author bpint
 *
 */
public class Business {	
 
    /** The employees working at the business */
    private Bag employees;
    public Bag getEmployees() { return employees; }
    public void addEmployee(Resident val) { employees.add(val); }
    public void removeEmployee(Resident val) { employees.remove(val); }

    /** The structure the business is located in */
    private Structure structure;
    public Structure getStructure() { return structure; }
    public void setStructure(Structure val) { structure = val; }

    /** The capacity of employees working at the business */
    private int employeeCapacity;
    public double getEmployeeCapacity() { return employeeCapacity; }
    public void setEmployeeCapacity(int val) { employeeCapacity = val; }

    public Business(Structure s) {
        employees = new Bag();
    }

    //each business has a capacity for the number of employees it can hire
    //determine if capacity been reached
    public boolean isEmployeeCapacityReached() {
        int numEmployees = employees.size();
        if (numEmployees == getEmployeeCapacity()) {
            return true;
        }
        else { return false; }
    }
}

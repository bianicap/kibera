package kibera;

import sim.util.Bag;

/**
 * The School class object assign students to a class within a school for purposes of interaction and social networks
 * (so that student's aren't assumed to interact with all students at an entire school)
 * 
 * @author bpint
 *
 */
public class SchoolClass {
    /** The School the class is in */
    private School school;
    public School getSchool() { return school; }
    public void setSchool(School val) { school = val; }
    
    /** The set of students in the same class */
    private Bag classmates;
    public Bag getClassmates() { return classmates; }
    public void setClassmates(Bag val) { classmates = val; }
    public void addClassmate(Resident val) { classmates.add(val); }
    public void removeClassmate(Resident val) {classmates.remove(val); }
    
    public SchoolClass(School s) {
        this.school = s;
        
        classmates = new Bag();
    }
    
}

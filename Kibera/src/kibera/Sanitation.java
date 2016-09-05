package kibera;

/**
 * The Sanitation object provides location of sanitation
 * Residents currently do not use sanitation points as its beyond the scope of the model. However, it was kept
 * as a placeholder.
 * 
 * @author bpint
 *
 */

public class Sanitation {

    /** The parcel the sanitation location resides on */
    private Parcel sanitationLocation;
    public Parcel getParcel() { return sanitationLocation; }
    public void setParcel(Parcel val) { sanitationLocation = val; }

    public Sanitation(Parcel p) {
        sanitationLocation = p;
    }
	
}

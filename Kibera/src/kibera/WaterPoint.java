package kibera;

/**
 * The Water Point object provides the location of water points (where residents can purchase barrel's of water)
 * 
 * @author bpint
 *
 */
public class WaterPoint {
    /** The parcel a water point resides on */
    private Parcel waterPointLocation;
    public Parcel getParcel() { return waterPointLocation; }
    public void setParcel(Parcel val) { waterPointLocation = val; }

    public WaterPoint(Parcel p) {
        waterPointLocation = p;
    }

}

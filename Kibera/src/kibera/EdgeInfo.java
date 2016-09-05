package kibera;

/**
 * The value of the link between residents. This value (which defines the strength of the 
 * relationship between two residents) can be incremented depending on the
 * interactions of the residents as the simulation runs
 *
 * @author bpint
 *
 */
public class EdgeInfo implements java.io.Serializable {
	
    //update the value of the edge betwen two nodes/agents
    double edgeValue;
    public double getEdgeValue() { return edgeValue; }

    public void setEdgeValue(double val) { 
        if ((edgeValue + val) < 0) {
            edgeValue = 0;
        }
        else {
            edgeValue = edgeValue + val; 
        }
    }

    public EdgeInfo(Double val) { 
        setEdgeValue(val); 
    }
}

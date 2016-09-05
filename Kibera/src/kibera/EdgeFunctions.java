package kibera;

import sim.field.network.Edge;
import sim.util.Bag;

/**
 * The Edge Functions checks if an edge exists between two nodes and can return the edge between the nodes
 * Nodes here are the agents
 * 
 * @author bpint
 *
 */
public class EdgeFunctions {
    
    /**
     * Determine if an edge exists between two nodes
     * 
     * @param node1 - the first agent
     * @param node2 - the second agent
     * @param kibera
     * 
     * @return true if an edge exists between two nodes, false otherwise
     * 
     */    
    public static boolean doesEdgeExist(Resident node1, Resident node2, Kibera kibera) {
        Bag myConnections = new Bag(kibera.socialNetwork.getEdgesOut(node1));
        
        //check if an edge exists
        if (myConnections != null) {
            for (int i = 0; i < myConnections.size(); i++) {
                Edge e = (Edge)(myConnections.get(i));
                Resident otherNode = (Resident) e.getOtherNode(node1);
                if (otherNode == node2) {
                        return true;
                }
            }
        }
        return false;

    }
    
    /**
     * Get the edge between two nodes
     * 
     * @param node1 - the first agent
     * @param node2 - the second agent
     * @param kibera
     * 
     * @return the edge between the agents
     * 
     */
    public static Edge getEdge(Resident node1, Resident node2, Kibera kibera) {
        Bag myConnections = new Bag(kibera.socialNetwork.getEdgesOut(node1));
        Edge myEdge = null;
        
        //find the edge between the nodes
        if (myConnections != null) {
            for (int i = 0; i < myConnections.size(); i++) {
                Edge e = (Edge)(myConnections.get(i));
                Resident otherNode = (Resident) e.getOtherNode(node1);
                if (otherNode == node2) {
                    return e;
                }
            }
        }

        return null;
    }
}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by nandofioretto on 11/10/16.
 */
public class Topology {

    private double densityKm2;
    private double gridSideMt;
    private double actuatorRadiusMt;
    private int numAgents;
    private int numClusters;
    private int agentsPerCluster;

    ArrayList<ArrayList<String>> clusters = new ArrayList<>();
    Map<String, Integer> mapToCluster = new HashMap<>();

    Map<String, ArrayList<String>> neighbors = new HashMap();
    public Topology(double densityKm2, double gridSideMt, double actuatorRadiusMt) {
        this.densityKm2 = densityKm2;
        this.gridSideMt = gridSideMt;
        this.actuatorRadiusMt = actuatorRadiusMt;

        generate();
    }
    
    //Only used for regenerating from CSV's
    public Topology(int numAgents, int numClusters) {
        this.numAgents   = numAgents;
        this.numClusters = numClusters;
        System.out.println("NUM OF AGENTS IN TOPO: " + numAgents);
        agentsPerCluster = (int) Math.ceil(numAgents / (double) numClusters);

        for (int cId = 0; cId < numClusters; cId++) {
            clusters.add(new ArrayList<>());
        }

        for(int aId = 1, cId = 0; aId <= numAgents; aId++, cId++) {
            if(cId == numClusters) cId = 0;
            String agtName = "h" + aId;
            (clusters.get(cId)).add(agtName);
            mapToCluster.put(agtName, cId);
        }

        for (int aId = 1; aId <= numAgents; aId++) {
            String agtName = "h" + aId;
            int cId = mapToCluster.get(agtName);
            neighbors.put(agtName, new ArrayList<>(clusters.get(cId)) );
        }

        for (int cId = 0; cId < numClusters-1; cId++) {
            String this_c = Utilities.genRand(clusters.get(cId ));
            String next_c = Utilities.genRand(clusters.get(cId + 1));
            neighbors.get(this_c).add(next_c);
            neighbors.get(next_c).add(this_c);
        }
    }
    
    private void generate() {

        // Compute number of clusters and number of agents per cluster
        numAgents = (int)((densityKm2 * gridSideMt) / 1000);
        numClusters = (int) Math.ceil(gridSideMt / actuatorRadiusMt);
        agentsPerCluster = (int) Math.ceil(numAgents / (double) numClusters);

        for (int cId = 0; cId < numClusters; cId++) {
            clusters.add(new ArrayList<>());
        }

        // --- Had to modify this because it broke on some edge cases (ex. 21 agents, 8 clusters --- this would cause last cluster to have no agents in it) this way they are distributed more fairly too
        for(int aId = 1, cId = 0; aId <= numAgents; aId++, cId++) {
            if(cId == numClusters) cId = 0;
            String agtName = "h" + aId;
            (clusters.get(cId)).add(agtName);
            mapToCluster.put(agtName, cId);
        }

        // Save clusters of agents
        /* ^^^^^REPLACED BY ABOVE^^^^^
        for (int aId = 1, cId = 0; aId <= numAgents; aId++)
        {
            String agtName = "home_" + aId;
            (clusters.get(cId)).add(agtName);
            mapToCluster.put(agtName, cId);

            if (aId % agentsPerCluster == 0) {
                cId++;
            }
        }
        */

        for (int aId = 1; aId <= numAgents; aId++) {
            String agtName = "h" + aId;
            int cId = mapToCluster.get(agtName);
            neighbors.put(agtName, new ArrayList<>(clusters.get(cId)) );
        }

        // Merge Agents in separate clusters:
        for (int cId = 0; cId < numClusters-1; cId++) {
            String this_c = Utilities.genRand(clusters.get(cId ));
            String next_c = Utilities.genRand(clusters.get(cId + 1));
            neighbors.get(this_c).add(next_c);
            neighbors.get(next_c).add(this_c);
        }
    }

    public int numAgents( ) {
        return numAgents;
    }

    public Set<String> getAgents() {
        return neighbors.keySet();
    }

    public int getNumAgents() {
        return numAgents;
    }

    public int getNumClusters() {
        return numClusters;
    }

    public int getAgentsPerCluster() {
        return agentsPerCluster;
    }

    ArrayList<String> getNeighbors(String agtName) {
        return neighbors.get(agtName);
    }

}

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SHDS {
    public static void main(String[] args) {
        int dev = 3; //number of devices to generate
        /**
        * Generating a topology:
        * Topology generateTopology(int cID, double gridLength, double clusters){ ... }
        *
        * By knowing the city and size of grid, we can determine how many agents there should be in the problem
        * int cID ---------- City identifier. We have 3 cities here:
        *      0) Des Moines
        *      1) Boston
        *      2) San Francisco
        * int clusters ----- Number of clusters in the problem (each cluster is a fully connected graph of houses, a neighborhood).
        *                    Each cluster is connected to one other cluster, connecting the whole graph together.
        * int gridLength --- Chunk of the city covered by the problem, this is in meters^2
        **/
        System.out.println(args[0]);
        JSONObject parameters = new JSONObject();
        try {
            String content = Utilities.readFile(args[0]);
            parameters = new JSONObject(content.trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(parameters.toString());
        System.out.println(
            "time_span: " + parameters.getInt("time_span") + "\n" +
            "time_granularity: " + parameters.getInt("time_granularity") + "\n" +
            //"house_ratio: " + parameters.getInt("time_span") + "\n" +
            "num_devices: " + parameters.getInt("num_devices") + "\n" +
            "gridLength: " + parameters.getInt("gridLength") + "\n" +
            "clusterDiv: " + parameters.getInt("clusterDiv") + "\n"
            );
        
        
        Topology topo = generateTopology(
            parameters.getInt("city"),
            parameters.getInt("gridLength"),
            parameters.getInt("clusterDiv")
        );
        /**File name*/
        String fileName = "instance_BO"
                + "_a" + topo.getNumAgents()
                + "_c" + topo.getNumClusters();// + ".json";
                
        System.out.println(fileName);

        /**
         * Generating the file
         * void generateSHDSInstances(String fileName, int nDevices, Topology topo){ ... }
         *
         * Takes in: 1) file name, 2) number of devices, and 3) topology of the problem
         **/
        //generateSHDSInstances(fileName, parameters.getInt("num_devices"), topo, parameters.getInt("time_span"), parameters.getInt("time_granularity"));
        generateExtras(parameters.getInt("time_span"), parameters.getInt("time_granularity"));
        //generateDatasets(parameters.getInt("time_span"), parameters.getInt("time_granularity"));
    }




    public static void generateDatasets(int time_span, int time_granularity) {
        /**************************************************
         *
         * Below is an example of how to generate data sets
         *
         **************************************************/

        String[] city = {
                "DM", //Des Moines
                "BO", //Boston
                "SF"  //San Francisco
        };

        double[] gridLength = { //relates (mostly) to number of agents
                10,
                30,
                50,
                100,
                350,
                500,
                850,
                1000,
                1500,
                2000,
        };

        double[] clusterDiv = { //multiply grid length by this and plug into radius in order for this to be number of clusters
                1,
                2,
                4,
                8,
                16,
                32,
                64,
                128,
                256,
                512,
                1024
        };

        /*
        int[] numDev = {
                2, 3, 4 ,5 ,6, 7, 8, 9, 10 ,11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };*/

        final int MIN_DEV = 2; final int MAX_DEV = 20; //minimum and maximum amount of devices per house to generate files for
        final int NUM_GRID_SIZES = 5; //number of different grid sizes to generate from the list above, some of the larger problems might be to big for some computers to handle
        for(int i = 0; i < city.length; i++) { // for each city
            for(int j = 0; j < NUM_GRID_SIZES; j++) { //for each grid size

                Topology topo = generateTopology(i, gridLength[j], 1);
                for (int k = 2; topo != null && k < clusterDiv.length; k++) {

                    topo = generateTopology(i, gridLength[j], clusterDiv[k]);
                    if (topo == null) continue; // if num_agents < agents_per_cluster then stop

                    for(int d = MIN_DEV; d <= MAX_DEV; d++) {
                        String fileName = "datasets/instance_" + city[i]
                                + "_a" + topo.getNumAgents()
                                + "_c" + topo.getNumClusters()
                                + "_d" + d;
                        generateSHDSInstances(fileName, d, topo, time_span, time_granularity);
                    }
                }
            }
        }
    }

    public static void generateExtras(int time_span, int time_granularity) {

        String[] city = {
                "DM", //Des Moines
                "BO", //Boston
                "SF"  //San Francisco
        };

        double[] gridLength = { //relates (mostly) to number of agents
                10,
                30,
                50,
                100,
                350,
                500,
                850,
                1000,
                1500,
                2000,
        };

        double[] clusterDiv = { //multiply grid length by this and plug into radius in order for this to be number of clusters
                1,
                4,
                10,
                20,
                100
        };

        
        int[] numDev = {
                2,
                5,
                10,
                15,
                20
        };
        
        for(int i = 0; i < city.length; i++) { // for each city
            for(int j = 0; j < gridLength.length; j++) { //for each grid size
            
                Topology topo = generateTopology(i, gridLength[j], 1);
                for (int k = 2; topo != null && k < clusterDiv.length; k++) {
                
                    topo = generateTopology(i, gridLength[j], clusterDiv[k]);
                    if (topo == null) continue; // if num_agents < agents_per_cluster then stop

                    for(int d = 0; d < numDev.length; d++) {
                        String fileName = "datasets/instance_" + city[i]
                                + "_a" + topo.getNumAgents()
                                + "_c" + topo.getNumClusters()
                                + "_d" + numDev[d];
                        generateSHDSInstances(fileName, numDev[d], topo, time_span, time_granularity);
                    }
                }
            }
        }
    }





    /**
     * By knowing the city and size of grid, we can determine how many agents there should be in the problem
     * int cID ---------- City identifier. We have 3 cities here:
     *      0) Des Moines
     *      1) Boston
     *      2) San Francisco
     * int clusters ----- Number of clusters in the problem (each cluster is a fully connected graph of houses, a neighborhood).
     *                    Each cluster is connected to one other cluster, connecting the whole graph together.
     * int gridLength --- Chunk of the city covered by the problem, this is in meters^2
     **/
    public static Topology generateTopology(int cID, double gridLength, double clusters) {
        double[] densityCity = {
                 718, // 0 --- Des Moines
                1357, // 1 --- Boston
                3766  // 2 --- San Francisco
        };
        if(clusters > (densityCity[cID] * gridLength)/1000) { // This means that there are more clusters than agents which is impossible
            return null;
        }
        return new Topology(densityCity[cID], gridLength, gridLength/clusters);
    }


    //TODO add a comment here
    public static void generateSHDSInstances(String fileName, int nDevices, Topology topo, int span, int granularity) {
        JSONArray devices = convertDevices(readDevices(), granularity);
        RuleGenerator ruleGen = new RuleGenerator(span, granularity, devices);
        Generator gen = new Generator(topo, ruleGen, nDevices, new int[]{1, 1, 1});

        try {
            FileWriter fileOut = new FileWriter(fileName+".json");
            fileOut.write(gen.generate(fileName + "_CSV.txt").toString());
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONArray readDevices() {
        try {
            String content = Utilities.readFile(Parameters.getDeviceDictionaryPath());

            JSONArray jArray = new JSONArray(content.trim());
            return jArray;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONArray convertDevices(JSONArray devices, int granularity) {
        for(int i = 0; i < devices.length(); i++) {
            Iterator<?> d_keys = devices.getJSONObject(i).keys();
            while(d_keys.hasNext()) {
                String d_name = (String) d_keys.next();
                JSONObject dev = devices.getJSONObject(i).getJSONObject(d_name);
                if(dev.getString("type").equals("actuator")) {
                    JSONObject actions = dev.getJSONObject("actions");
                    Iterator<?> a_keys = actions.keys();
                    while(a_keys.hasNext()) {
                        String a_name = (String)a_keys.next();
                        JSONObject a = actions.getJSONObject(a_name);
                        JSONArray effects = a.getJSONArray("effects");
                        for(int j = 0; j < effects.length(); j++) {
                            JSONObject e = effects.getJSONObject(j);
                            e.put("delta", e.getDouble("delta") / 60.0 * granularity);
                            effects.put(j, e);
                        }
                        a.put("power_consumed", a.getDouble("power_consumed") / 60.0 * granularity);
                        a.put("effects", effects);
                        actions.put(a_name, a);
                    }
                    dev.put("actions", actions);
                }
                devices.put(i, devices.getJSONObject(i).put(d_name, dev));
            }
        }

        return devices;
    }

}

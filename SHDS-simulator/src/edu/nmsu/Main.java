package edu.nmsu;

import edu.nmsu.problem.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        /**
        * What do I need for home specs / model specs?
        * distribution of small, medium, and large houses
        * --- need to create a different device dictionary for each
        * --- determine how to show distribution; maybe a ratio? 1 to 1 to 1 would be 33% of each
        * --- price schema, not sure how to represent this when there are a ton of time steps
        **/
        /** TODO HIGH PRIORITY:
         *  Make everything in minutes and convert at the generation step based on granularity AND time span (giving total time steps)
         *  Device power can still be in kWh, just have to remember that! (it's what we used in the paper)
         *      HOWEVER device delta effect must change depending on the time granularity (also perhaps I need to look at how time span for a schedule when generating rules too if I have time to implement those checks)
         *      Deltas need to change to be consistent with paper (examples: washer affects state property 6's delta by '1' per minute, roomba affects 2 by 0.676% per min).
         *          - Device deltas
         *          - Goal states for active rules / constraints(passive rules)
         *      Rule generator needs to know device dictionary AND whether its for a small, medium, or large house
         *      Have to change temperature_cool to be positive to work with my new rule generator or put an exception for it...
         *  QUESTIONS:
         *      Aamir:
         *          What is the name of the new water heater? --- Right now the water heater is just called "water_tank".
         *      Nando:
         *          Should I change the names of the sensor properties in the generator to reflect the names in the paper?
         *          If yes, should I change name in paper to be "dish_cleanliness" etc.
         **/

        int dev = 2; //number of devices to generate
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
        Topology topo = generateTopology(0, 100, 1);
        /**File name*/
        String fileName = "instance_BO"
                + "_a" + topo.getNumAgents()
                + "_c" + topo.getNumClusters()
                + "_d" + dev;// + ".json";

        /**
         * Generating the file
         * void generateSHDSInstances(String fileName, int nDevices, Topology topo){ ... }
         *
         * Takes in: 1) file name, 2) number of devices, and 3) topology of the problem
         **/
        //generateSHDSInstances(fileName, dev, topo, 12, 60);

        //RuleParser rp = new RuleParser();
        //System.out.println(rp.readFile("ruleCSV.txt").toString(2));
        //To generate data sets of the same size as the ones used in the tables from the paper uncomment this:
        generateDatasets();
    }




    public static void generateDatasets() {
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
                3000,
                5000
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

        final int MIN_DEV = 3; final int MAX_DEV = 6; //minimum and maximum amount of devices per house to generate files for
        final int NUM_GRID_SIZES = 5; //number of different grid sizes to generate from the list above, some of the larger problems might be to big for some computers to handle
        int time_span = 12; //TODO comment these
        int time_granularity = 60;
        for(int i = 0; i < city.length; i++) {
            for(int j = 0; j < NUM_GRID_SIZES; j++) {
                Topology temp = generateTopology(i, gridLength[j], 1);
                /*
                for (int d = MIN_DEV; d <= MAX_DEV; d++) {
                    String fileName = "resources/data/instance_" + city[i]
                            + "_a" + temp.getNumAgents()
                            + "_c" + temp.getNumClusters()
                            + "_d" + d;// + ".json";
                    //System.out.println(fileName);
                    generateSHDSInstances(fileName, d, temp, 12, 60); //TODO put time_span and time_granularity variables here
                }
                for (int k = 0; k < clusterDiv.length; k++) {
                    Topology temp2 = generateTopology(i, gridLength[j], clusterDiv[k]);
                    if (temp2 == null) continue; // if num_agents < agents_per_cluster then stop
                    temp = temp2;
                }

                String fileName = "resources/data/instance_" + city[i]
                        + "_a" + temp.getNumAgents()
                        + "_c" + temp.getNumClusters()
                        + "_d" + MAX_DEV;// + ".json";
                //System.out.println(fileName);
                generateSHDSInstances(fileName, MAX_DEV, temp, 12, 60);*/

                String fileName = "resources/inputs/instance_" + city[i]
                        + "_a" + temp.getNumAgents()
                        + "_c" + temp.getNumClusters();// + ".json";
                generateSHDSInstances(fileName, MAX_DEV, temp, 12, 60);
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
        JSONObject exp = gen.generate();


        ArrayList<Integer> temp_1 = new ArrayList<>();
        for(int i = 3; i <= 6; i++) temp_1.add(i);
        ArrayList<Integer> temp_2 = new ArrayList<>();
        temp_2.add(1); temp_2.add(7);
        ArrayList<Integer> temp_3 = new ArrayList<>();
        temp_3.add(2); temp_3.add(8);

        try {
            FileWriter fileOut = new FileWriter(fileName+"_1.json");
            fileOut.write(gen.generate(temp_1, fileName + "_CSV_1.txt").toString());
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter fileOut = new FileWriter(fileName+"_2.json");
            fileOut.write(gen.generate(temp_2, fileName + "_CSV_2.txt").toString());
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter fileOut = new FileWriter(fileName+"_3.json");
            fileOut.write(gen.generate(temp_3, fileName + "_CSV_3.txt").toString());
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

/*
    public static void generateSHDSInstances(String fileName, int nDevices, Topology topo, int span, int granularity) {
        JSONArray devices = convertDevices(readDevices(), granularity);
        RuleGenerator ruleGen = new RuleGenerator(span, granularity, devices);
        Generator gen = new Generator(topo, ruleGen, nDevices, new int[]{1, 1, 1});
        JSONObject exp = gen.generate();

        try {
            FileWriter fileOut = new FileWriter(fileName);
            fileOut.write(exp.toString());
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
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

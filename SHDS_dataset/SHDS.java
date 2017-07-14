import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SHDS {
    public static void main(String[] args) {
        String[] city = {
                "DM", //Des Moines
                "BO", //Boston
                "SF"  //San Francisco
        };

        /*
         Generating a topology:
         Topology generateTopology(int cID, double gridLength, double clusters){ ... }

         By knowing the city and size of grid, we can determine how many agents there should be in the problem
         int cID ---------- City identifier. We have 3 cities here:
              0) Des Moines
              1) Boston
              2) San Francisco
         int clusters ----- Number of clusters in the problem (each cluster is a fully connected graph of houses, a neighborhood).
                            Each cluster is connected to one other cluster, connecting the whole graph together.
         int gridLength --- Chunk of the city covered by the problem, this is in meters^2
        */
        JSONObject settings = new JSONObject();
        try {
            String content = Utilities.readFile(Parameters.getSettingsPath());
            settings = new JSONObject(content.trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(args.length == 0) {
            System.out.println("No arguments found. Please re-run with '-help' argument for more information.");
            return;
        }
        Topology topo;
        switch(args[0]) {
            case "-datasets":
                generateDatasets(settings.getInt("time_span"), settings.getInt("time_granularity"));
            break;
            case "-extras" :
                generateExtras(settings.getInt("time_span"), settings.getInt("time_granularity"));
            break;
            case "-generate":
                topo = generateTopology(
                    Integer.parseInt(args[1]),
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3])
                    );
                String fileName = "datasets/instance_"
                    + city[Integer.parseInt(args[1])]
                    + "_a" + topo.getNumAgents()
                    + "_c" + topo.getNumClusters();
                generateSHDSInstances(fileName, Integer.parseInt(args[4]), topo, settings.getInt("time_span"), settings.getInt("time_granularity"));
            break;
            case "-test": 
                if (args.length == 5) {
                    for (int m = 0; m < Integer.parseInt(args[4]); m++) {
                        fileName = "datasets/ins_" + args[1] + "_" + m;
                        generateSHDSTest(fileName, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                    }
                } else {
                    fileName = "datasets/instance_r" + args[1] + "_s" + args[2] + "_g" + args[3];
                    generateSHDSTest(fileName, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                }
      
            break;
            case "-tests": 
                if (args.length == 4) {
                    for (int m = 1; m <= 8; m++) {
                        for (int n = 0; n < Integer.parseInt(args[3]); n++) {
                            fileName = "datasets/ins_" + m + "_" + n;
                            generateSHDSTest(fileName, m, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                        }
                    }
                } else {
                    for (int m = 1; m <= 8; m++) {
                        fileName = "datasets/instance_r" + m + "_s" + args[1] + "_g" + args[2];
                        generateSHDSTest(fileName, m, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                    }
                }
            break;
            case "-regenerate":
                String temp = args[1].replaceAll("c", "").replaceAll("a", "");
                String[] CSVFile = temp.split("_");
                //System.out.println("AGENTS: "+topo.getNumAgents());
                regenDataset(args[1].replaceAll("_CSV.txt", ""), settings.getInt("time_span"), settings.getInt("time_granularity"));
            break;
            case "-genAgents":
                topo = new Topology(Integer.parseInt(args[4]), Integer.parseInt(args[4]));
                int[] houseRatio;
                if(args[1].equals("all")) { //if user types all instead of a rule_id, generate every possible combination of rule_id and house_ratio
                    for(int r = 1; r <= 8; r++) {
                        houseRatio = new int[]{1, 0, 0};
                        fileName = "datasets/ins_" + r + "_" + args[2] + "_" + args[3] + "_a" + args[4] + "_h" + 0;
                        generateSHDSTest(fileName, r, topo, Integer.parseInt(args[2]), Integer.parseInt(args[3]), houseRatio);
                        houseRatio = new int[]{0, 1, 0};
                        fileName = "datasets/ins_" + r + "_" + args[2] + "_" + args[3] + "_a" + args[4] + "_h" + 1;
                        generateSHDSTest(fileName, r, topo, Integer.parseInt(args[2]), Integer.parseInt(args[3]), houseRatio);
                        houseRatio = new int[]{0, 0, 1};
                        fileName = "datasets/ins_" + r + "_" + args[2] + "_" + args[3] + "_a" + args[4] + "_h" + 2;
                        generateSHDSTest(fileName, r, topo, Integer.parseInt(args[2]), Integer.parseInt(args[3]), houseRatio);
                    }
                } else {
                    fileName = "datasets/ins_" + args[1] + "_" + args[2] + "_" + args[3] + "_a" + args[4] + "_h" + args[5];

                    switch(args[5]){
                        case "0":
                        case "small":
                            houseRatio = new int[]{1, 0 ,0};
                        break;
                        case "1":
                        case "medium":
                            houseRatio = new int[]{0, 1 ,0};
                        break;
                        case "2":
                        case "large":
                            houseRatio = new int[]{0, 0 ,1};
                        break;
                        default:
                            houseRatio = new int[]{0, 1, 0};
                    }
                    generateSHDSTest(fileName, Integer.parseInt(args[1]), topo, Integer.parseInt(args[2]), Integer.parseInt(args[3]), houseRatio);
                }
            break;
            case "-help":
                System.out.println(
                    "-datasets\n\t" +
                            "generates datasets with same settings used in paper.\n" +
                    "-extras\n\t" +
                            "generates extra datasets mentioned in paper.\n" +
                    "-generate <cityID> <gridLength> <clusterDiv> <numDevices>\n\t" +
                            "generates a custom dataset with extra arguments as settings.\n" +
                    "-genAgents <rule_id> <time_span> <time_granularity> <num_agents> <house_size>\n\t" +
                            "generate a dataset of a single active rule for <num_agents> agents of given house size.\n" +
                    "-regenerate <fileName>\n\t" +
                            "regenerates a dataset from a CSV file (provided from generation)\n" +
                    "-test <rule_id> <time_span> <time_granularity> <OPTIONAL:num_files>\n\t" +
                            "generates a dataset with 1 house and 1 rule (with the given ruleID)\n" +
                    "-tests <time_span> <time_granularity> <OPTIONAL:num_files>\n\t" +
                            "generates a dataset with 1 house FOR EACH rule\n");
            break;
        }
        
        /*
          Generating the file
          void generateSHDSInstances(String fileName, int nDevices, Topology topo){ ... }

          Takes in: 1) file name, 2) number of devices, and 3) topology of the problem
         */

    }
    
    
    
    
    
    
    private static void generateDatasets(int time_span, int time_granularity) {
        /*

         Below is an example of how to generate data sets

         */

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

    private static void generateExtras(int time_span, int time_granularity) {

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
    private static Topology generateTopology(int cID, double gridLength, double clusters) {
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

    private static void generateSHDSTest(String fileName, int rule_id, int span, int gran) {
        JSONArray devices = convertDevices(readDevices(), gran);
        RuleGenerator ruleGen = new RuleGenerator(span, gran, devices);
        Generator gen = new Generator(new Topology(1, 1), ruleGen, 1, new int[] { 1, 1, 1 });
        ArrayList rList = new ArrayList();
        rList.add(Integer.valueOf(rule_id));
        try {
            FileWriter fileOut = new FileWriter(fileName + ".json");
            fileOut.write(gen.generate("false", rList).toString(2));
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // used for num_agents test with a specific rule id and house ratio
    public static void generateSHDSTest(String fileName, int rule_id, Topology topo, int span, int gran, int[] houseRatio) {
        JSONArray devices = convertDevices(readDevices(), gran);
        RuleGenerator ruleGen = new RuleGenerator(span, gran, devices);
        Generator gen = new Generator(topo, ruleGen, 2, houseRatio);
        ArrayList rList = new ArrayList();
        rList.add(rule_id);
        //rList.add(Integer.valueOf(1));
        try {
            FileWriter fileOut = new FileWriter(fileName + ".json");
            fileOut.write(gen.generate("false", rList).toString(2));
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Generate a dataset
    public static void generateSHDSInstances(String fileName, int nDevices, Topology topo, int span, int granularity) {
        JSONArray devices = convertDevices(readDevices(), granularity);
        RuleGenerator ruleGen = new RuleGenerator(span, granularity, devices);
        Generator gen = new Generator(topo, ruleGen, nDevices, new int[]{1, 1, 1});

        try {
            FileWriter fileOut = new FileWriter(fileName+".json");
            fileOut.write(gen.generate(fileName + "_CSV.txt").toString(2));
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Regenerate a dataset from a CSV file
    public static void regenDataset(String fileName, int span, int granularity) {
        JSONArray devices = convertDevices(readDevices(), granularity);
        RuleGenerator ruleGen = new RuleGenerator(span, granularity, devices);
        Generator gen = new Generator(new Topology(1,1), ruleGen, 0, new int[]{1, 1, 1});

        try {
            FileWriter fileOut = new FileWriter(fileName.replaceAll(".txt", "")+".json");
            fileOut.write(gen.regenerate(fileName).toString(2));
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

    private static int getCID(String city) {
        switch(city) {
            case "DM":
                return 0;
            case "BO":
                return 1;
            case "SF":
                return 2;
        }
        return -1;
    }
}

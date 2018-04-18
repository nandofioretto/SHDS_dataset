import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates SHDS problems
 */
public class Generator {

    private final int timeHorizon;
    private final double[] priceSchema;
    private int nDevices;
    private RuleGenerator ruleGenerator;
    private Topology topology;
    private int[] houseRatio;

    private ArrayList<String> ruleCSV = new ArrayList<>();

    public Generator(Topology topology, RuleGenerator ruleGenerator, int nDevices, int[] houseRatio) {
        this.topology = topology;
        this.ruleGenerator = ruleGenerator;
        this.nDevices = nDevices;
        this.houseRatio = houseRatio;
        priceSchema = Parameters.getPriceSchema();
        timeHorizon = (ruleGenerator.getSpan() * 60 / ruleGenerator.getGran());
    }


    private double[] generateBackgroundLoad() {
        double[] bg = new double[timeHorizon];
        for (int i = 0; i < timeHorizon; i++) {
            bg[i] = Utilities.round(ThreadLocalRandom.current().nextDouble(0, 0.3), 2);
        }
        return bg;
    }

    private int[] numEachHouse() {
        int[] numEach = new int[3];
        int total     = houseRatio[0] + houseRatio[1] + houseRatio[2];
        int mult      = topology.getNumAgents() / total;
        int remainder = topology.getNumAgents() % total;
        numEach[0] = houseRatio[0] * mult;
        numEach[1] = houseRatio[1] * mult;
        numEach[2] = houseRatio[2] * mult;
        for(int i = 0; i < remainder; i++) {
            numEach[i%3]++;
        }
        return numEach;
    }

    public JSONObject generateHVAC(String fileName, String city, String season) {
        // All agents within a cluster share a constraint
        JSONObject jExperiment = new JSONObject();
        double[] bgLoads;
        try {
            jExperiment.put("horizon", timeHorizon);
            jExperiment.put("priceSchema", priceSchema);
            jExperiment.put("granularity", ruleGenerator.getGran());
            jExperiment.put("city", city);
            jExperiment.put("season", season);
            ruleGenerator.addProperties(timeHorizon, ruleGenerator.getGran(), topology.getNumAgents(), topology.getNumClusters()); // <-- todo fix this goofy mess

            JSONObject jAgents = new JSONObject();

            int aCount = 0;
            int hType = 0;
            int[] numEachType = numEachHouse();
            for (String agtName : topology.getAgents()) {

                //shifts to the next type of house (small, medium, or large)
                while(aCount == numEachType[hType] && hType < 3) {
                    hType++;
                    aCount = 0;
                }

                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();

                for (String neigName : topology.getNeighbors(agtName)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }

                Random rand = new Random();

                if(rand.nextBoolean()){
                    jAgent.put("batteries", rand.nextInt(1500) + 500);
                    int num_solar = (rand.nextInt(2)+1) * 10;
                    jAgent.put("solar", num_solar);
                } else {
                    jAgent.put("batteries", 0);
                    jAgent.put("solar", 0);
                }

                jAgent.put("houseType", hType);
                ruleGenerator.addHouseType(hType);
                jAgent.put("neighbors", jNeighbors);
                bgLoads = generateBackgroundLoad();
                jAgent.put("backgroundLoad", bgLoads);
                ruleGenerator.addBG(bgLoads);
                jAgent.put("rules", ruleGenerator.generateRulesHVAC(nDevices, hType, season));
                jAgents.put(agtName, jAgent);
                aCount++;
            }
            jExperiment.put("agents", jAgents);

            ruleCSV = ruleGenerator.getRuleCSV();
            Utilities.writeFile(fileName, ruleCSV);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jExperiment;
    }

    public JSONObject generate(String fileName, String season) {
        // All agents within a cluster share a constraint
        JSONObject jExperiment = new JSONObject();
        double[] bgLoads;
        try {
            jExperiment.put("horizon", timeHorizon);
            jExperiment.put("priceSchema", priceSchema);
            jExperiment.put("granularity", ruleGenerator.getGran());
            ruleGenerator.addProperties(timeHorizon, ruleGenerator.getGran(), topology.getNumAgents(), topology.getNumClusters()); // <-- todo fix this goofy mess

            JSONObject jAgents = new JSONObject();

            int aCount = 0;
            int hType = 0;
            int[] numEachType = numEachHouse();
            for (String agtName : topology.getAgents()) {

                //shifts to the next type of house (small, medium, or large)
                while(aCount == numEachType[hType] && hType < 3) {
                    hType++;
                    aCount = 0;
                }

                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();
                /*
                for (String neigName : topology.getNeighbors(agtName)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }
                */

                jAgent.put("houseType", hType);
                ruleGenerator.addHouseType(hType);
                jAgent.put("neighbors", jNeighbors);
                bgLoads = generateBackgroundLoad();
                jAgent.put("backgroundLoad", bgLoads);
                ruleGenerator.addBG(bgLoads);
                jAgent.put("rules", ruleGenerator.generateRules(nDevices, hType, season));
                jAgents.put(agtName, jAgent);
                aCount++;
            }
            jExperiment.put("agents", jAgents);

            ruleCSV = ruleGenerator.getRuleCSV();
            Utilities.writeFile(fileName, ruleCSV);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jExperiment;
    }

    //generate only rules from the list
    public JSONObject generate(String fileName, ArrayList<Integer> rList, String season) {
        // All agents within a cluster share a constraint
        JSONObject jExperiment = new JSONObject();
        double[] bgLoads = new double[timeHorizon];
        try {
            jExperiment.put("horizon", timeHorizon);
            jExperiment.put("priceSchema", priceSchema);
            jExperiment.put("granularity", ruleGenerator.getGran());
            
            JSONObject jAgents = new JSONObject();

            int aCount = 0;
            int hType = 0;
            int[] numEachType = numEachHouse();
            for (String agtName : topology.getAgents()) {

                //shifts to the next type of house (small, medium, or large)
                while(aCount == numEachType[hType] && hType < 3) {
                    hType++;
                    aCount = 0;
                }

                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();

                for (String neigName : topology.getNeighbors(agtName)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }

                jAgent.put("houseType", hType);
                jAgent.put("neighbors", jNeighbors);
                bgLoads = generateBackgroundLoad();
                jAgent.put("backgroundLoad", bgLoads);
                ruleGenerator.addBG(bgLoads);
                jAgent.put("rules", ruleGenerator.generateRules(rList.size(), hType, rList, season));
                jAgents.put(agtName, jAgent);
                aCount++;
            }
            jExperiment.put("agents", jAgents);

            ruleCSV = ruleGenerator.getRuleCSV();
            if (!fileName.equals("false")) {
                Utilities.writeFile(fileName, ruleCSV);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jExperiment;
    }

    
    //regenerate the file with the saved rule data from a CSV
    public JSONObject regenerate(String fileName) {
        // All agents within a cluster share a constraint
        JSONObject jExperiment = new JSONObject();
        RuleParser parse = new RuleParser();
        JSONArray house = parse.readFile(fileName);
        Topology topo = new Topology(house.getInt(2), house.getInt(3));
        JSONObject jRules   = house.getJSONObject(4);
        JSONObject jBgLoads = house.getJSONObject(5);
        JSONObject jHType   = house.getJSONObject(6);
        try {
            jExperiment.put("horizon", house.getInt(0));
            jExperiment.put("granularity", house.getInt(1));
            jExperiment.put("priceSchema", priceSchema);
            JSONObject jAgents = new JSONObject();
            int hType;

            for (String agtName : topo.getAgents()) {

                hType = jHType.getInt(agtName);

                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();

                for (String neigName : topo.getNeighbors(agtName)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }

                jAgent.put("houseType", hType);
                jAgent.put("neighbors", jNeighbors);
                jAgent.put("backgroundLoad", jBgLoads.get(agtName));
                jAgent.put("rules", jRules.getJSONArray(agtName));
                jAgents.put(agtName, jAgent);
            }
            jExperiment.put("agents", jAgents);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jExperiment;
    }

    public JSONObject regenerate(String fileName, int clusters) {
        // All agents within a cluster share a constraint
        JSONObject jExperiment = new JSONObject();
        RuleParser parse = new RuleParser();
        JSONArray house = parse.readFile(fileName);
        Topology topo = new Topology(house.getInt(2), clusters);
        JSONObject jRules   = house.getJSONObject(4);
        JSONObject jBgLoads = house.getJSONObject(5);
        JSONObject jHType   = house.getJSONObject(6);
        try {
            jExperiment.put("horizon", house.getInt(0));
            jExperiment.put("granularity", house.getInt(1));
            jExperiment.put("priceSchema", priceSchema);
            JSONObject jAgents = new JSONObject();
            int hType;

            for (String agtName : topo.getAgents()) {

                hType = jHType.getInt(agtName);

                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();

                for (String neigName : topo.getNeighbors(agtName)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }

                jAgent.put("houseType", hType);
                jAgent.put("neighbors", jNeighbors);
                jAgent.put("backgroundLoad", jBgLoads.get(agtName));
                jAgent.put("rules", jRules.getJSONArray(agtName));
                jAgents.put(agtName, jAgent);
            }
            jExperiment.put("agents", jAgents);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jExperiment;
    }

}
